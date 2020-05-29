package multi.process.prefs;

import android.content.ContentResolver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

public class MultiPreferences {
    private ContentResolver resolver;
    private String name;

    public MultiPreferences(String prefFileName, ContentResolver resolver) {
        this.name = prefFileName;
        this.resolver = resolver;
    }

    public boolean hasKey(String key) {
        return MultiProvider.extractBooleanFromCursor(MultiProvider.performQuery(
                MultiProvider.createQueryUri(name, key, MultiProvider.CODE_HAS_KEY), resolver), false);
    }

    public Map<String, ?> getAll() {
        return MultiProvider.extractMapFromCursor(MultiProvider.performQuery(
                MultiProvider.createQueryUri(name, "*", MultiProvider.CODE_ALL), resolver));
    }

    @Nullable
    public String getString(final String key, final String defaultValue) {
        return MultiProvider.extractStringFromCursor(MultiProvider.performQuery(
                MultiProvider.createQueryUri(name, key, MultiProvider.CODE_STRING), resolver), defaultValue);
    }

    public int getInt(final String key, final int defaultValue) {
        return MultiProvider.extractIntFromCursor(MultiProvider.performQuery(
                MultiProvider.createQueryUri(name, key, MultiProvider.CODE_INTEGER), resolver), defaultValue);
    }

    public float getFloat(final String key, final float defaultValue) {
        return MultiProvider.extractFloatFromCursor(MultiProvider.performQuery(
                MultiProvider.createQueryUri(name, key, MultiProvider.CODE_INTEGER), resolver), defaultValue);
    }

    public long getLong(final String key, final long defaultValue) {
        return MultiProvider.extractLongFromCursor(MultiProvider.performQuery(
                MultiProvider.createQueryUri(name, key, MultiProvider.CODE_LONG), resolver), defaultValue);
    }

    public boolean getBoolean(final String key, final boolean defaultValue) {
        return MultiProvider.extractBooleanFromCursor(MultiProvider.performQuery(
                MultiProvider.createQueryUri(name, key, MultiProvider.CODE_BOOLEAN), resolver), defaultValue);
    }

    /**
     *
     */

    public void setString(final String key, @NonNull final String value) {
        resolver.update(MultiProvider.createQueryUri(name, key, MultiProvider.CODE_STRING),
                MultiProvider.createContentValues(key, value), null, null);
    }

    public void setInt(final String key, final int value) {
        resolver.update(MultiProvider.createQueryUri(name, key, MultiProvider.CODE_INTEGER),
                MultiProvider.createContentValues(key, value), null, null);
    }

    public void setFloat(final String key, final float value) {
        resolver.update(MultiProvider.createQueryUri(name, key, MultiProvider.CODE_FLOAT),
                MultiProvider.createContentValues(key, value), null, null);
    }

    public void setLong(final String key, final long value) {
        resolver.update(MultiProvider.createQueryUri(name, key, MultiProvider.CODE_LONG),
                MultiProvider.createContentValues(key, value), null, null);
    }

    public void setBoolean(final String key, final boolean value) {
        resolver.update(MultiProvider.createQueryUri(name, key, MultiProvider.CODE_BOOLEAN),
                MultiProvider.createContentValues(key, value), null, null);
    }

    /**
     *
     */

    public void removePreference(final String key) {
        resolver.delete(MultiProvider.createQueryUri(name, key, MultiProvider.CODE_REMOVE_KEY),
                null, null);
    }

    public void clearPreferences() {
        resolver.delete(MultiProvider.createQueryUri(name, "", MultiProvider.CODE_PREFS),
                null, null);
    }
}