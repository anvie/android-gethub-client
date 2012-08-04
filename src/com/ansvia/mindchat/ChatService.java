/*
 * Copyright (c) 2012. Ansvia Inc.
 * Author: Robin Syihab.
 */

package com.ansvia.mindchat;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Main chat service.
 */
public class ChatService extends IntentService {

    private static final String TAG = "ChatService";

    // @TODO(*): jangan di hard-coded.
    private static final String CHANNEL = "localhost";

    private static final String NS_MESSAGE = "chat::message";


    public ChatService(String name) {
        super(name);
    }

    public ChatService(){
        super(TAG);
    }

    /**
     * Class ini digunakan sebagai data receiver
     * ketika binding sukses maka class ini
     * akan mendapatkan push data dari server.
     */
    private class DataReceiver extends GethubClientDataReceiver {
        private GethubClient gethub = null;
        private String sessid = null;

        public DataReceiver(GethubClient gethub, String sessid){
            this.gethub = gethub;
            this.sessid = sessid;
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
                    gethub.message(CHANNEL, message + "? apa itu?", sessid);
                }
            }catch (JSONException e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "in onHandleIntent()");

        GethubClient gethub = GethubClient.getInstance();

        // set your gethub server address here.
        gethub.connect("10.0.2.2", 6060);

        String sessid = gethub.authorize(intent.getStringExtra("userName"), intent.getStringExtra("password"));

        if(sessid == null){
            Log.i(TAG, "Cannot authorize user");
            return;
        }

        if(!gethub.join(CHANNEL, sessid)){
            Log.i(TAG, "Cannot join to channel");
            return;
        }

        gethub.bind(CHANNEL, sessid, new DataReceiver(gethub, sessid));

    }
}
