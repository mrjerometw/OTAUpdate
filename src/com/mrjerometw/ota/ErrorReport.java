package com.mrjerometw.ota;

public class ErrorReport 
{
	private int mCode = -1;
	private String mMessage = "";
	public ErrorReport()
	{
		
	}
	public ErrorReport setInfo(int code, String message)
	{
		mCode = code;
		mMessage = message;
		return this;
	}
	public String getMessage()
	{
		return mMessage;
	}
	public int getCode()
	{
		return mCode;
	}

	
}
