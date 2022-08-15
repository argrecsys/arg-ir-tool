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
package es.uam.irg.ir;

import es.uam.irg.ir.gui.ArgumentIRForm;
import es.uam.irg.utils.FunctionUtils;
import java.util.Map;

/**
 * Program main class.
 */
public class ArgumentIR {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here

        // Program hyperparameters from JSON config file
        Map<String, Object> params = InitParams.readInitParams();
        String language = (String) params.get("language");
        String dataPath = (String) params.get("data_path");
        System.out.format(">> Language: %s, Data folder path: %s\n", language, dataPath);

        // Show tool gui
        showWinform(language, dataPath);
    }

    /**
     * Creates and displays the Argument-IR form.
     *
     * @param language
     * @param datapath
     */
    private static void showWinform(String language, String dataPath) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ArgumentIRForm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            FunctionUtils.printWithDatestamp(">> ARG-IR BEGINS");
            new ArgumentIRForm(language, dataPath);
            FunctionUtils.printWithDatestamp(">> ARG-IR ENDS");
        });
    }

}
