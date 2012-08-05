/*
 * Copyright (c) 2012. Ansvia Inc.
 * Author: Robin Syihab.
 */

package com.ansvia.mindchat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.io.IOException;
import java.util.LinkedList;



public class ChatRoomActivity extends  Activity implements View.OnClickListener {

    private static final String TAG = "ChatRoomActivity";
    private static final String NS_MESSAGE = "chat::message";

    private LinkedList<String> chatMessages = new LinkedList<String>();

    // @TODO(*): jangan di hard-coded.
    //private static final String CHANNEL = "localhost";

    private String sessid = null;
    private String channel = null;

    LinearLayout chatContainer = null;
    private LinkedList<String> participants = null;
    private String userName;


    private class NewMessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra("data");
            Boolean self = intent.getBooleanExtra("self", false);
            chatMessages.add(text);
            appendMessage(text);
            if(!self){
                playSound();
            }
        }
    }

    private class ParticipantReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String userName = intent.getStringExtra("userName");
            appendStatus("joined: " + userName);
        }
    }

    private class MainLinearLayout extends LinearLayout {

        public MainLinearLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.chat_room, this);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            final int proposedheight = MeasureSpec.getSize(heightMeasureSpec);
            final int actualHeight = getHeight();

            if (actualHeight > proposedheight){
                // Keyboard is shown
                Log.d(TAG, "Keyboard is shown");

                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);

                ScrollView chatContainerFrame = (ScrollView)findViewById(R.id.chatContainerFrame);

                chatContainerFrame.setLayoutParams(new LinearLayout.LayoutParams(
                        metrics.widthPixels, proposedheight - 100));

            } else {
                // Keyboard is hidden
                Log.d(TAG, "Keyboard is hidden");

                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);

                ScrollView chatContainerFrame = (ScrollView)findViewById(R.id.chatContainerFrame);
                chatContainerFrame.setLayoutParams(new LinearLayout.LayoutParams(
                        metrics.widthPixels, metrics.heightPixels - 200));
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }



    private void playSound() {
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        if (alert!=null){
            MediaPlayer mplayer = new MediaPlayer();
            try {
                mplayer.setDataSource(this, alert);
                //final AudioManager audman = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                mplayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
                mplayer.setLooping(false);
                mplayer.prepare();
                mplayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.chat_room);
        setContentView(new MainLinearLayout(this, null));

        registerReceiver(new NewMessageReceiver(), new IntentFilter("new.message"));
        registerReceiver(new ParticipantReceiver(), new IntentFilter("participant"));

        GethubClient gethub = GethubClient.getInstance();

        Bundle extras = getIntent().getExtras();

        this.sessid = extras.getString("sessid");
        this.channel = extras.getString("channel");
        this.userName = extras.getString("userName");

        this.participants = gethub.participants(channel, sessid);

        this.chatMessages = gethub.messages(channel, sessid);

        initializeUi();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setContentView(R.layout.chat_room);
        initializeUi();
    }

    private void initializeUi() {
        chatContainer = (LinearLayout)findViewById(R.id.chatContainer);


//        GethubClient gethub = GethubClient.getInstance();

        appendStatus("Welcome " + userName);

        if(participants.size() > 0){
            StringBuilder strb = new StringBuilder();
            strb.append("Participants: ");
            for(String participant : participants){
                strb.append(participant).append(", ");
            }
            appendStatus(strb.toString().substring(0, strb.toString().length()-2));
        }

        Button btnSend = (Button)findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        ScrollView chatContainerFrame = (ScrollView)findViewById(R.id.chatContainerFrame);
        //LinearLayout mainContainer = (LinearLayout)findViewById(R.id.mainContainer);

//        Log.d(TAG, "outer: " + mainContainer.getLayoutParams().width + ", " + mainContainer.getLayoutParams().height);
//        Log.d(TAG, "inner: " + chatContainerFrame.getLayoutParams().width + ", " + chatContainerFrame.getLayoutParams().height);
//        Log.d(TAG, "chatCont: " + chatContainer.getLayoutParams().width + ", " + chatContainer.getLayoutParams().height);

        chatContainerFrame.setLayoutParams(new LinearLayout.LayoutParams(
                metrics.widthPixels, metrics.heightPixels - 200));

        // fill with chat messages
        for(String msg : chatMessages){
            appendMessage(msg);
        }

    }

    private void appendStatus(String status) {

        TextView text = new TextView(this);
        text.setId((int)System.currentTimeMillis());
        text.setText(status);
        text.setTextColor(Color.parseColor("#8AE6B8"));
        text.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        chatContainer.addView(text);
        chatContainer.refreshDrawableState();
    }

    public void appendMessage(String message){

        TextView text = new TextView(this);
        text.setId((int)System.currentTimeMillis());
        text.setText(message);
        text.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        chatContainer.addView(text);
        chatContainer.refreshDrawableState();
    }

    @Override
    public void onClick(View view) {
        EditText text = (EditText)findViewById(R.id.inputMessage);

        GethubClient gethub = GethubClient.getInstance();

        gethub.message(channel, text.getText().toString(), sessid);

        text.setText("");
    }
}
