package multi.process.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.WorkerThread;

import java.util.Map;

final class PreferenceInteractor {
    private static final String DEFAULT_STRING = "";
    private static final float DEFAULT_FLOAT = -1F;
    private static final int DEFAULT_INT = -1;
    private static final long DEFAULT_LONG = -1L;
    private static final boolean DEFAULT_BOOLEAN = false;

    private SharedPreferences sharedPreferences;

    PreferenceInteractor(Context context, String preferenceName) {
        this.sharedPreferences = context.getSharedPreferences(preferenceName, Context.MODE_PRIVATE);
    }

    boolean hasKey(String key) {
        return sharedPreferences.contains(key);
    }

    String getString(String key) {
        return sharedPreferences.getString(key, DEFAULT_STRING);
    }

    @WorkerThread
    void setString(String key, String value) {
        sharedPreferences.edit().putString(key, value).commit();
    }

    int getInt(String key) {
        return sharedPreferences.getInt(key, DEFAULT_INT);
    }

    @WorkerThread
    void setInt(String key, int value) {
        sharedPreferences.edit().putInt(key, value).commit();
    }

    float getFloat(String key) {
        return sharedPreferences.getFloat(key, DEFAULT_FLOAT);
    }

    @WorkerThread
    void setFloat(String key, float value) {
        sharedPreferences.edit().putFloat(key, value).commit();
    }

    long getLong(String key) {
        return sharedPreferences.getLong(key, DEFAULT_LONG);
    }

    @WorkerThread
    void setLong(String key, long value) {
        sharedPreferences.edit().putLong(key, value).commit();
    }

    boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, DEFAULT_BOOLEAN);
    }

    @WorkerThread
    void setBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).commit();
    }

    Map<String, ?> getAll() {
        return sharedPreferences.getAll();
    }

    @WorkerThread
    void removePref(String key) {
        sharedPreferences.edit().remove(key).commit();
    }

    @WorkerThread
    void clearPreference() {
        sharedPreferences.edit().clear().commit();
    }
}