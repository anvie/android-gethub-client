/*
 * Copyright (c) 2012. Ansvia Inc.
 * Author: Robin Syihab.
 */

package com.ansvia.mindchat;

import android.app.IntentService;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Main chat service.
 */
public class ChatService extends IntentService {

    private static final String TAG = "ChatService";
    private static final String NS_MESSAGE = "chat::message";
    private static final String GETHUB_HOST = "www.gethub.us";
    private static final int GETHUB_PORT = 6060;


    // @TODO(*): jangan di hard-coded.
    private static final String CHANNEL = "www.gethub.us";



    /**
     * Class ini digunakan sebagai data receiver
     * ketika binding sukses maka class ini
     * akan mendapatkan push data dari server.
     */
    private class DataReceiver extends GethubClientDataReceiver {
        private GethubClient gethub = null;
        private String sessid = null;
        private String currentUserName = null;

        public DataReceiver(GethubClient gethub, String sessid, String currentUserName){
            this.gethub = gethub;
            this.sessid = sessid;
            this.currentUserName = currentUserName;
        }

        /**
         * Implementasi method ini untuk menghandle
         * data dari server.
         * data sudah berupa {{JSONObject}}
         * @param data data dari server.
         */
        @Override
        public void onReceive(JSONObject data) {
            Log.d(TAG, "receiver got: " + data.toString());

            try {
                String ns = data.getString("ns");
                if (ns.equals(NS_MESSAGE)){
                    String message = data.getString("message");
                    Log.i(TAG, "from " + data.getString("from") + ": " + message);
//                    if(!data.getString("from").equals(currentUserName)){
//                        gethub.message(CHANNEL, message + "? apa itu?", sessid);
//                    }
                    //appendMessage(data.getString("from") + ": " + message);
                    Intent intent = new Intent("new.message");
                    intent.putExtra("data", data.getString("from") + ": " + message);
                    sendBroadcast(intent);
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }




    public ChatService(String name) {
        super(name);
    }

    public ChatService(){
        super(TAG);
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "in onHandleIntent()");

        GethubClient gethub = GethubClient.getInstance();

        // set your gethub server address here.
        gethub.connect(GETHUB_HOST, GETHUB_PORT);

        try {


            String sessid = gethub.authorize(intent.getStringExtra("userName"), intent.getStringExtra("password"));

            if(sessid == null){
                Log.i(TAG, "Cannot authorize user");
                return;
            }

            if(!gethub.join(CHANNEL, sessid)){
                Log.i(TAG, "Cannot join to channel");
                return;
            }

            DataReceiver dataRec = new DataReceiver(gethub, sessid, intent.getStringExtra("userName"));


            Intent chatRoom = new Intent(this, ChatRoomActivity.class);

            chatRoom.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


            chatRoom.putExtra("sessid", sessid);
            chatRoom.putExtra("userName", intent.getStringExtra("userName"));
            chatRoom.putExtra("password", intent.getStringExtra("password"));
            chatRoom.putExtra("channel", CHANNEL);

            startActivity(chatRoom);


            gethub.bind(CHANNEL, sessid, dataRec);
        }catch (Exception e){
            Intent errorIntent = new Intent("error");
            errorIntent.putExtra("data", e.getMessage());
            sendBroadcast(errorIntent);
        }

    }
}

