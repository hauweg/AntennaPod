package de.danoeh.antennapod.playback.service.internal;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import de.danoeh.antennapod.storage.preferences.UserPreferences;

public class MotionDetector implements SensorEventListener {
    private static final float SMOOTHING = 0.9f;
    private static final long MIN_INTERVAL_MILLIS = 500;

    private final SensorManager sensorManager;
    private final Runnable onMotion;
    private float threshold;
    private float[] average;
    private long lastMotion = 0;

    public MotionDetector(Context context, Runnable onMotion) {
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.onMotion = onMotion;
    }

    public void start() {
        Sensor sensor = sensorManager == null ? null : sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensor == null || !sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)) {
            throw new UnsupportedOperationException("Accelerometer not supported");
        }
        threshold = switch (UserPreferences.getSleepTimerMotionSensitivity()) {
            case "low" -> 0.5f;
            case "high" -> 0.08f;
            default -> 0.2f;
        };
        average = null;
    }

    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (average == null) {
            average = event.values.clone();
            return;
        }
        float sum = 0;
        for (int i = 0; i < 3; i++) {
            float diff = event.values[i] - average[i];
            sum += diff * diff;
            average[i] = SMOOTHING * average[i] + (1 - SMOOTHING) * event.values[i];
        }
        long now = System.currentTimeMillis();
        if (Math.sqrt(sum) > threshold && now - lastMotion > MIN_INTERVAL_MILLIS) {
            lastMotion = now;
            onMotion.run();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
