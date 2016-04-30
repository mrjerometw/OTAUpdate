
package com.mrjerometw.ota.db;

import java.util.List;

import com.mrjerome.ota.entities.ThreadInfo;

public interface ThreadDAO
{
	public void insertThread(ThreadInfo threadInfo);

	public void deleteThread(String url);

	public void updateThread(String url, int thread_id, int finished);

	public List<ThreadInfo> getThreads(String url);

	public boolean isExists(String url, int thread_id);
}
