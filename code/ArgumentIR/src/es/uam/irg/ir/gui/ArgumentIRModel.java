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

import es.uam.irg.decidemadrid.db.DMDBManager;
import es.uam.irg.decidemadrid.entities.DMComment;
import es.uam.irg.decidemadrid.entities.DMCommentTree;
import es.uam.irg.decidemadrid.entities.DMProposal;
import es.uam.irg.decidemadrid.entities.DMProposalSummary;
import es.uam.irg.ir.InfoRetriever;
import es.uam.irg.utils.FunctionUtils;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private static final boolean VERBOSE = true;

    // Class objects
    private final DecimalFormat df;
    private final DateTimeFormatter dtf;
    // Class data variables
    private ReportFormatter formatter;
    private final Map<String, Object> mdbSetup;
    private final Map<String, Object> msqlSetup;

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
        this.df = new DecimalFormat(decimalFormat);
        this.dtf = DateTimeFormatter.ofPattern(dateFormat);
        this.mdbSetup = FunctionUtils.getDatabaseConfiguration(FunctionUtils.MONGO_DB);
        this.msqlSetup = FunctionUtils.getDatabaseConfiguration(FunctionUtils.MYSQL_DB);

        // Loading HTML reports
        createDataFormatter();

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
            List<Integer> docList = this.retriever.queryData(query, nTop, reRankBy);
            long finish = System.nanoTime();
            double timeElapsed = (finish - start) / 1000000;
            int nReports = docList.size();
            System.out.println(">> Found " + nReports + " hits in " + timeElapsed + " ms");

            // Create user report
            if (docList.size() > 0) {
                StringBuilder body = new StringBuilder();
                DMProposal proposal;
                DMProposalSummary summary;

                // Format data
                for (int docId : docList) {
                    proposal = proposals.get(docId);
                    summary = proposalSummaries.get(docId);
                    body.append(this.formatter.getProposalInfoReport(proposal, summary));
                }

                result = this.formatter.getProposalListReport();
                result = result.replace("$N_REPORTS$", "" + nReports);
                result = result.replace("$TIME_ELAPSED$", df.format(timeElapsed));
                result = result.replace("$CURRENT_TIME$", dtf.format(LocalDateTime.now()));
                result = result.replace("$CONTENT$", body.toString());
            }
        }

        return result;
    }

    /**
     *
     */
    private void createDataFormatter() {
        this.formatter = new ReportFormatter();
        System.out.println(" - Reports numbers: " + this.formatter.getReportsSize());
    }

    /**
     *
     */
    private void createIndex() {
        System.out.println(">> Creating Lucene index...");
        this.retriever = new InfoRetriever();
        this.retriever.createIndex(proposals, proposalSummaries);
    }

    /**
     *
     */
    private void loadData() {
        proposals = new HashMap<>();
        proposalComments = new HashMap<>();

        // Connecting to databse
        try {
            System.out.println(">> Loading data...");
            DMDBManager dbManager = null;
            if (msqlSetup != null && msqlSetup.size() == 4) {
                dbManager = new DMDBManager(msqlSetup);
            } else {
                dbManager = new DMDBManager();
            }

            // Get proposals
            proposals = dbManager.selectProposals();

            // Get proposal summaries
            proposalSummaries = dbManager.selectProposalsSummary();

            // Get proposal comments
            proposalComments = dbManager.selectComments();

            // Get comments trees
            proposalCommentTrees = dbManager.selectCommentTrees();

        } catch (Exception ex) {
            Logger.getLogger(InfoRetriever.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Show results
        if (VERBOSE) {
            System.out.println(" - Number of proposals: " + proposals.size());
            System.out.println(" - Number of proposal summaries: " + proposals.size());
            System.out.println(" - Number of comments: " + proposalComments.size());
        }
    }

}
