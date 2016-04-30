package com.mrjerometw.ota;



import com.mrjerometw.ota.R;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class MainActivity extends Activity
{

	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		try
		{
			Log.i(Constants.mTag, "MainActivity OnCreate");
			super.onCreate(savedInstanceState);
			setContentView(R.layout.main_activity);
		}
		catch(Exception ex)
		{
			Log.i(Constants.mTag, ex.toString());
		}
	}
	private int mTriggerConnectionStatusActivity = 0;
	
	@Override 
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{
		if (keyCode != KeyEvent.KEYCODE_MEDIA_PLAY)
			return super.onKeyDown(keyCode, event);
		
		if (mTriggerConnectionStatusActivity < 5)
			mTriggerConnectionStatusActivity++;
		else
		{
			Intent myIntent = new Intent();
			myIntent.setComponent(new ComponentName("com.inspur.ota", "com.inspur.ota.OTAConnectionStatusActivity"));
			startActivityForResult(myIntent, 0);
		}
		return super.onKeyDown(keyCode, event);
		
	}
	@Override
	protected void onResume()
	{
		super.onResume();
		Log.i(Constants.mTag, "MainActivity onResume");
		ListView listView = (ListView)findViewById(R.id.listview_downLoad);
		ConfigClassAdapter configClassAdapter = new ConfigClassAdapter(this, ConfigClass.getInstance(this));
        listView.setAdapter(configClassAdapter);
	}
	private class ConfigClassAdapter extends BaseAdapter 
	{
		private LayoutInflater mInflater;
		private ConfigClass mConfigClass = null;
		private Context mContext = null;
		public ConfigClassAdapter(Context context, ConfigClass configClass) 
		{
			mContext = context;
			mInflater = LayoutInflater.from(context);
			mConfigClass = configClass;
		}
		
		@Override
		public int getCount() 
		{
			return mConfigClass.getFileNameSize();
		}

		@Override
		public Object getItem(int position) 
		{
			return mConfigClass.getFileName(position);
		}
		
		@Override
		public long getItemId(int position) 
		{
			return position;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) 
		{
			ViewHolder holder;

			if (convertView == null) 
			{
				convertView = mInflater.inflate(R.layout.main_activity_listview_item, parent, false);
				holder = new ViewHolder();
				holder.fileNameTextView = (TextView) convertView.findViewById(R.id.textviewFileName);
				holder.conditionTextView = (TextView) convertView.findViewById(R.id.textViewCondition);
				convertView.setTag(holder);
			} 
			else 
			{
				holder = (ViewHolder) convertView.getTag();
			}
			holder.fileNameTextView.setText(mConfigClass.getFileName(position));
			holder.conditionTextView.setText("("+ mConfigClass.getVersion(position) +")");
			return convertView;
		}
		
		class ViewHolder 
		{
			TextView fileNameTextView;
			TextView conditionTextView;
		}

	}
}
