/*
 * Copyright (c) 2012. Ansvia Inc.
 * Author: Robin Syihab.
 */

package com.ansvia.mindchat;

import android.util.Log;
import com.ansvia.mindchat.exceptions.UnauthorizedException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;

public class GethubClient {

    private static final String TAG = "GethubClient";
    private static final String API_VERSION = "1";

    private static final String AUTHORIZE = "{\"ns\":\"authorize\",\"version\":" + API_VERSION + ",\"id\":\"%d\",\"userName\":\"%s\",\"password\":\"%s\"}";
    private static final String JOIN = "{\"ns\":\"chat::join\",\"version\":" + API_VERSION + ",\"id\":\"%d\",\"channel\":\"%s\",\"sessid\":\"%s\"}";
    private static final String PARTICIPANTS = "{\"ns\":\"chat::channel::participants\",\"version\":" + API_VERSION + ",\"id\":\"%d\",\"channel\":\"%s\",\"sessid\":\"%s\"}";
    private static final String BIND = "{\"ns\":\"chat::bind\",\"version\":" + API_VERSION + ",\"id\":\"%d\",\"channel\":\"%s\",\"sessid\":\"%s\"}";
    private static final String MESSAGE = "{\"ns\":\"chat::message\",\"version\":" + API_VERSION + ",\"id\":\"%d\",\"channel\":\"%s\",\"sessid\":\"%s\",\"message\":\"%s\"}";
    private static final String CHANNEL_LAST_MESSAGE = "{\"ns\":\"chat::channel::messages\",\"version\":" + API_VERSION + ",\"id\":\"%d\",\"channel\":\"%s\",\"sessid\":\"%s\"}";


    private Socket socket = null;
    private int incrementalId = 0;
    private String host = "127.0.0.1";
    private int port = 6060;

    private static GethubClient ourInstance = new GethubClient();

    public static GethubClient getInstance() {
        return ourInstance;
    }

    private GethubClient() {
    }

    /**
     * Connect to Gethub server.
     * @param host ip or host name.
     * @param port port number.
     */
    public void connect(String host, int port){

        this.host = host;
        this.port = port;

        connectInternal(host, port);
    }

    /**
     * Close connection.
     */
    public void close(){
        if(socket != null){
            try {
                socket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Generate incremental id.
     * Thread safe.
     * @private
     * @return number.
     */
    private int genId(){
        synchronized (this){
            return ++incrementalId;
        }
    }

    /**
     * Authorize user by providing user name and password.
     * @param userName name of user to authorize.
     * @param password password.
     * @return session id if success otherwise null.
     */
    public String authorize(String userName, String password) throws UnauthorizedException {
        String resultStr = sendPacketInternal(String.format(AUTHORIZE, genId(), userName, password), 1024);
        String rv = null;
        try {
            JSONObject jsonResult = new JSONObject(resultStr);

            if(jsonResult.has("result")){
                jsonResult = jsonResult.getJSONObject("result");

                rv = jsonResult.getString("sessid");
            }else{
                if (jsonResult.has("error")){
                    throw new UnauthorizedException(jsonResult.getString("error"));
                }
            }

        }catch (JSONException e){
            e.printStackTrace();
        }
        return rv;
    }


    /**
     * Join to channel.
     * @param channelName channel name target.
     * @param sessid id of session.
     * @return true if success otherwise not.
     */
    public boolean join(String channelName, String sessid){
        boolean rv = false;
        try {
            String result = sendPacketInternal(String.format(JOIN, genId(), channelName, sessid), 1024);

            JSONObject jo = new JSONObject(result);
            jo = jo.getJSONObject("result");

            rv = jo.has("participants");
        }catch (Exception e){
            e.printStackTrace();
        }
        return rv;
    }


    /**
     * Get channel participants.
     * @param channelName name of channel to get.
     * @param sessid id of session.
     * @return if success return string array of participants otherwise null.
     */
    public LinkedList<String> participants(String channelName, String sessid){
        LinkedList<String> rv = null;

        try {
            String result = sendPacketInternal(String.format(PARTICIPANTS, genId(), channelName, sessid), 1024);

            JSONObject jo = new JSONObject(result);
            jo = jo.getJSONObject("result");

            JSONArray parts = jo.getJSONArray("participants");

            LinkedList<String> participants = new LinkedList<String>();

            int i = 0;
            while (i < parts.length()){
                participants.add(parts.getString(i++));
            }

            rv = participants;
        }catch (Exception e){
            e.printStackTrace();
        }
        return rv;
    }



    /**
     * Start binding to channel.
     * this will creating new connection to allow us binding without
     * worry about race condition in multi threading environment.
     * @param channelName name of channel to bind.
     * @param sessid session id.
     */
    public void bind(String channelName, String sessid, GethubClientDataReceiver receiver){
        Socket s = null;
        try {

            String data = String.format(BIND, genId(), channelName, sessid);

            s = new Socket(this.host, this.port);

            OutputStream os = s.getOutputStream();

            os.write((data + "\n").getBytes());

            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));

            char [] buff = new char[1024];

            br.read(buff);

            while (true){
                int read = br.read(buff);

                if (read > 0){
                    String resultStr = new String(buff, 0, read);
                    JSONObject jo = new JSONObject(resultStr);
                    jo = jo.getJSONObject("result");
                    receiver.onReceive(jo);
                }else{
                    break;
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(s!=null){
                try {
                    s.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Send chat message.
     * @param channelName name of channel.
     * @param message message to send.
     * @param sessid id of session.
     * @return true if success otherwise not.
     */
    public boolean message(String channelName, String message, String sessid){
        // @TODO(robin): don't hard-coded return result.
        boolean rv = true;
        try {
            String result = sendPacketInternal(String.format(MESSAGE, genId(), channelName, sessid, message), 1024);

            JSONObject jo = new JSONObject(result);
            jo = jo.getJSONObject("result");

            Log.d(TAG, jo.toString());
        }catch (Exception e){
            e.printStackTrace();
        }
        return rv;
    }


    public LinkedList<String> messages(String channelName, String sessid) {
        LinkedList<String> rv = new LinkedList<String>();
        try {
            String result = sendPacketInternal(String.format(CHANNEL_LAST_MESSAGE, genId(), channelName, sessid), 1024*4);

            JSONObject jo = new JSONObject(result);
            jo = jo.getJSONObject("result");

            JSONArray messages = jo.getJSONArray("messages");

            int i = 0;
            while (i < messages.length()){
                JSONObject msg = messages.getJSONObject(i++);
                rv.add(msg.getString("user") + ": " + msg.getString("message"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return rv;
    }


    private int tried = 0;

    /**
     * Send packet to server.
     * @param data data to send.
     * @return string result (should decoded using json decoder).
     */
    public String sendPacketInternal(String data, int packetSize){

        String resultStr = "";

        try {

            OutputStream os = socket.getOutputStream();

            os.write((data + "\n").getBytes());

            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            char [] buff = new char[packetSize];

            int read = br.read(buff);

            if (read > 0){
                resultStr = new String(buff, 0, read);
            }

            tried = 0;

        }catch (Exception e){
            e.printStackTrace();

            // reconnect
            connectInternal(host, port);

            tried++;
            if(tried < 5){
                resultStr = sendPacketInternal(data, packetSize);
            }else{
                Log.e(TAG, "Give up, cannot retry connect to gethub server.");
                tried = 0;
            }

        }

        return resultStr;
    }

    /**
     * internal method to connect to Gethub server.
     * @param host host name or ip address.
     * @param port port number.
     */
    private void connectInternal(String host, int port) {
        try {
            socket = new Socket(host, port);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void logout() {
        close();
    }
}
