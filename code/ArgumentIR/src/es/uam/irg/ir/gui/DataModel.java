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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private static final String[] CSV_FILE_HEADER = {"proposal_id", "argument_id", "relevance", "quality", "timestamp"};
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
    private Map<Integer, Double> controversyScores;
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
    public ArgumentLabel getArgumentLabel(String argumentId) {
        if (proposalLabels.containsKey(argumentId)) {
            return proposalLabels.get(argumentId);
        }
        return null;
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
     * @param similarity
     * @param nPage
     * @return
     */
    public String getQueryResult(String query, String reRankBy, String similarity, int nPage) {
        String result = "";

        if (query.isEmpty()) {
            result = this.formatter.getNoValidQueryReport();

        } else {
            // Elapsed time variables
            long start, finish;
            int timeElapsed1 = 0, timeElapsed2 = 0;

            // 1. Data querying, reranking and pagination
            start = System.nanoTime();
            List<Integer> docList = retrieveInformation(query, reRankBy, similarity);
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
                List<Argument> arguments = proposalArguments.get(docId);
                double controversy = (controversyScores.containsKey(docId) ? controversyScores.get(docId) : 0.0);

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
     * @param relevance
     * @param quality
     * @return
     */
    public boolean saveArgument(Argument arg, String relevance, String quality) {
        boolean result = false;

        // Insert/update argument
        if (arg.isValid()) {
            MongoDbManager mngManager = new MongoDbManager(mdbSetup);
            mngManager.upsertDocument(arg.getDocument(), Filters.eq("argumentID", arg.getId()), new UpdateOptions().upsert(true));
            proposalArguments = mngManager.selectProposalArguments(MAX_TREE_LEVEL);
            updateModelLabel(arg.getId(), relevance, quality);
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
     * @param relevance
     * @param quality
     */
    public void updateModelLabel(String argumentId, String relevance, String quality) {
        String timeStamp = DateTimeFormatter.ofPattern(dateFormat).format(LocalDateTime.now());
        ArgumentLabel label = new ArgumentLabel(argumentId, relevance, quality, timeStamp);
        proposalLabels.put(argumentId, label);
        isDirty = true;
        FunctionUtils.printWithDatestamp(" - Argument '" + argumentId + "' has been annotated as '" + relevance + "' and '" + quality + "'");
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
     * Logarithm of the weighted sum of the topical relevance of arguments in a
     * document.
     *
     * @param id
     * @return
     */
    private Map<Integer, Double> getArgumentativeScores() {
        Map<Integer, Double> scores = new HashMap<>();
        int totalArgs = 0;

        for (int docId : proposalArguments.keySet()) {
            List<Argument> args = proposalArguments.get(docId);
            totalArgs += args.size();
            double score = 0.0;

            for (Argument arg : args) {
                ArgumentLabel label = getArgumentLabel(arg.getId());
                String relevance = label.getRelevance().toUpperCase();

                if (relevance.equals("VERY_RELEVANT")) {
                    score += 3.0;
                } else if (relevance.equals("RELEVANT")) {
                    score += 2.0;
                } else if (relevance.equals("NOT_RELEVANT")) {
                    score += 1.0;
                } else if (relevance.equals("")) {
                    score += 0.5;
                } else if (relevance.equals("SPAM")) {
                    score += -2;
                }
            }

            // Normalization
            if (score <= 1) {
                score = 0.150515;
            } else {
                score = Math.log(score);
            }

            scores.put(docId, score);
        }

        FunctionUtils.printWithDatestamp(" - Number of retrieved arguments: " + totalArgs);
        return scores;
    }

    /**
     *
     * @param id
     * @return
     */
    private Map<Integer, Double> getControversyScores(Map<Integer, ControversyScore> controversyScores) {
        Map<Integer, Double> scores = new HashMap<>();

        controversyScores.keySet().forEach(docId -> {
            double score = controversyScores.get(docId).getValue();
            scores.put(docId, score);
        });

        return scores;
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
            controversyScores = getControversyScores(dbManager.selectProposalControversy());
            FunctionUtils.printWithDatestamp(" - Number of controversy scores: " + controversyScores.size());

        } catch (Exception ex) {
            Logger.getLogger(InfoRetriever.class
                    .getName()).log(Level.SEVERE, null, ex);
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
     * Information retrieval and Argument-based re-ranking modules. Retrieves
     * documents from the index and uses a cache to optimize queries.
     *
     * @param query
     * @param reRankBy
     * @param similarity
     * @return
     */
    private List<Integer> retrieveInformation(String query, String reRankBy, String similarity) {
        List<Integer> docList;
        String key = (query + "|" + reRankBy + "|" + similarity).toLowerCase();

        if (cache.containsKey(key)) {
            docList = cache.get(key);
            FunctionUtils.printWithDatestamp(">> Loaded " + docList.size() + " hits");

        } else {
            // Module 5 & 6
            FunctionUtils.printWithDatestamp(">> Data reranked by: " + reRankBy);

            reRankBy = reRankBy.toUpperCase();
            if (reRankBy.equals("NOTHING")) {
                docList = this.retriever.retrieveInformation(query, similarity);

            } else {
                Map<Integer, Double> scores = new HashMap<>();
                if (reRankBy.equals("ARGUMENTS")) {
                    scores = getArgumentativeScores();

                } else if (reRankBy.equals("CONTROVERSY")) {
                    scores = controversyScores;
                }

                docList = this.retriever.retrieveInformation(query, similarity, scores);
            }
            FunctionUtils.printWithDatestamp(">> Found " + docList.size() + " hits");
            cache.put(key, docList);
        }

        nRows = docList.size();
        return docList;
    }

}
