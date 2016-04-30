package com.mrjerometw.ota;


import java.util.ArrayList;
import java.util.List;

import com.mrjerometw.ota.R;
import com.mrjerometw.ota.OTABroadcastReceiver.IOTAListenerEvent;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class OTAConnectionStatusActivity extends Activity implements IOTAListenerEvent
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
	@Override
	protected void onResume()
	{
		super.onResume();
		Log.i(Constants.mTag, "MainActivity onResume");
		ListView listView = (ListView)findViewById(R.id.listview_downLoad);
		List<String> tasks = new ArrayList<String>();
		tasks.add("Checking machine network");
		tasks.add("Checking machine connects OTA Server");
		tasks.add("Checking machine storage exist");
		tasks.add("Checking machine storage can be read/write");
		new chckAsyncTask().execute(tasks); 
		ConfigClassAdapter configClassAdapter = new ConfigClassAdapter(this, ConfigClass.getInstance(this), tasks);
        listView.setAdapter(configClassAdapter);
	}
	private class ConfigClassAdapter extends BaseAdapter 
	{
		private LayoutInflater mInflater;
		private Context mContext = null;
		private List<String> mTasks = null;
		public ConfigClassAdapter(Context context, ConfigClass configClass,List<String> tasks) 
		{
			mContext = context;
			mInflater = LayoutInflater.from(context);
			mTasks = tasks;
		}
		
		@Override
		public int getCount() 
		{
			return mTasks.size();
		}

		@Override
		public Object getItem(int position) 
		{
			return mTasks.get(position);
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
			holder.fileNameTextView.setText(mTasks.get(position));
			holder.conditionTextView.setText("(Checking)");
			return convertView;
		}
		
		class ViewHolder 
		{
			TextView fileNameTextView;
			TextView conditionTextView;
		}
	}
	//public abstract class AsyncTask<Params, Progress, Result> {
	class chckAsyncTask extends AsyncTask<List<String>, Integer, Boolean>{

		// protected abstract Result doInBackground(Params... params);
		@Override
		protected Boolean doInBackground(List<String>... param) 
		{
			List<String> tasks = param[0];
			return null;
		}

		//protected void onPostExecute(Result result) 
		@Override
		protected void onPostExecute(Boolean result) 
		{
			super.onPostExecute(result);
		}

	}
	@Override
	public void DownloadProgress(ConfigClass configClass) 
	{
		
	}
	@Override
	public void ErrorReport(int errorCode, String message) {
		// TODO Auto-generated method stub
		
	}
}
