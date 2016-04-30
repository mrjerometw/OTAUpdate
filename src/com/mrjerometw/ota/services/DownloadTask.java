
package com.mrjerometw.ota.services;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import com.mrjerome.ota.entities.FileInfo;
import com.mrjerome.ota.entities.ThreadInfo;
import com.mrjerometw.ota.Constants;
import com.mrjerometw.ota.OTABroadcastReceiver;
import com.mrjerometw.ota.db.ThreadDAO;
import com.mrjerometw.ota.db.ThreadDAOImpl;

import android.R.integer;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.util.Log;
import android.widget.Toast;


public class DownloadTask
{
	private Context mContext = null;
	private FileInfo mFileInfo = null;
	private ThreadDAO mDao = null;
	private int mFinised = 0;
	public boolean isPause = false;
	private int mThreadCount = 1;
	private List<DownloadThread> mDownloadThreadList = null;
	
	/** 
	 *@param mContext
	 *@param mFileInfo
	 */
	public DownloadTask(Context mContext, FileInfo mFileInfo, int count)
	{
		this.mContext = mContext;
		this.mFileInfo = mFileInfo;
		this.mThreadCount = count;
		mDao = new ThreadDAOImpl(mContext);
	}
	
	public void downLoad()
	{

		List<ThreadInfo> threads = mDao.getThreads(mFileInfo.getUrl());
		ThreadInfo threadInfo = null;
		
		if (0 == threads.size())
		{

			int len = mFileInfo.getLength() / mThreadCount;
			for (int i = 0; i < mThreadCount; i++)
			{

				threadInfo = new ThreadInfo(i, mFileInfo.getUrl(),
						len * i, (i + 1) * len - 1, 0);
				
				if (mThreadCount - 1 == i)
				{
					threadInfo.setEnd(mFileInfo.getLength());
				}
				
				
				threads.add(threadInfo);
				mDao.insertThread(threadInfo);
			}
		}

		mDownloadThreadList = new ArrayList<DownloadThread>();

		for (ThreadInfo info : threads)
		{
			DownloadThread thread = new DownloadThread(info);
			thread.setPriority(Thread.MIN_PRIORITY);
			thread.start();

			mDownloadThreadList.add(thread);
		}
	}
	

	private class DownloadThread extends Thread
	{
		private ThreadInfo mThreadInfo = null;
		public boolean isFinished = false;

		/** 
		 *@param mInfo
		 */
		public DownloadThread(ThreadInfo mInfo)
		{
			this.mThreadInfo = mInfo;
		}
		
		/**
		 * @see Thread#run()
		 */
		@Override
		public void run()
		{
			HttpURLConnection connection = null;
			RandomAccessFile raf = null;
			InputStream inputStream = null;
			int errorTime = 0;
			boolean bSuccesfulFlag = false;
			while (true)
			{
				try
				{
					Log.i(Constants.mTag, "Prepare download URL:"+mFileInfo.getUrl()+", times:"+errorTime);
					URL url = new URL(mThreadInfo.getUrl());
					connection = (HttpURLConnection) url.openConnection();
					connection.setConnectTimeout(30000);
					connection.setRequestMethod("GET");
	
					int start = mThreadInfo.getStart() + mThreadInfo.getFinished();
	//				connection.setRequestProperty("Range",
	//						"bytes=" + start + "-" + mThreadInfo.getEnd());
	
					File file = new File(Constants.DOWNLOAD_PATH,
							mFileInfo.getFileName());
					raf = new RandomAccessFile(file, "rwd");
					raf.seek(start);
					Intent intent = new Intent();
					intent.setAction(Constants.ACTION_UPDATE);
					mFinised += mThreadInfo.getFinished();
					//Log.i(MainActivity.TAG, mThreadInfo.getId() + "finished = " + mThreadInfo.getFinished());
					int responseCode = connection.getResponseCode();
					Log.i(Constants.mTag, "File:"+mFileInfo.getUrl()+",Response Code:"+responseCode);
					
					if (responseCode == 206 || responseCode == 200) //HttpStatus.SC_PARTIAL_CONTENT
					{
						inputStream = connection.getInputStream();
						byte buf[] = new byte[1024 * 1024];
						int len = -1;
						long time = System.currentTimeMillis();
						while ((len = inputStream.read(buf)) != -1)
						{
							raf.write(buf, 0, len);
							mFinised += len;
							mThreadInfo.setFinished(mThreadInfo.getFinished() + len);
							if (System.currentTimeMillis() - time > 1000)
							{
								time = System.currentTimeMillis();
								double currentIndex = mFinised;
								double totalLength = mFileInfo.getLength();
								int f = (int)((currentIndex  / totalLength)*100.0);
								if (f > mFileInfo.getFinished())
								{
									Intent tempIntent = new Intent(mContext, DownloadService.class);
									tempIntent.setAction(Constants.ACTION_UPDATE);
									tempIntent.putExtra("finished", f);
									tempIntent.putExtra("id", mFileInfo.getId());
									tempIntent.putExtra(Constants.ACTION_FILE_TOTAL_LENGTH, totalLength);
									tempIntent.putExtra(Constants.ACTION_FILE_CURRENT_INDEX, currentIndex);
									tempIntent.putExtra(Constants.mStringExtraNameDownloadFileName, mFileInfo.getFileName());
									mContext.startService(tempIntent);
								}
							}
							
	
							if (isPause)
							{
								mDao.updateThread(mThreadInfo.getUrl(),	
										mThreadInfo.getId(), 
										mThreadInfo.getFinished());								
								return;
							}
						}
	
						isFinished = true;
						checkAllThreadFinished();
						bSuccesfulFlag = true;
					}
					else
					{
						Log.i(Constants.mTag, "Error Times:"+errorTime+",(DownloadTask) Error: the "+mFileInfo.getFileName()+" http response code is "+ responseCode);
						bSuccesfulFlag = false;
					}
				}
				catch (Exception e)
				{
					bSuccesfulFlag = false;
					Log.i(Constants.mTag, "Error Times:"+errorTime+",exception: " +e.toString());

				}
				finally
				{
					try
					{
						if (connection != null)
						{
							connection.disconnect();
						}
						if (raf != null)
						{
							raf.close();
						}
						if (inputStream != null)
						{
							inputStream.close();
						}
					}
					catch (Exception e2)
					{
						e2.printStackTrace();
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
					if (!bSuccesfulFlag && errorTime > 3)
					{
						Log.i(Constants.mTag, mThreadInfo.getUrl()+" Download Failed");
						Intent tempIntent = new Intent(mContext, DownloadService.class);
						tempIntent.setAction(Constants.ACTION_ERROR);
						mContext.startService(tempIntent);
						return;
					}
					else if (bSuccesfulFlag)
						return;
				}
			}
		}
	}

	private synchronized void checkAllThreadFinished()
	{
		boolean allFinished = true;

		for (DownloadThread thread : mDownloadThreadList)
		{
			if (!thread.isFinished)
			{
				allFinished = false;
				break;
			}
		}
		
		if (allFinished)
		{
			mDao.deleteThread(mFileInfo.getUrl());
			Intent intent = new Intent(mContext, DownloadService.class);
			intent.setAction(Constants.ACTION_FINISHED);
			intent.putExtra("fileInfo", mFileInfo);
			mContext.startService(intent);
		}
	}
}
