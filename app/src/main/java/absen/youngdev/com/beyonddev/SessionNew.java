package absen.youngdev.com.beyonddev;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by Denny on 01/04/2018.
 */

public class SessionNew {
    private static String TAG = SessionNew.class.getSimpleName();

    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;

    private static final String PREF_NAME = "IsCheck";
    private static final String KEY_IS_CHECK = "isCheckIn";
    private static final String KEY_IS_OUT = "isCheckOut";
    private static final String KEY_IS_DATE = "isDate";
    private static final String KEY_IS_TIMEIN = "isTimeIn";
    private static final String KEY_IS_TIMEOUT = "isTimeOut";
    private static final String KEY_IS_ATIN = "isAtIn";
    private static final String KEY_IS_ATOUT = "isAtOut";
    private static final String KEY_IS_TGL = "isTgl";


    public SessionNew(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setDate(String isDate){
        editor.putString(KEY_IS_DATE, isDate);
        editor.commit();
        Log.d(TAG, "Date is insert "+isDate);
    }

    public void setTimeIn(String isTimeIn){
        editor.putString(KEY_IS_TIMEIN, isTimeIn);
        editor.commit();
        Log.d(TAG, "Time in is insert "+isTimeIn);
    }

    public void setTimeOut(String isTimeOut){
        editor.putString(KEY_IS_TIMEOUT, isTimeOut);
        editor.commit();
        Log.d(TAG, "Time out is insert "+isTimeOut);
    }

    public void setAtIn(String isAtIn){
        editor.putString(KEY_IS_ATIN, isAtIn);
        editor.commit();
        Log.d(TAG, "At in is insert "+isAtIn);
    }

    public void setAtOut(String isAtOut){
        editor.putString(KEY_IS_ATOUT, isAtOut);
        editor.commit();
        Log.d(TAG, "At out is insert "+isAtOut);
    }

    public void setTgl(String isTgl){
        editor.putString(KEY_IS_TGL, isTgl);
        editor.commit();
        Log.d(TAG, "Tgl in is insert "+isTgl);
    }

    public void setCheck(boolean isCheckIn){
        editor.putBoolean(KEY_IS_CHECK, isCheckIn);
        editor.commit();
        Log.d(TAG, "Check In session modified");
    }

    public void setOut(boolean isCheckOut){
        editor.putBoolean(KEY_IS_OUT, isCheckOut);
        editor.commit();
        Log.d(TAG, "Check Out session modified");
    }


    public boolean isCheck(){
        return pref.getBoolean(KEY_IS_CHECK, false);
    }

    public String isTimeIn(){
        return pref.getString(KEY_IS_TIMEIN, "none");
    }

    public String isTimeOut(){
        return pref.getString(KEY_IS_TIMEOUT, "none");
    }

    public String isAtIn(){
        return pref.getString(KEY_IS_ATIN, "none");
    }

    public String isAtOut(){
        return pref.getString(KEY_IS_ATOUT, "none");
    }

    public String isTgl(){
        return pref.getString(KEY_IS_TGL, "none");
    }

    public boolean isOut(){
        return pref.getBoolean(KEY_IS_OUT, false);
    }

    public String isDate(){
        return pref.getString(KEY_IS_DATE, "10/01/1990");
    }
}
