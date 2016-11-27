# pn-push

## Overview
This is an implementation of pubnub push notification using GCM (cough...FCM) for android. PubNub is a real time data stream service. 
It has a Java SDK which offers real time push notification service.Implimentation is quiet straight forward and Pubnub official 
documentation has a step by step instruction. But upgrading the SDK from v3 to v4 some previous defination and method doesnot work 
anymore. Though V4 has documentation for push notifications but those are only provided in code snipets. Moreover Google shifted from GCM to FCM
which creates a little bit of confusion. So in this project push notification for android client is implemented using Pubnub v4 and FCM.

## Libraries used
* Pubnub 4.2.0
* Firebase-messeging 9.6.0
* Butterknife 8.4.0 (optional)
* Logger 1.15 (optional)
* Eventbus 3.0.0 (optional)

## How push notification works
1. First a client android device has to register to FCM (Firebase cloud messaging). It does this by sending sender id to FCM
2. FCM sends a registration token to the client against the sender id.
3. Client now has to store the token to a webserver.
4. To send a push notification client has to send the registration id and message TO GCM server.
5. After getting a push request GCM send to the client recognizing it the registration id.

Using service like Pubnub removes the hassle of creating and setting up a webserver.

## How push notification is implemented using pubnub
Step 1 and 2 remains the same. Now client will subscribe to a pubnub channel using the registration id and channel name. So, if any data is emitted to the channel pubnub 
sends the id and message to the GCM server. GCM sends the notification to all the registered devices listening to the channel. da-ta!!! no need to setup a personal webserver.

## Lets get our hands dirty

## Set up FCM
Go to (https://console.firebase.google.com) and create a new project.From the project dashboard add your android project to firebase by providing your project package name.Download the configuration file (google_services.json) go to android studio and copy it to your app root module directory. To your project level build.gradle file add

```
classpath 'com.google.gms:google-services:3.0.0'
```
and to app level build.gradle file

```
compile 'com.google.firebase:firebase-messaging:9.6.0'
```
now sync the gradle. Now if we go to (https://console.developers.google.com/), under credentials there will be three key automatically generated. The server key is needed to connect FCM service to Pubnub.

## Set up Pubnub

Go to app level build.gradle file and add

```
compile group: 'com.pubnub', name: 'pubnub', version: '4.2.0'

```
Lets move to (https://admin.pubnub.com/#/login) and create a new project. In the project dashboard turn on Mobile push notification and 
put the FCM server key on the GCM API key field. (It should be written FCM API key. They should call their front-end developers). Remember to save the changes. A publish and a subscribe key will be provided. Upto this point all the required API's are intregated. So now we can move to the coding part.

## Classes
we need below four classes
1. MainActivity: We will initialize and control the view here. Call the FCMRegistrationIntentService and create a broadcast service which will receive the token when GCM sends it.

2. FCMRegistrationIntentService: It will extends a IntentService. In it's onHandleIntent method we are going to get the token from the instanceId by sending the sender id. We also configure Pubnub here.

```
PNConfiguration pnConfiguration = new PNConfiguration();
        pnConfiguration.setSubscribeKey(Config.SUB_KEY);
        pnConfiguration.setPublishKey(Config.PUB_KEY);
        pnConfiguration.setSecure(false);
        mPubnub = new PubNub(pnConfiguration);
```
pubnub subscribe and publish key provided in the dashboard is required here. After successfully received the registration token it should be added to the pubnub object.

```
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
```
4. FcmListenerService: Mainly responsible for receiving the message from FCM server. It has a onMessageReceived method which gets the message and upon receiving createa a notification to alert the user.

5.MyInstanceIDListenerService: Basically responsible start the registration service when token needs to be refreshed.
