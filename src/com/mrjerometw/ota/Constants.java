package com.mrjerometw.ota;

import android.os.Environment;
import android.util.Log;

public class Constants 
{
	public static final String mServiceAction = "android.intent.action.OTA_UPDATES";
	public static final String mTag = "OTA";
	public static final String mStringExtraNameNotifyWakeUpDownloadActivity = "OTABroadcastReceiver_Wakeup_Download_Activity";
	public static final String mStringExtraNameNotifyDownloadOnlyCheckVersion = "OTABroadcastReceiver_DOWNLOAD_ONLY_CHECK";
	public static final String mStringExtraNameNotifyDownload = "OTABroadcastReceiver_DOWNLOAD";
	public static final String mStringExtraNameUpdateLocalVersionTxt = "OTABroadcastReceiver_UPDATE_VERSION";
	public static final String mStringExtraNameDownloadFails= "OTABroadcastReceiver_DOWNLOAD_FAILS";
	public static final String mStringExtraNameDownloadServerVersion = "OTABroadcastReceiver_Server_Version_DOWNLOAD";
	public static final String mStringExtraNameErrorReport = "OTABroadcastReceiver_ERROR_REPORT";
	public static final String mOTAKernelVersion = "OTAKernelVersion";
	public static final String mOTABootloaderVersion = "OTABootLoaderVersion";
	public static final String mOTADriverVersion = "OTADriverVersion";
	public static final String mOTAAndroidSystemVersion = "OTAAndroidSystemVersion";
	public static final String mOTASharePreferenceString = "OTAUpdateSettings";
	public static final String mServerVersionFileName = "VERSION.txt";
	public static String DOWNLOAD_PATH = "";
	public static final String ACTION_CHECK_VERSION = "ACTON_CHECK_VERSION";
	public static final String ACTION_START = "ACTION_DOWNLOAD";
	public static final String ACTION_STOP = "ACTION_STOP";
	public static final String ACTION_UPDATE = "ACTION_UPDATE";
	public static final String ACTION_FINISHED = "ACTION_FINISHED";
	public static final String ACTION_ERROR = "ACTION_ERROR";
	public static final String ACTION_ERROR_REPORT = "ACTION_ERROR_REPORT";
	public static final String ACTION_FILE_TOTAL_LENGTH = "TotalLength";
	public static final String ACTION_FILE_CURRENT_INDEX = "CurrentIndex";
	public static final String mStringExtraNameDownloadFileName = "DownloadFileName";
	public static final String SYSTEM_PROPERTY_OTA_SERVER_IP = "persist.OTA_SERVER_IP";
	public static final String SYSTEM_PROPERTY_OTA_SAVE_FOLDER = "persist.OTA_SAVE_FOLDER";
	public static final int MSG_INIT = 0;
	private static void ReadDownloadPath()
	{

		Constants.DOWNLOAD_PATH = PropertyUtils.get(SYSTEM_PROPERTY_OTA_SAVE_FOLDER, "");
	}

    static {
        ReadDownloadPath();
    }
}
