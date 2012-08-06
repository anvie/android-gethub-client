/*
 * Copyright (c) 2012. Ansvia Inc.
 * Author: Robin Syihab.
 */

package com.ansvia.mindchat;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Class ini digunakan sebagai data receiver
 * ketika binding sukses maka class ini
 * akan mendapatkan push data dari server.
 */
public class PacketHandler extends GethubClientDataReceiver {

    private final static String TAG = "PacketHandler";

    public class Namespace {

        static final String MESSAGE = "chat::message";
        static final String CHANNEL_PARTICIPANTS = "chat::channel::participants";
        static final String CHANNEL_LAST_MESSAGES = "chat::channel::messages";
        static final String JOIN = "chat::join";

    }

    private GethubClient gethub = null;
    private String sessid = null;
    private String currentUserName = null;
    private IntentService ctx = null;

    public PacketHandler(IntentService ctx, GethubClient gethub, String sessid, String currentUserName){
        this.gethub = gethub;
        this.sessid = sessid;
        this.currentUserName = currentUserName;
        this.ctx = ctx;
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
            if (ns.equals(Namespace.MESSAGE)){
                String message = data.getString("message");

                Log.i(TAG, "from " + data.getString("from") + ": " + message);

                Intent intent = new Intent("new.message");
                intent.putExtra("data", data.getString("from") + ": " + message);
                intent.putExtra("self", data.getString("from").equalsIgnoreCase(currentUserName));

                sendBroadcast(intent);

            }else if(ns.equals(Namespace.CHANNEL_PARTICIPANTS)){

            }else if(ns.equals(Namespace.JOIN)){

                JSONArray participants =  data.getJSONArray("participants");

                int i = 0;
                while (i < participants.length() - 1){
                    String userName = participants.getString(i);
                    Intent intent = new Intent("participant");
                    intent.putExtra("userName", userName);
                    sendBroadcast(intent);
                }

            }

        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void sendBroadcast(Intent intent) {
        this.ctx.sendBroadcast(intent);
    }


}
