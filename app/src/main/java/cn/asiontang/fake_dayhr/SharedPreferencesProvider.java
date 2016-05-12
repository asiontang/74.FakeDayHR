package cn.asiontang.fake_dayhr;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

import java.util.Map;

public class SharedPreferencesProvider extends ContentProvider
{
    public static final String EXTRA_KEY_STR_KEYNAME = "需要保存到配置文件的Key";
    public static final String EXTRA_KEY_STR_VALUE = "需要保存到配置文件的Value";
    public static final String SHARED_PREFERENCES_FILE_NAME_MAIN = "main";

    @Override
    @Deprecated
    public int delete(final Uri uri, final String selection, final String[] selectionArgs)
    {
        return 0;
    }

    @Override
    @Deprecated
    public String getType(final Uri uri)
    {
        return null;
    }

    @Override
    public Uri insert(final Uri uri, final ContentValues values)
    {
        final String fileName = uri.getLastPathSegment();
        final String key = values.getAsString(EXTRA_KEY_STR_KEYNAME);

        //noinspection ConstantConditions
        final SharedPreferences sharedPreferences = this.getContext().getSharedPreferences(fileName, Context.MODE_PRIVATE);
        final boolean result = sharedPreferences.edit().putString(key, values.getAsString(EXTRA_KEY_STR_VALUE)).commit();

        return result ? uri : null;
    }

    @Override
    public boolean onCreate()
    {
        return true;
    }

    @Override
    public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder)
    {
        final String fileName = uri.getLastPathSegment();

        //noinspection ConstantConditions
        final SharedPreferences sharedPreferences = this.getContext().getSharedPreferences(fileName, Context.MODE_PRIVATE);

        final Map<String, ?> all = sharedPreferences.getAll();
        if (all.size() == 0)
            return null;

        final MatrixCursor cursor = new MatrixCursor(all.keySet().toArray(new String[all.size()]), 1);
        cursor.addRow(all.values());
        return cursor;
    }

    @Override
    @Deprecated
    public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs)
    {
        return 0;
    }
}
