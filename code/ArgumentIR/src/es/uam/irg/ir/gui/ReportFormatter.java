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

import es.uam.irg.decidemadrid.entities.DMComment;
import es.uam.irg.decidemadrid.entities.DMCommentTree;
import es.uam.irg.decidemadrid.entities.DMProposal;
import es.uam.irg.decidemadrid.entities.DMProposalSummary;
import es.uam.irg.io.IOManager;
import java.util.List;
import java.util.Map;

/**
 * HTML report formatter class.
 */
public class ReportFormatter {

    private static final String REPORTS_PATH = "Resources/views/";

    private Map<String, String> reports;

    public ReportFormatter() {
        loadReports();
    }

    public String getNoValidQueryReport() {
        return reports.get("NO_VALID_QUERY");
    }

    /**
     *
     * @param proposal
     * @param summary
     * @param commentTrees
     * @param comments
     * @return
     */
    public String getProposalInfoReport(DMProposal proposal, DMProposalSummary summary, List<DMCommentTree> commentTrees, Map<Integer, DMComment> comments) {
        String report = reports.get("PROPOSAL_INFO");
        StringBuilder body = new StringBuilder();

        report = report.replace("$TITLE$", proposal.getTitle());
        report = report.replace("$DATE$", proposal.getDate());
        report = report.replace("$NUM_COMMENTS$", "" + proposal.getNumComments());
        report = report.replace("$NUM_SUPPORTS$", "" + proposal.getNumSupports());
        report = report.replace("$CODE$", proposal.getCode());
        report = report.replace("$CATEGORIES$", summary.getCategories());
        report = report.replace("$DISTRICTS$", summary.getDistricts());
        report = report.replace("$TOPICS$", summary.getTopics());
        report = report.replace("$URL$", proposal.getUrl());
        report = report.replace("$SUMMARY$", proposal.getSummary());

        if (commentTrees != null) {
            for (DMCommentTree tree : commentTrees) {
                body.append(tree.toHtml(comments));
            }
        }
        report = report.replace("$COMMENTS$", body.toString());

        return report;
    }

    public String getProposalListReport() {
        return reports.get("PROPOSAL_LIST");
    }

    public int getReportsSize() {
        return this.reports.size();
    }

    /**
     * Loads all available reports into memory from disk.
     */
    private void loadReports() {
        reports = IOManager.readHtmlReports(REPORTS_PATH);
    }

}
