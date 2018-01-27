package absen.youngdev.com.beyonddev;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class DaftarActivity extends Activity {

    EditText uNama,uFullname,uPassword;
    Button uDaftar;
    TelephonyManager telephonyManager;
    private static final String TAG = DaftarActivity.class.getSimpleName();
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar);

        checkStatePermission();
        uNama = (EditText) findViewById(R.id.txt_username);
        uFullname = (EditText) findViewById(R.id.txt_fullname);
        uPassword = (EditText) findViewById(R.id.txt_password);

        uDaftar = (Button) findViewById(R.id.btn_register);
        uDaftar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptDaftar();
            }
        });
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if ( id == android.R.id.home ) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void attemptDaftar() {
        checkStatePermission();
        String imeinumber = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Reset errors.
        uNama.setError(null);
        uFullname.setError(null);
        uPassword.setError(null);

        // Store values at the time of the login attempt.
        String fullname = uFullname.getText().toString();
        String password = uPassword.getText().toString();
        String nama = uNama.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            uPassword.setError(getString(R.string.error_invalid_password));
            focusView = uPassword;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(fullname)) {
            uFullname.setError(getString(R.string.error_field_required));
            focusView = uFullname;
            cancel = true;
        }

        if(TextUtils.isEmpty(nama)) {
            uNama.setError(getString(R.string.err_msg_name));
            focusView = uNama;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            new UserAddTask().execute(nama, fullname, password, imeinumber);
        }
    }

    public boolean checkStatePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(DaftarActivity.this,
                                        new String[]{Manifest.permission.READ_PHONE_STATE},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();
                Log.d(TAG, "Minta Permission!");

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST_LOCATION);
                Log.d(TAG, "ini else ya!");
            }
            return false;
        } else {
            return true;
        }
    }

    public class UserAddTask extends AsyncTask<String, String, JSONObject> {

        ProgressDialog pdLoading = new ProgressDialog(DaftarActivity.this);
        JSONParser jsonParser = new JSONParser();
        private static final String TAG_INFO = "info";
        private static final String DAFTAR_URL = "http://172.16.31.199:1999/C_Register";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //this method will be running on UI thread
            pdLoading.setMessage("\tLoading...");
            pdLoading.setCancelable(false);
            pdLoading.show();

        }


        @Override
        protected JSONObject doInBackground(String... args) {
            try {

                HashMap<String, String> params = new HashMap<>();
                params.put("nama", args[0]);
                params.put("fullname", args[1]);
                params.put("password", args[2]);
                params.put("imei", args[3]);


                Log.d("request", "starting");

                JSONObject json = jsonParser.makeHttpRequest(
                        DAFTAR_URL, "POST", params);

                if (json != null) {
                    Log.d("JSON result", json.toString());

                    return json;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        protected void onPostExecute(JSONObject json) {
            String info = "";


            if (json != null) {
                //Toast.makeText(LoginActivity.this, json.toString(),
                //Toast.LENGTH_LONG).show();

                try {
                    info = json.getString(TAG_INFO);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            if(info.equals("success")) {
                pdLoading.dismiss();
                Toast.makeText(DaftarActivity.this, "User Berhasil Dibuat, Hubungi Admin Untuk Aktivasi!",
                        Toast.LENGTH_LONG).show();
                Intent intent = new Intent(DaftarActivity.this, NewLoginActivity.class);
                startActivity(intent);
                DaftarActivity.this.finish();
            }else if(info.equals("kolom")){
                pdLoading.dismiss();
                Toast.makeText(DaftarActivity.this, "Isi Semua Kolom !!",
                        Toast.LENGTH_LONG).show();
            }else{
                pdLoading.dismiss();
                Toast.makeText(DaftarActivity.this, "Username atau Handphone sudah terdaftar !!",
                        Toast.LENGTH_LONG).show();
            }

        }

    }
}
