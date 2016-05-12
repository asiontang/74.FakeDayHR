package cn.asiontang.fake_dayhr;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.Set;

import de.robv.android.xposed.XposedBridge;

@SuppressWarnings("unused")
public class XSharedPreferencesEx
{
    private String mFileName;
    private Context mContext;
    private InnerEditor mInnerEditor;
    private HashMap<String, String> mMap = null;

    public XSharedPreferencesEx()
    {
    }

    private static Uri getUri(final String fileName)
    {
        return Uri.parse("content://cn.asiontang.SharedPreferencesProvider//" + fileName);
    }

    /**
     * 听过发送Service请求来达到设置 SharedPreferences 的键值对的作用。
     *
     * @param fileName SharedPreferences 文件名
     * @param key      需要保存到 SharedPreferences 的 键
     * @param value    需要保存到 SharedPreferences 的 值（已经序列为String）
     */
    private static void setKeyValue(final Context context, final String fileName, final String key, final Object value, final Class type)
    {
        if (context == null)
        {
            XposedBridge.log("setKeyValue context == null:\n    " + key + "\n        " + value);
            return;
        }
        final ContentValues values = new ContentValues();
        values.put(SharedPreferencesProvider.EXTRA_KEY_STR_KEYNAME, key);
        values.put(SharedPreferencesProvider.EXTRA_KEY_STR_VALUE, String.valueOf(value));
        final ContentResolver contentResolver = context.getContentResolver();
        final Uri result = contentResolver.insert(getUri(fileName), values);
        if (result == null)
            XposedBridge.log("setKeyValue result == null:\n    " + key + "\n        " + value);
    }

    public InnerEditor edit()
    {
        if (this.mInnerEditor != null)
            return this.mInnerEditor;

        return this.mInnerEditor = new InnerEditor(this.mContext, this.mFileName);
    }

    public boolean getBoolean(final String key, final boolean defValue)
    {
        final String value = getString(key, null);
        if (value == null)
            return defValue;
        return Boolean.parseBoolean(value);
    }

    public double getDouble(final String key, final double defValue)
    {
        final String value = getString(key, null);
        if (value == null)
            return defValue;
        return Double.parseDouble(value);
    }

    public float getFloat(final String key, final float defValue)
    {
        final String value = getString(key, null);
        if (value == null)
            return defValue;
        return Float.parseFloat(value);
    }

    public int getInt(final String key, final int defValue)
    {
        final String value = getString(key, null);
        if (value == null)
            return defValue;
        return Integer.parseInt(value);
    }

    public String getString(final String key, final String defValue)
    {
        //调用时，耗时大约3秒左右，所有不能频繁调用 query
        if (this.mMap == null)
        {
            this.mMap = new HashMap<>();
            Cursor query = null;
            try
            {
                query = this.mContext.getContentResolver().query(getUri(this.mFileName), null, key, null, null);
                if (query == null)
                    return defValue;

                query = ((CursorWrapper) query).getWrappedCursor();

                if (!query.moveToNext())
                    return defValue;

                final int count = query.getColumnCount();
                if (count == 0)
                    return defValue;

                for (int i = 0; i < count; i++)
                    this.mMap.put(query.getColumnName(i), query.getString(i));
            }
            catch (final Exception e)
            {
                Log.e("-------", e.getMessage());
                e.printStackTrace();
                return defValue;
            }
            finally
            {
                if (query != null)
                    query.close();
            }
        }
        if (!this.mMap.containsKey(key))
            return defValue;
        return this.mMap.get(key);
    }

    public void init(final Context context, final String fileName)
    {
        this.mFileName = fileName;
        this.mContext = context;

        if (this.mInnerEditor != null)
            this.mInnerEditor.init(context, fileName);
    }

    public static class InnerEditor implements SharedPreferences.Editor
    {
        private Object mValue;
        private String mKey;
        private Class mType;
        private Context mContext;
        private String mFileName;

        public InnerEditor(final Context context, final String fileName)
        {
            this.mContext = context;
            this.mFileName = fileName;
        }

        @Override
        @Deprecated
        public void apply()
        {
            commit();
        }

        @Override
        @Deprecated
        public SharedPreferences.Editor clear()
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean commit()
        {
            setKeyValue(this.mContext, this.mFileName, this.mKey, this.mValue, this.mType);
            return true;
        }

        public void init(final Context context, final String fileName)
        {
            this.mContext = context;
            this.mFileName = fileName;
        }

        @Override
        public SharedPreferences.Editor putBoolean(final String key, final boolean value)
        {
            this.mKey = key;
            this.mValue = value;
            this.mType = Boolean.class;
            return this;
        }

        public SharedPreferences.Editor putDouble(final String key, final double value)
        {
            this.mKey = key;
            this.mValue = value;
            this.mType = Double.class;
            return this;
        }

        @Override
        public SharedPreferences.Editor putFloat(final String key, final float value)
        {
            this.mKey = key;
            this.mValue = value;
            this.mType = Float.class;
            return this;
        }

        @Override
        public SharedPreferences.Editor putInt(final String key, final int value)
        {
            this.mKey = key;
            this.mValue = value;
            this.mType = Integer.class;
            return this;
        }

        @Override
        @Deprecated
        public SharedPreferences.Editor putLong(final String key, final long value)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public SharedPreferences.Editor putString(final String key, final String value)
        {
            this.mKey = key;
            this.mValue = value;
            this.mType = String.class;
            return this;
        }

        @Override
        @Deprecated
        public SharedPreferences.Editor putStringSet(final String key, final Set<String> values)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        @Deprecated
        public SharedPreferences.Editor remove(final String key)
        {
            throw new UnsupportedOperationException();
        }
    }
}
