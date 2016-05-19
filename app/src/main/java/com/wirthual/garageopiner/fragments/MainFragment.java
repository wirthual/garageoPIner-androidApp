package com.wirthual.garageopiner.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.wirthual.garageopiner.R;
import com.wirthual.garageopiner.communication.CommunicationService;

public class MainFragment extends Fragment implements
        android.view.View.OnClickListener {


    Button btntrigger1;
    Button btntriggertime1;
    Button btntrigger2;
    Button btntriggertime2;

    TextView lblDoor2;
    TextView lblDoor1;

    NotificationCompat.Builder mBuilder;
    ConnectivityManager cm;

    SharedPreferences prefs;
    NetworkInfo mWifi;

    BroadcastReceiver wifiChanged;
    IntentFilter intentFilter;

    private Handler handler;
    private Runnable runnable;

    Intent i;

    public MainFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);

        wifiChanged = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                if (action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                    if (intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED, false)) {
                        //Wait until wifi is really connected not just turned on
                        handler.postDelayed(runnable, 1000);
                    } else {
                        //Wifi turned off, disable buttons imediately
                        HandleWifiState(false);
                    }
                }
            }
        };

        runnable = new Runnable() {
            @Override
            public void run() {
                if (!isConnectedViaWifi()) {
                    //if not connected, calls itself again
                    handler.postDelayed(this, 100);
                } else {
                    //If connected, enable buttons
                    HandleWifiState(true);
                }
            }
        };

        handler = new Handler();

        View rootView = inflater.inflate(R.layout.fragment_main_2, container,
                false);

        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

        btntrigger1 = (Button) rootView.findViewById(R.id.btn_trigger1);
        btntrigger1.setOnClickListener(this);
        btntrigger1.setText(R.string.btntxt_trigger);
        btntriggertime1 = (Button) rootView.findViewById(R.id.btn_triggertime1);
        btntriggertime1.setOnClickListener(this);

        lblDoor2 = (TextView) rootView.findViewById(R.id.textView2);
        lblDoor1 = (TextView) rootView.findViewById(R.id.textView1);


        btntrigger2 = (Button) rootView.findViewById(R.id.btn_trigger2);
        btntrigger2.setOnClickListener(this);
        btntrigger2.setText(R.string.btntxt_trigger);
        btntriggertime2 = (Button) rootView.findViewById(R.id.btn_triggertime2);
        btntriggertime2.setOnClickListener(this);


        return rootView;
    }

    @Override
    public void onResume() {
        i = new Intent(getActivity(), CommunicationService.class);

        super.onResume();

        mWifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        boolean timeControl = prefs.getBoolean("timecontrol", false);
        boolean twoDoors = prefs.getBoolean("twodoors", false);

        lblDoor1.setText(prefs.getString("door1", "Door 1"));
        lblDoor2.setText(prefs.getString("door2", "Door 2"));

        if (!timeControl) {
            if (twoDoors) {
                btntrigger1.setVisibility(View.VISIBLE);
                btntrigger2.setVisibility(View.VISIBLE);
                btntriggertime1.setVisibility(View.GONE);
                btntriggertime2.setVisibility(View.GONE);
                lblDoor2.setVisibility(View.VISIBLE);
                lblDoor1.setVisibility(View.VISIBLE);
            } else {
                btntrigger1.setVisibility(View.VISIBLE);
                btntrigger2.setVisibility(View.GONE);
                btntriggertime1.setVisibility(View.GONE);
                btntriggertime2.setVisibility(View.GONE);
                lblDoor2.setVisibility(View.GONE);
                lblDoor1.setVisibility(View.GONE);
            }
        } else {
            if (twoDoors) {
                btntrigger1.setVisibility(View.VISIBLE);
                btntrigger2.setVisibility(View.VISIBLE);
                btntriggertime1.setVisibility(View.VISIBLE);
                btntriggertime2.setVisibility(View.VISIBLE);
                lblDoor2.setVisibility(View.VISIBLE);
                lblDoor1.setVisibility(View.VISIBLE);
            } else {
                btntrigger1.setVisibility(View.VISIBLE);
                btntrigger2.setVisibility(View.GONE);
                btntriggertime1.setVisibility(View.VISIBLE);
                btntriggertime2.setVisibility(View.GONE);
                lblDoor2.setVisibility(View.GONE);
                lblDoor1.setVisibility(View.GONE);
            }
            String time = prefs.getString("time", "3");
            String triggertime = this.getString(R.string.btntxt_triggertime) + " " + time + " " + this.getString(R.string.minutes);

            btntriggertime1.setText(triggertime);
            btntriggertime2.setText(triggertime);

        }

        HandleWifiState(mWifi.isConnected());

        //registering our receiver
        this.getActivity().registerReceiver(wifiChanged, intentFilter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_trigger1:
                i.setAction(CommunicationService.ACTION_TOGGLE_IN1);
                getActivity().getApplicationContext().startService(i);
                break;
            case R.id.btn_triggertime1:
                i.setAction(CommunicationService.ACTION_TIMERSTART_IN1);
                getActivity().getApplicationContext().startService(i);
                break;
            case R.id.btn_trigger2:
                i.setAction(CommunicationService.ACTION_TOGGLE_IN2);
                getActivity().getApplicationContext().startService(i);
                break;
            case R.id.btn_triggertime2:
                i = new Intent(getActivity(), CommunicationService.class);
                i.setAction(CommunicationService.ACTION_TIMERSTART_IN2);
                getActivity().getApplicationContext().startService(i);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //unregister our receiver
        this.getActivity().unregisterReceiver(this.wifiChanged);
    }

    void HandleWifiState(boolean connected) {

        if (connected) {
            btntrigger1.setEnabled(true);
            btntriggertime1.setEnabled(true);
            btntrigger2.setEnabled(true);
            btntriggertime2.setEnabled(true);
        } else {
            btntrigger1.setEnabled(false);
            btntriggertime1.setEnabled(false);
            btntrigger2.setEnabled(false);
            btntriggertime2.setEnabled(false);
        }
    }

    private boolean isConnectedViaWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return mWifi.isConnected();
    }


}
