/*
 * Copyright (c) 2012. Ansvia Inc.
 * Author: Robin Syihab.
 */

package com.ansvia.mindchat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Main chat service.
 */
public class ChatService extends Service {

    private static final String TAG = "ChatService";

    private static final String GETHUB_HOST = "175.103.44.38"; // www.gethub.us
    //private static final int GETHUB_PORT = 6060;

    //private static final String GETHUB_HOST = "10.0.2.2";
    private static final int GETHUB_PORT = 6060;


    // @TODO(*): jangan di hard-coded.
    private static final String CHANNEL = "www.gethub.us";
    private LogoutEventReceiver logoutEventReceiver;
    private String userName;
    private String sessid;
    private Thread thread;
    private NotificationManager mNM;

    // for compatibility
    private static final Class<?>[] mSetForegroundSignature = new Class[] {
            boolean.class};
    private static final Class<?>[] mStartForegroundSignature = new Class[] {
            int.class, Notification.class};
    private static final Class<?>[] mStopForegroundSignature = new Class[] {
            boolean.class};
//    private int FOREGROUND_ID = 1;
    private String password;
    private ChatMessageSender msgSender;

//
//    public ChatService(String name) {
//        super(name);
//    }
//
//    public ChatService(){
//        super(TAG);
//    }
//
//

    private class LogoutEventReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            GethubClient gethub = GethubClient.getInstance();
            gethub.logout();
        }
    }

    private class ChatMessageSender extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            GethubClient gethub = GethubClient.getInstance();
            String channelName = intent.getStringExtra("channel");
            String message = intent.getStringExtra("message");
            String _sessid = intent.getStringExtra("sessid");
            try {
                gethub.message(channelName, message, _sessid);
            } catch (IOException e) {
                e.printStackTrace();
                showError(e.getMessage());
            }
        }
    }

//    @Override
//    protected void onHandleIntent(Intent intent) {
//
//        Log.d(TAG, "in onHandleIntent()");
//
//
//        this.logoutEventReceiver = new LogoutEventReceiver();
//
//        registerReceiver(this.logoutEventReceiver, new IntentFilter("logout"));
//
//        GethubClient gethub = GethubClient.getInstance();
//
//        // set your gethub server address here.
//        gethub.connect(GETHUB_HOST, GETHUB_PORT);
//
//        try {
//
//
//            String sessid = gethub.authorize(intent.getStringExtra("userName"), intent.getStringExtra("password"));
//
//            if(sessid == null){
//                Log.i(TAG, "Cannot authorize user");
//                showError("Cannot authorize user, check your connection.");
//                return;
//            }
//
//            if(!gethub.join(CHANNEL, sessid)){
//                Log.i(TAG, "Cannot join to channel");
//                showError("Cannot join to channel " + CHANNEL);
//                return;
//            }
//
//            Intent chatRoomInitial = new Intent("chatroom.init");
//            chatRoomInitial.putExtra("sessid", sessid);
//            chatRoomInitial.putExtra("userName", intent.getStringExtra("userName"));
//            chatRoomInitial.putExtra("channel", CHANNEL);
//            sendBroadcast(chatRoomInitial);
//
//            //DataReceiver dataRec = new DataReceiver(gethub, sessid, intent.getStringExtra("userName"));
//
//            PacketHandler handler = new PacketHandler(this, gethub, sessid, intent.getStringExtra("userName"));
//
//            gethub.bind(CHANNEL, sessid, handler);
//
//        }catch (Exception e){
//            showError(e.getMessage());
//        }
//
//    }

    private void showError(String msg){
        Intent errorIntent = new Intent("error");
        errorIntent.putExtra("data", msg);
        sendBroadcast(errorIntent);
    }


    @Override
    public void onCreate() {
        super.onCreate();

        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        try {
            mStartForeground = getClass().getMethod("startForeground",
                    mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground",
                    mStopForegroundSignature);


        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }
        try {
            mSetForeground = getClass().getMethod("setForeground",
                    mSetForegroundSignature);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "OS doesn't have Service.startForeground OR Service.setForeground!");
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

//        Notification ntf = new Notification(R.drawable.icon, "Mindchat", System.currentTimeMillis());
//        Intent chatRoom=new Intent(this, ChatRoomActivity.class);
//
////        chatRoom.putExtra("sessid", sessid);
//        chatRoom.putExtra("userName", userName);
//        chatRoom.putExtra("password", password);
//
//        chatRoom.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//
//        PendingIntent pi = PendingIntent.getActivity(this, 0, chatRoom, 0);
//
//        ntf.setLatestEventInfo(this, "Fake Player",
//                "Now Playing: \"Ummmm, Nothing\"",
//                pi);
//
//        ntf.flags |= Notification.FLAG_FOREGROUND_SERVICE;
//
//        startForegroundCompat(FOREGROUND_ID, ntf);
    }

    private class GethubBinder implements Runnable {

        private String userName;
        private String password;
        private String sessid;

        public GethubBinder(String userName, String password){
            this.userName = userName;
            this.password = password;
        }

        @Override
        public void run() {
            GethubClient gethub = GethubClient.getInstance();

            try {
                gethub.connect(GETHUB_HOST, GETHUB_PORT);

                this.sessid = gethub.authorize(this.userName, this.password);

                if(sessid == null){
                    Log.i(TAG, "Cannot authorize user");
                    showError("Cannot authorize user, check your connection.");
                    return;
                }

                if(!gethub.join(CHANNEL, sessid)){
                    Log.i(TAG, "Cannot join to channel");
                    showError("Cannot join to channel " + CHANNEL);
                    return;
                }

                String[] participants = gethub.participants(CHANNEL, sessid);
                String[] chatMessages = gethub.messages(CHANNEL, sessid);

                Intent chatRoomInitial = new Intent("chatroom.init");

                chatRoomInitial.putExtra("sessid", sessid);
                chatRoomInitial.putExtra("userName", this.userName);
                chatRoomInitial.putExtra("channel", CHANNEL);
                chatRoomInitial.putExtra("participants", participants);
                chatRoomInitial.putExtra("chatMessages", chatMessages);
                sendBroadcast(chatRoomInitial);


                PacketHandler handler = new PacketHandler(ChatService.this, gethub, this.sessid, this.userName);
                gethub.bind(CHANNEL, this.sessid, handler);

            }catch (IOException e){
                e.printStackTrace();
                showError("Cannot connect to server. " + e.getMessage());
            }catch (Exception e){
                e.printStackTrace();
                showError(e.getMessage());
            }

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        //Log.d(TAG, "in onHandleIntent()");

        this.logoutEventReceiver = new LogoutEventReceiver();
        this.msgSender = new ChatMessageSender();

        registerReceiver(this.logoutEventReceiver, new IntentFilter("logout"));
        registerReceiver(this.msgSender, new IntentFilter("send.message"));

        //GethubClient gethub = GethubClient.getInstance();

        // set your gethub server address here.

        this.userName = intent.getStringExtra("userName");
        this.password = intent.getStringExtra("password");

        this.thread = new Thread(new GethubBinder(this.userName, this.password));
        this.thread.start();


        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver(logoutEventReceiver);
        unregisterReceiver(msgSender);

        GethubClient gethub = GethubClient.getInstance();
        gethub.close();
//        stopForegroundCompat(FOREGROUND_ID);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private Method mSetForeground;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mSetForegroundArgs = new Object[1];
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];

    void invokeMethod(Method method, Object[] args) {
        try {
            method.invoke(this, args);
        } catch (InvocationTargetException e) {
            // Should not happen.
            Log.w("ApiDemos", "Unable to invoke method", e);
        } catch (IllegalAccessException e) {
            // Should not happen.
            Log.w("ApiDemos", "Unable to invoke method", e);
        }
    }

    /**
     * This is a wrapper around the new startForeground method, using the older
     * APIs if it is not available.
     */
    void startForegroundCompat(int id, Notification notification) {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            invokeMethod(mStartForeground, mStartForegroundArgs);
            return;
        }

        // Fall back on the old API.
        mSetForegroundArgs[0] = Boolean.TRUE;
        invokeMethod(mSetForeground, mSetForegroundArgs);
        mNM.notify(id, notification);
    }

    /**
     * This is a wrapper around the new stopForeground method, using the older
     * APIs if it is not available.
     */
    void stopForegroundCompat(int id) {
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            invokeMethod(mStopForeground, mStopForegroundArgs);
            return;
        }

        // Fall back on the old API.  Note to cancel BEFORE changing the
        // foreground state, since we could be killed at that point.
        mNM.cancel(id);
        mSetForegroundArgs[0] = Boolean.FALSE;
        invokeMethod(mSetForeground, mSetForegroundArgs);
    }


}

