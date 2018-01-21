package com.invictrixrom.updater;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import java.io.BufferedInputStream;
import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.io.FileInputStream;

//Sign boot.img
//Push boot.img to opposite partition

public class MagiskInstaller {

	private MagiskCallback callback;
	private String bootImagePath = "";
	private Context context;

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
		new MagiskTask(callback).execute();
	}

	public String extractMagisk(String magiskPath) {
		try {
			File outDir = new File(new File(magiskPath).getParentFile().getAbsolutePath() + "/magiskout");
			outDir.mkdir();
			File outFile = new File(outDir.getAbsolutePath() + "/magiskboot");
			FileOutputStream magiskOut = new FileOutputStream(outFile);
			Utilities.extractFromZip(magiskPath, Utilities.getMagiskArch() + "/magiskboot", magiskOut);

			outFile = new File(outDir.getAbsolutePath() + "/magiskinit");
			magiskOut = new FileOutputStream(outFile);
			Utilities.extractFromZip(magiskPath, Utilities.getMagiskArch() + "/magiskinit", magiskOut);

			outFile = new File(outDir.getAbsolutePath() + "/boot_patch.sh");
			magiskOut = new FileOutputStream(outFile);
			Utilities.extractFromZip(magiskPath, "common/boot_patch.sh", magiskOut);

			outFile = new File(outDir.getAbsolutePath() + "/magisk.apk");
			magiskOut = new FileOutputStream(outFile);
			Utilities.extractFromZip(magiskPath, "common/magisk.apk", magiskOut);

			outFile = new File(outDir.getAbsolutePath() + "/util_functions.sh");
			magiskOut = new FileOutputStream(outFile);
			Utilities.extractFromZip(magiskPath, "common/util_functions.sh", magiskOut);

			outFile = new File(outDir.getAbsolutePath() + "/update-binary");
			magiskOut = new FileOutputStream(outFile);
			Utilities.extractFromZip(magiskPath, "META-INF/com/google/android/update-binary", magiskOut);

			return outDir.getAbsolutePath();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public void installMagisk(String magiskPath) {
		Shell.runCommand("chmod 755 " + magiskPath + "/*");
		Shell.runCommand("cp " + bootImagePath + " " + magiskPath + "/boot.img");
		Shell.runCommand("cd " + magiskPath);

		boolean highcomp = false;

		boolean isSigned = false;
		try (InputStream in = new FileInputStream(new File(magiskPath + "/boot.img"))) {
			isSigned = SignBoot.verifySignature(in, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Shell.runCommand("KEEPFORCEENCRYPT=false KEEPVERITY=false HIGHCOMP=" + highcomp  + " sh " + magiskPath + "/update-binary indep " + magiskPath + "/boot_patch.sh " + bootImagePath);

		if(isSigned) {
			File signed = new File(magiskPath + "/signed.img");
			AssetManager assets = context.getAssets();
			try (
				InputStream in = new FileInputStream(magiskPath + "/new-boot.img");
				OutputStream out = new BufferedOutputStream(new FileOutputStream(signed));
				InputStream keyIn = assets.open("private.key.pk8");
				InputStream certIn = assets.open("public.certificate.x509.pem")
			) {
				SignBoot.doSignature("/boot", in, out, keyIn, certIn);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			Shell.runCommand("mv -f signed.img " + bootImagePath);
		} else {
			Shell.runCommand("mv -f new-boot.img " + bootImagePath);
		}
		Shell.closeShell();
	}

	private class MagiskTask extends AsyncTask<Void, Integer, String> {
		private MagiskCallback callback;

		public MagiskTask(MagiskCallback callback) {
			this.callback = callback;
		}

		@Override
		protected String doInBackground(Void... params) {
			int count;
			try {
				File magiskDir = new File(context.createDeviceProtectedStorageContext().getFilesDir().getParent() + "/install");
				magiskDir.mkdir();
				String out = magiskDir.getAbsolutePath() + "/magisk.zip";
				URL url = new URL("https://tiny.cc/latestmagisk");

				URLConnection connection = url.openConnection();
				connection.connect();

				long contentLength = connection.getContentLengthLong();

				InputStream input = new BufferedInputStream(url.openStream());
				OutputStream output = new FileOutputStream(out);

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

				return out;
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
		protected void onPostExecute(String outputFile) {
			callback.magiskDownloaded((new File(outputFile).exists()), outputFile);
		}

	}

}
