package com.smalam.pseudozero.pbpush;

import android.content.Intent;

import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by SAYED on 11/18/2016.
 */

public class MyInstanceIDListenerService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        startActivity(new Intent(this, FCMRegistrationIntentService.class).putExtra("register",true).putExtra("tokenRefreshed",true));
    }
}
