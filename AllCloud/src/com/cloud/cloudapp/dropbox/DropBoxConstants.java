package com.cloud.cloudapp.dropbox;

import com.dropbox.client2.session.Session.AccessType;

public class DropBoxConstants {
	public static final String OVERRIDEMSG = "File name with this name already exists.Do you want to replace this file?";
	final static public String DROPBOX_APP_KEY = "wqk9ttk0psaxgdt";
	final static public String DROPBOX_APP_SECRET = "55w5h274w1kqiml";
	public static boolean mLoggedIn = false;
	final static public AccessType ACCESS_TYPE = AccessType.DROPBOX;

	final static public String ACCOUNT_PREFS_NAME = "prefs";
	final static public String ACCESS_KEY_NAME = "ACCESS_KEY";
	final static public String ACCESS_SECRET_NAME = "ACCESS_SECRET";

}
