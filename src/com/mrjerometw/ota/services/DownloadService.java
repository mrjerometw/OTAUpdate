
package com.mrjerometw.ota.services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.mrjerome.ota.entities.FileInfo;
import com.mrjerometw.ota.Constants;
import com.mrjerometw.ota.OTABroadcastReceiver;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.webkit.WebView.FindListener;

public class DownloadService extends Service
{

	private Map<Integer, DownloadTask> mTasks = 
			new LinkedHashMap<Integer, DownloadTask>();
	private static boolean mIsCheckServerVersion = false; 
	/**
	 * Step1: Check Version
	 * Step2: Update Files
	 * @see Service#onStartCommand(Intent, int, int)
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		try
		{
			if (Constants.ACTION_CHECK_VERSION.equals(intent.getAction()))
			{
				mIsCheckServerVersion = true;
				List<FileInfo> fileInfoList = (List<FileInfo>) intent.getSerializableExtra("fileInfo");
				for (int index = 0 ;index < fileInfoList.size() ;index++)
				{
					FileInfo fileInfo = fileInfoList.get(index);
					Log.i(Constants.mTag , "Check Version:" + fileInfo.toString());
					Thread thread = new InitThread(fileInfo);
					thread.setPriority(Thread.MIN_PRIORITY);
					thread.start();
				}
			}
			else if ( Constants.ACTION_START.equals(intent.getAction()))
			{
				List<FileInfo> fileInfoList = (List<FileInfo>) intent.getSerializableExtra("fileInfo");
				FileInfo fileInfo = fileInfoList.get(0);
				if (fileInfo!=null)
				{
					Log.i(Constants.mTag , "Start download:" + fileInfo.toString());
					Thread thread = new InitThread(fileInfo);
					thread.setPriority(Thread.MIN_PRIORITY);
					thread.start();
				}
			}
			else if (Constants.ACTION_STOP.equals(intent.getAction()))
			{
				FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
				Log.i(Constants.mTag , "Stop:" + fileInfo.toString());
	
				DownloadTask task = mTasks.get(fileInfo.getId());
				if (task != null)
				{
					task.isPause = true;
				}
			}
			else if (Constants.ACTION_UPDATE.equals(intent.getAction()))
			{
				double totalLength = intent.getDoubleExtra(Constants.ACTION_FILE_TOTAL_LENGTH, 0);
				double currentIndex = intent.getDoubleExtra(Constants.ACTION_FILE_CURRENT_INDEX, 0);
				String fileName = intent.getStringExtra(Constants.mStringExtraNameDownloadFileName);
	            intent = new Intent();
	            intent.setAction(Constants.mServiceAction); 
	            intent.putExtra(Constants.ACTION_FILE_TOTAL_LENGTH, totalLength);
	            intent.putExtra(Constants.ACTION_FILE_CURRENT_INDEX, currentIndex);
	            intent.putExtra(Constants.mStringExtraNameDownloadFileName, fileName);
	            sendBroadcast(intent);	
			}
			else if (Constants.ACTION_ERROR.equals(intent.getAction()))
			{
	            intent = new Intent();
	            intent.setAction(Constants.mServiceAction); 
	            intent.putExtra(Constants.mStringExtraNameDownloadFails, "");
	            sendBroadcast(intent);	
			}
			else if (Constants.ACTION_ERROR_REPORT.equals(intent.getAction()))
			{
	            intent = new Intent();
	            intent.setAction(Constants.mServiceAction); 
	            intent.putExtra(Constants.mStringExtraNameErrorReport, "");
	            sendBroadcast(intent);					
			}
			else if (Constants.ACTION_FINISHED.equals(intent.getAction()))
			{
				FileInfo fileInfo = (FileInfo) intent.getSerializableExtra("fileInfo");
				if (mIsCheckServerVersion)
				{
		            intent = new Intent();
		            intent.setAction(Constants.mServiceAction); 
		            intent.putExtra(Constants.mStringExtraNameNotifyDownload, fileInfo.getFileName());
		            sendBroadcast(intent);		
					mIsCheckServerVersion = false;
		        }
				else 
				{
		            intent = new Intent();
		            intent.setAction(Constants.mServiceAction);
		            intent.putExtra(Constants.mStringExtraNameUpdateLocalVersionTxt, fileInfo.getFileName());
		            sendBroadcast(intent);	
				}
			}
		}
		catch(Exception e)
		{
			Log.i(Constants.mTag , "Exception" + e.toString());
			e.printStackTrace();
		}
		return super.onStartCommand(intent, flags, startId);
	}
	private Handler mHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg) {
			switch (msg.what)
			{
				case Constants.MSG_INIT:
					FileInfo fileInfo = (FileInfo) msg.obj;
					Log.i(Constants.mTag, "Init:" + fileInfo);
					DownloadTask task = new DownloadTask(DownloadService.this, fileInfo, 1);
					task.downLoad();
					mTasks.put(fileInfo.getId(), task);
					break;

				default:
					break;
			}
		};
	};
	
	private class InitThread extends Thread
	{
		private FileInfo mFileInfo = null;
		
		public InitThread(FileInfo mFileInfo)
		{
			this.mFileInfo = mFileInfo;
		}
		
		/**
		 * @see Thread#run()
		 */
		@Override
		public void run()
		{
			HttpURLConnection connection = null;
			RandomAccessFile raf = null;
			int length = -1;
			int errorTime = 0;
			boolean bSuccesfulFlag = false;
			while (true)
			{
				try
				{
					Log.i(Constants.mTag, "Prepare download URL:"+mFileInfo.getUrl()+", times:"+errorTime);
					URL url = new URL(mFileInfo.getUrl());
					connection = (HttpURLConnection) url.openConnection();
					connection.setConnectTimeout(30000);
					connection.setRequestMethod("GET");
					
					int responseCode = connection.getResponseCode();
					if (responseCode == 200)// HttpStatus.SC_OK
					{
						length = connection.getContentLength();
					}
					Log.i(Constants.mTag, "File:"+mFileInfo.getUrl()+",Response Code:"+responseCode+",File Length:"+length);
					if (length >0)
					{
						File dir = new File(Constants.DOWNLOAD_PATH);
						if (!dir.exists())
						{
							dir.mkdir();
						}
		
						File file = new File(dir, mFileInfo.getFileName());
						raf = new RandomAccessFile(file, "rwd");
						raf.setLength(length);
						bSuccesfulFlag = true;
					}
					else
						Log.i(Constants.mTag, "(DownloadService )Error: the "+mFileInfo.getFileName()+" http response code is "+ responseCode);
					
				}
				catch (Exception e)
				{
					bSuccesfulFlag = false;
					e.printStackTrace();
					Log.i(Constants.mTag , "Exception" + e.toString());
				}
				finally
				{
					if (connection != null)
					{
						connection.disconnect();
					}
					if (raf != null)
					{
						try
						{
							raf.close();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
					if (!bSuccesfulFlag)
					{
						errorTime++;
						try
						{
							Thread.sleep(5000);
						}
						catch(Exception ex)
						{
							
						}
					}
					if (errorTime > 4 || bSuccesfulFlag)
					{
						mFileInfo.setLength(length);
						mHandler.obtainMessage(Constants.MSG_INIT, mFileInfo).sendToTarget();
						return;
					}
				}
			}
		}
	}
	
	/**
	 * @see Service#onBind(Intent)
	 */
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

}
