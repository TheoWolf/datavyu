/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.datavyu.views;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.datavyu.Datavyu;
import org.datavyu.models.db.Argument;
import org.datavyu.models.db.DataStore;
import org.datavyu.models.db.UserWarningException;
import org.datavyu.models.db.Variable;
import org.datavyu.undoableedits.AddVariableEdit;

import javax.swing.undo.UndoableEdit;

/**
 * The dialog for users to add new variables to the spreadsheet.
 */
public final class NewVariableV extends DatavyuDialog {

    /**
     * The logger for this class.
     */
    private static final Logger logger = LogManager.getLogger(NewVariableV.class);
    /**
     * The database to add the new variable to.
     */
    private DataStore dataStore;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField nameField;
    private javax.swing.JButton okButton;

    /**
     * Constructor, creates a new form to create a new variable.
     *
     * @param parent The parent of this form.
     * @param modal  Should the dialog be modal or not?
     */
    public NewVariableV(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
        logger.info("newVar - show");
        initComponents();
        setName(this.getClass().getSimpleName());

        dataStore = Datavyu.getProjectController().getDataStore();

        getRootPane().setDefaultButton(okButton);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameField = new javax.swing.JTextField();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/datavyu/views/resources/NewVariableV");
        setTitle(bundle.getString("title.text"));
        setName("Form");
        setResizable(false);

        nameField.setName("nameField");

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.datavyu.Datavyu.class).getContext().getResourceMap(NewVariableV.class);
        okButton.setText(resourceMap.getString("okButton.text"));
        okButton.setName("okButton");
        okButton.setPreferredSize(new java.awt.Dimension(65, 23));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setLabel(bundle.getString("cancelButton.text"));
        cancelButton.setName("cancelButton");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jLabel1.setText(resourceMap.getString("jLabel1.text"));
        jLabel1.setName("jLabel1");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                        .add(layout.createSequentialGroup()
                                                .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                                .add(18, 18, 18)
                                                .add(cancelButton))
                                        .add(layout.createSequentialGroup()
                                                .add(jLabel1)
                                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                                .add(nameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 290, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                        .add(layout.createSequentialGroup()
                                .addContainerGap()
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(nameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                        .add(jLabel1))
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                                        .add(cancelButton)
                                        .add(okButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * The action to invoke when the user selects the OK button.
     *
     * @param evt The event that triggered this action.
     */
    private void okButtonActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_okButtonActionPerformed
        try {
            logger.info("newVar - create column:" + getVariableType());
            Variable var = dataStore.createVariable(getVariableName(), getVariableType());
            Datavyu.getProjectController().setLastCreatedVariable(var);

            // record the effect
            UndoableEdit edit = new AddVariableEdit(getVariableName(), getVariableType());

            // Display any changes.
            Datavyu.getView().getComponent().validate();
            Datavyu.getView().getUndoSupport().postEdit(edit);

            dispose();

            // Whoops, user has done something strange - show warning dialog.
        } catch (UserWarningException fe) {
            Datavyu.getApplication().showWarningDialog(fe);

        }
    }// GEN-LAST:event_okButtonActionPerformed

    /**
     * The action to invoke when the user selects the cancel button.
     *
     * @param evt The event that triggered this action.
     */
    private void cancelButtonActionPerformed(final java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cancelButtonActionPerformed
        logger.info("newVar - cancel create.");
        dispose();

    }// GEN-LAST:event_cancelButtonActionPerformed

    /**
     * @return The name of the new variable the user has specified.
     */
    public String getVariableName() {
        return nameField.getText();
    }

    /**
     * @return The type of variable the user has selected to use.
     */
    public Argument.Type getVariableType() {
        return Argument.Type.MATRIX; //this is now the only allowed
    }
    // End of variables declaration//GEN-END:variables
}
