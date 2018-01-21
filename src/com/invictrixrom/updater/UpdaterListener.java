package com.invictrixrom.updater;

public interface UpdaterListener {
	void updateProgress(int progress);

	void updateStatusChange(int status);

	void updateComplete(int status);
}

