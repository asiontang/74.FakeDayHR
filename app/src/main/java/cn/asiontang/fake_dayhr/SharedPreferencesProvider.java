package cn.asiontang.fake_dayhr;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

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

        Log.e("----insert fileName", fileName);
        Log.e("----insert key", key);
        Log.e("----insert value", values.getAsString(EXTRA_KEY_STR_VALUE));
        Log.e("----insert result", String.valueOf(result));
        return result ? uri : null;
    }

    @Override
    public boolean onCreate()
    {
        return true;
    }

    @Override
    @Deprecated
    public Cursor query(final Uri uri, final String[] projection, final String selection, final String[] selectionArgs, final String sortOrder)
    {
        final String fileName = uri.getLastPathSegment();

        //noinspection ConstantConditions
        final SharedPreferences sharedPreferences = this.getContext().getSharedPreferences(fileName, Context.MODE_PRIVATE);

        final Map<String, ?> all = sharedPreferences.getAll();
        if (all.size() == 0)
            return null;
        return new InnerCursor(all);
    }

    @Override
    @Deprecated
    public int update(final Uri uri, final ContentValues values, final String selection, final String[] selectionArgs)
    {
        return 0;
    }

    private class InnerCursor implements Cursor
    {
        private final Map<String, ?> mMap;
        private final String[] mKeys;
        private final Object[] mValues;

        public InnerCursor(final Map<String, ?> all)
        {
            this.mMap = all;
            this.mKeys = all.keySet().toArray(new String[all.size()]);
            this.mValues = all.values().toArray(new Object[all.size()]);
        }

        @Override
        public void close()
        {
        }

        @Override
        public void copyStringToBuffer(final int columnIndex, final CharArrayBuffer buffer)
        {

        }

        @Override
        public void deactivate()
        {

        }

        @Override
        public byte[] getBlob(final int columnIndex)
        {
            return new byte[0];
        }

        @Override
        public int getColumnCount()
        {
            return this.mKeys.length;
        }

        @Override
        public int getColumnIndex(final String columnName)
        {
            return 0;
        }

        @Override
        public int getColumnIndexOrThrow(final String columnName) throws IllegalArgumentException
        {
            return 0;
        }

        @Override
        public String getColumnName(final int columnIndex)
        {
            return this.mKeys[columnIndex];
        }

        @Override
        public String[] getColumnNames()
        {
            return this.mKeys;
        }

        @Override
        public int getCount()
        {
            return this.mMap == null ? 0 : this.mMap.size();
        }

        @Override
        public double getDouble(final int columnIndex)
        {
            return 0;
        }

        @Override
        public Bundle getExtras()
        {
            return null;
        }

        @Override
        public void setExtras(final Bundle extras)
        {

        }

        @Override
        public float getFloat(final int columnIndex)
        {
            return 0;
        }

        @Override
        public int getInt(final int columnIndex)
        {
            return 0;
        }

        @Override
        public long getLong(final int columnIndex)
        {
            return 0;
        }

        @Override
        public Uri getNotificationUri()
        {
            return null;
        }

        @Override
        public int getPosition()
        {
            return 0;
        }

        @Override
        public short getShort(final int columnIndex)
        {
            return 0;
        }

        @Override
        public String getString(final int columnIndex)
        {
            return String.valueOf(this.mValues[columnIndex]);
        }

        @Override
        public int getType(final int columnIndex)
        {
            return 0;
        }

        @Override
        public boolean getWantsAllOnMoveCalls()
        {
            return false;
        }

        @Override
        public boolean isAfterLast()
        {
            return false;
        }

        @Override
        public boolean isBeforeFirst()
        {
            return false;
        }

        @Override
        public boolean isClosed()
        {
            return false;
        }

        @Override
        public boolean isFirst()
        {
            return true;
        }

        @Override
        public boolean isLast()
        {
            return true;
        }

        @Override
        public boolean isNull(final int columnIndex)
        {
            return false;
        }

        @Override
        public boolean move(final int offset)
        {
            return true;
        }

        @Override
        public boolean moveToFirst()
        {
            return true;
        }

        @Override
        public boolean moveToLast()
        {
            return true;
        }

        @Override
        public boolean moveToNext()
        {
            return true;
        }

        @Override
        public boolean moveToPosition(final int position)
        {
            return true;
        }

        @Override
        public boolean moveToPrevious()
        {
            return true;
        }

        @Override
        public void registerContentObserver(final ContentObserver observer)
        {

        }

        @Override
        public void registerDataSetObserver(final DataSetObserver observer)
        {

        }

        @Override
        public boolean requery()
        {
            return false;
        }

        @Override
        public Bundle respond(final Bundle extras)
        {
            return null;
        }

        @Override
        public void setNotificationUri(final ContentResolver cr, final Uri uri)
        {

        }

        @Override
        public void unregisterContentObserver(final ContentObserver observer)
        {

        }

        @Override
        public void unregisterDataSetObserver(final DataSetObserver observer)
        {

        }
    }
}
