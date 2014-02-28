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

import com.usermetrix.jclient.UserMetrix;
import org.datavyu.Configuration;

import javax.swing.*;

public class UserMetrixV extends DatavyuDialog {

    /**
     * Creates new form UserMetrixV
     */
    public UserMetrixV(final java.awt.Frame parent, final boolean modal) {
        super(parent, modal);
        initComponents();
        groupCanSend.add(radioCanSend);
        groupCanSend.add(radioDoNotSend);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        groupCanSend = new javax.swing.ButtonGroup();
        radioCanSend = new javax.swing.JRadioButton();
        radioDoNotSend = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        Blurb = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.datavyu.Datavyu.class).getContext().getResourceMap(UserMetrixV.class);
        setTitle(resourceMap.getString("Form.title")); 
        setName("Form"); 
        setResizable(false);

        radioCanSend.setFont(resourceMap.getFont("radioCanSend.font")); 
        radioCanSend.setText(resourceMap.getString("radioCanSend.text")); 
        radioCanSend.setName("radioCanSend"); 
        radioCanSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioCanSendActionPerformed(evt);
            }
        });

        radioDoNotSend.setText(resourceMap.getString("radioDoNotSend.text")); 
        radioDoNotSend.setName("radioDoNotSend"); 
        radioDoNotSend.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioDoNotSendActionPerformed(evt);
            }
        });

        jScrollPane1.setName("jScrollPane1"); 

        Blurb.setBackground(resourceMap.getColor("Blurb.background")); 
        Blurb.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Blurb.setEditable(false);
        Blurb.setText(resourceMap.getString("Blurb.text")); 
        Blurb.setMargin(new java.awt.Insets(10, 10, 10, 10));
        Blurb.setName("Blurb"); 
        jScrollPane1.setViewportView(Blurb);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(77, 77, 77)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(radioCanSend)
                                        .addComponent(radioDoNotSend))
                                .addContainerGap(115, Short.MAX_VALUE))
                        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 443, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioCanSend)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(radioDoNotSend)
                                .addContainerGap(25, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Action to invoke when the user has clicked on the can send radio button.
     *
     * @param evt The event that triggered this action.
     */
    private void radioCanSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioCanSendActionPerformed
        Configuration.getInstance().setCanSendLogs(radioCanSend.isSelected());
        UserMetrix.setCanSendLogs(radioCanSend.isSelected());
        this.dispose();
    }//GEN-LAST:event_radioCanSendActionPerformed

    /**
     * Action to invoke when the user has clicked on the do not send radio
     * button.
     *
     * @param evt The event that triggered this action.
     */
    private void radioDoNotSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioDoNotSendActionPerformed
        Configuration.getInstance().setCanSendLogs(radioCanSend.isSelected());
        UserMetrix.setCanSendLogs(radioCanSend.isSelected());
        this.dispose();
    }//GEN-LAST:event_radioDoNotSendActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextPane Blurb;
    private javax.swing.ButtonGroup groupCanSend;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JRadioButton radioCanSend;
    private javax.swing.JRadioButton radioDoNotSend;
    // End of variables declaration//GEN-END:variables
}
