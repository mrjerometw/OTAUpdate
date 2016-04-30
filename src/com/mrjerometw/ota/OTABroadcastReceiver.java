package com.mrjerometw.ota;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.mrjerometw.ota.R;
import com.mrjerome.ota.entities.FileInfo;
import com.mrjerometw.ota.ConfigClass.DownloadStatus;
import com.mrjerometw.ota.services.DownloadService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class OTABroadcastReceiver extends BroadcastReceiver 
{ 
	private static List<IOTAListenerEvent> mOTAListenerEvent  = new ArrayList<IOTAListenerEvent>();
	public interface IOTAListenerEvent
	{
		//public void Update(ConfigClass configClass);
		public void DownloadProgress(ConfigClass configClass);
		public void ErrorReport(int errorCode, String message);
	}
	public static void addListener(IOTAListenerEvent listener)
	{
		mOTAListenerEvent.add(listener);
	}
	public static void removeListener(IOTAListenerEvent listener)
	{
		mOTAListenerEvent.remove(listener);
	}
	private static Context mContext = null;
	private ConfigClass mConfigClass = null;
	//private static int mSequence = 0 ;
	private static boolean mFlagOnlyCheckVersion = false;
	private static boolean mFlagWakeUpDownloadActivity = false;
	@Override
    public void onReceive(Context context, Intent intent) 
    { 
		try
		{
			if (mConfigClass == null)
			{
				mConfigClass = ConfigClass.getInstance(context);
			}
			
			if (Constants.mServiceAction.equals(intent.getAction()))
			{
				if (intent.hasExtra(Constants.mStringExtraNameDownloadServerVersion))
				{
					if (mContext == null && context != null)
						mContext = context;
					if (intent.hasExtra(Constants.mStringExtraNameNotifyDownloadOnlyCheckVersion))
						mFlagOnlyCheckVersion = intent.getBooleanExtra(Constants.mStringExtraNameNotifyDownloadOnlyCheckVersion, false);
					if (intent.hasExtra(Constants.mStringExtraNameNotifyWakeUpDownloadActivity))
						mFlagWakeUpDownloadActivity = intent.getBooleanExtra(Constants.mStringExtraNameNotifyWakeUpDownloadActivity, false);
					int sequence = intent.getIntExtra(Constants.mStringExtraNameDownloadServerVersion, 0);
					String fileVersionPath = mConfigClass.getFileName(sequence) + "_"+ Constants.mServerVersionFileName;
					checkOTAVersion(fileVersionPath);					
				}
				else if (intent.hasExtra(Constants.mStringExtraNameNotifyDownload))
				{
					String versionFilePath = Constants.DOWNLOAD_PATH + "/"+intent.getStringExtra(Constants.mStringExtraNameNotifyDownload);
					float serverFileVersion = getServerFileVersion(versionFilePath);
					int seguence = getSequenceByVersionPath(versionFilePath);
					mConfigClass.modifyServerVersion(seguence, serverFileVersion);
					float clientFileVersion = mConfigClass.getVersion(seguence);
					Log.i(Constants.mTag, "Client Current Version:"+clientFileVersion+", Server Version:"+serverFileVersion);
					if ((serverFileVersion > clientFileVersion) && !mFlagOnlyCheckVersion)
					{
						OTAdownload(mConfigClass.getFileName(seguence));
					}
					else
					{
						if (mFlagOnlyCheckVersion)
							Log.i(Constants.mTag, versionFilePath + " don't need update, Just check file version, Current Version:"+clientFileVersion+", Server Version:"+serverFileVersion);
						else
							Log.i(Constants.mTag, versionFilePath+" cannot Download .. Current Version:"+clientFileVersion+", Server Version:"+serverFileVersion);
						downloadNextFile(seguence + 1);
					}	
					
				}
				else if (intent.hasExtra(Constants.mStringExtraNameUpdateLocalVersionTxt))
				{
					String fileName = intent.getStringExtra(Constants.mStringExtraNameUpdateLocalVersionTxt);
					int sequence = getSequenceByVersionPath(fileName);
					if (!mFlagOnlyCheckVersion)
					{
						mConfigClass.modifyVersion(sequence,mConfigClass.getServerVersion(sequence));
						mConfigClass.getDownloadStatus().getFileDownloadStatus(sequence).setDownloadComplete(true);
					}
					downloadNextFile(sequence++);
				}
				else if (intent.hasExtra(Constants.ACTION_FILE_TOTAL_LENGTH)&& 
						 intent.hasExtra(Constants.ACTION_FILE_CURRENT_INDEX) &&
						 intent.hasExtra(Constants.mStringExtraNameDownloadFileName))
				{
					double totalLength = intent.getDoubleExtra(Constants.ACTION_FILE_TOTAL_LENGTH, 1);
					double currentIndex = intent.getDoubleExtra(Constants.ACTION_FILE_CURRENT_INDEX, 1);
					String fileName = intent.getStringExtra(Constants.mStringExtraNameDownloadFileName);
					int seguence = getSequenceByVersionPath(fileName);
					DownloadStatus status = mConfigClass.getDownloadStatus();
					status.setFileDownloadStatus(seguence, currentIndex, totalLength);
					notifyOTAListenerUpdate(mConfigClass); 
					
				}
				else if (intent.hasExtra(Constants.mStringExtraNameDownloadFails))
				{
					mConfigClass.getDownloadStatus().setNetworkConnectionSuccesful(false);
					downloadNextFile(mConfigClass.getFileNameSize() + 1);
				}
				else if (intent.hasExtra(Constants.mStringExtraNameErrorReport))
				{
					
				}
			}

		}
		catch(Exception ex)
		{
			Log.i(Constants.mTag, ex.toString());
	
		}
        
    }
	private void downloadNextFile(int sequence)
	{
		notifyOTAListenerUpdate(mConfigClass);
		
		if (sequence < mConfigClass.getFileNameSize())
		{
            Intent tempIntent = new Intent();
            tempIntent.setAction(Constants.mServiceAction);
            tempIntent.putExtra(Constants.mStringExtraNameDownloadServerVersion, sequence);
			onReceive(null, tempIntent);
		}
		else
		{
//			for (int index = 0 ; index <mOTAListenerEvent.size() ; index++)
//			{
//				mOTAListenerEvent.get(index).Update(mConfigClass);
//			}
			if (mFlagWakeUpDownloadActivity)
			{
				if (mConfigClass.checkAllFileAreNewest())
				{
					String message = String.valueOf(mContext.getResources().getText(R.string.update_finish));   				
					Log.i(Constants.mTag, message);
					Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
				}
				else if (mConfigClass.getDownloadStatus().getNetworkConnectionSuccesful())
				{
					
					Intent intent = new Intent();
			        intent.setClass(mContext, FileDownloadProgressActivity.class);
			        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			        mContext.startActivity(intent);
				}
				else
				{
					String message = String.valueOf(mContext.getResources().getText(R.string.upgrade_fails));
    				Log.i(Constants.mTag, message);
    				Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
				}
			}
		}
	}
	private void notifyOTAListenerUpdate(ConfigClass configClass)
	{
		for (int index = 0 ; index <mOTAListenerEvent.size() ; index++)
			mOTAListenerEvent.get(index).DownloadProgress(configClass);
	}
	
	private void checkOTAVersion(String versionfilePath)
	{
        List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
        Log.i(Constants.mTag, "Prepare check server version ... ServeIP: "+mConfigClass.getServerIP());
        String URL = mConfigClass.getServerIP() + "/" + versionfilePath;
	    fileInfoList.add(new FileInfo(0, URL, versionfilePath, 0, 0));
	    Log.i(Constants.mTag, "Prepare check server version URL: "+URL);
		Intent intent = new Intent(mContext, DownloadService.class);
		intent.setAction(Constants.ACTION_CHECK_VERSION);
		intent.putExtra("fileInfo", (Serializable) fileInfoList);
		mContext.startService(intent);	
	}
	private void OTAdownload(String filePath)
	{
        List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
        Log.i(Constants.mTag, "Prepare update ... ServeIP: "+mConfigClass.getServerIP());

    	String URL = mConfigClass.getServerIP() + "/" + filePath;
        fileInfoList.add(new FileInfo(0, URL, filePath, 0, 0));
        Log.i(Constants.mTag, "Prepare update URL: "+URL);
		Intent intent = new Intent(mContext, DownloadService.class);
		intent.setAction(Constants.ACTION_START);
		intent.putExtra("fileInfo", (Serializable) fileInfoList);
		mContext.startService(intent);
		Log.i(Constants.mTag, "Prepare startup DownloadService");

	}
	private int getSequenceByVersionPath(String versionPath)
	{
		int squence = -1;
		String orgString = versionPath.replace("_"+Constants.mServerVersionFileName, "");
		if (mConfigClass != null)
		{
			for (int index = 0 ; index <mConfigClass.getFileNameSize() ; index++)
			{
				if (orgString.contains(mConfigClass.getFileName(index)))
				{
					squence = index;
					break;
				}
			}
		}
		Log.i(Constants.mTag, "getSequenceByVersionPath: " +versionPath + ", Sqeuence: "+squence);
		return squence;
	}
	private float getServerFileVersion(String serverFilePath)
	{
		File serverVersionFile = new File(serverFilePath);
		StringBuilder text = new StringBuilder();
		float value = 0;
		try 
		{ 
		    BufferedReader br = new BufferedReader(new FileReader(serverVersionFile));
		    String line;
		 
		    while ((line = br.readLine()) != null) 
		    {
		        text.append(line);
		        text.append('\n');
		    } 
		    br.close();
		    value = Float.parseFloat(text.toString());
		} 
		catch (IOException e) 
		{
			Log.i(Constants.mTag, "Excepetion: getSequenceByVersionPath: File name"+serverVersionFile+ ","+e.toString());
			return 0;
		} 
		
		return value;
	}
} 
