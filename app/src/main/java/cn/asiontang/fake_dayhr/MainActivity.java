package cn.asiontang.fake_dayhr;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.asiontang.BaseAdapterEx3;

public class MainActivity extends Activity implements AdapterView.OnItemClickListener
{
    private XSharedPreferencesEx mMainPreferences;
    private TextView mSelectedIndexTextView;
    private InnerAdapter mInnerAdapter;

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.mMainPreferences = new XSharedPreferencesEx();
        this.mMainPreferences.init(this, SharedPreferencesProvider.SHARED_PREFERENCES_FILE_NAME_MAIN);

        this.mSelectedIndexTextView = (TextView) findViewById(android.R.id.primary);
        this.mSelectedIndexTextView.setText("Selected Location Index:" + this.mMainPreferences.getInt("SelectedLocationIndex", -1));

        final ListView list = (ListView) findViewById(android.R.id.list);
        list.setAdapter(this.mInnerAdapter = new InnerAdapter(this, new ArrayList<KeyValueEntity>()));
        list.setEmptyView(findViewById(android.R.id.empty));
        list.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id)
    {
        final KeyValueEntity item = (KeyValueEntity) parent.getItemAtPosition(position);
        final Map<String, ?> all = item.Value.getAll();
        final StringBuilder stringBuilder = new StringBuilder();
        for (final Map.Entry<String, ?> entry : all.entrySet())
        {
            stringBuilder.append(entry.getKey());
            stringBuilder.append(": ");
            stringBuilder.append(entry.getValue());
            stringBuilder.append("\n");
        }
        new AlertDialog.Builder(this)//
                .setTitle("Location Detail")
                .setMessage(stringBuilder)//
                .setNegativeButton("取消", null)
                .setPositiveButton("设置为默认值", new DialogInterface.OnClickListener()
                {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onClick(final DialogInterface dialog, final int which)
                    {
                        MainActivity.this.mMainPreferences.edit().putInt("SelectedLocationIndex", item.Key).commit();
                        MainActivity.this.mSelectedIndexTextView.setText("Selected Location Index:" + MainActivity.this.mMainPreferences.getInt("SelectedLocationIndex", -1));
                    }
                })
                .show();
    }

    @Override
    protected void onResume()
    {
        final List<KeyValueEntity> items = this.mInnerAdapter.getOriginaItems();
        items.clear();
        final int count = this.mMainPreferences.getInt("count", -1);
        if (count != -1)
            for (int i = 1; i <= count; i++)
                items.add(new KeyValueEntity(this, i));
        this.mInnerAdapter.refresh();

        super.onResume();
    }

    class KeyValueEntity
    {
        int Key;
        SharedPreferences Value;

        public KeyValueEntity(final Context context, final int i)
        {
            this.Key = i;
            this.Value = context.getSharedPreferences("" + i, MODE_PRIVATE);
        }
    }

    class InnerAdapter extends BaseAdapterEx3<KeyValueEntity>
    {
        public InnerAdapter(final Context context, final List<KeyValueEntity> objects)
        {
            super(context, R.layout.activity_main_list_item, objects);
        }

        @Override
        public void convertView(final ViewHolder viewHolder, final KeyValueEntity item)
        {
            viewHolder.getTextView(android.R.id.text1).setText(String.format("%s - %s:%s\n%s", item.Key//
                    , item.Value.getString("getLatitude", "")//
                    , item.Value.getString("getLongitude", "")//
                    , item.Value.getString("getAddress", "")//
            ));
        }
    }
}
