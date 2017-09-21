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
package org.datavyu.util;

import java.util.HashSet;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;


/**
 * ClockTime is a class which can be used as a time to keep multiple objects in sync.
 */
public final class ClockTimer {

    /** Clock tick period */
    private static final long CLOCK_TICK = 31L;

    /** Clock initial delay */
    private static final long CLOCK_DELAY = 0L;

    /** Convert nanoseconds to milliseconds */
    private static final long NANO_IN_MILLI = 1000000L;

    /** Current time of the clock */
    private double time;

    /** Used to calculate elapsed time */
    private long nanoTime;

    /** Is the clock currently stopped */
    private boolean isStopped;

    /** State of the clock in the previous tick */
    private boolean oldIsStopped;

    /** Update multiplier */
    private float rate = 1F;

    /** The set of objects that listen to this clock */
    private Set<ClockListener> clockListeners = new HashSet<>();

    /**
     * Default constructor.
     */
    public ClockTimer() {
        this(0L);
    }

    /**
     * Constructor.
     *
     * @param initialTime Intial clock time.
     */
    public ClockTimer(final long initialTime) {
        time = initialTime;
        isStopped = true;
        oldIsStopped = true;
        Timer clock = new Timer();
        clock.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                tick();
            }
        }, CLOCK_DELAY, CLOCK_TICK);
    }

    public synchronized void setTimeWithoutNotify(final long newTime) {
        time = newTime;
        time = Math.max(time, 0);
    }

    /**
     * @return Current clock time.
     */
    public synchronized long getTime() {
        return (long) time;
    }

    /**
     * @param newTime Millisecond time to set clock to.
     */
    public synchronized void setTime(final long newTime) {
        if (isStopped) {
            time = newTime;
            time = Math.max(time, 0);
            notifyStep();
        } else {
            stop();
        }
    }

    /**
     * @return Current clock rate.
     */
    public synchronized float getRate() {
        return rate;
    }

    /**
     * @param newRate Multiplier for CLOCK_TICK.
     */
    public synchronized void setRate(final float newRate) {
        rate = newRate;
        notifyRate();
    }

    /**
     * Initiate starting of clock.
     */
    public synchronized void start() {
        if (isStopped) {
            nanoTime = System.nanoTime();
            isStopped = false;
        }
    }

    /**
     * Set flag to stop clock at next time update (boundary).
     */
    public synchronized void stop() {
        if (!isStopped) {
            isStopped = true;
            setRate(0);
        }
    }

    /**
     * @param milliseconds Time step to apply to current time when clock stopped.
     */
    public synchronized void stepTime(final long milliseconds) {
        time += milliseconds;
        time = Math.max(time, 0);
        notifyStep();
    }


    /**
     * @return True if clock is stopped.
     */
    public synchronized boolean isStopped() {
        return isStopped;
    }

    /**
     * @param listener Listener requiring clockTick updates.
     */
    public synchronized void registerListener(final ClockListener listener) {
        clockListeners.add(listener);
    }

    /**
     * The "tick" of the clock - updates listeners of changes in time.
     */
    private synchronized void tick() {
        if (oldIsStopped != isStopped) {
            if (isStopped) {
                notifyStop();
            } else {
                notifyStart();
            }
            oldIsStopped = isStopped;
        }

        if (!isStopped) {
            long currentNano = System.nanoTime();
            time += rate * (currentNano - nanoTime) / NANO_IN_MILLI;
            nanoTime = currentNano;
            notifyTick();
        }
    }

    /**
     * Notify clock listeners of tick event.
     */
    private void notifyTick() {
        for (ClockListener clockListener : clockListeners) {
            clockListener.clockTick((long) time);
        }
    }

    /**
     * Notify clock listeners of rate update event.
     */
    private void notifyRate() {
        for (ClockListener clockListener : clockListeners) {
            clockListener.clockRate(rate);
        }
    }

    /**
     * Notify clock listeners of start event.
     */
    private void notifyStart() {
        for (ClockListener clockListener : clockListeners) {
            clockListener.clockStart((long) time);
        }
    }

    /**
     * Notify clock listeners of stop event.
     */
    private void notifyStop() {
        for (ClockListener clockListener : clockListeners) {
            clockListener.clockStop((long) time);
        }
    }

    /**
     * Notify clock listeners of time step event.
     */
    private void notifyStep() {
        for (ClockListener clockListener : clockListeners) {
            clockListener.clockStep((long) time);
        }
    }

    /**
     * Listener interface for clock 'ticks'.
     */
    public interface ClockListener {

        /**
         * @param time Current time in milliseconds
         */
        void clockTick(long time);

        /**
         * @param time Current time in milliseconds
         */
        void clockStart(long time);

        /**
         * @param time Current time in milliseconds
         */
        void clockStop(long time);

        /**
         * @param rate Current (updated) rate.
         */
        void clockRate(float rate);

        /**
         * @param time Current time in milliseconds
         */
        void clockStep(long time);
    }
}
