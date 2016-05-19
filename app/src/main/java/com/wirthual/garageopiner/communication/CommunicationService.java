package com.wirthual.garageopiner.communication;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.wirthual.garageopiner.R;
import com.wirthual.garageopiner.utils.NotificationUpdateRunnable;


public class CommunicationService extends IntentService {
    Handler mHandler;

    public static final String NOTIFICATION = "com.wirthual.garageopiner.communication";
    public static final String MODE = "toggle";
    public static final String ACTION_TOGGLE_IN1 = "/toggleIN1";
    public static final String ACTION_TOGGLE_IN2 = "/toggleIN2";
    public static final String ACTION_TIMERSTART_IN1 = "/timeControlIN1";
    public static final String ACTION_TIMERSTOPP_IN1 = "stopTimeControlIN1";
    public static final String ACTION_TIMERSTART_IN2 = "/timeControlIN2";
    public static final String ACTION_TIMERSTOPP_IN2 = "stopTimeControlIN2";


    public String time;
    String timestring;
    int timeint;

    NotificationCompat.Builder mBuilder;
    NotificationManager notificationManager;

    static NotificationUpdateRunnable updateNotifitaction1;
    static NotificationUpdateRunnable updateNotifitaction2;


    public CommunicationService() {
        super("CommunicationService");
        mHandler = new Handler();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getAction();

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        time = prefs.getString("time", "2 Minuten");
        timestring = GetTimeFromPreferenceEntry(time);
        timeint = Integer.valueOf(timestring);

        String username = prefs.getString("username", "admin");
        String password = prefs.getString("password", "garageopiner");
        String ipaddress = prefs.getString("ipadress", "192.168.2.107");
        int port = Integer.valueOf(prefs.getString("port", "80"));

        try {
            // Connect to Raspberry
            GarageoPInerHTTPClient client = new GarageoPInerHTTPClient(ipaddress, port);
            client.setCredentials(username, password);

            if (CommunicationService.ACTION_TOGGLE_IN1.equals(action)) {
                stopNotification1();
                client.sendRequest("GET", "/toggleIN1");
                notificationManager.cancel(NotificationUpdateRunnable.idIN1);
            }
            if (CommunicationService.ACTION_TOGGLE_IN2.equals(action)) {
                stopNotification2();
                client.sendRequest("GET", "/toggleIN2");
                notificationManager.cancel(NotificationUpdateRunnable.idIN2);
            }
            if (CommunicationService.ACTION_TIMERSTART_IN1.equals(action)) {
                stopNotification1();
                String request = "/timeControlIN1?seconds=" + timestring;
                client.sendRequest("GET", request);
                notificationManager.cancel(NotificationUpdateRunnable.idIN1);
                makeNotification(NotificationUpdateRunnable.idIN1, CommunicationService.ACTION_TOGGLE_IN1, CommunicationService.ACTION_TIMERSTOPP_IN1);
            }
            if (CommunicationService.ACTION_TIMERSTOPP_IN1.equals(action)) {
                stopNotification1();
                client.sendRequest("GET", "/stopTimeControlIN1");
                notificationManager.cancel(NotificationUpdateRunnable.idIN1);
            }
            if (CommunicationService.ACTION_TIMERSTART_IN2.equals(action)) {
                stopNotification2();
                String request = "/timeControlIN2?seconds=" + timestring;
                client.sendRequest("GET", request);
                notificationManager.cancel(NotificationUpdateRunnable.idIN2);
                ;
                makeNotification(NotificationUpdateRunnable.idIN2, CommunicationService.ACTION_TOGGLE_IN2, CommunicationService.ACTION_TIMERSTOPP_IN2);
            }
            if (CommunicationService.ACTION_TIMERSTOPP_IN2.equals(action)) {
                stopNotification2();
                client.sendRequest("GET", "/stopTimeControlIN2");
                notificationManager.cancel(NotificationUpdateRunnable.idIN2);
            }

        } catch (Exception e) {
            mHandler.post(new DisplayToast(this, this.getString(R.string.connectionerror)));
        }

    }

    private void makeNotification(int id, String actionToggle, String actionCancel) {

        Intent i = new Intent(this, CommunicationService.class);
        i.setAction(actionCancel);
        PendingIntent pIntent = PendingIntent.getService(this, id + 1, i,
                PendingIntent.FLAG_CANCEL_CURRENT);

        Intent i2 = new Intent(this, CommunicationService.class);
        i2.setAction(actionToggle);
        PendingIntent pIntent2 = PendingIntent.getService(this, id + 2, i2,
                PendingIntent.FLAG_CANCEL_CURRENT);

        String title = (String) this.getText(R.string.notificationtitle) + " ";
        if (id == NotificationUpdateRunnable.idIN1) {
            title += (String) this.getText(R.string.door1);
        }
        if (id == NotificationUpdateRunnable.idIN2) {
            title += (String) this.getText(R.string.door2);
        }

        mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle(title)
                .setSmallIcon(R.drawable.ic_garageropener)
                .setAutoCancel(false)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        this.getText(R.string.cancel), pIntent)
                .addAction(android.R.drawable.ic_media_play,
                        this.getText(R.string.now), pIntent2);


        String txt1 = (String) getText(R.string.notificationsubtitle);
        String txt2 = (String) getText(R.string.seconds);


        if (id == NotificationUpdateRunnable.idIN1) {
            updateNotifitaction1 = new NotificationUpdateRunnable(mBuilder, notificationManager, timeint, txt1, txt2, id);
            Thread notificationUpdateThread1 = new Thread(updateNotifitaction1);
            notificationUpdateThread1.start();
        } else {
            updateNotifitaction2 = new NotificationUpdateRunnable(mBuilder, notificationManager, timeint, txt1, txt2, id);
            Thread notificationUpdateThread2 = new Thread(updateNotifitaction2);
            notificationUpdateThread2.start();
        }

    }

    private String GetTimeFromPreferenceEntry(String time) {
        int seconds = Integer.valueOf(time) * 60;
        return String.valueOf(seconds);
    }


    private void stopNotification1() {
        if (updateNotifitaction1 != null) {
            updateNotifitaction1.stopRunning();
        }
    }

    private void stopNotification2() {
        if (updateNotifitaction2 != null) {
            updateNotifitaction2.stopRunning();
        }
    }

}


