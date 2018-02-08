package com.invictrixrom.updater;

import android.content.res.AssetManager;
import android.os.AsyncTask;

import java.io.BufferedInputStream;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.io.FileInputStream;

public class MagiskInstaller {

	private MagiskCallback callback;
	private String bootImagePath = "";
	private Context context;
	private String magiskZipPath = "";
	private String magiskInstallPath = "";
	private String magiskStockBootPath = "";
	private String magiskBootPath = "";
	private String magiskSignedBootPath = "";

	public void setBootImagePath(String bootImagePath) {
		this.bootImagePath = bootImagePath;
	}

	public void setCallback(MagiskCallback callback) {
		this.callback = callback;
	}

	public MagiskInstaller(Context context) {
		this.context = context;
	}

	public void startDownload() {
		new MagiskDownloadTask(callback).execute();
	}

	public void installMagisk(boolean postInstall) {
		new MagiskInstallTask(this.callback, postInstall).execute();
	}

	private class MagiskDownloadTask extends AsyncTask<Void, Integer, Void> {
		private MagiskCallback callback;

		public MagiskDownloadTask(MagiskCallback callback) {
			this.callback = callback;
		}

		@Override
		protected Void doInBackground(Void... params) {
			int count;
			try {
				magiskInstallPath = context.createDeviceProtectedStorageContext().getFilesDir().getParent() + "/install";
				File magiskDir = new File(magiskInstallPath);
				magiskDir.mkdir();

				magiskZipPath = magiskInstallPath + "/magisk.zip";

				URL latestMagiskUrl = new URL("https://tiny.cc/latestmagisk");

				URLConnection connection = latestMagiskUrl.openConnection();
				connection.connect();

				long contentLength = connection.getContentLengthLong();

				InputStream input = new BufferedInputStream(latestMagiskUrl.openStream());
				OutputStream output = new FileOutputStream(magiskZipPath);

				byte data[] = new byte[1024];
				long total = 0;
				while ((count = input.read(data)) != -1) {
					total += count;
					output.write(data, 0, count);
					publishProgress((int) (((float) total / (float) contentLength) * 100));
				}

				output.flush();
				output.close();
				input.close();

			} catch (Exception e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(Integer... progress) {
			callback.magiskDownloadProgress(progress[0]);
		}

		@Override
		protected void onPostExecute(Void result) {
			callback.magiskDownloaded((new File(magiskZipPath).exists()));
		}

	}

	private class MagiskInstallTask extends AsyncTask<Void, Integer, Enum> {
		private MagiskCallback callback;
		private boolean postInstall;

		public MagiskInstallTask(MagiskCallback callback, boolean postInstall) {
			this.callback = callback;
			this.postInstall = postInstall;
		}

		private boolean extractMagisk() {
			try {
				String magiskArch = Utilities.getMagiskArch();

				File outFile = new File(magiskInstallPath + "/magiskboot");
				FileOutputStream magiskOut = new FileOutputStream(outFile);
				Utilities.extractFromZip(magiskZipPath, magiskArch + "/magiskboot", magiskOut);

				outFile = new File(magiskInstallPath + "/magiskinit");
				magiskOut = new FileOutputStream(outFile);
				Utilities.extractFromZip(magiskZipPath, magiskArch + "/magiskinit", magiskOut);

				outFile = new File(magiskInstallPath + "/boot_patch.sh");
				magiskOut = new FileOutputStream(outFile);
				Utilities.extractFromZip(magiskZipPath, "common/boot_patch.sh", magiskOut);

				outFile = new File(magiskInstallPath + "/magisk.apk");
				magiskOut = new FileOutputStream(outFile);
				Utilities.extractFromZip(magiskZipPath, "common/magisk.apk", magiskOut);

				outFile = new File(magiskInstallPath + "/util_functions.sh");
				magiskOut = new FileOutputStream(outFile);
				Utilities.extractFromZip(magiskZipPath, "common/util_functions.sh", magiskOut);

				outFile = new File(magiskInstallPath + "/update_binary");
				magiskOut = new FileOutputStream(outFile);
				Utilities.extractFromZip(magiskZipPath, "META-INF/com/google/android/update-binary", magiskOut);

			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
			return true;
		}

		private MagiskInstallCodes modBootImage() {
			Shell.runCommand("chmod 755 \"" + magiskInstallPath + "/*\"");
			magiskStockBootPath = magiskInstallPath + "/boot.img";
			magiskBootPath = magiskInstallPath + "/new-boot.img";
			magiskSignedBootPath = magiskInstallPath + "/signed.img";

			Shell.runCommand("cp \"" + bootImagePath + "\" \"" + magiskStockBootPath + "\"");
			Shell.runCommand("cd \"" + magiskInstallPath + "\"");
			if(!new File(magiskStockBootPath).exists()) {
				return MagiskInstallCodes.BOOT_IMAGE_COPY_FAILED;
			}

			boolean highcomp = false;

			Shell.runCommand("KEEPFORCEENCRYPT=false KEEPVERITY=false HIGHCOMP=" + highcomp + " sh \"" + magiskInstallPath + "/update-binary\" indep \"" + magiskInstallPath + "/boot_patch.sh\" \"" + magiskStockBootPath + "\"");
			if(!new File(magiskBootPath).exists()) {
				return MagiskInstallCodes.MODIFYING_FAILED;
			}
			return MagiskInstallCodes.SUCCESS;
		}

		private boolean signBootImage() {
			AssetManager assets = context.getAssets();
			try (
				InputStream in = new FileInputStream(magiskBootPath);
				OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(magiskSignedBootPath)));
				InputStream keyIn = assets.open("private.key.pk8");
				InputStream certIn = assets.open("public.certificate.x509.pem")
			) {
				SignBoot.doSignature("/boot", in, out, keyIn, certIn);
			} catch (Exception ex) {
				ex.printStackTrace();
				return false;
			}
			return true;
		}

		private boolean flashBoot() {
			String currentSlot = Utilities.getSystemProperty(context.getString(R.string.slot_prop));
			if (postInstall) {
				if (currentSlot.equals("_b")) {
					currentSlot = "_a";
				} else {
					currentSlot = "_b";
				}
			}
			Utilities.pullBootimage(bootImagePath, context.getString(R.string.boot_block_name) + currentSlot);
			//No real way to verify yet
			return true;
		}

		@Override
		protected Enum doInBackground(Void... params) {
			publishProgress(R.string.extracting_magisk);
			if(!extractMagisk()) {
				return MagiskInstallCodes.EXTRACT_FAILED;
			}

			publishProgress(R.string.modifying_boot_image);
			MagiskInstallCodes modBootImageRet = modBootImage();
			if(modBootImageRet != MagiskInstallCodes.SUCCESS) {
				return modBootImageRet;
			}

			boolean isSigned = false;
			try (InputStream in = new FileInputStream(new File(magiskStockBootPath))) {
				isSigned = SignBoot.verifySignature(in, null);
			} catch (Exception e) {
				e.printStackTrace();
				return MagiskInstallCodes.SIGNING_FAILED;
			}

			if (isSigned) {
				publishProgress(R.string.signing_boot_image);
				if(!signBootImage()) {
					return MagiskInstallCodes.SIGNING_FAILED;
				}

				Shell.runCommand("mv -f \"" + magiskSignedBootPath + "\" \"" + bootImagePath + "\"");
			} else {
				Shell.runCommand("mv -f \"" + magiskBootPath + " \"" + bootImagePath + "\"");
			}
			Shell.closeShell();

			publishProgress(R.string.installing_boot_image);
			return (flashBoot()) ? MagiskInstallCodes.SUCCESS:MagiskInstallCodes.INSTALLING_FAILED;
		}

		@Override
		protected void onProgressUpdate(Integer... status) {
			callback.magiskInstallStatusUpdate(status[0]);
		}

		@Override
		protected void onPostExecute(Enum retCode) {
			callback.magiskInstallComplete((MagiskInstallCodes) retCode);
		}

	}

	public enum MagiskInstallCodes {
		SUCCESS,
		EXTRACT_FAILED,
		BOOT_IMAGE_COPY_FAILED,
		MODIFYING_FAILED,
		SIGNING_FAILED,
		INSTALLING_FAILED
	}

}
