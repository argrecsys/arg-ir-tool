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
import es.uam.irg.nlp.am.arguments.ArgumentLabel;
import es.uam.irg.utils.FunctionUtils;
import es.uam.irg.utils.StringUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Argument IR data model class.
 */
public class DataModel {

    // Class constants
    private static final String[] CSV_FILE_HEADER = {"proposal_id", "argument_id", "label", "timestamp"};
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
    private Map<Integer, List<Argument>> proposalArguments;
    private Map<Integer, List<DMCommentTree>> proposalCommentTrees;
    private Map<Integer, DMComment> proposalComments;
    private Map<String, ArgumentLabel> proposalLabels;
    private Map<Integer, DMProposalSummary> proposalSummaries;
    private Map<Integer, DMProposal> proposals;
    private InfoRetriever retriever;

    /**
     * Constructor.
     *
     * @param decimalFormat
     * @param dateFormat
     */
    public DataModel(String decimalFormat, String dateFormat) {
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
     * Returns argument relation taxonomy.
     *
     * @return
     */
    public Map<String, List<String>> getArgumentTaxonomy() {
        Map<String, List<String>> taxonomy = new HashMap<>();
        taxonomy.put("Cause", Arrays.asList(new String[]{"Condition", "Reason"}));
        taxonomy.put("Clarification", Arrays.asList(new String[]{"Conclusion", "Exemplification", "Restatement", "Summary"}));
        taxonomy.put("Consequence", Arrays.asList(new String[]{"Explanation", "Goal", "Result"}));
        taxonomy.put("Contrast", Arrays.asList(new String[]{"Alternative", "Comparison", "Concession", "Opposition"}));
        taxonomy.put("Elaboration", Arrays.asList(new String[]{"Addition", "Precision", "Similarity"}));
        return taxonomy;
    }

    /**
     *
     * @param id
     * @return
     */
    public DMComment getComment(int id) {
        return this.proposalComments.get(id);
    }

    /**
     * 
     * @return 
     */
    public ReportFormatter getFormatter() {
        return this.formatter;
    }
    
    /**
     *
     * @param id
     * @return
     */
    public DMProposal getProposal(int id) {
        return this.proposals.get(id);
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
    public String getQueryResult(String query, String reRankBy, int nTop) {
        String result = "";

        if (query.isEmpty()) {
            result = this.formatter.getNoValidQueryReport();

        } else {
            // Elapsed time variables
            long start, finish;
            int timeElapsed1 = 0, timeElapsed2 = 0;

            // 1. Query and rerank data
            start = System.nanoTime();
            List<Integer> ids = this.retriever.queryData(query);
            FunctionUtils.printWithDatestamp(">> Found " + ids.size() + " hits");
            List<Integer> docList = rankResults(ids, reRankBy, nTop);
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

                String report = this.formatter.getProposalInfoReport((i + 1), proposal, summary, commentTrees, proposalComments, arguments, controversy, proposalLabels);
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
        boolean result = IOManager.saveDictToCsvFile(LABELS_FILEPATH, CSV_FILE_HEADER, proposalLabels, true);
        isDirty = !result;
        return result;
    }

    /**
     *
     * @param argumentId
     * @param value
     */
    public void updateModelLabel(String argumentId, String value) {
        String timeStamp = DateTimeFormatter.ofPattern(dateFormat).format(LocalDateTime.now());
        ArgumentLabel label = new ArgumentLabel(argumentId, value, timeStamp);
        proposalLabels.put(argumentId, label);
        isDirty = true;
        FunctionUtils.printWithDatestamp(" - Argument '" + argumentId + "' has been annotated as " + value);
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
        proposalLabels = IOManager.readDictFromCsvFile(LABELS_FILEPATH);
        FunctionUtils.printWithDatestamp(" - Number of argument labels: " + proposalLabels.size());
    }

    /**
     * Ranks the results and returns the N top records according to a specified
     * criterion.
     *
     * @param docs
     * @param reRankBy
     * @return
     */
    private List<Integer> rankResults(List<Integer> ids, String reRankBy, int nTop) {
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

        return docList.stream().limit(nTop).toList();
    }

}