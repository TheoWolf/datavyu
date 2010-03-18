package org.openshapa.controllers.component;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;

import javax.swing.JComponent;
import javax.swing.event.EventListenerList;
import javax.swing.event.MouseInputAdapter;

import org.openshapa.event.NeedleEvent;
import org.openshapa.event.NeedleEventListener;
import org.openshapa.models.component.NeedleModel;
import org.openshapa.models.component.ViewableModel;
import org.openshapa.views.component.NeedlePainter;

/**
 * NeedleController is responsible for managing a NeedlePainter
 */
public class NeedleController {
    /** View */
    private transient final NeedlePainter view;
    /** Models */
    private transient final NeedleModel needleModel;
    private final ViewableModel viewableModel;
    /** Listeners interested in needle painter events */
    private transient final EventListenerList listenerList;

    public NeedleController() {
        view = new NeedlePainter();

        needleModel = new NeedleModel();
        needleModel.setPaddingTop(0);
        needleModel.setPaddingLeft(101);

        viewableModel = new ViewableModel();

        view.setViewableModel(viewableModel);
        view.setNeedleModel(needleModel);

        final NeedleListener needleListener = new NeedleListener();
        view.addMouseListener(needleListener);
        view.addMouseMotionListener(needleListener);

        listenerList = new EventListenerList();
    }

    /**
     * Register the listener to be notified of needle events
     * 
     * @param listener
     */
    public void addNeedleEventListener(final NeedleEventListener listener) {
        synchronized (this) {
            listenerList.add(NeedleEventListener.class, listener);
        }
    }

    /**
     * Removed the listener from being notified of needle events
     * 
     * @param listener
     */
    public void removeNeedleEventListener(final NeedleEventListener listener) {
        synchronized (this) {
            listenerList.remove(NeedleEventListener.class, listener);
        }
    }

    /**
     * Used to fire a new event informing listeners about the new needle time.
     * 
     * @param newTime
     */
    private void fireNeedleEvent(final long newTime) {
        synchronized (this) {
            NeedleEvent e = new NeedleEvent(this, newTime);
            Object[] listeners = listenerList.getListenerList();
            /*
             * The listener list contains the listening class and then the
             * listener instance.
             */
            for (int i = 0; i < listeners.length; i += 2) {
                if (listeners[i] == NeedleEventListener.class) {
                    ((NeedleEventListener) listeners[i + 1]).needleMoved(e);
                }
            }
        }
    }

    /**
     * Set the current time to be represented by the needle.
     * 
     * @param currentTime
     */
    public void setCurrentTime(final long currentTime) {
        /** Format for representing time. */
        DateFormat df = new SimpleDateFormat("HH:mm:ss:SSS");
        df.setTimeZone(new SimpleTimeZone(0, "NO_ZONE"));
        needleModel.setCurrentTime(currentTime);
        view.setToolTipText(df.format(new Date(currentTime)));
        view.setNeedleModel(needleModel);
    }

    /**
     * @return Current time represented by the needle
     */
    public long getCurrentTime() {
        return needleModel.getCurrentTime();
    }

    /**
     * @return a clone of the viewable model
     */
    public ViewableModel getViewableModel() {
        // return a clone to avoid model tainting
        return viewableModel.clone();
    }

    /**
     * Copies the given viewable model
     * 
     * @param viewableModel
     */
    public void setViewableModel(final ViewableModel viewableModel) {
        /*
         * Just copy the values, do not spread references all over the place to
         * avoid model tainting.
         */
        this.viewableModel.setEnd(viewableModel.getEnd());
        this.viewableModel.setIntervalTime(viewableModel.getIntervalTime());
        this.viewableModel.setIntervalWidth(viewableModel.getIntervalWidth());
        this.viewableModel.setZoomWindowEnd(viewableModel.getZoomWindowEnd());
        this.viewableModel.setZoomWindowStart(viewableModel
                .getZoomWindowStart());
        view.setViewableModel(this.viewableModel);
    }

    /**
     * @return View used by the controller
     */
    public JComponent getView() {
        return view;
    }

    /**
     * Inner class used to handle intercepted events.
     */
    private class NeedleListener extends MouseInputAdapter {
        private transient final Cursor eastResizeCursor =
                Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
        private transient final Cursor defaultCursor =
                Cursor.getDefaultCursor();

        @Override
        public void mouseEntered(final MouseEvent e) {
            JComponent source = (JComponent) e.getSource();
            source.setCursor(eastResizeCursor);
        }

        @Override
        public void mouseExited(final MouseEvent e) {
            JComponent source = (JComponent) e.getSource();
            source.setCursor(defaultCursor);
        }

        @Override
        public void mouseMoved(final MouseEvent e) {
            mouseEntered(e);
        }

        @Override
        public void mouseDragged(final MouseEvent e) {
            int x = e.getX();
            // Bound the x values
            if (x < 0) {
                x = 0;
            }
            if (x > view.getSize().width) {
                x = view.getSize().width;
            }

            // Calculate the time represented by the new location
            float ratio =
                    viewableModel.getIntervalWidth()
                            / viewableModel.getIntervalTime();
            float newTime =
                    (x - needleModel.getPaddingLeft() + viewableModel
                            .getZoomWindowStart()
                            * ratio)
                            / ratio;
            if (newTime < 0) {
                newTime = 0;
            }
            if (newTime > viewableModel.getZoomWindowEnd()) {
                newTime = viewableModel.getZoomWindowEnd();
            }
            fireNeedleEvent(Math.round(newTime));
        }

    }

}
