/*
 * ContinuousDataViewer.java
 *
 * Created on January 17, 2007, 11:33 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package au.com.nicta.openshapa.cont;

//import au.com.nicta.openshapa.*;

/**
 * Default interface for all Continuous Data Viewers
 * @author FGA
 */
public interface ContinuousDataViewer
{
    public void createNewCell();
    public void jogBack();
    public void stop();
    public void jogForward();
    public void shuttleBack();
    public void pause();
    public void shuttleForward();
    public void rewind();
    public void play();
    public void forward();
    public void setCellOffset();
    public void find();
    public void goBack();
    public void setNewCellOnset();
    public void syncCtrl();
    public void sync();
    public void setCellOnset();
} //End of ContinuousDataViewer interface definition
