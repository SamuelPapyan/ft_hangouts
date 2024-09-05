package com.example.ft_hangouts;

import android.app.Application;

import java.util.Timer;
import java.util.TimerTask;

public class App extends Application{
    private static final long MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;

    public boolean wasInBackground;
    private Timer mActivityTransitionTimer;
    private TimerTask mActivityTransitionTimerTask;

    public void startActivityTransitionTimer() {
        this.mActivityTransitionTimer = new Timer();
        this.mActivityTransitionTimerTask = new TimerTask() {
            @Override
            public void run() {
                App.this.wasInBackground = true;
            }
        };
        this.mActivityTransitionTimer.schedule(mActivityTransitionTimerTask,
                MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    public void stopActivityTransitionTimer() {
        if (this.mActivityTransitionTimerTask != null) {
            this.mActivityTransitionTimerTask.cancel();
        }

        if (this.mActivityTransitionTimer != null) {
            this.mActivityTransitionTimer.cancel();
        }

        this.wasInBackground = false;
    }
}
