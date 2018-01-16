package com.invictrixrom.updater;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.UpdateEngine;

import java.util.zip.ZipFile;

public class MainActivity extends Activity implements UpdaterListener {

    TextView statusText, progressText;
    Button installButton, chooseButton;

    String filePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        chooseButton = findViewById(R.id.choose_zip);
        installButton = findViewById(R.id.install);
        statusText = findViewById(R.id.status);
        progressText = findViewById(R.id.progress_text);

        statusText.setText("Select a Zip to Update");
        progressText.setText("Waiting...");

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
                    ZipFile zipFile;
                    try {
                        zipFile = new ZipFile(filePath);
                        boolean isABUpdate = ABUpdate.isABUpdate(zipFile);
                        zipFile.close();
                        if (isABUpdate) {
                            ABUpdate.start(filePath, MainActivity.this);
                        }
                    } catch (Exception ex) {
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
        progressText.setText("Installing Update: " + progress + "%");
    }

    @Override
    public void notifyUpdateStatusChange(int status) {
        switch (status) {
            case UpdateEngine.UpdateStatusConstants.IDLE: {
                statusText.setText("Status: Idle");
            }
                break;
            case UpdateEngine.UpdateStatusConstants.CHECKING_FOR_UPDATE: {
                statusText.setText("Status: Checking For Update");
            }
                break;
            case UpdateEngine.UpdateStatusConstants.UPDATE_AVAILABLE: {
                statusText.setText("Status: Update Available");
            }
                break;
            case UpdateEngine.UpdateStatusConstants.DOWNLOADING: {
                statusText.setText("Status: Downloading");
            }
                break;
            case UpdateEngine.UpdateStatusConstants.VERIFYING: {
                statusText.setText("Status: Verifying");
            }
                break;
            case UpdateEngine.UpdateStatusConstants.FINALIZING: {
                statusText.setText("Status: Finalizing");
            }
                break;
            case UpdateEngine.UpdateStatusConstants.UPDATED_NEED_REBOOT: {
                statusText.setText("Status: Updated. Needs Reboot");
            }
                break;
            case UpdateEngine.UpdateStatusConstants.REPORTING_ERROR_EVENT: {
                statusText.setText("Status: Error");
            }
                break;
            case UpdateEngine.UpdateStatusConstants.ATTEMPTING_ROLLBACK: {
                statusText.setText("Status: Attempting Rollback");
            }
                break;
            case UpdateEngine.UpdateStatusConstants.DISABLED: {
                statusText.setText("Status: Disabled");
            }
                break;
            default:
                break;
        }
    }

    @Override
    public void notifyUpdateComplete(int status) {
        switch (status) {
            case UpdateEngine.ErrorCodeConstants.SUCCESS: {
                statusText.setText("Status: Update Success");
            }
                break;
            case UpdateEngine.ErrorCodeConstants.ERROR: {
                statusText.setText("Status: Update Error");
            }
                break;
            case UpdateEngine.ErrorCodeConstants.FILESYSTEM_COPIER_ERROR: {
                statusText.setText("Status: Filesystem Error");
            }
                break;
            case UpdateEngine.ErrorCodeConstants.POST_INSTALL_RUNNER_ERROR: {
                statusText.setText("Status: Post Install Error");
            }
                break;
            case UpdateEngine.ErrorCodeConstants.PAYLOAD_MISMATCHED_TYPE_ERROR: {
                statusText.setText("Status: Payload Mismatched Type");
            }
                break;
            case UpdateEngine.ErrorCodeConstants.INSTALL_DEVICE_OPEN_ERROR: {
                statusText.setText("Status: Install Device Already Open");
            }
                break;
            case UpdateEngine.ErrorCodeConstants.KERNEL_DEVICE_OPEN_ERROR: {
                statusText.setText("Status: Kernel Device Already Open");
            }
                break;
            case UpdateEngine.ErrorCodeConstants.DOWNLOAD_TRANSFER_ERROR: {
                statusText.setText("Status: Download Transfer Error");
            }
                break;
            case UpdateEngine.ErrorCodeConstants.PAYLOAD_HASH_MISMATCH_ERROR: {
                statusText.setText("Status: Payload Hash Error");
            }
                break;
            case UpdateEngine.ErrorCodeConstants.PAYLOAD_SIZE_MISMATCH_ERROR: {
                statusText.setText("Status: Payload Size Mismatch Error");
            }
                break;
            case UpdateEngine.ErrorCodeConstants.DOWNLOAD_PAYLOAD_VERIFICATION_ERROR: {
                statusText.setText("Status: Download Payload Verification Error");
            }
                break;
            default:
                break;
        }
    }
}
