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
import es.uam.irg.ir.InfoRetriever;
import es.uam.irg.nlp.am.arguments.Argument;
import es.uam.irg.utils.FunctionUtils;
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
    private static final int MAX_TREE_LEVEL = 3;
    private static final boolean VERBOSE = true;

    // Class objects
    private final ReportFormatter formatter;
    private final Map<String, Object> mdbSetup;
    private final Map<String, Object> msqlSetup;

    // Class data variables
    private Map<Integer, ControversyScore> controversyScores;
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
        this.formatter = new ReportFormatter(decimalFormat, dateFormat);
        this.mdbSetup = FunctionUtils.getDatabaseConfiguration(FunctionUtils.MONGO_DB);
        this.msqlSetup = FunctionUtils.getDatabaseConfiguration(FunctionUtils.MYSQL_DB);

        // Data loading and IR index creation
        loadData();
        createIndex();
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
            // Query data
            long start = System.nanoTime();
            List<Integer> ids = this.retriever.queryData(query, nTop);
            long finish = System.nanoTime();
            int timeElapsed = (int) ((finish - start) / 1000000);
            int nReports = ids.size();
            FunctionUtils.printWithDatestamp(">> Found " + nReports + " hits in " + timeElapsed + " ms");

            // Rerank data
            List<Integer> docList = sortResults(ids, reRankBy);
            FunctionUtils.printWithDatestamp(">> Data reranked by: " + reRankBy);

            // Create user report
            if (docList.size() > 0) {
                StringBuilder body = new StringBuilder();
                String report;

                // Format data
                for (int i = 0; i < docList.size(); i++) {
                    int docId = docList.get(i);
                    DMProposal proposal = proposals.get(docId);
                    DMProposalSummary summary = proposalSummaries.get(docId);
                    List<DMCommentTree> commentTrees = proposalCommentTrees.get(docId);
                    List<Argument> arguments = (reRankBy.equals("Arguments") ? proposalArguments.get(docId) : new ArrayList<>());
                    double controversy = (controversyScores.containsKey(docId) ? controversyScores.get(docId).getValue() : 0.0);

                    report = this.formatter.getProposalInfoReport((i + 1), proposal, summary, commentTrees, proposalComments, arguments, controversy);
                    body.append(report);
                }
                result = body.toString();
            }

            // Update final report
            result = this.formatter.getProposalsReport(nReports, timeElapsed, result);
        }

        return result;
    }

    /**
     *
     */
    private void createIndex() {
        FunctionUtils.printWithDatestamp(">> Creating Lucene index");
        this.retriever = new InfoRetriever();
        this.retriever.createIndex(proposals, proposalSummaries);
    }

    /**
     *
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

            // Get proposal summaries
            proposalSummaries = dbManager.selectProposalSummaries();

            // Get proposal comments
            proposalComments = dbManager.selectComments();

            // Get comments trees
            proposalCommentTrees = dbManager.selectCommentTrees();

            // Get arguments data
            proposalArguments = mngManager.selectProposalArguments(MAX_TREE_LEVEL);

            // Get proposal controversy scores
            controversyScores = dbManager.selectProposalControversy();

            // Show results
            if (VERBOSE) {
                System.out.println(" - Number of proposals: " + proposals.size());
                System.out.println(" - Number of proposal summaries: " + proposalSummaries.size());
                System.out.println(" - Number of comments: " + proposalComments.size());
                System.out.println(" - Number of comment trees: " + proposalCommentTrees.size());
                System.out.println(" - Number of arguments: " + proposalArguments.size());
                System.out.println(" - Number of arguments: " + proposalArguments.size());
                System.out.println(" - Number of controversy scores: " + controversyScores.size());
            }

        } catch (Exception ex) {
            Logger.getLogger(InfoRetriever.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param docs
     * @param reRankBy
     * @return
     */
    private List<Integer> sortResults(List<Integer> ids, String reRankBy) {
        List<Integer> docList = new ArrayList<>();

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

        return docList;
    }

}
