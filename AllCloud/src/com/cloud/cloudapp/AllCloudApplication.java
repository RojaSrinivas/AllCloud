package com.cloud.cloudapp;

import com.box.boxandroidlibv2.BoxAndroidClient;
import com.box.boxandroidlibv2.jsonparsing.AndroidBoxResourceHub;
import com.box.boxjavalibv2.jsonparsing.BoxJSONParser;
import com.box.boxjavalibv2.jsonparsing.IBoxJSONParser;
import com.box.boxjavalibv2.jsonparsing.IBoxResourceHub;

import android.app.Application;

public class AllCloudApplication extends Application {
    public static final String CLIENT_ID = "to16ir40kzfixqi1we0gkbvikyv0zkdy";
    public static final String CLIENT_SECRET = "RT1jfSJ29X1bwWbAadUVDL4gclc9aSlb";
    public static final String REDIRECT_URL = "https://www.google.com";

    private BoxAndroidClient mClient;

    public void setClient(BoxAndroidClient client) {
        this.mClient = client;
    }

    /**
     * Gets the BoxAndroidClient for this app.
     * 
     * @return a singleton instance of BoxAndroidClient.
     */
    public BoxAndroidClient getClient() {
        return mClient;
    }

    public IBoxJSONParser getJSONParser() {
        return new BoxJSONParser(getResourceHub());
    }

    public IBoxResourceHub getResourceHub() {
        return new AndroidBoxResourceHub();
    }
}
