/*
 * Copyright (c) 2012. Ansvia Inc.
 * Author: Robin Syihab.
 */

package com.ansvia.mindchat;

import android.util.Log;
import org.json.JSONObject;

abstract class GethubClientDataReceiver {
    private static final String TAG = "GethubClientDataReceiver";

    public void onReceive(JSONObject data){
        // override this method.
        Log.i(TAG, data.toString());
    }
}
