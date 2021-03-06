package com.cloud.cloudapp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.ProgressBar;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFile.DownloadProgressListener;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;

/**
 * An activity to illustrate how to open contents and listen the download
 * progress if the file is not already sync'ed.
 */
public class DriveDownloadActivity extends BaseDemoActivity {

	private static final String TAG = "DriveDownloadActivity";

	/**
	 * Request code to handle the result from file opening activity.
	 */
	private static final int REQUEST_CODE_OPENER = 1;

	/**
	 * Progress bar to show the current download progress of the file.
	 */
	private ProgressBar mProgressBar;

	/**
	 * File that is selected with the open file activity.
	 */
	private DriveId mSelectedFileDriveId;

	@Override
	protected void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.activity_progress);
		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
		mProgressBar.setMax(100);
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		super.onConnected(connectionHint);

		// If there is a selected file, open its contents.
		if (mSelectedFileDriveId != null) {
			open();
			return;
		}

		// Let the user pick an mp4 or a jpeg file if there are
		// no files selected by the user.
		IntentSender intentSender = Drive.DriveApi
				.newOpenFileActivityBuilder()
				.setMimeType(
						new String[] { "video/mp4", "image/jpeg", "image/png",
								"application/pdf", "application/excel",
								"application/mspowerpoint",
								"application/msword", "application/zip",
								"application/vnd.android.package-archive" })
				.build(getGoogleApiClient());
		try {
			startIntentSenderForResult(intentSender, REQUEST_CODE_OPENER, null,
					0, 0, 0);
		} catch (SendIntentException e) {
			Log.w(TAG, "Unable to send intent", e);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_OPENER && resultCode == RESULT_OK) {
			mSelectedFileDriveId = (DriveId) data
					.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
			Log.v(TAG, "DRIVEID ==>> "+mSelectedFileDriveId);
		} else {
			super.onActivityResult(requestCode, resultCode, data);
			finish();
		}
	}

	private void open() {
		// Reset progress dialog back to zero as we're
		// initiating an opening request.
		mProgressBar.setProgress(0);
		DownloadProgressListener listener = new DownloadProgressListener() {
			@Override
			public void onProgress(long bytesDownloaded, long bytesExpected) {
				// Update progress dialog with the latest progress.
				int progress = (int) (bytesDownloaded * 100 / bytesExpected);
				Log.d(TAG,
						String.format("Loading progress: %d percent", progress));
				mProgressBar.setProgress(progress);
			}
		};
		Drive.DriveApi
				.getFile(getGoogleApiClient(), mSelectedFileDriveId)
				.open(getGoogleApiClient(), DriveFile.MODE_READ_WRITE, listener)
				.setResultCallback(driveContentsCallback);
		mSelectedFileDriveId = null;
	}

	private ResultCallback<DriveContentsResult> driveContentsCallback = new ResultCallback<DriveContentsResult>() {
		@Override
		public void onResult(DriveContentsResult result) {

			if (!result.getStatus().isSuccess()) {
				showMessage("Error while opening the file contents");
				return;
			}
			
			DriveContents driveContents = result.getDriveContents();
			try {
			    ParcelFileDescriptor parcelFileDescriptor = driveContents.getParcelFileDescriptor();
			    FileInputStream fileInputStream = new FileInputStream(parcelFileDescriptor
			        .getFileDescriptor());
			    // Read to the end of the file.
			    fileInputStream.read(new byte[fileInputStream.available()]);

			    // Append to the file.
			    FileOutputStream fileOutputStream = new FileOutputStream(parcelFileDescriptor
			        .getFileDescriptor());
			    Writer writer = new OutputStreamWriter(fileOutputStream);
			    writer.write("hello world");
			} catch (IOException e) {
			    e.printStackTrace();
			}
			/*showMessage("File downloaded");*/
			finish();
	
		}
	};
}
