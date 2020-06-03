package multi.process.prefs;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MultiProvider extends ContentProvider {
    private static final String PROVIDER_NAME = BuildConfig.LIBRARY_PACKAGE_NAME + ".multiprocessprefs";

    /**
     * Define all Content Urls for each type, String, int, long & boolean
     */
    private static final String URL_STRING = "content://" + PROVIDER_NAME + "/string/";
    private static final String URL_INT = "content://" + PROVIDER_NAME + "/integer/";
    private static final String URL_LONG = "content://" + PROVIDER_NAME + "/long/";
    private static final String URL_BOOLEAN = "content://" + PROVIDER_NAME + "/boolean/";
    private static final String URL_ALL = "content://" + PROVIDER_NAME + "/all/";
    private static final String URL_FLOAT = "content://" + PROVIDER_NAME + "/float/";
    private static final String URL_HAS_KEY = "content://" + PROVIDER_NAME + "/haskey/";

    // Special URL just for clearing preferences
    private static final String URL_PREFERENCES = "content://" + PROVIDER_NAME + "/prefs/";
    private static final String URL_DELETE_KEY = "content://" + PROVIDER_NAME + "/remove/";

    static final int CODE_STRING = 1;
    static final int CODE_INTEGER = 2;
    static final int CODE_LONG = 3;
    static final int CODE_BOOLEAN = 4;
    static final int CODE_PREFS = 5;
    static final int CODE_REMOVE_KEY = 6;
    static final int CODE_ALL = 7;
    static final int CODE_FLOAT = 8;
    static final int CODE_HAS_KEY = 9;

    static final String KEY = "key";
    static final String VALUE = "value";

    /**
     * Create UriMatcher to match all requests
     */
    private static final UriMatcher uriMatchers;

    static {
        uriMatchers = new UriMatcher(UriMatcher.NO_MATCH);
        // */* = wildcard  (name or file name / key)
        uriMatchers.addURI(PROVIDER_NAME, "string/*/*", CODE_STRING);
        uriMatchers.addURI(PROVIDER_NAME, "integer/*/*", CODE_INTEGER);
        uriMatchers.addURI(PROVIDER_NAME, "long/*/*", CODE_LONG);
        uriMatchers.addURI(PROVIDER_NAME, "boolean/*/*", CODE_BOOLEAN);
        uriMatchers.addURI(PROVIDER_NAME, "prefs/*/", CODE_PREFS);
        uriMatchers.addURI(PROVIDER_NAME, "all/*/*", CODE_ALL);
        uriMatchers.addURI(PROVIDER_NAME, "float/*/*", CODE_FLOAT);
        uriMatchers.addURI(PROVIDER_NAME, "haskey/*/*", CODE_HAS_KEY);
        uriMatchers.addURI(PROVIDER_NAME, "remove/*/*", CODE_REMOVE_KEY);
    }

    // Use a concurrentHashMap here to make sure it's safe to read & write from multiple threads
    // and not throw an Exception
    private Map<String, PreferenceInteractor> prefsMap = new ConcurrentHashMap<>();

    @Override
    public boolean onCreate() {
        return true;
    }

    /**
     * Get a new Preference Interactor, or return a previously used Interactor
     *
     * @param preferenceName the name of the preference file
     * @return a new interactor, or current one in the map
     */
    PreferenceInteractor getPreferenceInteractor(String preferenceName) {
        PreferenceInteractor pref = prefsMap.get(preferenceName);

        if (pref != null) {
            return pref;
        }

        pref = new PreferenceInteractor(getContext(), preferenceName);
        prefsMap.put(preferenceName, pref);
        return pref;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final PreferenceInteractor interactor = getPreferenceInteractor(uri.getPathSegments().get(1));

        switch (uriMatchers.match(uri)) {
            case CODE_STRING:
                final String s = uri.getPathSegments().get(2);
                return interactor.hasKey(s) ? preferenceToCursor(interactor.getString(s)) : null;
            case CODE_INTEGER:
                final String i = uri.getPathSegments().get(2);
                return interactor.hasKey(i) ? preferenceToCursor(interactor.getInt(i)) : null;
            case CODE_LONG:
                final String l = uri.getPathSegments().get(2);
                return interactor.hasKey(l) ? preferenceToCursor(interactor.getLong(l)) : null;
            case CODE_BOOLEAN:
                final String b = uri.getPathSegments().get(2);
                return interactor.hasKey(b) ? preferenceToCursor(interactor.getBoolean(b) ? 1 : 0) : null;
            case CODE_FLOAT:
                final String f = uri.getPathSegments().get(2);
                return interactor.hasKey(f) ? preferenceToCursor(interactor.getFloat(f)) : null;
            case CODE_ALL:
                return preferenceToCursor(interactor.getAll());
            case CODE_HAS_KEY:
                final String h = uri.getPathSegments().get(2);
                return preferenceToCursor(interactor.hasKey(h) ? 1 : 0);
        }

        return null;
    }


    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (values != null) {
            final PreferenceInteractor pref = getPreferenceInteractor(uri.getPathSegments().get(1));
            final String key = values.getAsString(KEY);

            switch (uriMatchers.match(uri)) {
                case CODE_STRING:
                    final String s = values.getAsString(VALUE);
                    pref.setString(key, s);
                    break;
                case CODE_INTEGER:
                    final int i = values.getAsInteger(VALUE);
                    pref.setInt(key, i);
                    break;
                case CODE_LONG:
                    final long l = values.getAsLong(VALUE);
                    pref.setLong(key, l);
                    break;
                case CODE_BOOLEAN:
                    final boolean b = values.getAsBoolean(VALUE);
                    pref.setBoolean(key, b);
                    break;
                case CODE_FLOAT:
                    final float f = values.getAsFloat(VALUE);
                    pref.setFloat(key, f);
            }

            getContext().getContentResolver().notifyChange(uri, null);
        }

        return 0;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        final PreferenceInteractor interactor = getPreferenceInteractor(uri.getPathSegments().get(1));

        switch (uriMatchers.match(uri)) {
            case CODE_REMOVE_KEY:
                interactor.removePref(uri.getPathSegments().get(2));
                break;
            case CODE_PREFS:
                interactor.clearPreference();
                break;
        }
        return 0;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("not supported");
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("not supported");
    }

    static String extractStringFromCursor(Cursor c, String defaultVal) {
        try (Cursor cursor = c) {
            if (cursor == null) {
                return defaultVal;
            }

            if (cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(MultiProvider.VALUE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return defaultVal;
    }

    static int extractIntFromCursor(Cursor c, int defaultVal) {
        try (Cursor cursor = c) {
            if (cursor == null) {
                return defaultVal;
            }

            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex(MultiProvider.VALUE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return defaultVal;
    }

    static float extractFloatFromCursor(Cursor c, float defaultVal) {
        try (Cursor cursor = c) {
            if (cursor == null) {
                return defaultVal;
            }

            if (cursor.moveToFirst()) {
                return cursor.getFloat(cursor.getColumnIndex(MultiProvider.VALUE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return defaultVal;
    }

    static long extractLongFromCursor(Cursor c, long defaultVal) {
        try (Cursor cursor = c) {
            if (cursor == null) {
                return defaultVal;
            }

            if (cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndex(MultiProvider.VALUE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return defaultVal;
    }

    static boolean extractBooleanFromCursor(Cursor c, boolean defaultVal) {
        try (Cursor cursor = c) {
            if (cursor == null) {
                return defaultVal;
            }

            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndex(MultiProvider.VALUE)) == 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return defaultVal;
    }

    static Map<String, ?> extractMapFromCursor(Cursor c) {
        ArrayMap<String, Object> entries = new ArrayMap<>();

        try (Cursor cursor = c) {
            if (cursor == null) {
                return entries;
            }

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                String key = cursor.getString(cursor.getColumnIndex(MultiProvider.KEY));
                int type = cursor.getType(cursor.getColumnIndex(MultiProvider.VALUE));
                Object value = null;

                if (type == Cursor.FIELD_TYPE_INTEGER) {
                    value = cursor.getInt(cursor.getColumnIndex(MultiProvider.VALUE));
                } else if (type == Cursor.FIELD_TYPE_FLOAT) {
                    value = cursor.getFloat(cursor.getColumnIndex(MultiProvider.VALUE));
                } else if (type == Cursor.FIELD_TYPE_STRING) {
                    String v = cursor.getString(cursor.getColumnIndex(MultiProvider.VALUE));
                    if (v.equals("true") || v.equals("false")) {
                        value = Boolean.parseBoolean(v);
                    } else {
                        value = v;
                    }
                }

                entries.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return entries;
    }

    static Uri createQueryUri(String prefFileName, String key, int prefType) {
        switch (prefType) {
            case CODE_STRING:
                return Uri.parse(URL_STRING + prefFileName + "/" + key);
            case CODE_INTEGER:
                return Uri.parse(URL_INT + prefFileName + "/" + key);
            case CODE_LONG:
                return Uri.parse(URL_LONG + prefFileName + "/" + key);
            case CODE_BOOLEAN:
                return Uri.parse(URL_BOOLEAN + prefFileName + "/" + key);
            case CODE_PREFS:
                return Uri.parse(URL_PREFERENCES + prefFileName + "/" + key);
            case CODE_ALL:
                return Uri.parse(URL_ALL + prefFileName + "/" + key);
            case CODE_FLOAT:
                return Uri.parse(URL_FLOAT + prefFileName + "/" + key);
            case CODE_HAS_KEY:
                return Uri.parse(URL_HAS_KEY + prefFileName + "/" + key);
            case CODE_REMOVE_KEY:
                return Uri.parse(URL_DELETE_KEY + prefFileName + "/" + key);
            default:
                throw new IllegalArgumentException("Not Supported Type : " + prefType);
        }
    }

    static <T> ContentValues createContentValues(String key, T value) {
        final ContentValues contentValues = new ContentValues();
        contentValues.put(MultiProvider.KEY, key);

        if (value instanceof String) {
            contentValues.put(MultiProvider.VALUE, (String) value);
        } else if (value instanceof Integer) {
            contentValues.put(MultiProvider.VALUE, (Integer) value);
        } else if (value instanceof Long) {
            contentValues.put(MultiProvider.VALUE, (Long) value);
        } else if (value instanceof Boolean) {
            contentValues.put(MultiProvider.VALUE, (Boolean) value);
        } else if (value instanceof Float) {
            contentValues.put(MultiProvider.VALUE, (Float) value);
        }

        return contentValues;
    }

    @Nullable
    static Cursor performQuery(Uri uri, ContentResolver resolver) {
        return resolver.query(uri, null, null, null, null, null);
    }

    /**
     * Convert a value into a cursor object using a Matrix Cursor
     *
     * @param value the value to be converetd
     * @param <T>   generic object type
     * @return a Cursor object
     */
    private <T> MatrixCursor preferenceToCursor(T value) {
        final MatrixCursor matrixCursor;

        if (value instanceof Map) {
            matrixCursor = new MatrixCursor(new String[]{MultiProvider.KEY, MultiProvider.VALUE}, ((Map) value).size());
            Map<String, ?> map = (Map) value;

            for (Map.Entry<String, ?> entry : map.entrySet()) {
                matrixCursor.addRow(new Object[]{entry.getKey(), entry.getValue()});
            }
        } else {
            matrixCursor = new MatrixCursor(new String[]{MultiProvider.VALUE}, 1);
            matrixCursor.addRow(new Object[]{value});
        }

        return matrixCursor;
    }
}