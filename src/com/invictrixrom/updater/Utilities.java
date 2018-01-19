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

		if(progress) {
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

	public static Notification.Builder finishNotification(NotificationManager mNotificationManager, Notification.Builder mBuilder, String title, String status, boolean error, int errorIconRes, int successIconRes, boolean progressFinished) {
		mBuilder = mBuilder.setSmallIcon(error ? errorIconRes:successIconRes)
				.setContentText(status)
				.setContentTitle(title)
				.setOngoing(false);

		if(progressFinished) {
			mBuilder = mBuilder.setProgress(0, 0, false);
		}

		mNotificationManager.notify(notification_id, mBuilder.build());

		return mBuilder;
	}

	public static boolean copyFile(File src, File dst) {
		if(src.getAbsolutePath().toString().equals(dst.getAbsolutePath().toString())) {
			return true;
		} else {
			try {
				InputStream is = new FileInputStream(src);
				OutputStream os = new FileOutputStream(dst);
				byte[] buff = new byte[1024];
				int len;
				while((len = is.read(buff)) > 0) {
					os.write(buff, 0, len);
				}
				is.close();
				os.close();
			} catch(Exception ex) {
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
				final String[] selectionArgs = new String[] { split[1] };

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
		final String[] projection = { column };

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

	public static String getUpdaterStatus(int status) {
		String statusString = "";
		switch (status) {
			case UpdateEngine.UpdateStatusConstants.IDLE: {
				statusString = "Idle";
			}
				break;
			case UpdateEngine.UpdateStatusConstants.CHECKING_FOR_UPDATE: {
				statusString = "Checking For Update";
			}
				break;
			case UpdateEngine.UpdateStatusConstants.UPDATE_AVAILABLE: {
				statusString = "Update Available";
			}
				break;
			case UpdateEngine.UpdateStatusConstants.DOWNLOADING: {
				statusString = "Downloading";
			}
				break;
			case UpdateEngine.UpdateStatusConstants.VERIFYING: {
				statusString = "Verifying";
			}
				break;
			case UpdateEngine.UpdateStatusConstants.FINALIZING: {
				statusString = "Finalizing";
			}
				break;
			case UpdateEngine.UpdateStatusConstants.UPDATED_NEED_REBOOT: {
				statusString = "Updated. Needs Reboot";
			}
				break;
			case UpdateEngine.UpdateStatusConstants.REPORTING_ERROR_EVENT: {
				statusString = "Error";
			}
				break;
			case UpdateEngine.UpdateStatusConstants.ATTEMPTING_ROLLBACK: {
				statusString = "Attempting Rollback";
			}
				break;
			case UpdateEngine.UpdateStatusConstants.DISABLED: {
				statusString = "Disabled";
			}
				break;
			default:
				break;
		}
		return statusString;
	}

	public static String getUpdaterCompleteStatus(int status) {
		String statusString = "";
		switch (status) {
			case UpdateEngine.ErrorCodeConstants.SUCCESS: {
				statusString = "Update Success";
			}
				break;
			case UpdateEngine.ErrorCodeConstants.ERROR: {
				statusString = "Update Error";
			}
				break;
			case UpdateEngine.ErrorCodeConstants.FILESYSTEM_COPIER_ERROR: {
				statusString = "Filesystem Error";
			}
				break;
			case UpdateEngine.ErrorCodeConstants.POST_INSTALL_RUNNER_ERROR: {
				statusString = "Post Install Error";
			}
				break;
			case UpdateEngine.ErrorCodeConstants.PAYLOAD_MISMATCHED_TYPE_ERROR: {
				statusString = "Payload Mismatched Type";
			}
				break;
			case UpdateEngine.ErrorCodeConstants.INSTALL_DEVICE_OPEN_ERROR: {
				statusString = "Install Device Already Open";
			}
				break;
			case UpdateEngine.ErrorCodeConstants.KERNEL_DEVICE_OPEN_ERROR: {
				statusString = "Kernel Device Already Open";
			}
				break;
			case UpdateEngine.ErrorCodeConstants.DOWNLOAD_TRANSFER_ERROR: {
				statusString = "Download Transfer Error";
			}
				break;
			case UpdateEngine.ErrorCodeConstants.PAYLOAD_HASH_MISMATCH_ERROR: {
				statusString = "Payload Hash Error";
			}
				break;
			case UpdateEngine.ErrorCodeConstants.PAYLOAD_SIZE_MISMATCH_ERROR: {
				statusString = "Payload Size Mismatch Error";
			}
				break;
			case UpdateEngine.ErrorCodeConstants.DOWNLOAD_PAYLOAD_VERIFICATION_ERROR: {
				statusString = "Download Payload Verification Error";
			}
				break;
			default:
				break;
		}
		return statusString;
	}
}
