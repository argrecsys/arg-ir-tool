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

import es.uam.irg.io.IOManager;
import es.uam.irg.ir.DocumentResult;
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

    public String getProposalInfoReport(DocumentResult prop) {
        String report = reports.get("PROPOSAL_INFO");
        report = report.replace("$TITLE$", prop.getTitle());
        report = report.replace("$DATE$", prop.getDate());
        report = report.replace("$NUM_COMMENTS$", "" + prop.getNumComments());
        report = report.replace("$NUM_SUPPORTS$", "" + prop.getNumSupports());
        report = report.replace("$CODE$", prop.getCode());
        report = report.replace("$CATEGORIES$", prop.getCategories());
        report = report.replace("$DISTRICTS$", prop.getDistricts());
        report = report.replace("$TOPICS$", prop.getTopics());
        report = report.replace("$URL$", prop.getUrl());
        report = report.replace("$SUMMARY$", prop.getSummary());
        report = report.replace("$ARGUMENTS$", "");
        return report;
    }

    public String getProposalListReport() {
        return reports.get("PROPOSAL_LIST");
    }

    private void loadReports() {
        reports = IOManager.readHtmlReports(REPORTS_PATH);
        System.out.println(" - Reports numbers: " + reports.size());
    }
}
