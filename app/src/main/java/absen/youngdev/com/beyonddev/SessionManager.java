package absen.youngdev.com.beyonddev;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

/**
 * Created by Denny on 10/29/2016.
 */

public class SessionManager {
    private static String TAG = SessionManager.class.getSimpleName();

    SharedPreferences pref;

    Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "BeyondDevLogin";
    private static final String KEY_IS_LOGGEDIN = "isLoggenIn";
    private static final String KEY_IS_USER = "isUser";
    private static final String KEY_IS_NAMA = "isNama";
    private static final String KEY_IS_EMAIL = "isEmail";
    private static final String KEY_IS_IDUSER = "isIdUser";
    private static final String KEY_IS_IDPHONE = "isIdPhone";


    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setLogin(boolean isLoggedIn){
        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);
        editor.commit();
        Log.d(TAG, "User login session modified");
    }

    public void setId(int isIdUser){
        editor.putInt(KEY_IS_IDUSER, isIdUser);
        editor.commit();
        Log.d(TAG, "ID User akses session modified");
    }

    public void setPhone(String isIdPhone){
        editor.putString(KEY_IS_IDPHONE, isIdPhone);
        editor.commit();
        Log.d(TAG, "ID Phone session modified");
    }

    public void setStatus(String isUser){
        editor.putString(KEY_IS_USER, isUser);
        editor.commit();
        Log.d(TAG, "User akses session modified");
    }

    public void setNama(String isNama){
        editor.putString(KEY_IS_NAMA, isNama);
        editor.commit();
        Log.d(TAG, "Username session modified");
    }

    public void setEmail(String isEmail){
        editor.putString(KEY_IS_EMAIL, isEmail);
        editor.commit();
        Log.d(TAG, "Email session modified");
    }

    public String isNama(){
        return pref.getString(KEY_IS_NAMA, "");
    }

    public int isId(){
        return pref.getInt(KEY_IS_IDUSER, 0);
    }

    public String isEmail(){
        return pref.getString(KEY_IS_EMAIL, "");
    }

    public String isUser(){
        return pref.getString(KEY_IS_USER, "");
    }

    public String isPhone(){
        return pref.getString(KEY_IS_IDPHONE, "");
    }

    public boolean isLoggedIn(){
        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }

}
