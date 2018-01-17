package com.invictrixrom.updater;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.UpdateEngine;

import java.util.zip.ZipFile;

public class MainActivity extends Activity implements UpdaterListener {

	private TextView statusText;
	private Button installButton, chooseButton;
	private ProgressBar progressBar;

	private String filePath = "";

	private static final int notification_id = 10242048;
	private static final String channel_id = "com.invictrixrom.updater";
	private static final String channel_name = "Invictrix ROM Updater";
	private static final String channel_description = "Invictrix ROM Updater";
	private NotificationManager mNotificationManager;
	private Notification.Builder mBuilder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity);

		chooseButton = findViewById(R.id.choose_zip);
		installButton = findViewById(R.id.install);
		statusText = findViewById(R.id.status);
		progressBar = findViewById(R.id.install_progress);

		statusText.setText("Select a Zip to Update");

		chooseButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
				intent.setType("*/*");
				intent.addCategory(Intent.CATEGORY_OPENABLE);

				try {
					startActivityForResult(
						Intent.createChooser(intent, "Select a Zip to Update"),
						0);
				} catch (android.content.ActivityNotFoundException ex) {
					Toast.makeText(MainActivity.this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
				}
			}
		});

		installButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!filePath.isEmpty()) {
					statusText.setText("Checking if A/B zip");
					buildNotification();
					progressBar.setIndeterminate(true);
					ZipFile zipFile;
					try {
						zipFile = new ZipFile(filePath);
						boolean isABUpdate = ABUpdate.isABUpdate(zipFile);
						zipFile.close();
						if (isABUpdate) {
							ABUpdate.start(filePath, MainActivity.this);
						} else {
							progressBar.setIndeterminate(false);
							progressBar.setMax(100);
							finishNotification("Not an A/B Update", true);
						}
					} catch (Exception ex) {
						finishNotification("Zip Open Error", true);
						statusText.setText("Could not open zip file, make sure you're using a file explorer and not DocumentsUI.");
					}
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case 0:
				if (resultCode == RESULT_OK) {
					Uri uri = data.getData();
					statusText.setText("Chosen Zip: " + uri.getPath());
					filePath = uri.getPath();
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void progressUpdate(int progress) {
		progressBar.setProgress(progress);
		updateNotification(progress);
	}

	@Override
	public void notifyUpdateStatusChange(int status) {
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
		statusText.setText(statusString);
		updateNotification(statusString);
	}

	@Override
	public void notifyUpdateComplete(int status) {
		String statusString = "";
		boolean isError = true;
		switch (status) {
			case UpdateEngine.ErrorCodeConstants.SUCCESS: {
				statusString = "Update Success";
				isError = false;
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
		statusText.setText(statusString);
		progressBar.setProgress(100);
		finishNotification(statusString, isError);
	}

	private void buildNotification() {
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationChannel mChannel = new NotificationChannel(channel_id, channel_name, NotificationManager.IMPORTANCE_MAX);
		mChannel.setDescription(channel_description);
		mChannel.enableLights(true);
		mNotificationManager.createNotificationChannel(mChannel);

		mBuilder = new Notification.Builder(this, channel_id)
				.setSmallIcon(R.drawable.ic_stat_system_update)
				.setContentTitle("Installing OTA")
				.setContentText("Verifying Valid A/B Update")
				.setProgress(100, 0, true)
				.setOngoing(true)
				.setOnlyAlertOnce(true);

		mNotificationManager.notify(notification_id, mBuilder.build());
	}

	private int lastProgress = 0;

	private void updateNotification(String status) {
		updateNotification(status, lastProgress);
		mBuilder.setContentTitle("Installing OTA")
			.setContentText(status);

		mNotificationManager.notify(notification_id, mBuilder.build());
	}

	private void updateNotification(int progress) {
		mBuilder.setContentTitle("Installing OTA")
			.setProgress(100, progress, false);

		mNotificationManager.notify(notification_id, mBuilder.build());
	}

	private void updateNotification(String status, int progress) {
		mBuilder.setContentTitle("Installing OTA")
			.setContentText(status)
			.setProgress(100, progress, false);

		mNotificationManager.notify(notification_id, mBuilder.build());
	}

	private void finishNotification(String status, boolean error) {
		if(error) {
			mBuilder.setContentTitle("Install Error")
				.setSmallIcon(R.drawable.ic_stat_error);
		} else {
			mBuilder.setContentTitle("Install Success");
		}

		progressBar.setProgress(0);

		mBuilder.setContentText(status)
			.setProgress(0, 0, false)
			.setOngoing(false);

		mNotificationManager.notify(notification_id, mBuilder.build());
	}
}
