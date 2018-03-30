package me.austindizzy.wvuprtstatus.app;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class HTTPRequestQueue {

    private static HTTPRequestQueue mInstance;
    private RequestQueue mRequestQueue;
    private Context mCtx;

    private HTTPRequestQueue(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
    }

    public static synchronized HTTPRequestQueue getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new HTTPRequestQueue(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
