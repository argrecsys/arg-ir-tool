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

import es.uam.irg.utils.FunctionUtils;
import java.awt.Color;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.html.HTMLDocument;

/**
 * Argument IR form class.
 */
public class ArgumentIRForm extends javax.swing.JFrame {

    // GUI constants
    public static final String HTML_CONTENT_TYPE = "text/html";
    public static final String DECIMAL_FORMAT = "0.000";
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private final AnnotationForm form;
    private final DataModel model;

    /**
     * Creates new form ArgumentIRForm
     */
    public ArgumentIRForm() {
        initComponents();
        this.model = new DataModel(DECIMAL_FORMAT, DATE_FORMAT);
        this.form = new AnnotationForm(model);
        this.setVisible(true);
    }

    /**
     * Closes winform.
     */
    private void closeForm() {
        if (model.isDirty()) {
            if (JOptionPane.showConfirmDialog(this, "Arguments have been annotated. Do you want to save the new labels?", "Confirm Dialog", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                model.saveLabelsToFile();
            }
        }
        this.setVisible(false);
        this.dispose();
        System.exit(0);
    }

    /**
     *
     * @param type
     */
    private void exportReportToFile(String type) {
        try {
            String filepath = selectFileToExport(type);
            String header = getReportHeader(type);
            String text = header + (type.equals("html") ? this.txtResult.getText() : this.txtResult.getDocument().getText(0, this.txtResult.getDocument().getLength()));
            FunctionUtils.writeStringToFile(filepath, text);

        } catch (BadLocationException ex) {
            Logger.getLogger(ArgumentIRForm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param type
     * @return
     */
    private String getReportHeader(String type) {
        String header = "";

        String query = this.txtQuery.getText().trim();
        int nTop = getTopRecordsOption();
        String reRankBy = this.cmbReranks.getSelectedItem().toString();
        header = String.format("Query: %s | Top: %d | Reranked by: %s", query, nTop, reRankBy);
        if (type.equals("html")) {
            header = String.format("<div>" + header + "</div>");
        }

        return header;
    }

    /**
     *
     * @return
     */
    private int getTopRecordsOption() {
        String nTopOption = this.cmbTop.getSelectedItem().toString();
        int nTop = (nTopOption.equals("All") ? Integer.MAX_VALUE : Integer.parseInt(nTopOption));
        return nTop;
    }

    /**
     *
     * @param doc
     * @param el
     * @param color
     */
    private void changeColor(HTMLDocument doc, Element el, Color color) {
        int start = el.getStartOffset();
        int end = el.getEndOffset();
        StyleContext ss = doc.getStyleSheet();
        Style style = ss.addStyle("HighlightedHyperlink", null);
        style.addAttribute(StyleConstants.Foreground, color);
        doc.setCharacterAttributes(start, end - start, style, false);
    }

    /**
     *
     * @param argumentId
     * @param target
     * @throws BadLocationException
     */
    private void highlightElements(String argumentId, Element target) throws BadLocationException {
        HTMLDocument html = (HTMLDocument) txtResult.getDocument();
        String[] options = {"relevant", "valid", "not-valid"};

        for (String tag : options) {
            String elemId = argumentId + "-" + tag;
            Element elem = html.getElement(elemId);
            Color linkColor = (elem.equals(target) ? ReportFormatter.HIGHLIGHT_COLOR_CURRENT : ReportFormatter.HIGHLIGHT_COLOR_DEFAULT);
            changeColor(html, elem, linkColor);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        fileChooser = new javax.swing.JFileChooser();
        lblQuery = new javax.swing.JLabel();
        txtQuery = new javax.swing.JTextField();
        btnSearch = new javax.swing.JButton();
        scrollPane = new javax.swing.JScrollPane();
        txtResult = new javax.swing.JEditorPane();
        lblTop = new javax.swing.JLabel();
        cmbTop = new javax.swing.JComboBox<>();
        lblRerankBy = new javax.swing.JLabel();
        cmbReranks = new javax.swing.JComboBox<>();
        menuBar = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        mItemExportHtml = new javax.swing.JMenuItem();
        mItemExportText = new javax.swing.JMenuItem();
        menuSeparator = new javax.swing.JPopupMenu.Separator();
        mItemClose = new javax.swing.JMenuItem();
        menuLabel = new javax.swing.JMenu();
        mItemSaveLabels = new javax.swing.JMenuItem();

        fileChooser.setDialogTitle("Export report");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Argument-enhanced Information Retrieval");
        setMinimumSize(new java.awt.Dimension(800, 400));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        lblQuery.setText("Query:");

        txtQuery.setToolTipText("Query");

        btnSearch.setText("Search");
        btnSearch.setToolTipText("Search");
        btnSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchActionPerformed(evt);
            }
        });

        txtResult.setEditable(false);
        txtResult.setContentType(HTML_CONTENT_TYPE);
        txtResult.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                txtResultHyperlinkUpdate(evt);
            }
        });
        scrollPane.setViewportView(txtResult);

        lblTop.setText("Top:");

        cmbTop.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "25", "50", "100", "All" }));

        lblRerankBy.setText("Rerank by:");

        cmbReranks.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Nothing", "Arguments", "Controversy" }));

        menuFile.setText("File");

        mItemExportHtml.setText("Export to Html");
        mItemExportHtml.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mItemExportHtmlActionPerformed(evt);
            }
        });
        menuFile.add(mItemExportHtml);

        mItemExportText.setText("Export to Text");
        mItemExportText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mItemExportTextActionPerformed(evt);
            }
        });
        menuFile.add(mItemExportText);
        menuFile.add(menuSeparator);

        mItemClose.setText("Close");
        mItemClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mItemCloseActionPerformed(evt);
            }
        });
        menuFile.add(mItemClose);

        menuBar.add(menuFile);

        menuLabel.setText("Label");

        mItemSaveLabels.setText("Save Labels");
        mItemSaveLabels.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mItemSaveLabelsActionPerformed(evt);
            }
        });
        menuLabel.add(mItemSaveLabels);

        menuBar.add(menuLabel);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblQuery)
                            .addComponent(lblTop))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(cmbTop, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(30, 30, 30)
                                .addComponent(lblRerankBy)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(cmbReranks, javax.swing.GroupLayout.PREFERRED_SIZE, 150, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(743, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(txtQuery)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnSearch)
                                .addGap(20, 20, 20))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(scrollPane)
                        .addGap(20, 20, 20))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblQuery)
                    .addComponent(txtQuery, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTop)
                    .addComponent(cmbTop, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblRerankBy)
                    .addComponent(cmbReranks, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(20, 20, 20)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                .addGap(20, 20, 20))
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Event: search and display results.
     *
     * @param evt
     */
    private void btnSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchActionPerformed
        // TODO add your handling code here:
        String query = this.txtQuery.getText().trim();
        String reRankBy = this.cmbReranks.getSelectedItem().toString();
        int nTop = getTopRecordsOption();
        // Query data
        String result = this.model.getQueryResult(query, reRankBy, nTop);

        // Display report
        this.txtResult.setText(result);
        this.txtResult.setCaretPosition(0);
    }//GEN-LAST:event_btnSearchActionPerformed

    /**
     * Event: close form.
     *
     * @param evt
     */
    private void mItemCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mItemCloseActionPerformed
        // TODO add your handling code here:
        closeForm();
    }//GEN-LAST:event_mItemCloseActionPerformed

    /**
     * Event: export report to html.
     *
     * @param evt
     */
    private void mItemExportHtmlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mItemExportHtmlActionPerformed
        // TODO add your handling code here:
        exportReportToFile("html");
    }//GEN-LAST:event_mItemExportHtmlActionPerformed

    /**
     * Event: export report to raw text.
     *
     * @param evt
     */
    private void mItemExportTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mItemExportTextActionPerformed
        // TODO add your handling code here:
        exportReportToFile("txt");
    }//GEN-LAST:event_mItemExportTextActionPerformed

    /**
     * Event: Catch events of hyperlinks.
     *
     * @param evt
     */
    private void txtResultHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_txtResultHyperlinkUpdate
        // TODO add your handling code here:
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
            try {
                String evtValue = evt.getURL().toURI().toString();

                if (evtValue.startsWith(ReportFormatter.APP_URL)) {
                    String[] tokens = evtValue.replace(ReportFormatter.APP_URL, "").split("/");
                    String action = tokens[0];

                    if (action.equals(ReportFormatter.MODE_VALIDATE)) {
                        String argumentId = tokens[1];
                        String value = tokens[2];
                        System.out.println(" - Action: " + action + ", argumentId: " + argumentId + ", value: " + value);

                        model.updateModelLabel(argumentId, value);
                        highlightElements(argumentId, evt.getSourceElement());

                    } else if (action.equals(ReportFormatter.MODE_ANNOTATE)) {
                        String mode = tokens[1];
                        int id = Integer.parseInt(tokens[2]);
                        System.out.println(" - Action: " + action + ", mode: " + mode + ", id: " + id);

                        if (mode.equals("PROPOSAL")) {
                            form.showProposal(id);

                        } else if (mode.equals("COMMENT")) {
                            form.showComment(id);
                        }
                    }

                } else {
                    // Open a regular URL
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI(evtValue));
                    }
                }

            } catch (URISyntaxException | IOException | BadLocationException ex) {
                Logger.getLogger(ArgumentIRForm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_txtResultHyperlinkUpdate

    /**
     * Event: save labels to file.
     *
     * @param evt
     */
    private void mItemSaveLabelsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mItemSaveLabelsActionPerformed
        // TODO add your handling code here:
        model.saveLabelsToFile();
    }//GEN-LAST:event_mItemSaveLabelsActionPerformed

    /**
     *
     * @param evt
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        closeForm();
    }//GEN-LAST:event_formWindowClosing

    /**
     *
     * @return
     */
    private String selectFileToExport(String ext) {
        String filepath = fileChooser.getCurrentDirectory() + "\\report N." + ext;
        fileChooser.setSelectedFile(new java.io.File(filepath));
        if (fileChooser.showDialog(this, "Save") == JFileChooser.APPROVE_OPTION) {
            filepath = fileChooser.getSelectedFile().toString();
            return filepath;
        }
        return "";
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnSearch;
    private javax.swing.JComboBox<String> cmbReranks;
    private javax.swing.JComboBox<String> cmbTop;
    private javax.swing.JFileChooser fileChooser;
    private javax.swing.JLabel lblQuery;
    private javax.swing.JLabel lblRerankBy;
    private javax.swing.JLabel lblTop;
    private javax.swing.JMenuItem mItemClose;
    private javax.swing.JMenuItem mItemExportHtml;
    private javax.swing.JMenuItem mItemExportText;
    private javax.swing.JMenuItem mItemSaveLabels;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenu menuLabel;
    private javax.swing.JPopupMenu.Separator menuSeparator;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTextField txtQuery;
    private javax.swing.JEditorPane txtResult;
    // End of variables declaration//GEN-END:variables

}
