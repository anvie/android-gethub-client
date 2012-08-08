/*
 * Copyright (c) 2012. Ansvia Inc.
 * Author: Robin Syihab.
 */

package com.ansvia.mindchat;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements View.OnClickListener {

    private static String TAG = "MainActivity";
    Button loginButton = null;
    Button logoutButton = null;
    private ErrorHandler errorReceiver;
    //private Intent svc;
    private Intent chatRoom = null;
    private EditText textUserName;
    private EditText textPassword;
    private TextView lblMainHeader;


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

//        this.errorReceiver = new ErrorHandler(this);
//        registerReceiver(this.errorReceiver, new IntentFilter("error"));

        loginButton = (Button)findViewById(R.id.login);
        logoutButton = (Button)findViewById(R.id.logout);
        textUserName = (EditText)findViewById(R.id.inputUserName);
        textPassword = (EditText)findViewById(R.id.inputPassword);
        lblMainHeader = (TextView)findViewById(R.id.lblMainHeader);


        try {
            PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            String origText = lblMainHeader.getText().toString();
            lblMainHeader.setText(origText + " " + pi.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        loginButton.setOnClickListener(this);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent("logout");
                sendBroadcast(intent);

                view.setVisibility(View.INVISIBLE);
                findViewById(R.id.txtUserName).setVisibility(View.VISIBLE);
                findViewById(R.id.inputUserName).setVisibility(View.VISIBLE);
                findViewById(R.id.txtPassword).setVisibility(View.VISIBLE);
                findViewById(R.id.inputPassword).setVisibility(View.VISIBLE);
                loginButton.setVisibility(View.VISIBLE);
                loginButton.setEnabled(true);

                SharedPreferences.Editor sp = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();

                sp.remove("userName");
                sp.remove("password");
                sp.commit();
            }
        });


    }
//
//    @Override
//    protected void onRestoreInstanceState(Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        Log.d(TAG, "started: " + savedInstanceState.getBoolean("started"));
//    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(chatRoom != null){
//            startActivity(chatRoom);
//        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        //sp.edit().clear().commit();
        String lastActivity = sp.getString("last_activity","");
        if (lastActivity.equals(ChatRoomActivity.class.getSimpleName())){
            String userName = sp.getString("userName", "");
            String password = sp.getString("password", "'");

            if(chatRoom != null){

                loginButton.setVisibility(View.INVISIBLE);

                chatRoom.putExtra("userName", userName);
                chatRoom.putExtra("password", password);

                startActivityIfNeeded(chatRoom, 0);
            }else {
                enterChatRoom(userName, password);
            }
        }else {

            if(chatRoom != null && !GlobalState.getInstance().authFailed){
                textUserName.setVisibility(View.INVISIBLE);
                textPassword.setVisibility(View.INVISIBLE);
                findViewById(R.id.txtUserName).setVisibility(View.INVISIBLE);
                findViewById(R.id.txtPassword).setVisibility(View.INVISIBLE);
                logoutButton.setVisibility(View.VISIBLE);
            }else{
                textUserName.setVisibility(View.VISIBLE);
                textPassword.setVisibility(View.VISIBLE);
                findViewById(R.id.txtUserName).setVisibility(View.VISIBLE);
                findViewById(R.id.txtPassword).setVisibility(View.VISIBLE);
                loginButton.setVisibility(View.VISIBLE);
                loginButton.setEnabled(true);
                logoutButton.setVisibility(View.INVISIBLE);
            }

        }
    }
//
//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putBoolean("started", true);
//    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregisterReceiver(this.errorReceiver);
        //stopService(svc);
    }

    @Override
    public void onClick(View view) {

        ((Button)(view)).setEnabled(false);

        String userName = textUserName.getText().toString();
        String password = textPassword.getText().toString();

        SharedPreferences.Editor sp = PreferenceManager.getDefaultSharedPreferences(this).edit();

        sp.putString("userName", userName);
        sp.putString("password", password);
        sp.commit();


        enterChatRoom(userName, password);

        view.setVisibility(View.INVISIBLE);

        //((Button)(view)).setEnabled(true);

    }

    private void enterChatRoom(String userName, String password) {

        EditText textUserName = (EditText)findViewById(R.id.inputUserName);
        EditText textPassword = (EditText)findViewById(R.id.inputPassword);


        if(chatRoom == null){
            chatRoom = new Intent(this, ChatRoomActivity.class);
            chatRoom.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

//        chatRoom.putExtra("sessid", sessid);
        chatRoom.putExtra("userName", userName);
        chatRoom.putExtra("password", password);
//        chatRoom.putExtra("channel", CHANNEL);

        startActivity(chatRoom);


        textUserName.setVisibility(View.INVISIBLE);
        textPassword.setVisibility(View.INVISIBLE);

        findViewById(R.id.txtUserName).setVisibility(View.INVISIBLE);
        findViewById(R.id.txtPassword).setVisibility(View.INVISIBLE);


        findViewById(R.id.logout).setVisibility(View.VISIBLE);
    }
}
