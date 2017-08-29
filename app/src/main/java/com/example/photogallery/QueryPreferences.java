package com.example.photogallery;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Виктор on 10.01.2017.
 */

public class QueryPreferences {
    private static final String PREF_SEARCH_QUERY = "searchQuery";
    private static final String PREF_LAST_RESULT_ID = "lastResultId";
    private static final String PREF_IS_ALARM_ON = "isAlarmOn";

    private Context mContext;

    public QueryPreferences(Context context){
        mContext = context;
    }

    public String getStoredQuery() {
        return PreferenceManager
                .getDefaultSharedPreferences(mContext)
                .getString(PREF_SEARCH_QUERY, null);
    }

    public void setStoredQuery(String query) {
        PreferenceManager
                .getDefaultSharedPreferences(mContext)
                .edit()
                .putString(PREF_SEARCH_QUERY, query)
                .apply();
    }

    public String getLastResultId(){
        return PreferenceManager
                .getDefaultSharedPreferences(mContext)
                .getString(PREF_LAST_RESULT_ID, null);
    }

    public void setLastResultId(String lastId){
        PreferenceManager
                .getDefaultSharedPreferences(mContext)
                .edit()
                .putString(PREF_LAST_RESULT_ID, lastId)
                .apply();
    }

    public boolean isAlarmOn(){
        return PreferenceManager
                .getDefaultSharedPreferences(mContext)
                .getBoolean(PREF_IS_ALARM_ON, false);
    }

    public void setAlarmOn(Boolean isOn){
        PreferenceManager
                .getDefaultSharedPreferences(mContext)
                .edit()
                .putBoolean(PREF_IS_ALARM_ON, isOn)
                .apply();
    }
}
