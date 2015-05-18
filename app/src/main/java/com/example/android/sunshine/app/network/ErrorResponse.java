package com.example.android.sunshine.app.network;

/**
 * Created by tomtang on 18/05/15.
 */
public class ErrorResponse {
    Error error;

    public static class Error {
        Data data;

        public static class Data {
            String message;
        }
    }
}