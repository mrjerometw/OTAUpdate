package com.mrjerometw.ota;


import java.util.ArrayList;
import java.util.List;

import com.mrjerometw.ota.R;
import com.mrjerometw.ota.ConfigClass.DownloadStatus;
import com.mrjerometw.ota.ConfigClass.FileDownloadStatus;
import com.mrjerometw.ota.OTABroadcastReceiver.IOTAListenerEvent;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;


public class FileDownloadProgressActivity extends Activity implements IOTAListenerEvent
{
	private Context mContext = null;
	private ProgressDialog mProgressDialog;
	private int mProgressValue = 0;
    private Handler mProgressHandler; 
    private static final int PROGRESS_DIALOG = 1;
    private int MESSAGE_WHAT_UPDATE_PROGRESS = 1;
    private ConfigClass mConfigClass = null;
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);
        mContext = this;
        OTABroadcastReceiver.addListener(this);
        setContentView(R.layout.boot_up);
        Intent intent = new Intent();
        intent.setAction(Constants.mServiceAction);
        intent.putExtra(Constants.mStringExtraNameDownloadServerVersion, 0);
        intent.putExtra(Constants.mStringExtraNameNotifyWakeUpDownloadActivity, false);
        intent.putExtra(Constants.mStringExtraNameNotifyDownloadOnlyCheckVersion, false);
        sendBroadcast(intent);
        mProgressHandler = new Handler(){  
        	   
            public void handleMessage(android.os.Message msg) 
            {
            	if (msg.what == MESSAGE_WHAT_UPDATE_PROGRESS)
            	{
            		if (mDownloadStatus == null)
            			return;
            		int fileSize = mDownloadStatus.getFileDownloadStatusSize();
            		
        			if (mConfigClass.checkAllFileAreNewest() || !mDownloadStatus.getNetworkConnectionSuccesful())
        			{
        				boolean updateStatus = mConfigClass.checkAllFileAreNewest();
        				mProgressDialog.dismiss();  
        				String message = "";
        				if (updateStatus)
        					message = String.valueOf(mContext.getResources().getText(R.string.update_finish));
        				else
        					message = String.valueOf(mContext.getResources().getText(R.string.upgrade_fails));
        				
        				Log.i(Constants.mTag, message);
        				Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
        				((Activity)mContext).finish();		
        			}
        			double percent = 0;
            		for (int index = 0 ; index < fileSize ; index ++ )
            		{
            			
            			FileDownloadStatus status = mDownloadStatus.getFileDownloadStatus(index);
            			if (mConfigClass.getVersion(index) >= mConfigClass.getServerVersion(index))
            				percent = percent + 25;
            			else
            				percent = percent + (status.getOffset()/status.getLength())*25;
            		}    
            		Log.i(Constants.mTag, "Downlad Percent:"+percent);
            		mProgressDialog.setProgress((int)percent);
            	}  	
            };  
        };  
        showDialog(PROGRESS_DIALOG);  
        mProgressValue = 0;  
        mProgressDialog.setProgress(0);  
       // mProgressHandler.sendEmptyMessage(0);
    }
   

	private DownloadStatus mDownloadStatus = null;
	@Override
	public void DownloadProgress(ConfigClass configClass) 
	{
		mConfigClass = configClass;
		mDownloadStatus = configClass.getDownloadStatus();
        mProgressDialog.setMax(100); 
		mProgressHandler.sendEmptyMessage(MESSAGE_WHAT_UPDATE_PROGRESS);  
//		this.runOnUiThread((new Runnable(){
//            @Override
//            public void run() 
//            {
//            	mProgressBar.setMax(max);
//            	mProgressBar.setProgress(current);
//    			if (current == max )
//    			{
//    				Log.i(Constants.mTag, String.valueOf(mContext.getResources().getText(R.string.update_finish)));
//    				Toast.makeText(mContext, mContext.getResources().getText(R.string.update_finish), Toast.LENGTH_LONG).show();
//    				((Activity)mContext).finish();
//    			}
//
//            }
//        }));
	}
	@Override
	protected void onDestroy()
	{
		OTABroadcastReceiver.removeListener(this);
		super.onDestroy();
	}
	
    @Override  
    protected Dialog onCreateDialog(int id) {  
   
        switch(id){  
   
        case PROGRESS_DIALOG :  
            mProgressDialog = new ProgressDialog(this);  
            mProgressDialog.setTitle("OTA");   
            mProgressDialog.setMessage("Downloading..."); 
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL); 
   
            return mProgressDialog;  
   
        }  
   
        return super.onCreateDialog(id);  
    }
	@Override
	public void ErrorReport(int errorCode, String message) {
		// TODO Auto-generated method stub
		
	}  
	   
}
