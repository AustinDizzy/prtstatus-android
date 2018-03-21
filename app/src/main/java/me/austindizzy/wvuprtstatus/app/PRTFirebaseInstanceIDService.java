package me.austindizzy.wvuprtstatus.app;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class PRTFirebaseInstanceIDService extends FirebaseInstanceIdService {

    final String userApiUri = "https://prtstat.us/api/user";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        sendIdToServer(refreshedToken);
        FirebaseMessaging.getInstance().subscribeToTopic("prt-updates");
        super.onTokenRefresh();
    }

    private void sendIdToServer(final String token) {
        StringRequest req = new StringRequest(Request.Method.POST, userApiUri, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //response
                Log.d("Reg Response", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                Log.d("Reg Error Response", error.getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("regID", token);
                return params;
            }
        };
        HTTPRequestQueue.getInstance(this.getApplicationContext()).addToRequestQueue(req);
    }
}
