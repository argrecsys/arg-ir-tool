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

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
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
    private static final int MAX_RECORDS_PER_PAGE = 25;
    private static final int MAX_TREE_LEVEL = 3;

    // Class objects
    private final Map<String, List<Integer>> cache;
    private final String dateFormat;
    private final ReportFormatter formatter;
    private final Map<String, Object> mdbSetup;
    private final Map<String, Object> msqlSetup;

    // Class data variables
    private Map<Integer, ControversyScore> controversyScores;
    private boolean isDirty;
    private int nRows;
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
        this.cache = new HashMap<>();
        this.dateFormat = dateFormat;
        this.formatter = new ReportFormatter(decimalFormat, dateFormat);
        this.mdbSetup = FunctionUtils.getDatabaseConfiguration(FunctionUtils.MONGO_DB);
        this.msqlSetup = FunctionUtils.getDatabaseConfiguration(FunctionUtils.MYSQL_DB);
        this.isDirty = false;
        this.nRows = 0;

        // Data loading and IR index creation
        loadData();
        createDocumentIndex();
        loadLabels();
    }

    /**
     *
     * @param argumentId
     * @return
     */
    public String getArgumentLabel(String argumentId) {
        String label = "";
        if (proposalLabels.containsKey(argumentId)) {
            label = proposalLabels.get(argumentId).getLabel();
        }
        return label;
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
     * @return
     */
    public int getNPages() {
        return 1 + (this.nRows / MAX_RECORDS_PER_PAGE);
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
     *
     * @param proposalId
     * @return
     */
    public List<Argument> getProposalArguments(int proposalId) {
        return this.proposalArguments.get(proposalId);
    }

    /**
     * Function that queries the data to the index (Apache Lucene) and returns a
     * valid report.
     *
     * @param query
     * @param reRankBy
     * @param nPage
     * @return
     */
    public String getQueryResult(String query, String reRankBy, int nPage) {
        String result = "";

        if (query.isEmpty()) {
            result = this.formatter.getNoValidQueryReport();

        } else {
            // Elapsed time variables
            long start, finish;
            int timeElapsed1 = 0, timeElapsed2 = 0;

            // 1. Data querying, reranking and pagination
            start = System.nanoTime();
            List<Integer> docList = retrieveInformation(query, reRankBy);
            docList = filterDataByPage(docList, nPage);
            finish = System.nanoTime();
            timeElapsed1 = (int) ((finish - start) / 1000000);

            // 2. Create user report
            start = System.nanoTime();
            StringBuilder body = new StringBuilder();
            for (int i = 0; i < docList.size(); i++) {
                int docId = docList.get(i);
                int ix = (nPage - 1) * MAX_RECORDS_PER_PAGE + (i + 1);
                DMProposal proposal = proposals.get(docId);
                DMProposalSummary summary = proposalSummaries.get(docId);
                List<DMCommentTree> commentTrees = proposalCommentTrees.get(docId);
                List<Argument> arguments = (reRankBy.equals("Arguments") ? proposalArguments.get(docId) : new ArrayList<>());
                double controversy = (controversyScores.containsKey(docId) ? controversyScores.get(docId).getValue() : 0.0);

                String report = this.formatter.getProposalInfoReport(ix, proposal, summary, commentTrees, proposalComments, arguments, controversy, proposalLabels);
                body.append(report);
            }
            finish = System.nanoTime();
            timeElapsed2 = (int) ((finish - start) / 1000000);

            // Update final report
            result = this.formatter.getProposalsReport(body.toString(), nRows, timeElapsed1, timeElapsed2);
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
     * @param arg
     * @param label
     * @return
     */
    public boolean saveArgument(Argument arg, String label) {
        boolean result = false;

        // Insert/update argument
        if (arg.isValid()) {
            MongoDbManager mngManager = new MongoDbManager(mdbSetup);
            mngManager.upsertDocument(arg.getDocument(), Filters.eq("argumentID", arg.getId()), new UpdateOptions().upsert(true));
            proposalArguments = mngManager.selectProposalArguments(MAX_TREE_LEVEL);
            updateModelLabel(arg.getId(), label);
            result = true;
            FunctionUtils.printWithDatestamp(" - Upserted argument: " + arg.getId());
        }

        return result;
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
     * Creates a full-text index (with Apache Lucene) on the documents.
     */
    private void createDocumentIndex() {
        FunctionUtils.printWithDatestamp(">> Creating Lucene full-text index");
        this.retriever = new InfoRetriever();
        this.retriever.createDocumentIndex(proposals, proposalSummaries);
    }

    /**
     * Filters the results and returns the selected N page.
     *
     * @param docList
     * @param nPage
     * @return
     */
    private List<Integer> filterDataByPage(List<Integer> docList, int nPage) {
        // Filter
        int init = (nPage - 1) * MAX_RECORDS_PER_PAGE;
        int end = Math.min(nPage * MAX_RECORDS_PER_PAGE, docList.size());
        docList = docList.subList(init, end);
        return docList;
    }

    /**
     * Logarithm of the weighted sum of the quality and quantity of arguments in
     * a document.
     *
     * @param id
     * @return
     */
    private double getArgumentativeScore(int id) {
        double score = 0.0;
        if (proposalArguments.containsKey(id)) {
            List<Argument> args = proposalArguments.get(id);
            for (Argument arg : args) {
                String label = getArgumentLabel(arg.getId()).toUpperCase();
                if (label.equals("RELEVANT")) {
                    score += 2.0;
                } else if (label.equals("VALID") || label.equals("")) {
                    score += 1.0;
                }
            }
            score = Math.log(score);
        }
        return score;
    }

    /**
     *
     * @param id
     * @return
     */
    private double getControversyScore(int id) {
        return (controversyScores.containsKey(id) ? controversyScores.get(id).getValue() : 0.0);
    }

    /**
     * Loads all data (proposals, comments, arguments, labels).
     */
    private void loadData() {
        try {
            FunctionUtils.printWithDatestamp(">> Creating connections");

            // Connecting to databases and fetching data
            DMDBManager dbManager = new DMDBManager(msqlSetup);
            MongoDbManager mngManager = new MongoDbManager(mdbSetup);

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
        FunctionUtils.printWithDatestamp(">> Loading argument labels");
        proposalLabels = IOManager.readDictFromCsvFile(LABELS_FILEPATH);
        FunctionUtils.printWithDatestamp(" - Number of argument labels: " + proposalLabels.size());
    }

    /**
     * Argument-based re-ranking module (6). Ranks the results according to a
     * specified criterion.
     *
     * @param docs
     * @param reRankBy
     * @return
     */
    private List<Integer> rerankingDocuments(List<Integer> ids, String reRankBy) {
        List<Integer> docList = new ArrayList<>();
        reRankBy = reRankBy.toUpperCase();

        // Rerank
        if (ids.size() > 0 && !StringUtils.isEmpty(reRankBy)) {
            if (reRankBy.equals("NOTHING")) {
                docList.addAll(ids);

            } else {
                Map<Integer, Double> scores = new HashMap<>();

                for (int id : ids) {
                    double score = 0.0;
                    if (reRankBy.equals("ARGUMENTS")) {
                        score = getArgumentativeScore(id);

                    } else if (reRankBy.equals("CONTROVERSY")) {
                        score = getControversyScore(id);
                    }
                    scores.put(id, score);
                }

                docList.addAll(FunctionUtils.sortMapByDblValue(scores).keySet());
            }
        }

        return docList;
    }

    /**
     * Information retrieval and Argument-based re-ranking modules. Retrieves
     * documents from the index and uses a cache to optimize queries.
     *
     * @param query
     * @param reRankBy
     * @return
     */
    private List<Integer> retrieveInformation(String query, String reRankBy) {
        List<Integer> docList;
        String key = query + reRankBy;

        if (cache.containsKey(key)) {
            docList = cache.get(key);
            FunctionUtils.printWithDatestamp(">> Loaded " + docList.size() + " hits");

        } else {
            // Module 5
            List<Integer> ids = this.retriever.retrieveInformation(query);
            FunctionUtils.printWithDatestamp(">> Found " + ids.size() + " hits");

            // Module 6
            docList = rerankingDocuments(ids, reRankBy);
            FunctionUtils.printWithDatestamp(">> Data reranked by: " + reRankBy);
            cache.put(key, docList);
        }

        nRows = docList.size();
        return docList;
    }

}
