package com.invictrixrom.updater;

public interface MagiskCallback {
	void magiskDownloadProgress(int progress);
	void magiskDownloaded(boolean success, String magiskPath);
}
