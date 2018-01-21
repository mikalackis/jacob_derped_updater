package com.invictrixrom.updater;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.provider.DocumentsContract;
import android.os.Environment;
import android.content.ContentUris;
import android.provider.MediaStore;
import android.database.Cursor;
import android.net.Uri;
import android.os.UpdateEngine;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.InputStream;

public class Utilities {

	private static final int notification_id = 10242048;
	private static final String channel_id = "com.invictrixrom.updater";
	private static final String channel_name = "Invictrix ROM Updater";
	private static final String channel_description = "Invictrix ROM Updater";

	public static void createNotificationChannel(NotificationManager mNotificationManager) {
		NotificationChannel mChannel = new NotificationChannel(channel_id, channel_name, NotificationManager.IMPORTANCE_MAX);
		mChannel.setDescription(channel_description);
		mChannel.enableLights(true);
		mNotificationManager.createNotificationChannel(mChannel);
	}

	public static Notification.Builder buildNotification(Context context, NotificationManager mNotificationManager, String title, int iconRes, String text, boolean progress, boolean progressIndeterminate, boolean alertOnce, boolean ongoing) {

		Notification.Builder mBuilder;

		mBuilder = new Notification.Builder(context, channel_id)
						   .setSmallIcon(iconRes)
						   .setContentTitle(title)
						   .setContentText(text)
						   .setOngoing(ongoing)
						   .setOnlyAlertOnce(true);

		if (progress) {
			mBuilder = mBuilder.setProgress(100, 0, progressIndeterminate);
		}

		mNotificationManager.notify(notification_id, mBuilder.build());
		return mBuilder;
	}

	public static Notification.Builder updateNotificationText(NotificationManager mNotificationManager, Notification.Builder mBuilder, String status) {
		mBuilder = mBuilder.setContentText(status);

		mNotificationManager.notify(notification_id, mBuilder.build());

		return mBuilder;
	}

	public static Notification.Builder updateNotificationProgress(NotificationManager mNotificationManager, Notification.Builder mBuilder, int progress, boolean indeterminate) {
		mBuilder = mBuilder.setProgress(100, progress, indeterminate);

		mNotificationManager.notify(notification_id, mBuilder.build());

		return mBuilder;
	}

	public static Notification.Builder updateNotificationTitle(NotificationManager mNotificationManager, Notification.Builder mBuilder, String title) {
		mBuilder = mBuilder.setContentTitle(title);

		mNotificationManager.notify(notification_id, mBuilder.build());

		return mBuilder;
	}

	public static Notification.Builder finishNotification(NotificationManager mNotificationManager, Notification.Builder mBuilder, String title, String status, boolean error, int errorIconRes, int successIconRes, boolean progressFinished) {
		mBuilder = mBuilder.setSmallIcon(error ? errorIconRes : successIconRes)
						   .setContentText(status)
						   .setContentTitle(title)
						   .setOngoing(false);

		if (progressFinished) {
			mBuilder = mBuilder.setProgress(0, 0, false);
		}

		mNotificationManager.notify(notification_id, mBuilder.build());

		return mBuilder;
	}

	public static boolean copyFile(File src, File dst) {
		if (src.getAbsolutePath().equals(dst.getAbsolutePath())) {
			return true;
		} else {
			try {
				InputStream is = new FileInputStream(src);
				OutputStream os = new FileOutputStream(dst);
				byte[] buff = new byte[1024];
				int len;
				while ((len = is.read(buff)) > 0) {
					os.write(buff, 0, len);
				}
				is.close();
				os.close();
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
		}
		return false;
	}

	public static String getPath(final Context context, final Uri uri) {
		if (DocumentsContract.isDocumentUri(context, uri)) {
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/" + split[1];
				}
			} else if (isDownloadsDocument(uri)) {
				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
				return getDataColumn(context, contentUri, null, null);
			} else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[]{split[1]};

				return getDataColumn(context, contentUri, selection, selectionArgs);
			}
		} else if ("content".equalsIgnoreCase(uri.getScheme())) {
			return getDataColumn(context, uri, null, null);
		} else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = {column};

		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}


	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	public static int getUpdaterStatus(int status) {
		int statusString = 0;
		switch (status) {
			case UpdateEngine.UpdateStatusConstants.IDLE: {
				statusString = R.string.idle;
			}
			break;
			case UpdateEngine.UpdateStatusConstants.CHECKING_FOR_UPDATE: {
				statusString = R.string.checking_for_update;
			}
			break;
			case UpdateEngine.UpdateStatusConstants.UPDATE_AVAILABLE: {
				statusString = R.string.update_available;
			}
			break;
			case UpdateEngine.UpdateStatusConstants.DOWNLOADING: {
				statusString = R.string.downloading;
			}
			break;
			case UpdateEngine.UpdateStatusConstants.VERIFYING: {
				statusString = R.string.verifying;
			}
			break;
			case UpdateEngine.UpdateStatusConstants.FINALIZING: {
				statusString = R.string.finalizing;
			}
			break;
			case UpdateEngine.UpdateStatusConstants.UPDATED_NEED_REBOOT: {
				statusString = R.string.updated_needs_reboot;
			}
			break;
			case UpdateEngine.UpdateStatusConstants.REPORTING_ERROR_EVENT: {
				statusString = R.string.error;
			}
			break;
			case UpdateEngine.UpdateStatusConstants.ATTEMPTING_ROLLBACK: {
				statusString = R.string.attempting_rollback;
			}
			break;
			case UpdateEngine.UpdateStatusConstants.DISABLED: {
				statusString = R.string.disabled;
			}
			break;
			default:
				break;
		}
		return statusString;
	}

	public static int getUpdaterCompleteStatus(int status) {
		int statusString = 0;
		switch (status) {
			case UpdateEngine.ErrorCodeConstants.SUCCESS: {
				statusString = R.string.update_success;
			}
			break;
			case UpdateEngine.ErrorCodeConstants.ERROR: {
				statusString = R.string.update_error;
			}
			break;
			case UpdateEngine.ErrorCodeConstants.FILESYSTEM_COPIER_ERROR: {
				statusString = R.string.filesystem_error;
			}
			break;
			case UpdateEngine.ErrorCodeConstants.POST_INSTALL_RUNNER_ERROR: {
				statusString = R.string.post_install_error;
			}
			break;
			case UpdateEngine.ErrorCodeConstants.PAYLOAD_MISMATCHED_TYPE_ERROR: {
				statusString = R.string.payload_mismatched;
			}
			break;
			case UpdateEngine.ErrorCodeConstants.INSTALL_DEVICE_OPEN_ERROR: {
				statusString = R.string.install_device_already_open;
			}
			break;
			case UpdateEngine.ErrorCodeConstants.KERNEL_DEVICE_OPEN_ERROR: {
				statusString = R.string.kernel_device_already_open;
			}
			break;
			case UpdateEngine.ErrorCodeConstants.DOWNLOAD_TRANSFER_ERROR: {
				statusString = R.string.download_transfer_error;
			}
			break;
			case UpdateEngine.ErrorCodeConstants.PAYLOAD_HASH_MISMATCH_ERROR: {
				statusString = R.string.payload_hash_error;
			}
			break;
			case UpdateEngine.ErrorCodeConstants.PAYLOAD_SIZE_MISMATCH_ERROR: {
				statusString = R.string.payload_size_mismatch_error;
			}
			break;
			case UpdateEngine.ErrorCodeConstants.DOWNLOAD_PAYLOAD_VERIFICATION_ERROR: {
				statusString = R.string.download_payload_verification_error;
			}
			break;
			default:
				break;
		}
		return statusString;
	}

	public static boolean pullBootimage(String partition, String output) {
		return DD.dd(partition, output);
	}

	public static boolean extractFromZip(String zipFile, String zipPath, FileOutputStream outputStream) {
		try {
			ZipInputStream magiskZip = new ZipInputStream(new FileInputStream(zipFile));
			ZipEntry zipEntry = null;
			while ((zipEntry = magiskZip.getNextEntry()) != null) {
				if (zipEntry.getName().equals(zipPath)) {
					byte[] buffer = new byte[9000];
					int len;
					while ((len = magiskZip.read(buffer)) != -1) {
						outputStream.write(buffer, 0, len);
					}
					outputStream.close();
					return true;
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public static String getMagiskArch() {
		String cpuAbi = getSystemProperty("ro.product.cpu.abi");
		String arch = "arm";
		if (cpuAbi.contains("arm64-v8a")) arch = "arm64";
		else if (cpuAbi.contains("x86_64")) arch = "x64";
		else if (cpuAbi.contains("x86")) arch = "x86";
		return arch;
	}

	public static String getSystemProperty(String propName) {
		String line;
		BufferedReader input = null;
		try {
			Process p = Runtime.getRuntime().exec("getprop " + propName);
			input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
			line = input.readLine();
			input.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return line;
	}
}
