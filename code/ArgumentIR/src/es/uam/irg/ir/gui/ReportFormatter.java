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
import es.uam.irg.nlp.am.arguments.Argument;
import es.uam.irg.nlp.am.arguments.ArgumentLabel;
import es.uam.irg.nlp.am.arguments.Sentence;
import es.uam.irg.utils.FunctionUtils;
import java.awt.Color;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * HTML report formatter class.
 */
public class ReportFormatter {

    // Class constants
    public static final String APP_URL = "https://www.web.es/";
    public static final Color HIGHLIGHT_COLOR_CURRENT = new Color(0, 100, 0);
    public static final Color HIGHLIGHT_COLOR_DEFAULT = Color.BLUE;
    public static final String MODE_ANNOTATE = "ANNOTATE";
    public static final String MODE_VALIDATE = "VALIDATE";
    private static final String REPORTS_PATH = "Resources/views/";

    private final DecimalFormat df;
    private final DateTimeFormatter dtf;
    private final String imgPath;
    private Map<String, String> reports;

    /**
     *
     * @param decimalFormat
     * @param dateFormat
     */
    public ReportFormatter(String decimalFormat, String dateFormat) {
        this.df = new DecimalFormat(decimalFormat);
        this.dtf = DateTimeFormatter.ofPattern(dateFormat);
        this.imgPath = getClass().getClassLoader().getResource("views/img/edit.png").getFile();
        loadReports();
    }

    /**
     * Creates comment HTML report.
     *
     * @param tree
     * @param comments
     * @param arguments
     * @param labels
     * @return
     */
    public String getCommentsInfoReport(DMCommentTree tree, Map<Integer, DMComment> comments, List<Argument> arguments, Map<String, ArgumentLabel> labels) {
        String report = "";

        if (tree != null) {
            report = reports.get("COMMENT_INFO");
            int nodeId = tree.getId();
            int leftPadding = tree.getLevel() * 15;
            DMComment currNode = comments.get(nodeId);
            Argument arg = getCommentArgument(currNode, arguments);
            String argLabel = getArgumentLabel(arg, labels);
            String btAnnotate = getAnnotationButton("COMMENT", nodeId);

            report = report.replace("PADDING-LEFTpx", (leftPadding + "px"));
            report = report.replace("$ID$", "" + nodeId);
            report = report.replace("$DATE$", currNode.getDate());
            report = report.replace("$VOTES$", "" + currNode.getNumVotes());
            report = report.replace("$NUM_POSITIVE$", "" + currNode.getNumVotesUp());
            report = report.replace("$NUM_NEGATIVE$", "" + currNode.getNumVotesDown());
            report = report.replace("$TEXT$", btAnnotate + " " + highlightArgument(currNode.getText(), arg, argLabel));

            for (DMCommentTree node : tree.getChildren()) {
                report += getCommentsInfoReport(node, comments, arguments, labels);
            }
        }

        return report;
    }

    /**
     *
     * @return
     */
    public String getNoValidQueryReport() {
        return reports.get("NO_VALID_QUERY");
    }

    /**
     * Creates proposal HTML report.
     *
     * @param ix
     * @param proposal
     * @param summary
     * @param commentTrees
     * @param comments
     * @param arguments
     * @param controversy
     * @param labels
     * @return
     */
    public String getProposalInfoReport(int ix, DMProposal proposal, DMProposalSummary summary, List<DMCommentTree> commentTrees,
            Map<Integer, DMComment> comments, List<Argument> arguments, Double controversy, Map<String, ArgumentLabel> labels) {
        String report = reports.get("PROPOSAL_INFO");
        StringBuilder body = new StringBuilder();
        Argument arg = getProposalArgument(proposal, arguments);
        String argLabel = getArgumentLabel(arg, labels);
        String btAnnotate = getAnnotationButton("PROPOSAL", proposal.getId());

        // Create main report
        report = report.replace("$IX$", "" + ix);
        report = report.replace("$TITLE$", proposal.getTitle().toUpperCase());
        report = report.replace("$CODE$", proposal.getCode());
        report = report.replace("$DATE$", proposal.getDate());
        report = report.replace("$NUM_ARGUMENTS$", "" + (arguments != null ? arguments.size() : 0));
        report = report.replace("$NUM_COMMENTS$", "" + proposal.getNumComments());
        report = report.replace("$NUM_SUPPORTS$", "" + proposal.getNumSupports());
        report = report.replace("$CONTROVERSY$", df.format(controversy));
        report = report.replace("$CATEGORIES$", summary.getCategories());
        report = report.replace("$DISTRICTS$", summary.getDistricts());
        report = report.replace("$TOPICS$", summary.getTopics());
        report = report.replace("$URL$", proposal.getUrl());
        report = report.replace("$SUMMARY$", btAnnotate + " " + highlightArgument(proposal.getSummary(), arg, argLabel));

        // Add comments
        if (commentTrees != null) {
            String commentReport;
            for (DMCommentTree tree : commentTrees) {
                commentReport = getCommentsInfoReport(tree, comments, arguments, labels);
                body.append(commentReport);
            }
        }
        report = report.replace("$COMMENTS$", body.toString());

        return report;
    }

    /**
     *
     * @param body
     * @param nReports
     * @param timeElapsed1
     * @param timeElapsed2
     * @return
     */
    public String getProposalsReport(String body, int nReports, int timeElapsed1, int timeElapsed2) {
        String result = reports.get("PROPOSAL_LIST");
        result = result.replace("$N_REPORTS$", "" + nReports);
        result = result.replace("$TIME_ELAPSED_1$", "" + timeElapsed1);
        result = result.replace("$TIME_ELAPSED_2$", "" + timeElapsed2);
        result = result.replace("$CURRENT_TIME$", dtf.format(LocalDateTime.now()));
        result = result.replace("$CONTENT$", body);
        return result;
    }

    /**
     *
     * @param claim
     * @return
     */
    public String highlightClaim(String claim) {
        return "<span style='padding:3px; background-color: #C7DEFA;'>" + claim + "</span>";
    }

    /**
     *
     * @param linker
     * @return
     */
    public String highlightLinker(String linker) {
        return "<span style='padding:3px; background-color: #ABD2AC; font-style: italic;'>(" + linker + ")</span>";
    }

    /**
     *
     * @param premise
     * @return
     */
    public String highlightPremise(String premise) {
        return "<span style='padding:3px; background-color: #DED7FB;'>" + premise + "</span>";
    }

    /**
     *
     * @param type
     * @param id
     * @return
     */
    private String getAnnotationButton(String type, int id) {
        return "<a href='" + APP_URL + MODE_ANNOTATE + "/" + type + "/" + id + "'><img src='file:" + imgPath + "' border=0></img></a>";
    }

    /**
     *
     * @param arg
     * @param labels
     * @return
     */
    private String getArgumentLabel(Argument arg, Map<String, ArgumentLabel> labels) {
        String value = "";
        if (arg != null && labels.containsKey(arg.getId())) {
            ArgumentLabel label = labels.get(arg.getId());
            value = label.getLabel();
        }
        return value;
    }

    /**
     *
     * @param comment
     * @param arguments
     * @return
     */
    private Argument getCommentArgument(DMComment comment, List<Argument> arguments) {
        if (arguments != null) {
            for (Argument arg : arguments) {
                String[] tokens = arg.getId().split("-");
                if (tokens.length >= 2 && Integer.parseInt(tokens[0]) == comment.getProposalId() && Integer.parseInt(tokens[1]) == comment.getId()) {
                    return arg;
                }
            }
        }
        return null;
    }

    /**
     *
     * @param proposal
     * @param arguments
     * @return
     */
    private Argument getProposalArgument(DMProposal proposal, List<Argument> arguments) {
        if (arguments != null) {
            for (Argument arg : arguments) {
                String[] tokens = arg.getId().split("-");
                if (tokens.length >= 2 && Integer.parseInt(tokens[0]) == proposal.getId() && Integer.parseInt(tokens[1]) == 0) {
                    return arg;
                }
            }
        }
        return null;
    }

    /**
     *
     * @param sent
     * @return
     */
    private String getSentenceText(Sentence sent) {
        String text = sent.getText();
        for (String entity : sent.getEntities()) {
            entity = entity.trim();
            if (!entity.isEmpty() && !entity.startsWith("https")) {
                text = text.replace(entity, "<strong>" + entity + "</strong>");
            }
        }
        return text;
    }

    /**
     *
     * @param id
     * @return
     */
    private String getValidationPanel(String id, String label) {
        String report = reports.get("VALIDATE_ARGUMENT");
        report = report.replace("ARG_ID", id);
        report = report.replace("$ARG_LINK$", APP_URL + MODE_VALIDATE + "/" + id);

        String[] options = {"RELEVANT", "NOT_VALID", "VALID"};
        for (String opt : options) {
            String tag = opt + "_LINK_COLOR";
            Color color = (opt.equals(label) ? HIGHLIGHT_COLOR_CURRENT : HIGHLIGHT_COLOR_DEFAULT);
            String hex = FunctionUtils.colorToHex(color);
            report = report.replace(tag, hex);
        }

        return report;
    }

    /**
     *
     * @param text
     * @param argument
     * @return
     */
    private String highlightArgument(String text, Argument arg, String label) {
        String newText = text;

        if (arg != null) {
            String claim = getSentenceText(arg.claim);
            String premise = getSentenceText(arg.premise);
            String hlClaim = highlightClaim(claim);
            String hlPremise = highlightPremise(premise);
            String hlConnector = highlightLinker(arg.linker.getText());
            String hlValidation = getValidationPanel(arg.getId(), label);

            newText = newText.replace(arg.claim.getText(), hlClaim);
            newText = newText.replace(arg.premise.getText(), hlPremise + " " + hlConnector + " " + hlValidation);
        }

        return newText;
    }

    /**
     * Loads all available reports into memory from disk.
     */
    private void loadReports() {
        reports = IOManager.readHtmlReports(REPORTS_PATH);
    }

}
