package com.example.johndoe.najamstanova;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    Boolean stanjePrivateChatForeground;
    private LocalBroadcastManager broadcaster;
    Integer IDporuke;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        mAuth = FirebaseAuth.getInstance();
        broadcaster = LocalBroadcastManager.getInstance(this);
    }


    // poziva se kada korisnik primi poruku
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // There are two types of messages data messages and notification messages. Data messages are handled here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options


        // TODO(developer): Handle FCM messages here.

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Data payload -- poruka je poslana i za Background i za Foreground
        if (remoteMessage.getData().size() > 0) {
            IDporuke = Integer.valueOf(remoteMessage.getData().get("IDporuke"));
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            // nemoj prikazati Notifikaciju ako je korisnik u aktivnosti PrivateChat (ako je PrivateChat u Foregroundu)
            SharedPreferences stanjeActivityaPrivateChat = PreferenceManager.getDefaultSharedPreferences(this);
            stanjePrivateChatForeground = stanjeActivityaPrivateChat.getBoolean("isInForeground", false);
            Log.d("MyFire", String.valueOf(stanjePrivateChatForeground) + "<>" + Globals.trenutniRECEIVER + "<>" + remoteMessage.getData().get("posiljateljUID"));
            if(!stanjePrivateChatForeground){        // Korisnik nema otvorenu aktivnost PrivatChat, prikazi Notifikaciju o novoj poruci, false je po defaultu.
                sendNotification(remoteMessage.getData().get("posiljateljUID"), remoteMessage.getData().get("posiljateljImePrezime"), remoteMessage.getData().get("primateljUID"), remoteMessage.getData().get("body"));     // ako je drugi korisnik s kojim trenutno ne Chata poslao poruku, onda ipak prikaži Notifikaciju
            }
            // ako je PrivateChat u Foregroundu i trenutni korisnik primi poruku, označi ih sve kao pročitane -- ovo zove "private BroadcastReceiver mMessageReceiver = new BroadcastReceiver()" u PrivateChat
            if (Globals.trenutniRECEIVER.equals(remoteMessage.getData().get("posiljateljUID")) && stanjePrivateChatForeground) {
                Intent intent = new Intent("MyData");
                intent.putExtra("PORUKEPROCITANE", "OZNACIPORUKEPROCITANE");
                broadcaster.sendBroadcast(intent);
            }
            // čak i ako je PrivateChat u Foregroundu, ipak pošalji Notifikaciju ako drugi korisnik s kojim user trenutno NE komunicira pošalje poruku
            if (!Globals.trenutniRECEIVER.equals(remoteMessage.getData().get("posiljateljUID")) && stanjePrivateChatForeground) {
                sendNotification(remoteMessage.getData().get("posiljateljUID"), remoteMessage.getData().get("posiljateljImePrezime"), remoteMessage.getData().get("primateljUID"), remoteMessage.getData().get("body"));     // ako je drugi korisnik s kojim trenutno ne Chata poslao poruku, onda ipak prikaži Notifikaciju
            }
            /*
            if (/* Check if data needs to be processed by long running job *//*) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                //scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }
            */
        }

        // Notification payload -- poruka je poslana samo za Foreground
        if (remoteMessage.getNotification() != null) {
            //sendNotification("postiljateljUID", remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    // [START on_new_token]
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }



    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }


    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }


    // za prikaz notifikacije da je stigla nova poruka
    private void sendNotification(String posiljateljUID, String posiljateljImePrezime, String primateljUID, String poruka) {
        Log.d("posiljateljImePrezime", posiljateljUID + "__" + posiljateljImePrezime);
        int notification_id = new Random().nextInt();
        Bundle podaci = new Bundle();
        podaci.putString("RECEIVERUID", posiljateljUID);
        podaci.putString("FIREBASEMESSAGINGSERVICE", "FIREBASEMESSAGINGSERVICE");       // kako bi se znalo da korisnik dolazi u PrivatChat iz Notifikacije
        Intent privateChat = new Intent(this, PrivateChat.class);
        privateChat.putExtras(podaci);
        privateChat.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, privateChat, PendingIntent.FLAG_ONE_SHOT);

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.home_notif)
                        .setLargeIcon((BitmapFactory.decodeResource(getResources(), R.drawable.home_notif)))
                        .setContentTitle(posiljateljImePrezime)
                        .setContentText(poruka)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setSound(defaultSoundUri)
                        .setOngoing(true)
                        //.setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //NotificationChannel channel = new NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationChannel channel = new NotificationChannel(posiljateljUID, "Channel human readable title", NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        if (notificationManager != null) {
            notificationManager.notify(primateljUID /* TAG of notification */, 0/* ID of notification */, notificationBuilder.build());
            Log.d("UKLANJAM", "stvorena je s: " + primateljUID);
        }
    }

}