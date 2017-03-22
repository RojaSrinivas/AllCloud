package com.cloud.cloudapp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveApi.MetadataBufferResult;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.widget.DataBufferAdapter;

/**
 * An activity illustrates how to list file results and infinitely populate the
 * results list view with data if there are more results.
 */
public class DriveListFilesActivity extends BaseDemoActivity {

	private ListView mListView;
	private DataBufferAdapter<Metadata> mResultsAdapter;
	private String mNextPageToken;
	private boolean mHasMore;

	private static final String TAG = "drive-quickstart";
	private static final int REQUEST_CODE_CAPTURE_IMAGE = 1;
	private static final int REQUEST_CODE_CREATOR = 2;
	private static final int REQUEST_CODE_RESOLUTION = 3;

	private Bitmap mBitmapToSave;

	@Override
	protected void onCreate(Bundle b) {
		super.onCreate(b);
		setContentView(R.layout.activity_drive_listfiles);

		mHasMore = true; // initial request assumes there are files results.
		findViewById(R.id.drive_upload).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mBitmapToSave == null) {
							// This activity has no UI of its own. Just start
							// the camera.
							startActivityForResult(new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE),
									REQUEST_CODE_CAPTURE_IMAGE);
							return;
						}

					}
				});
		mListView = (ListView) findViewById(R.id.listViewResults);
		mResultsAdapter = new ResultsAdapter(this);
		mListView.setAdapter(mResultsAdapter);
		mListView.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			/**
			 * Handles onScroll to retrieve next pages of results if there are
			 * more results items to display.
			 */
			@Override
			public void onScroll(AbsListView view, int first, int visible,
					int total) {
				if (mNextPageToken != null && first + visible + 5 < total) {
					retrieveNextPage();
				}
			}
		});
	}

	/**
	 * Create a new file and save it to Drive.
	 */
	private void saveFileToDrive() {
		// Start by creating a new contents, and setting a callback.
		Log.i(TAG, "Creating new contents.");
		final Bitmap image = mBitmapToSave;
		Drive.DriveApi.newDriveContents(getGoogleApiClient())
				.setResultCallback(new ResultCallback<DriveContentsResult>() {

					@Override
					public void onResult(DriveContentsResult result) {
						// If the operation was not successful, we cannot do
						// anything
						// and must
						// fail.
						if (!result.getStatus().isSuccess()) {
							Log.i(TAG, "Failed to create new contents.");
							return;
						}
						// Otherwise, we can write our data to the new contents.
						Log.i(TAG, "New contents created.");
						// Get an output stream for the contents.
						OutputStream outputStream = result.getDriveContents()
								.getOutputStream();
						// Write the bitmap data from it.
						ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
						image.compress(Bitmap.CompressFormat.PNG, 100,
								bitmapStream);
						try {
							outputStream.write(bitmapStream.toByteArray());
						} catch (IOException e1) {
							Log.i(TAG, "Unable to write file contents.");
						}
						// Create the initial metadata - MIME type and title.
						// Note that the user will be able to change the title
						// later.
						MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
								.setMimeType("image/jpeg").setTitle(".png")
								.build();
						// Create an intent for the file chooser, and start it.
						IntentSender intentSender = Drive.DriveApi
								.newCreateFileActivityBuilder()
								.setInitialMetadata(metadataChangeSet)
								.setInitialDriveContents(
										result.getDriveContents())
								.build(getGoogleApiClient());
						try {
							startIntentSenderForResult(intentSender,
									REQUEST_CODE_CREATOR, null, 0, 0, 0);
						} catch (SendIntentException e) {
							Log.i(TAG, "Failed to launch file chooser.");
						}
					}
				});
	}

	@Override
	protected void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_CAPTURE_IMAGE:
			// Called after a photo has been taken.
			if (resultCode == Activity.RESULT_OK) {
				// Store the image data as a bitmap for writing later.
				mBitmapToSave = (Bitmap) data.getExtras().get("data");
				saveFileToDrive();
			}
			break;
		case REQUEST_CODE_CREATOR:
			// Called after a file is saved to Drive.
			if (resultCode == RESULT_OK) {
				Log.i(TAG, "Image successfully saved.");
				mBitmapToSave = null;
				// Just start the camera again for another photo.
				mResultsAdapter.clear();
				mNextPageToken=null;
				mHasMore=true;
				//retrieveNextPage();
			}
			break;
		}
	}

	/**
	 * Clears the result buffer to avoid memory leaks as soon as the activity is
	 * no longer visible by the user.
	 */
	@Override
	protected void onStop() {
		super.onStop();
		// mResultsAdapter.clear();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mResultsAdapter.clear();
	}

	/**
	 * Handles the Drive service connection initialization and inits the first
	 * listing request.
	 */
	@Override
	public void onConnected(Bundle connectionHint) {
		super.onConnected(connectionHint);
		retrieveNextPage();
	}

	/**
	 * Retrieves results for the next page. For the first run, it retrieves
	 * results for the first page.
	 */
	private void retrieveNextPage() {
		// if there are no more results to retrieve,
		// return silently.
		if (!mHasMore) {
			return;
		}
		// retrieve the results for the next page.
		Query query = new Query.Builder().setPageToken(mNextPageToken).build();
		Drive.DriveApi.query(getGoogleApiClient(), query).setResultCallback(
				metadataBufferCallback);
	}

	/**
	 * Appends the retrieved results to the result buffer.
	 */
	private final ResultCallback<MetadataBufferResult> metadataBufferCallback = new ResultCallback<MetadataBufferResult>() {
		@Override
		public void onResult(MetadataBufferResult result) {
			if (!result.getStatus().isSuccess()) {
				showMessage("Problem while retrieving files");
				return;
			}
			mResultsAdapter.append(result.getMetadataBuffer());
			mNextPageToken = result.getMetadataBuffer().getNextPageToken();
			mHasMore = mNextPageToken != null;
		}
	};
}
