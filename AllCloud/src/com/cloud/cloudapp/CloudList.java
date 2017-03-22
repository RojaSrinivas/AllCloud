package com.cloud.cloudapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.cloud.cloudapp.dropbox.DropboxActivity;

public class CloudList extends Activity implements OnClickListener {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.cloudlistlayout);
		findViewById(R.id.id_dropbox).setOnClickListener(this);
		findViewById(R.id.id_googledrive).setOnClickListener(this);
		findViewById(R.id.id_box).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.id_dropbox:
			startActivity(new Intent(CloudList.this, DropboxActivity.class));
			break;
		case R.id.id_googledrive:
			//Toast.makeText(CloudList.this, "Coming Soon...!", Toast.LENGTH_SHORT).show();
			//startActivity(new Intent(CloudList.this, DriveListFilesActivity.class));
			startActivity(new Intent(CloudList.this, DriveDownloadActivity.class));
			break;
		case R.id.id_box:
			//Toast.makeText(CloudList.this, "Coming Soon...!", Toast.LENGTH_SHORT).show();
			startActivity(new Intent(CloudList.this, BoxActivity.class));
			break;
	
		default:
			break;
		}
	}
}
