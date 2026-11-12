package de.danoeh.antennapod.playback.service.internal;

import android.content.Context;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import de.danoeh.antennapod.event.playback.SleepTimerUpdatedEvent;
import de.danoeh.antennapod.storage.preferences.UserPreferences;

public class MotionSleepTimer extends ClockSleepTimer {
    private static final String TAG = "MotionSleepTimer";

    private long restTime;
    private MotionDetector motionDetector;

    public MotionSleepTimer(final Context context) {
        super(context);
    }

    @Override
    public void start(long initialWaitingTime) {
        restTime = initialWaitingTime + UserPreferences.getSleepTimerMotionFadeMillis();
        super.start(restTime);
        motionDetector = new MotionDetector(getContext(), this::reset);
        try {
            motionDetector.start();
        } catch (UnsupportedOperationException e) {
            Log.w(TAG, "Unable to listen for movement: " + e.getMessage());
            motionDetector = null;
        }
    }

    @Override
    public void stop() {
        if (motionDetector != null) {
            motionDetector.stop();
            motionDetector = null;
        }
        super.stop();
    }

    @Override
    public void reset() {
        Log.d(TAG, "Movement detected, restarting rest time");
        updateRemainingTime(restTime);
        EventBus.getDefault().postSticky(SleepTimerUpdatedEvent.updated(getTimeLeft()));
    }

    @Override
    protected void notifyAboutExpiry() {
    }

    public long getRestTime() {
        return restTime;
    }
}
