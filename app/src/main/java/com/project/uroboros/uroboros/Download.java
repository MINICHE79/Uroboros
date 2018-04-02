package com.project.uroboros.uroboros;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

/**
 * Created by INSPIRON on 21/02/2018.
 */

public class Download{
    Activity activity = null;
    String title = null;
    String[] split = null;
    public Download(Activity activity, String title){
        this.activity = activity;
        this.title = title;
    }
    public boolean downloadFile(String URL) {
        File direct = new File(Environment.getExternalStorageDirectory()
                + "/Uroboros/" + title);

        split = URL.split("/");

        if (!direct.exists()) {
            direct.mkdirs();
        }

        DownloadManager mgr = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(URL);
        DownloadManager.Request request = new DownloadManager.Request(
                downloadUri);

        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle(title)
                .setDescription("4Chan Download")
                .setDestinationInExternalPublicDir("/Uroboros/"+title, split[4]);

        mgr.enqueue(request);
        return true;
    }
}
