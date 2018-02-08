package com.invictrixrom.updater;

public interface MagiskCallback {
	void magiskDownloadProgress(int progress);

	void magiskDownloaded(boolean success);

	void magiskInstallStatusUpdate(int resStatus);

	void magiskInstallComplete(MagiskInstaller.MagiskInstallCodes statusCode);
}
