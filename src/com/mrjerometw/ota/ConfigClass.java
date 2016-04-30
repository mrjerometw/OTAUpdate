package com.mrjerometw.ota;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Environment;
import android.util.Log;
import android.util.Xml;
public class ConfigClass 
{
	private String mServerIP = "";
	private List<String> mFileNames = new ArrayList<String>();
	private float mLocalKernelVersion = -2;
	private float mLocalBootloaderVersion = -2;
	private float mLocalDriverVersion = -2;
	private float mLocalAndroidSystemVersion = -2;
	private float mServerKernelVersion = -2;
	private float mServerBootloaderVersion = -2;
	private float mServerDriverVersion = -2;
	private float mServerAndroidSystemVersion = -2;
	private static ConfigClass mConfigClass = null;
	private DownloadStatus mDownloadStatus = null;
	private SharedPreferences mSharePreferences = null;

	private ConfigClass()
	{
		mDownloadStatus = new DownloadStatus();
		
	}
	private void init(Context context)
	{
		passConfig(context);
		mSharePreferences = context.getSharedPreferences(Constants.mOTASharePreferenceString, 0);
		if (mConfigClass != null)
		{
			mConfigClass.setAndroidSystemVersion(mSharePreferences.getFloat(Constants.mOTAAndroidSystemVersion, -1));
			mConfigClass.setBootloaderVersion(mSharePreferences.getFloat(Constants.mOTABootloaderVersion, -1));
			mConfigClass.setDriverVersion(mSharePreferences.getFloat(Constants.mOTADriverVersion, -1));
			mConfigClass.setKernelVersion(mSharePreferences.getFloat(Constants.mOTAKernelVersion, -1));		
		}
	}
	public void modifyVersion(int sequence, float version)
	{
		if (sequence == 0)
			mConfigClass.setKernelVersion(version);
		else if (sequence == 1)
			mConfigClass.setBootloaderVersion(version);
		else if (sequence == 2)
			mConfigClass.setDriverVersion(version);
		else if (sequence == 3)
			mConfigClass.setAndroidSystemVersion(version);
		
	}
	public float getVersion(int sequence)
	{
		if (sequence == 0)
			return mConfigClass.getKernelVersion();
		else if (sequence == 1)
			return mConfigClass.getBootloaderVersion();
		else if (sequence == 2)
			return mConfigClass.getDriverVersion();
		else if (sequence == 3)
			return mConfigClass.getAndroidSystemVersion();
		
		return -2;
	}
	public static ConfigClass getInstance(Context context)
	{
		if (mConfigClass == null)
		{
			mConfigClass = new ConfigClass();
			mConfigClass.init(context);
		}
		return mConfigClass;
	}
	public float getServerKernelVersion() 
	{
		return mServerKernelVersion;
	}
	public void setServerKernelVersion(float serverKernelVersion) 
	{
		mServerKernelVersion = serverKernelVersion;
	}
	public float getServerBootloaderVersion() 
	{
		return mServerBootloaderVersion;
	}
	public void setServerBootloaderVersion(float serverBootloaderVersion) 
	{
		mServerBootloaderVersion = serverBootloaderVersion;
	}
	public float getServerDriverVersion() 
	{
		return mServerDriverVersion;
	}
	public void setServerDriverVersion(float serverDriverVersion) 
	{
		mServerDriverVersion = serverDriverVersion;
	}
	public float getServerAndroidSystemVersion() {
		return mServerAndroidSystemVersion;
	}
	public void setServerAndroidSystemVersion(float serverAndroidSystemVersion) {
		mServerAndroidSystemVersion = serverAndroidSystemVersion;
	}
	public float getBootloaderVersion() 
	{
		return mLocalBootloaderVersion;
	}
	public void setBootloaderVersion(float bootloaderVersion) 
	{
		mLocalBootloaderVersion = bootloaderVersion;
		if (mSharePreferences != null)
		{
			SharedPreferences.Editor editor = mSharePreferences.edit();
			editor.putFloat(Constants.mOTABootloaderVersion, mLocalBootloaderVersion);
			editor.commit();
		}
	}
	public float getDriverVersion() 
	{
		return mLocalDriverVersion;
	}
	public void setDriverVersion(float driverVersion) 
	{
		mLocalDriverVersion = driverVersion;
		if (mSharePreferences != null)
		{
			SharedPreferences.Editor editor = mSharePreferences.edit();
			editor.putFloat(Constants.mOTADriverVersion, mLocalDriverVersion);
			editor.commit();
		}
	}
	public float getAndroidSystemVersion() {
		return mLocalAndroidSystemVersion;
	}
	public void setAndroidSystemVersion(float androidSystemVersion) 
	{
		mLocalAndroidSystemVersion = androidSystemVersion;
		if (mSharePreferences != null)
		{
			SharedPreferences.Editor editor = mSharePreferences.edit();
			editor.putFloat(Constants.mOTAAndroidSystemVersion, mLocalAndroidSystemVersion);
			editor.commit();
		}
	}
	public String getServerIP()
	{
		return mServerIP;
	}
	public void setServerIP(String serverIP)
	{
		mServerIP = serverIP;
	}
	public List<String> getFileNames()
	{
		return mFileNames;
	}
	public void addFileNames(String fileName)
	{
		mFileNames.add(fileName);
		mDownloadStatus.addFileDownStatus(fileName);
	}
	public int getFileNameSize()
	{
		return mFileNames.size();
	}
	public String getFileName(int index)
	{
		return mFileNames.get(index);
	}
	public void setKernelVersion(float version)
	{
		mLocalKernelVersion = version;
		if (mSharePreferences != null)
		{
			SharedPreferences.Editor editor = mSharePreferences.edit();
			editor.putFloat(Constants.mOTAKernelVersion, mLocalKernelVersion);
			editor.commit();
		}
	}
	public float getKernelVersion()
	{
		return mLocalKernelVersion;
	}
	private void passConfig(Context context)
	{
		if (context == null)
			return;
		Log.i(Constants.mTag, "Prepare Pass Config");
		AssetManager asset = context.getAssets();

		try {
			InputStream inputStream = asset.open("config.xml");
			XmlPullParser configXML = Xml.newPullParser();
			configXML.setInput(inputStream, "utf-8");
			int eventType = configXML.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) 
			{  
				if(eventType == XmlPullParser.START_TAG) 
				{
					String tagName = configXML.getName();
					configXML.next();
					if (tagName.equalsIgnoreCase("SERVER_IP"))
					{
						String serverip = PropertyUtils.get(Constants.SYSTEM_PROPERTY_OTA_SERVER_IP,"");
						if (serverip == null || serverip.length() == 0)
							serverip = configXML.getText();
						
						mConfigClass.setServerIP(serverip);
						
					}
					else if (tagName.equalsIgnoreCase("SERVER_FILES"))
					{
						configXML.next();
						while(eventType != XmlPullParser.END_TAG)
						{
							if (eventType == XmlPullParser.START_TAG) 
							{
								tagName = configXML.getName();
								if (tagName.equalsIgnoreCase("FILE"))
								{
									String fileName = configXML.nextText();
									mConfigClass.addFileNames(fileName);
								}
							}
							configXML.next();
							eventType = configXML.getEventType();
							//mServerIP = configXML.getText();
						}
					}
					else if (tagName.equalsIgnoreCase("SAVE_FOLDER"))
					{
						if ((Constants.DOWNLOAD_PATH == null) || (Constants.DOWNLOAD_PATH.length() == 0 ))
						{
							Constants.DOWNLOAD_PATH = configXML.getText();
							if (Constants.DOWNLOAD_PATH == null || Constants.DOWNLOAD_PATH.length() == 0)
							{
								Constants.DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()
								+ "/downloads/";
							}
						}
					}
				}
				configXML.next();
				eventType = configXML.getEventType();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	public void modifyServerVersion(int sequence, float serverFileVersion) 
	{
		if (sequence == 0)
			mConfigClass.setServerKernelVersion(serverFileVersion);
		else if (sequence == 1)
			mConfigClass.setServerBootloaderVersion(serverFileVersion);
		else if (sequence == 2)
			mConfigClass.setServerDriverVersion(serverFileVersion);
		else if (sequence == 3)
			mConfigClass.setServerAndroidSystemVersion(serverFileVersion);
		
	}
    public boolean checkAllFileAreNewest()
    {
    	if (mConfigClass == null)
    		return false;
    	for (int index = 0 ; index < mConfigClass.getFileNameSize() ; index++)
    	{
    		if (mConfigClass.getServerVersion(index) > mConfigClass.getVersion(index) )
    			return false;
    	}
    	return true;
    }
	public float getServerVersion(int sequence) {
		if (sequence == 0)
			return mConfigClass.getServerKernelVersion();
		else if (sequence == 1)
			return mConfigClass.getServerBootloaderVersion();
		else if (sequence == 2)
			return mConfigClass.getServerDriverVersion();
		else if (sequence == 3)
			return mConfigClass.getServerAndroidSystemVersion();
		
		return -2;
	}
	
	public DownloadStatus getDownloadStatus()
	{
		return mDownloadStatus;
	}
	
	public class DownloadStatus
	{
		private boolean mIsNetworkConnectionSuccesful  = true;
		private List<FileDownloadStatus> mFileDownloadStatusList = null;
		public DownloadStatus()
		{
			mFileDownloadStatusList = new ArrayList<FileDownloadStatus>();
		}
		public void addFileDownStatus(String fileName)
		{
			mFileDownloadStatusList.add(new FileDownloadStatus(fileName));			
		}
		public void setNetworkConnectionSuccesful(boolean flag)
		{
			mIsNetworkConnectionSuccesful = flag;
		}
		public boolean getNetworkConnectionSuccesful()
		{
			return mIsNetworkConnectionSuccesful;
		}
		public void setFileDownloadStatus(int sequence, double offset, double length)
		{
			if (sequence < 0 || sequence > mFileDownloadStatusList.size())
				return;
			mFileDownloadStatusList.get(sequence).setOffset(offset).setLength(length);
		}
		public FileDownloadStatus getFileDownloadStatus(int sequence)
		{
			if (sequence < 0 || sequence > mFileDownloadStatusList.size())
				return null;
			
			return mFileDownloadStatusList.get(sequence);
		}
		public int getFileDownloadStatusSize()
		{
			return mFileDownloadStatusList.size();
		}
	}
	
	public class FileDownloadStatus
	{
		private String mFileName = "";
		private boolean mIsDownloadComplete = false;
		private double mOffset = 0;
		private double mLength = 1;
		public FileDownloadStatus(String fileName)
		{
			setFileName(fileName);
			setDownloadComplete(false);
		}
		public boolean IsDownloadComplete()
		{
			return mIsDownloadComplete;
		}
		public FileDownloadStatus setDownloadComplete(boolean flag)
		{
			mIsDownloadComplete = flag;
			return this;
		}
		public FileDownloadStatus setOffset(double offset)
		{
			mOffset = offset;
			return this;
		}
		public double getOffset()
		{
			return mOffset;
		}
		public FileDownloadStatus setLength(double length)
		{
			mLength = length;
			return this;
		}
		public double getLength()
		{
			return mLength;
		}
		public FileDownloadStatus setFileName(String fileName)
		{
			mFileName = fileName;
			return this;
		}
		public String getFileName()
		{
			return mFileName;
		}
	}
	
}
