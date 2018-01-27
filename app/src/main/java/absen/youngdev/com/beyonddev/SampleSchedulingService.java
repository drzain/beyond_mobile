package absen.youngdev.com.beyonddev;

import android.app.IntentService;
import android.content.Intent;
import android.os.StrictMode;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Denny on 01/14/2018.
 */

public class SampleSchedulingService extends IntentService {
    public SampleSchedulingService() {
        super("SchedulingService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        OutputStream os = null;
        InputStream is = null;
        HttpURLConnection conn = null;
        try {
            //constants
            URL url = new URL("http://172.16.31.199:1999/C_Version");
            JSONObject jsonObject = new JSONObject();
            int currentversionCode = BuildConfig.VERSION_CODE;
            jsonObject.put("version", currentversionCode);

            String message = jsonObject.toString();
            StringBuffer response = null;
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /*milliseconds*/);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setFixedLengthStreamingMode(message.getBytes().length);

            //make some HTTP header nicety
            conn.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

            //open
            conn.connect();

            //setup send
            os = new BufferedOutputStream(conn.getOutputStream());
            os.write(message.getBytes());
            Log.d("SEND TO SERVER:", String.valueOf(message.getBytes()));
            //clean up
            os.flush();

            //do somehting with response
            is = conn.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            int serverversion= Integer.parseInt(response.toString());

            Log.d("SERVER Response:", String.valueOf(Integer.parseInt(response.toString())));
            os.close();
            is.close();
            conn.disconnect();
            if(serverversion>currentversionCode){
                Intent myIntent = new Intent(this, ShowNote.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(myIntent);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        // Release the wake lock provided by the BroadcastReceiver.

        SampleAlarmReceiver.completeWakefulIntent(intent);

        // END_INCLUDE(service_onhandle)
    }
}
