package com.google.android.gms.common.api;

/** Compile-time declaration only. The installed app provides Play services' implementation. */
public class ApiException extends Exception {
    public int getStatusCode() { return 0; }
}
