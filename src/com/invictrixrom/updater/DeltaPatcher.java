package com.invictrixrom.updater;

import android.os.AsyncTask;

public class DeltaPatcher {

	private String oldUpdate, deltaFile, outPath;
	private DeltaCallback callback;

	public DeltaPatcher(String oldUpdate, String deltaFile, String outPath) {
		this.oldUpdate = oldUpdate;
		this.deltaFile = deltaFile;
		this.outPath = outPath;
	}

	public void setCallback(DeltaCallback callback) {
		this.callback = callback;
	}

        public void patchUpdate() {
		new PatchTask().execute();
	}

	private class PatchTask extends AsyncTask<Void, Void, Boolean> {
     		protected Boolean doInBackground(Void... params) {
			Delta.patch(DeltaPatcher.this.oldUpdate, DeltaPatcher.this.deltaFile, DeltaPatcher.this.outPath);
			return false;
     		}

		protected void onPostExecute(Boolean success) {
			DeltaPatcher.this.callback.deltaDone(success, DeltaPatcher.this.outPath);
		}
	}
}
