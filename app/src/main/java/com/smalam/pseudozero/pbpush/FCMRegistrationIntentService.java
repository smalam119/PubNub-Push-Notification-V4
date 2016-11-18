package com.smalam.pseudozero.pbpush;

import android.app.IntentService;

import android.content.Intent;
import android.content.SharedPreferences;

import android.preference.PreferenceManager;

import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import com.orhanobut.logger.Logger;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.enums.PNPushType;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.push.PNPushAddChannelResult;
import com.pubnub.api.models.consumer.push.PNPushRemoveChannelResult;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;

/**
 * Created by SAYED on 11/18/2016.
 */

public class FCMRegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    boolean isReg;
    String token = null;
    public static final String REGISTRATION_SUCCESS = "RegistrationSuccess";
    public static final String REGISTRATION_ERROR = "RegistrationError";
    private PubNub mPubnub;

    public FCMRegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        isReg = intent.getExtras().getBoolean("isReg");

        //punnub configuration
        PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey(Config.SUB_KEY);
        pnConfiguration.setPublishKey(Config.PUB_KEY);
        pnConfiguration.setSecure(false);
        mPubnub = new PubNub(pnConfiguration);

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

            EventBus.getDefault().post(new AppEvent("msg","token received"));

            //Displaying the token in the log
            Logger.d(token);

            //on registration complete creating intent with success
            registrationComplete = new Intent(REGISTRATION_SUCCESS);

            //Putting the token to the intent
            registrationComplete.putExtra("token", token);

            enablePushOnChannel(token,Config.CHANNEL_NAME);

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

            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(Config.TOKEN, "");
            editor.commit();

            EventBus.getDefault().post(new AppEvent("msg","token removed"));

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

    private void enablePushOnChannel(String regId,String channelName) {
        //adding regId to pubnub channel
        mPubnub.addPushNotificationsOnChannels()
                .pushType(PNPushType.GCM)
                .channels(Arrays.asList(channelName))
                .deviceId(regId)
                .async(new PNCallback<PNPushAddChannelResult>() {
                    @Override
                    public void onResponse(PNPushAddChannelResult result, PNStatus status) {
                        if (status.isError()) {
                            Logger.d("Error on push notification" + status.getErrorData());
                        } else {
                            Logger.d("Push notification added ");
                        }
                    }
                });

    }

    private void disablePushOnChannel(String regId,String channelName){
        //removing regId to pubnub channel
        mPubnub.removePushNotificationsFromChannels()
                .deviceId(regId)
                .channels(Arrays.asList(channelName))
                .pushType(PNPushType.GCM)
                .async(new PNCallback<PNPushRemoveChannelResult>() {
                    @Override
                    public void onResponse(PNPushRemoveChannelResult result, PNStatus status) {
                        if (status.isError()) {
                            Logger.d("Error on push notification" + status.getErrorData());
                        } else {
                            Logger.d("Push notification removed ");
                        }
                    }
                });
    }
}
