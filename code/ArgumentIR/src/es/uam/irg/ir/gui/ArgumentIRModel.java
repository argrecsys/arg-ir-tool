/**
 * Copyright 2022
 * Andrés Segura-Tinoco
 * Information Retrieval Group at Universidad Autonoma de Madrid
 *
 * This is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * the current software. If not, see <http://www.gnu.org/licenses/>.
 */
package es.uam.irg.ir.gui;

import es.uam.irg.decidemadrid.controversy.ControversyScore;
import es.uam.irg.decidemadrid.db.DMDBManager;
import es.uam.irg.decidemadrid.db.MongoDbManager;
import es.uam.irg.decidemadrid.entities.DMComment;
import es.uam.irg.decidemadrid.entities.DMCommentTree;
import es.uam.irg.decidemadrid.entities.DMProposal;
import es.uam.irg.decidemadrid.entities.DMProposalSummary;
import es.uam.irg.io.IOManager;
import es.uam.irg.ir.InfoRetriever;
import es.uam.irg.nlp.am.arguments.Argument;
import es.uam.irg.utils.FunctionUtils;
import es.uam.irg.utils.StringUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Argument IR data model class.
 */
public class ArgumentIRModel {

    // Class constants
    private static final String LABELS_FILEPATH = "../../results/labels.csv";
    private static final int MAX_TREE_LEVEL = 3;

    // Class objects
    private final String dateFormat;
    private final ReportFormatter formatter;
    private final Map<String, Object> mdbSetup;
    private final Map<String, Object> msqlSetup;

    // Class data variables
    private Map<Integer, ControversyScore> controversyScores;
    private boolean isDirty;
    private Map<String, String> labels;
    private Map<Integer, List<Argument>> proposalArguments;
    private Map<Integer, List<DMCommentTree>> proposalCommentTrees;
    private Map<Integer, DMComment> proposalComments;
    private Map<Integer, DMProposalSummary> proposalSummaries;
    private Map<Integer, DMProposal> proposals;
    private InfoRetriever retriever;

    /**
     * Constructor.
     *
     * @param decimalFormat
     * @param dateFormat
     */
    public ArgumentIRModel(String decimalFormat, String dateFormat) {
        this.dateFormat = dateFormat;
        this.formatter = new ReportFormatter(decimalFormat, dateFormat);
        this.mdbSetup = FunctionUtils.getDatabaseConfiguration(FunctionUtils.MONGO_DB);
        this.msqlSetup = FunctionUtils.getDatabaseConfiguration(FunctionUtils.MYSQL_DB);
        this.isDirty = false;

        // Data loading and IR index creation
        loadData();
        createIndex();
        loadLabels();
    }

    /**
     * Function that queries the data to the index (Apache Lucene) and returns a
     * valid report.
     *
     * @param query
     * @param nTop
     * @param reRankBy
     * @return
     */
    public String getQueryResult(String query, int nTop, String reRankBy) {
        String result = "";

        if (query.isEmpty()) {
            result = this.formatter.getNoValidQueryReport();

        } else {
            // Elapsed time variables
            long start, finish;
            int timeElapsed1 = 0, timeElapsed2 = 0;

            // 1. Query and rerank data
            start = System.nanoTime();
            List<Integer> ids = this.retriever.queryData(query, nTop);
            FunctionUtils.printWithDatestamp(">> Found " + ids.size() + " hits");
            List<Integer> docList = sortResults(ids, reRankBy);
            FunctionUtils.printWithDatestamp(">> Data reranked by: " + reRankBy);
            finish = System.nanoTime();
            timeElapsed1 = (int) ((finish - start) / 1000000);

            // 2. Create user report
            start = System.nanoTime();
            StringBuilder body = new StringBuilder();
            for (int i = 0; i < docList.size(); i++) {
                int docId = docList.get(i);
                DMProposal proposal = proposals.get(docId);
                DMProposalSummary summary = proposalSummaries.get(docId);
                List<DMCommentTree> commentTrees = proposalCommentTrees.get(docId);
                List<Argument> arguments = (reRankBy.equals("Arguments") ? proposalArguments.get(docId) : new ArrayList<>());
                double controversy = (controversyScores.containsKey(docId) ? controversyScores.get(docId).getValue() : 0.0);

                String report = this.formatter.getProposalInfoReport((i + 1), proposal, summary, commentTrees, proposalComments, arguments, controversy, labels);
                body.append(report);
            }
            finish = System.nanoTime();
            timeElapsed2 = (int) ((finish - start) / 1000000);

            // Update final report
            result = this.formatter.getProposalsReport(body.toString(), docList.size(), timeElapsed1, timeElapsed2);
            FunctionUtils.printWithDatestamp(">> The results report has been created");
        }

        return result;
    }

    /**
     *
     * @return
     */
    public boolean isDirty() {
        return isDirty;
    }

    /**
     *
     * @return
     */
    public boolean saveLabelsToFile() {
        String header = "arg_id,label,timestamp\n";
        boolean result = IOManager.saveDictToCsvFile(LABELS_FILEPATH, header, labels);
        isDirty = !result;
        return result;
    }

    /**
     *
     * @param argumentId
     * @param label
     */
    public void updateModelLabel(String argumentId, String label) {
        String timeStamp = DateTimeFormatter.ofPattern(dateFormat).format(LocalDateTime.now());
        String value = label + "," + timeStamp;
        labels.put(argumentId, value);
        isDirty = true;
        FunctionUtils.printWithDatestamp(" - Argument '" + argumentId + "' has been annotated as " + label);
    }

    /**
     * Creates an Apache Lucene index of the data.
     */
    private void createIndex() {
        FunctionUtils.printWithDatestamp(">> Creating Lucene index");
        this.retriever = new InfoRetriever();
        this.retriever.createIndex(proposals, proposalSummaries);
    }

    /**
     * Loads all data (proposals, comments, arguments, labels).
     */
    private void loadData() {
        try {
            FunctionUtils.printWithDatestamp(">> Creating connections");

            // Connecting to databases and fetching data
            DMDBManager dbManager;
            if (msqlSetup != null && msqlSetup.size() == 4) {
                dbManager = new DMDBManager(msqlSetup);
            } else {
                dbManager = new DMDBManager();
            }

            MongoDbManager mngManager;
            if (mdbSetup != null && mdbSetup.size() == 4) {
                mngManager = new MongoDbManager(mdbSetup);
            } else {
                mngManager = new MongoDbManager();
            }

            FunctionUtils.printWithDatestamp(">> Loading data");

            // Get proposals
            proposals = dbManager.selectProposals();
            FunctionUtils.printWithDatestamp(" - Number of proposals: " + proposals.size());

            // Get proposal summaries
            proposalSummaries = dbManager.selectProposalSummaries();
            FunctionUtils.printWithDatestamp(" - Number of proposal summaries: " + proposalSummaries.size());

            // Get proposal comments
            proposalComments = dbManager.selectComments();
            FunctionUtils.printWithDatestamp(" - Number of comments: " + proposalComments.size());

            // Get comments trees
            proposalCommentTrees = dbManager.selectCommentTrees();
            FunctionUtils.printWithDatestamp(" - Number of comment trees: " + proposalCommentTrees.size());

            // Get arguments data
            proposalArguments = mngManager.selectProposalArguments(MAX_TREE_LEVEL);
            FunctionUtils.printWithDatestamp(" - Number of arguments: " + proposalArguments.size());

            // Get proposal controversy scores
            controversyScores = dbManager.selectProposalControversy();
            FunctionUtils.printWithDatestamp(" - Number of controversy scores: " + controversyScores.size());

        } catch (Exception ex) {
            Logger.getLogger(InfoRetriever.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     */
    private void loadLabels() {
        FunctionUtils.printWithDatestamp(">> Loading labels");
        labels = IOManager.readDictFromCsvFile(LABELS_FILEPATH);
        FunctionUtils.printWithDatestamp(" - Number of labels: " + labels.size());
    }

    /**
     *
     * @param docs
     * @param reRankBy
     * @return
     */
    private List<Integer> sortResults(List<Integer> ids, String reRankBy) {
        List<Integer> docList = new ArrayList<>();

        if (ids.size() > 0 && !StringUtils.isEmpty(reRankBy)) {
            if (reRankBy.equals("Nothing")) {
                docList.addAll(ids);

            } else if (reRankBy.equals("Arguments")) {
                Map<Integer, Integer> argsByProp = new HashMap<>();

                for (int id : ids) {
                    int nArgs = (proposalArguments.containsKey(id) ? proposalArguments.get(id).size() : 0);
                    argsByProp.put(id, nArgs);
                }
                docList.addAll(FunctionUtils.sortMapByIntValue(argsByProp).keySet());

            } else if (reRankBy.equals("Controversy")) {
                Map<Integer, Double> controvByProp = new HashMap<>();

                for (int id : ids) {
                    double score = (controversyScores.containsKey(id) ? controversyScores.get(id).getValue() : 0.0);
                    controvByProp.put(id, score);
                }
                docList.addAll(FunctionUtils.sortMapByDblValue(controvByProp).keySet());
            }
        }

        return docList;
    }

}
