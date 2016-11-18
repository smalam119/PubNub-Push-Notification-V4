package com.smalam.pseudozero.pbpush;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.orhanobut.logger.Logger;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {


    private BroadcastReceiver mRegistrationBroadcastReceiver;

    @BindView(R.id.reg_button) Button regBtn;
    @BindView(R.id.unreg_button) Button unregBtn;

    @OnClick(R.id.reg_button) void Register() {
        StartRegistrationService("register");
    }
    @OnClick(R.id.unreg_button) void Unregister() {
        StartRegistrationService("unregister");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if(!settings.getString(Config.TOKEN,"").isEmpty())
        {
            regBtn.setVisibility(View.GONE);
            unregBtn.setVisibility(View.VISIBLE);
        }
        else{
            regBtn.setVisibility(View.VISIBLE);
            unregBtn.setVisibility(View.GONE);
        }


        mRegistrationBroadcastReceiver = new BroadcastReceiver() {

            //We are sending the broadcast from GCMRegistrationIntentService
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(FCMRegistrationIntentService.REGISTRATION_SUCCESS)) {

                    //Getting the registration token from the intent
                    String token = intent.getStringExtra("token");

                    //Displaying if registration is complete
                    if (token != null) {
                        Snackbar snackbar = Snackbar
                                .make(findViewById(android.R.id.content), "Registration complete", Snackbar.LENGTH_LONG);

                        snackbar.show();
                    } else {
                        Snackbar snackbar = Snackbar
                                .make(findViewById(android.R.id.content), "Not registered", Snackbar.LENGTH_LONG);

                        snackbar.show();
                    }

                    //if the intent is not with success then displaying error messages
                } else if (intent.getAction().equals(FCMRegistrationIntentService.REGISTRATION_ERROR)) {
                    Snackbar snackbar = Snackbar
                            .make(findViewById(android.R.id.content), "GCM registration error!", Snackbar.LENGTH_LONG);

                    snackbar.show();
                } else {
                    Snackbar snackbar = Snackbar
                            .make(findViewById(android.R.id.content), "error!", Snackbar.LENGTH_LONG);

                    snackbar.show();
                }
            }
        };

    }




    //Registering receiver on activity resume
    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(FCMRegistrationIntentService.REGISTRATION_SUCCESS));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(FCMRegistrationIntentService.REGISTRATION_ERROR));
    }


    //Unregistering receiver on activity paused
    @Override
    protected void onPause() {
        super.onPause();
        Logger.d("MainActivity", "onPause");
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    private void StartRegistrationService(String register){
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        //if play service is not available
        if(ConnectionResult.SUCCESS != resultCode) {

            //If play service is supported but not installed
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {

                //Displaying message that play service is not installed
                Toast.makeText(getApplicationContext(), "Google Play Service is not install/enabled in this device!", Toast.LENGTH_LONG).show();
                GooglePlayServicesUtil.showErrorNotification(resultCode, getApplicationContext());

                //If play service is not supported
                //Displaying an error message
            } else {
                Toast.makeText(getApplicationContext(), "This device does not support for Google Play Service!", Toast.LENGTH_LONG).show();
            }

            //If play service is available
        } else {

            //Starting intent to register device
            Intent intent = new Intent(this, FCMRegistrationIntentService.class);
            if(register.equals("register")) intent.putExtra("isReg",true);
            else intent.putExtra("isReg",false);
            startService(intent);
        }
    }

}
