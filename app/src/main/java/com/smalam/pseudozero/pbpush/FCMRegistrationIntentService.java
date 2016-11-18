package com.smalam.pseudozero.pbpush;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.orhanobut.logger.Logger;

/**
 * Created by SAYED on 11/18/2016.
 */

public class FCMRegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    boolean isReg;
    String token = null;
    public static final String REGISTRATION_SUCCESS = "RegistrationSuccess";
    public static final String REGISTRATION_ERROR = "RegistrationError";

    public FCMRegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        isReg = intent.getExtras().getBoolean("isReg");
        if(isReg) registerGCM();
        else UnRegisterGCM();

    }

    private void registerGCM() {
        //Registration complete intent initially null
        Intent registrationComplete = null;

        //Register token is also null
        //we will get the token on successfull registration
        try {
            //Creating an instanceid
            InstanceID instanceID = InstanceID.getInstance(getApplicationContext());

            //Getting the token from the instance id
            token = instanceID.getToken(Config.SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(Config.TOKEN, token);
            editor.commit();

            //Displaying the token in the log
            Logger.d(token);

            //on registration complete creating intent with success
            registrationComplete = new Intent(REGISTRATION_SUCCESS);

            //Putting the token to the intent
            registrationComplete.putExtra("token", token);

        } catch (Exception e) {
            //If any error occurred
            Logger.d("GCMRegIntentService", "Registration error");
            registrationComplete = new Intent(REGISTRATION_ERROR);
        }

        //Sending the broadcast that registration is completed
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void UnRegisterGCM() {
        //Registration complete intent initially null
        Intent registrationComplete = null;

        //Register token is also null
        //we will get the token on successfull registration
        try {
            //Creating an instanceid
            InstanceID instanceID = InstanceID.getInstance(getApplicationContext());

            //delete the token from the instanceid
            instanceID.deleteToken(Config.SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE);

            //on registration complete creating intent with success
            registrationComplete = new Intent(REGISTRATION_SUCCESS);


        } catch (Exception e) {
            //If any error occurred
            Logger.d("GCMRegIntentService", "Registration error");
            registrationComplete = new Intent(REGISTRATION_ERROR);
        }

        //Sending the broadcast that registration is completed
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }
}
