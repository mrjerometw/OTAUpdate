package com.mrjerometw.ota;


import java.util.ArrayList;
import java.util.List;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;


public class LauncherActivity extends Activity 
{
//	private Activity mActivity = null;
//	private ProgressBar mProgressBar;
	private int mPrgressIndex =  0;
	
	private static boolean bOnlyOne = true;
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
//        Intent intent = new Intent();
//        intent.setClass(LauncherActivity.this, FileDownloadProgressActivity.class);
//        startActivity(intent);
        if (bOnlyOne)
        {
        	bOnlyOne = false;
	        Thread thread = new Thread(new Runnable()
	        {
	            @Override
	            public void run() 
	            {
	                while(mPrgressIndex<10)
	                {
	                    try
	                    {
	                    	if (enableEthernet())
	                    	{
	                	        Intent intent = new Intent();
	                	        intent.setAction(Constants.mServiceAction);
	                	        intent.putExtra(Constants.mStringExtraNameDownloadServerVersion, 0);
	                	        intent.putExtra(Constants.mStringExtraNameNotifyDownloadOnlyCheckVersion, true);
	                	        intent.putExtra(Constants.mStringExtraNameNotifyWakeUpDownloadActivity, true);
	                	        sendBroadcast(intent);
	                	        break;
	                    	}
	                    	else
	                    	{
	        					Log.i(Constants.mTag, "Cannot connect Internet !");
		                    	mHandle.sendMessage(mHandle.obtainMessage());
		                        Thread.sleep(5000);
	                    	}
	                    }
	                    catch(Throwable t)
	                    {
	                    	Log.i(Constants.mTag, "Exception:"+t.toString());
	                    }
	                }
	            }
	        });
	        thread.setPriority(Thread.MIN_PRIORITY);
	        thread.start();
        }
        Log.i(Constants.mTag, "LauncherActivity OnCreate");
		try
		{
			Intent myIntent = new Intent();
			myIntent.setComponent(new ComponentName("com.amlogic.mediaboxlauncher", "com.amlogic.mediaboxlauncher.Launcher"));
//			myIntent.setComponent(new ComponentName("com.android.launcher", "com.android.launcher2.Launcher"));
//			myIntent.setComponent(new ComponentName("com.htc.launcher", "com.htc.launcher.Launcher"));
			
			startActivityForResult(myIntent, 0);
//			myIntent.setComponent(new ComponentName("com.android.launcher", "com.android.launcher2.Launcher"));
//			startActivity(myIntent);
		}
		catch(Exception ex)
		{
			Log.i(Constants.mTag, ex.toString());
		}
		finish();
        //finish();
//        mActivity = this;
//        setContentView(R.layout.boot_up);
//        mProgressBar = (ProgressBar)findViewById(R.id.progressBar);
//        mProgressBar.setMax(100);
//       

    }
   
    Handler mHandle = new Handler()
    {
        @Override
        public void handleMessage(Message msg) 
        {
        	mPrgressIndex++;
//        	mProgressBar.setProgress(10 * mPrgressIndex);
        }
    };
    
    private boolean enableEthernet()
    {
    	boolean result = false;
    	ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE); 
    	NetworkInfo info = connManager.getActiveNetworkInfo();
    	if (info == null || !info.isConnected() || !info.isAvailable() || (info.getType()!=ConnectivityManager.TYPE_ETHERNET))
	    	return false;
    	else 
	    	return true;
    	
    }
}
