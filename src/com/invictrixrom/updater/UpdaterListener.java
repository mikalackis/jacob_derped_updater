package com.invictrixrom.updater;

public interface UpdaterListener {
	void progressUpdate(int progress);
	void notifyUpdateStatusChange(int status);
	void notifyUpdateComplete(int status);
}

