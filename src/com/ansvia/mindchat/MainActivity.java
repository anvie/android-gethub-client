/*
 * Copyright (c) 2012. Ansvia Inc.
 * Author: Robin Syihab.
 */

package com.ansvia.mindchat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity implements View.OnClickListener {

    Button loginButton = null;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        loginButton = (Button)findViewById(R.id.login);
        loginButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        EditText textUserName = (EditText)findViewById(R.id.inputUserName);
        EditText textPassword = (EditText)findViewById(R.id.inputPassword);

        String userName = textUserName.getText().toString();
        String password = textPassword.getText().toString();

        Intent svc = new Intent(this, ChatService.class);

        svc.putExtra("userName", userName);
        svc.putExtra("password", password);

        startService(svc);

    }
}
