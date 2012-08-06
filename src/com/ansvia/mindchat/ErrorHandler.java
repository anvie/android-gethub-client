/*
 * Copyright (c) 2012. Ansvia Inc.
 * Author: Robin Syihab.
 */

package com.ansvia.mindchat;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;



class ErrorHandler extends BroadcastReceiver {

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
                ErrorHandler.this.onOk();
                dialogInterface.dismiss();
            }
        });
        ad.show();
    }

    void onOk() {
        // override this
    }
}
