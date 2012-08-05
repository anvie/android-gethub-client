/*
 * Copyright (c) 2012. Ansvia Inc.
 * Author: Robin Syihab.
 */

package com.ansvia.mindchat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity implements View.OnClickListener {

    Button loginButton = null;
    Button logoutButton = null;
    private ErrorHandler errorReceiver;
    private Intent svc;


    private class ErrorHandler extends BroadcastReceiver {

        Activity activity = null;

        public ErrorHandler(Activity act){
            this.activity = act;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra("data");
            AlertDialog ad = new AlertDialog.Builder(activity).create();
            ad.setCancelable(false);
            ad.setMessage(data);
            ad.setButton("OK", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            ad.show();
        }
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        this.errorReceiver = new ErrorHandler(this);
        registerReceiver(this.errorReceiver, new IntentFilter("error"));

        loginButton = (Button)findViewById(R.id.login);
        logoutButton = (Button)findViewById(R.id.logout);

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
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(this.errorReceiver);
    }

    @Override
    public void onClick(View view) {

        EditText textUserName = (EditText)findViewById(R.id.inputUserName);
        EditText textPassword = (EditText)findViewById(R.id.inputPassword);

        ((Button)(view)).setEnabled(false);

        String userName = textUserName.getText().toString();
        String password = textPassword.getText().toString();

        if(svc != null){
            stopService(svc);
        }

        svc = new Intent(this, ChatService.class);

        svc.putExtra("userName", userName);
        svc.putExtra("password", password);

        startService(svc);




        textUserName.setVisibility(View.INVISIBLE);
        textPassword.setVisibility(View.INVISIBLE);
        view.setVisibility(View.INVISIBLE);
        findViewById(R.id.txtUserName).setVisibility(View.INVISIBLE);
        findViewById(R.id.txtPassword).setVisibility(View.INVISIBLE);


        findViewById(R.id.logout).setVisibility(View.VISIBLE);

        //((Button)(view)).setEnabled(true);

    }
}
