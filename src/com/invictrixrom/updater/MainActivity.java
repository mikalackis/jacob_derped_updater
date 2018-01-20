package com.invictrixrom.updater;

import android.app.Activity;
import android.app.Notification;
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
import java.io.File;
import java.util.zip.ZipFile;

public class MainActivity extends Activity implements UpdaterListener, DeltaCallback {

	private TextView statusText;
	private Button installButton, chooseButton;
	private ProgressBar progressBar;

	private String filePath = "";

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

		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Utilities.createNotificationChannel(mNotificationManager);

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
					statusText.setText("Checking File");
					mBuilder = Utilities.buildNotification(MainActivity.this, mNotificationManager, "Installing OTA", R.drawable.ic_stat_system_update, "Checking File", true, true, true, true);
					progressBar.setIndeterminate(true);
					Toast.makeText(MainActivity.this, "PullBootImage " + ((Utilities.pullBootimage("/dev/block/sde11")) ? "Success":"Failed"), Toast.LENGTH_LONG).show();
					File cachedFile = new File(getApplicationInfo().dataDir + "/update.zip");
					if(filePath.endsWith(".delta")) {
						if(!cachedFile.exists()) {
							progressBar.setIndeterminate(false);
							progressBar.setMax(100);
							mBuilder = Utilities.finishNotification(mNotificationManager, mBuilder, "Delta Failed", "Can't use delta update without an existing zip cached.", true, R.drawable.ic_stat_error, R.drawable.ic_stat_success, true);
							statusText.setText("Can't use delta update without an existing zip cached, update with a full zip first");
						} else {
							DeltaPatcher patcher = new DeltaPatcher(cachedFile.getAbsolutePath(), filePath, cachedFile.getAbsolutePath() + "2");
							patcher.setCallback(MainActivity.this);
							mBuilder = Utilities.updateNotificationText(mNotificationManager, mBuilder, "Patching Cached Build");
							patcher.patchUpdate();
						}
					} else {
						startUpdate(filePath);
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
					statusText.setText("Chosen Zip: " + Utilities.getPath(this, uri));
					filePath = Utilities.getPath(this, uri);
				}
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void startUpdate(String updateFile) {
		File cachedFile = new File(getApplicationInfo().dataDir + "/update.zip");
		ZipFile zipFile;
		try {
			zipFile = new ZipFile(updateFile);
			boolean isABUpdate = ABUpdate.isABUpdate(zipFile);
			zipFile.close();
			if (isABUpdate) {
				mBuilder = Utilities.updateNotificationText(mNotificationManager, mBuilder, "Caching Build");
				Utilities.copyFile(new File(updateFile), cachedFile);
				ABUpdate.start(updateFile, MainActivity.this);
			} else {
				progressBar.setIndeterminate(false);
				progressBar.setMax(100);
				mBuilder = Utilities.finishNotification(mNotificationManager, mBuilder, "Update Failed", "Not an A/B Update.", true, R.drawable.ic_stat_error, R.drawable.ic_stat_success, true);
				statusText.setText("Not an A/B Update");
			}
		} catch (Exception ex) {
			mBuilder = Utilities.finishNotification(mNotificationManager, mBuilder, "Update Failed", "Zip Open Error", true, R.drawable.ic_stat_error, R.drawable.ic_stat_success, true);
			statusText.setText("Could not open zip file.");
		}
	}

	@Override
	public void updateProgress(int progress) {
		progressBar.setIndeterminate(false);
		progressBar.setMax(100);
		progressBar.setProgress(progress);
		mBuilder = Utilities.updateNotificationProgress(mNotificationManager, mBuilder, progress, false);
	}

	@Override
	public void updateStatusChange(int status) {
		String statusString = Utilities.getUpdaterStatus(status);
		statusText.setText(statusString);
		mBuilder = Utilities.updateNotificationText(mNotificationManager, mBuilder, statusString);
	}

	@Override
	public void updateComplete(int status) {
		String statusString = Utilities.getUpdaterCompleteStatus(status);
		boolean isError = true;
		if (status == UpdateEngine.ErrorCodeConstants.SUCCESS) {
			isError = false;
		}
		statusText.setText(statusString);
		progressBar.setProgress(100);
		mBuilder = Utilities.finishNotification(mNotificationManager, mBuilder, isError ? "Update Failed":"Update Success", statusString, isError, R.drawable.ic_stat_error, R.drawable.ic_stat_success, true);
	}

	@Override
	public void deltaDone(boolean success, String resultPath) {
		if(success) {
			mBuilder = Utilities.updateNotificationText(mNotificationManager, mBuilder, "Finished Patching");
			startUpdate(resultPath);
		} else {
			mBuilder = Utilities.finishNotification(mNotificationManager, mBuilder, "Patching Failed", "Error", true, R.drawable.ic_stat_error, R.drawable.ic_stat_success, true);
		}
		File resultFile = new File(resultPath);
		File cachedFile = new File(getApplicationInfo().dataDir + "/update.zip");
		cachedFile.delete();
		Utilities.copyFile(resultFile, cachedFile);
	}

}
