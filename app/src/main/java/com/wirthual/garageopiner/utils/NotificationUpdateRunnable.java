package com.wirthual.garageopiner.utils;

import android.app.NotificationManager;
import android.support.v4.app.NotificationCompat;

import java.util.concurrent.TimeUnit;


public class NotificationUpdateRunnable implements Runnable {

    public static final int idIN1 = 1000;
    public static final int idIN2 = 2000;

    NotificationCompat.Builder mBuilder;
    NotificationManager mNotifyManager;

    String contentText;
    String s;

    int maxtime;
    long maxtimeNs;
    int id;
    int notificationId;

    boolean running = true;

    public NotificationUpdateRunnable(NotificationCompat.Builder builder, NotificationManager man, int seconds, String content, String second, int id) {
        this.mBuilder = builder;
        this.maxtime = seconds;
        mNotifyManager = man;

        contentText = content;
        s = second;

        this.id = id;
    }

    @Override
    public void run() {
        int incr;

        long startTime = currentTime();

        long endTime = startTime + this.maxtime;

        while ((currentTime() < endTime) && running) {
            long diff = endTime - currentTime();
            String contentString = contentText + " " + String.valueOf(diff) + " " + s;
            mBuilder.setProgress(maxtime, (int) diff, false);

            mBuilder.setContentText(contentString);
            mNotifyManager.notify(id, mBuilder.build());
            try {
                Thread.sleep(200);
            } catch (Exception ex) {
                mNotifyManager.cancel(id);
            }
        }

        mNotifyManager.cancel(id);

    }

    long currentTime() {
        return TimeUnit.SECONDS.convert(System.nanoTime(), TimeUnit.NANOSECONDS);
    }

    public void stopRunning() {
        this.running = false;
    }

    public boolean isRunning() {
        return this.running;
    }
}

	


