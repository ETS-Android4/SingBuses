package com.itachi1706.busarrivalsg.AsyncTasks;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.itachi1706.busarrivalsg.NTUBusActivity;
import com.itachi1706.busarrivalsg.R;
import com.itachi1706.busarrivalsg.Util.StaticVariables;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

/**
 * Created by Kenneth on 07/9/2018
 * for SingBuses in package com.itachi1706.busarrivalsg.AsyncTasks
 */
public class GetNTUData extends AsyncTask<String, Void, Integer> {

    private WeakReference<Activity> activityRef;
    private Exception except;
    private int update;

    public GetNTUData(Activity activity, boolean update) {
        this.activityRef = new WeakReference<>(activity);
        this.update = (update) ? 1 : 0;
    }

    @Override
    protected Integer doInBackground(String... routes) {
        StringBuilder routeString = new StringBuilder();
        if (routes.length == 1) {
            routeString = new StringBuilder(routes[0]);
        } else {
            for (String r : routes) {
                routeString.append(r).append(",");
            }
            routeString = new StringBuilder(routeString.toString().replaceAll(",$", ""));
        }
        Activity mActivity = activityRef.get();
        String url = "http://api.itachi1706.com/api/ntubus.php?route=" + routeString.toString() + "&update=" + update;
        Log.d("NTUData", url);
        String tmp;
        try {
            URL urlConn = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlConn.openConnection();
            conn.setConnectTimeout(StaticVariables.HTTP_QUERY_TIMEOUT);
            conn.setReadTimeout(StaticVariables.HTTP_QUERY_TIMEOUT);
            InputStream in = conn.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder str = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null)
            {
                str.append(line);
            }
            in.close();
            tmp = str.toString();
        } catch (IOException e) {
            except = e;
            return 1;
        }

        Log.d("NTUData", tmp);
        if (!StaticVariables.checkIfYouGotJsonString(tmp)) {
            except = new Exception(mActivity.getResources().getString(R.string.toast_message_invalid_json));
            return 2;
        }

        // TODO: Do the processing here
        Intent sendForMapParsingIntent = new Intent(NTUBusActivity.RECEIVE_NTU_DATA_EVENT);
        sendForMapParsingIntent.putExtra("data", tmp);
        mActivity.runOnUiThread(() -> LocalBroadcastManager.getInstance(mActivity).sendBroadcast(sendForMapParsingIntent));
        return 0;
    }

    protected void onPostExecute(Integer errorCode) {
        Context context = activityRef.get();
        if (except != null && errorCode != 0) {
            Log.e("NTUData", "Exception occurred (" + except.getMessage() + ")");
            if (except instanceof SocketTimeoutException) {
                Toast.makeText(context, "NTU API did not respond after the period of time", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, except.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
