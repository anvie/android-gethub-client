/*
 * Copyright (c) 2012. Ansvia Inc.
 * Author: Robin Syihab.
 */

package com.ansvia.mindchat;

public class GlobalState {
    private static GlobalState ourInstance = new GlobalState();

    public boolean authFailed = true;

    public static GlobalState getInstance() {
        return ourInstance;
    }

    private GlobalState() {
    }
}
