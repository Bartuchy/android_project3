package com.example.project3;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class DownloadService extends IntentService {
    private static final String TAG = DownloadService.class.getSimpleName();
    private static final String NOTIFICATION_CHANNEL_ID = "com.example.project3.notification_channel";
    private static final String NOTIFICATION_CHANNEL_NAME = "com.example.project3.notification_channel";
    private static final int NOTIFICATION_ID = 1;
    private static final String DOWNLOAD_KEY = "com.example.project3.bytes_downloaded";

    public static final String ACTION_BROADCAST = "com.example.project3.broadcast";
    public static final String ACTION_DOWNLOAD_FILE = "com.example.project3.download_file";
    public static final String FILE_EXTRA = "com.example.service.project3.file";
    public static final String URL_EXTRA = "com.example.service.project3.url";
    public static final String IN_PROGRESS = "com.example.service.project3.in_progress";
    public static final String FINISHED = "com.example.service.project3.finished";


    private NotificationManager notificationManager;
    private int bytesDownloaded = 0;
    private int size = 0;
    private String status;

    public DownloadService(String name) {
        super(name);
    }

    public DownloadService() {
        super("DownloadService");
    }

    public static void runService(Context context, String parameter) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_DOWNLOAD_FILE);
        intent.putExtra(URL_EXTRA, parameter);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        prepareNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
    }

    private void prepareNotificationChannel() {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.app_name);
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name,
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private Notification createNotification() {
        Intent notificationIntent = new Intent(this, DownloadService.class);
        notificationIntent.putExtra(DOWNLOAD_KEY, bytesDownloaded);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this)
                .addParentStack(MainActivity.class)
                .addNextIntent(notificationIntent);

        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this,
                NOTIFICATION_CHANNEL_ID)
                .setContentText("Downloaded bytes: " + bytesDownloaded)
                .setContentTitle("Currently downloading")
                .setProgress(100, bytesDownloaded, false)
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setWhen(System.currentTimeMillis())
                .setPriority(Notification.PRIORITY_HIGH)
                .setOngoing(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder.setChannelId(NOTIFICATION_CHANNEL_ID);
        }
        return builder.build();

    }

    private void prepareForDownloading(Intent intent) throws IOException {
        String action = intent.getAction();

        if (ACTION_DOWNLOAD_FILE.equals(action)) {
            String url = intent.getStringExtra(URL_EXTRA);
            startDownloadingProcess(url);
        }
    }

    private void startDownloadingProcess(String url) throws IOException {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;

        URL _url = new URL(url);
        inputStream = createInputStream(_url);
        outputStream = createOutputStream(_url);
    }

    private InputStream createInputStream(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        size = connection.getContentLength();
        return new DataInputStream(connection.getInputStream());
    }

    private FileOutputStream createOutputStream(URL url) throws FileNotFoundException {
        File tempFile = new File(url.getFile());
        File outputFile = new File(
                Environment.getExternalStorageState()
                        + File.separator
                        + tempFile.getName());
        if (outputFile.exists()) {
            outputFile.delete();
        }
        return new FileOutputStream(outputFile.getPath());
    }
}
