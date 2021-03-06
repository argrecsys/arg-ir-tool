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
import es.uam.irg.decidemadrid.entities.DMProposal;
import es.uam.irg.nlp.am.arguments.Argument;
import es.uam.irg.nlp.am.arguments.ArgumentLabel;
import es.uam.irg.nlp.am.arguments.ArgumentLinker;
import es.uam.irg.nlp.am.arguments.ArgumentPattern;
import es.uam.irg.nlp.am.arguments.Sentence;
import es.uam.irg.utils.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;

/**
 *
 */
public class AnnotationForm extends javax.swing.JDialog {

    private Argument sentArg;
    private final DataModel model;
    private String sentText;
    private String sentClaim;
    private String sentPremise;
    private final Map<String, List<String>> taxonomy;
    private String argumentId;
    private int commentId;
    private int parentId;
    private int userId;
    private boolean result;

    /**
     * Creates new form ArgumentForm
     *
     * @param model
     */
    public AnnotationForm(DataModel model) {
        initComponents();
        this.model = model;
        this.taxonomy = model.getRelationTaxonomy();
        this.commentId = 0;
        this.userId = 0;
        this.parentId = 0;
        this.argumentId = "";
        this.sentArg = null;
        this.sentText = "";
        this.sentClaim = "";
        this.sentPremise = "";
        loadCategoriesModel();
        this.result = false;
    }

    public boolean getStatus() {
        return this.result;
    }

    /**
     *
     * @param id
     */
    public void showProposal(int id) {
        DMProposal proposal = model.getProposal(id);

        if (proposal != null) {

            // Save global ids
            int proposalId = proposal.getId();
            this.commentId = -1;
            this.userId = proposal.getUserId();
            this.parentId = -1;
            this.argumentId = proposalId + "-0-1-1";

            // Save sentences variables
            this.sentArg = model.getFormatter().getArgumentByProposal(proposal, model.getProposalArguments(proposalId));
            this.sentText = proposal.getSummary();
            ArgumentLinker linker = null;
            if (this.sentArg != null) {
                this.sentClaim = this.sentArg.claim.getText();
                this.sentPremise = this.sentArg.premise.getText();
                linker = this.sentArg.linker;
            }
            ArgumentLabel label = model.getArgumentLabel(argumentId);

            // Fill graphic controls
            fillGUI(true, "Proposal", proposal.getDate(), linker, label);
        }
    }

    /**
     *
     * @param id
     */
    public void showComment(int id) {
        DMComment comment = model.getComment(id);

        if (comment != null) {

            // Save global ids
            int proposalId = comment.getProposalId();
            this.commentId = comment.getId();
            this.userId = comment.getUserId();
            this.parentId = comment.getParentId();
            this.argumentId = proposalId + "-" + commentId + "-1-1";

            // Save sentences variables
            this.sentArg = model.getFormatter().getArgumentByComment(comment, model.getProposalArguments(proposalId));
            this.sentText = comment.getText();
            ArgumentLinker linker = null;
            if (this.sentArg != null) {
                this.sentClaim = this.sentArg.claim.getText();
                this.sentPremise = this.sentArg.premise.getText();
                linker = this.sentArg.linker;
            }
            ArgumentLabel label = model.getArgumentLabel(argumentId);

            // Fill graphic controls
            fillGUI(true, "Comment", comment.getDate(), linker, label);
        }
    }

    /**
     *
     * @param relevance
     * @return
     */
    private String convertLabelToTitle(String label) {
        return StringUtils.toTitleCase(label.replace("_", " "));
    }

    /**
     *
     * @param display
     * @param type
     * @param date
     * @param label
     */
    private void fillGUI(boolean display, String type, String date, ArgumentLinker linker, ArgumentLabel label) {
        this.cmbType.setSelectedItem(type);
        this.txtDate.setText(date);
        if (linker != null) {
            this.cmbCategory.setSelectedItem(StringUtils.toTitleCase(linker.getCategory()));
            this.cmbSubCategory.setSelectedItem(StringUtils.toTitleCase(linker.getSubCategory()));
            this.cmbIntention.setSelectedItem(StringUtils.toTitleCase(linker.getIntention()));
        }
        if (label != null) {
            this.cmbTopicalRelevance.setSelectedItem(convertLabelToTitle(label.getRelevance()));
            this.cmbRhetoricQuality.setSelectedItem(convertLabelToTitle(label.getQuality()));
        }
        highlightArgument();
        this.setVisible(display);
    }

    /**
     *
     */
    private void highlightArgument() {
        String sentence = this.sentText;

        if (!this.sentClaim.isEmpty()) {
            String hlClaim = model.getFormatter().highlightClaim(this.sentClaim);
            sentence = sentence.replace(this.sentClaim, hlClaim);
        }

        if (!this.sentPremise.isEmpty()) {
            String hlPremise = model.getFormatter().highlightPremise(this.sentPremise);
            sentence = sentence.replace(this.sentPremise, hlPremise);
        }

        this.txtMessage.setText(sentence);
        this.txtMessage.setCaretPosition(0);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblType = new javax.swing.JLabel();
        lblDate = new javax.swing.JLabel();
        txtDate = new javax.swing.JTextField();
        btnSave = new javax.swing.JButton();
        btnClose = new javax.swing.JButton();
        cmbType = new javax.swing.JComboBox<>();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtMessage = new javax.swing.JTextPane();
        lblRelation = new javax.swing.JLabel();
        cmbCategory = new javax.swing.JComboBox<>();
        cmbSubCategory = new javax.swing.JComboBox<>();
        lblEvaluation = new javax.swing.JLabel();
        cmbTopicalRelevance = new javax.swing.JComboBox<>();
        cmbIntention = new javax.swing.JComboBox<>();
        btnClear = new javax.swing.JButton();
        btnClaim = new javax.swing.JButton();
        btnPremise = new javax.swing.JButton();
        lnlAnnotation = new javax.swing.JLabel();
        lblTopicalRelevance = new javax.swing.JLabel();
        cmbRhetoricQuality = new javax.swing.JComboBox<>();
        lblRhetoricQuality = new javax.swing.JLabel();
        lblCategory = new javax.swing.JLabel();
        lblSubCategory = new javax.swing.JLabel();
        lblIntention = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Arguments Annotation Form");
        setModal(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        lblType.setText("Type:");

        lblDate.setText("Date:");

        txtDate.setEditable(false);

        btnSave.setText("Save");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnClose.setText("Close");
        btnClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseActionPerformed(evt);
            }
        });

        cmbType.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "None", "Proposal", "Comment" }));
        cmbType.setEnabled(false);

        txtMessage.setEditable(false);
        txtMessage.setContentType("text/html"); // NOI18N
        jScrollPane2.setViewportView(txtMessage);

        lblRelation.setText("Relation:");

        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "(None)" }));
        cmbCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCategoryActionPerformed(evt);
            }
        });

        cmbSubCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "(None)" }));

        lblEvaluation.setText("Evaluation:");

        cmbTopicalRelevance.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Spam", "Not Relevant", "Relevant", "Very Relevant" }));

        cmbIntention.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "(None)", "Support", "Attack", "Qualifier" }));

        btnClear.setText("Clear");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        btnClaim.setText("Claim");
        btnClaim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClaimActionPerformed(evt);
            }
        });

        btnPremise.setText("Premise");
        btnPremise.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPremiseActionPerformed(evt);
            }
        });

        lnlAnnotation.setText("Annotation:");

        lblTopicalRelevance.setText("Topical relevance");

        cmbRhetoricQuality.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Low Quality", "Sufficient", "High Quality" }));

        lblRhetoricQuality.setText("Rhetoric quality");

        lblCategory.setText("Category");

        lblSubCategory.setText("Sub-Category");

        lblIntention.setText("Intent");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(lblType)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(lblDate)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(txtDate, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(btnSave)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnClose))
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(lblEvaluation)
                                        .addComponent(lnlAnnotation))
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                            .addComponent(btnClear)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(btnClaim)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(btnPremise)
                                            .addGap(30, 30, 30)
                                            .addComponent(lblRelation)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                            .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(cmbSubCategory, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addComponent(cmbIntention, javax.swing.GroupLayout.PREFERRED_SIZE, 102, javax.swing.GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(cmbTopicalRelevance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(lblTopicalRelevance))
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                .addGroup(layout.createSequentialGroup()
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                    .addComponent(cmbRhetoricQuality, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                    .addComponent(lblRhetoricQuality)
                                                    .addGap(9, 9, 9))))))
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 659, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 20, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblCategory)
                .addGap(51, 51, 51)
                .addComponent(lblSubCategory)
                .addGap(51, 51, 51)
                .addComponent(lblIntention)
                .addGap(59, 59, 59))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblType)
                    .addComponent(cmbType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblDate)
                    .addComponent(txtDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblCategory)
                    .addComponent(lblSubCategory)
                    .addComponent(lblIntention))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblRelation)
                    .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbSubCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbIntention, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnClear)
                    .addComponent(btnClaim)
                    .addComponent(btnPremise)
                    .addComponent(lnlAnnotation))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTopicalRelevance)
                    .addComponent(lblRhetoricQuality))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbTopicalRelevance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblEvaluation)
                    .addComponent(cmbRhetoricQuality, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(btnClose))
                .addGap(13, 13, 13))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
        // TODO add your handling code here:
        this.setVisible(false);
    }//GEN-LAST:event_btnCloseActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        this.setVisible(false);
    }//GEN-LAST:event_formWindowClosing

    /**
     *
     * @param evt
     */
    private void cmbCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCategoryActionPerformed
        // TODO add your handling code here:
        this.cmbSubCategory.removeAllItems();
        String currCategory = this.cmbCategory.getSelectedItem().toString();

        if (taxonomy.containsKey(currCategory)) {
            List<String> items = taxonomy.get(currCategory);
            items.forEach(item -> {
                this.cmbSubCategory.addItem(item);
            });
        }
    }//GEN-LAST:event_cmbCategoryActionPerformed

    /**
     *
     * @param evt
     */
    private void btnClaimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClaimActionPerformed
        // TODO add your handling code here:
        this.sentClaim = this.txtMessage.getSelectedText();
        highlightArgument();
    }//GEN-LAST:event_btnClaimActionPerformed

    /**
     *
     * @param evt
     */
    private void btnPremiseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPremiseActionPerformed
        // TODO add your handling code here:
        this.sentPremise = this.txtMessage.getSelectedText();
        highlightArgument();
    }//GEN-LAST:event_btnPremiseActionPerformed

    /**
     *
     * @param evt
     */
    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        // TODO add your handling code here:
        this.sentClaim = "";
        this.sentPremise = "";
        this.txtMessage.setText(sentText);
        this.cmbCategory.setSelectedIndex(0);
        this.cmbIntention.setSelectedIndex(0);
        this.cmbTopicalRelevance.setSelectedIndex(0);
        this.cmbRhetoricQuality.setSelectedIndex(0);
    }//GEN-LAST:event_btnClearActionPerformed

    /**
     *
     * @param evt
     */
    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        // TODO add your handling code here:
        if (validation()) {
            String category = this.cmbCategory.getSelectedItem().toString();
            String subCategory = this.cmbSubCategory.getSelectedItem().toString();
            String intent = this.cmbIntention.getSelectedItem().toString();
            String relevance = this.cmbTopicalRelevance.getSelectedItem().toString();
            String quality = this.cmbRhetoricQuality.getSelectedItem().toString();
            saveArgument(argumentId, userId, commentId, parentId, this.sentText, this.sentClaim, this.sentPremise, category, subCategory, intent, relevance, quality);
            this.setVisible(false);
        } else {
            JOptionPane.showMessageDialog(this, "Error! You must enter all the elements of the argument.", "Error dialog", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    /**
     *
     */
    private void loadCategoriesModel() {
        List<String> categories = new ArrayList<>(this.taxonomy.keySet());
        java.util.Collections.sort(categories);
        categories.forEach(item -> {
            this.cmbCategory.addItem(item);
        });
    }

    /**
     *
     * @param argumentId
     * @param userId
     * @param commentId
     * @param parentId
     * @param text
     * @param claim
     * @param premise
     * @param category
     * @param subCategory
     * @param intent
     * @param relevance
     * @param quality
     */
    private void saveArgument(String argumentId, int userId, int commentId, int parentId, String text, String claim, String premise, String category, String subCategory, String intent, String relevance, String quality) {
        Sentence majorClaim = (sentArg != null ? sentArg.getMajorClaim() : new Sentence());
        Sentence sClaim = new Sentence(claim);
        Sentence sPremise = new Sentence(premise);
        String mainVerb = (sentArg != null ? sentArg.getMainVerb() : "");
        String syntacticTree = (sentArg != null ? sentArg.getSyntacticTree() : "");
        ArgumentLinker linker = new ArgumentLinker(category.toUpperCase(), subCategory.toUpperCase(), intent.toLowerCase(), "");
        ArgumentPattern sentPattern = new ArgumentPattern("[manual]", 1);

        // Create and save new argument
        System.out.println(">> Save/update argument");
        Argument arg = new Argument(argumentId, userId, commentId, parentId, text, false, sClaim, sPremise, mainVerb, linker, sentPattern, syntacticTree);
        arg.setMajorClaim(majorClaim);
        this.result = model.saveArgument(arg, relevance, quality);
    }

    /**
     * Form validation method.
     *
     * @return
     */
    private boolean validation() {
        if (StringUtils.isEmpty(this.sentClaim) || StringUtils.isEmpty(this.sentPremise) || this.cmbCategory.getSelectedIndex() == 0 || this.cmbIntention.getSelectedIndex() == 0) {
            return false;
        }
        return true;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnClaim;
    private javax.swing.JButton btnClear;
    private javax.swing.JButton btnClose;
    private javax.swing.JButton btnPremise;
    private javax.swing.JButton btnSave;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JComboBox<String> cmbIntention;
    private javax.swing.JComboBox<String> cmbRhetoricQuality;
    private javax.swing.JComboBox<String> cmbSubCategory;
    private javax.swing.JComboBox<String> cmbTopicalRelevance;
    private javax.swing.JComboBox<String> cmbType;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblCategory;
    private javax.swing.JLabel lblDate;
    private javax.swing.JLabel lblEvaluation;
    private javax.swing.JLabel lblIntention;
    private javax.swing.JLabel lblRelation;
    private javax.swing.JLabel lblRhetoricQuality;
    private javax.swing.JLabel lblSubCategory;
    private javax.swing.JLabel lblTopicalRelevance;
    private javax.swing.JLabel lblType;
    private javax.swing.JLabel lnlAnnotation;
    private javax.swing.JTextField txtDate;
    private javax.swing.JTextPane txtMessage;
    // End of variables declaration//GEN-END:variables

}
