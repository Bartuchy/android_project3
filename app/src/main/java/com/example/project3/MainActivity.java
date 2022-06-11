package com.example.project3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 999;
    private final DownloadInfoBroadcastReceiver broadcastReceiver = new DownloadInfoBroadcastReceiver();

    private TextView addressTextView;
    private TextView fileTypeTextView;
    private TextView downloadedBytesTextView;
    private TextView fileSizeTextView;
    private TextView fileSizeEditText;
    private TextView fileTypeEditText;
    private TextView downloadedBytesEditText;
    private EditText addressEditText;
    private Button downloadInfoButton;
    private Button downloadFileButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindComponents();
        addListenerToDownloadInfoButton();
        addListenerToDownloadFileButton();
    }

    private void bindComponents() {
        addressTextView = findViewById(R.id.addressTextView);
        fileTypeTextView = findViewById(R.id.fileTypeTextView);
        downloadedBytesTextView = findViewById(R.id.downloadedBytesTextView);
        fileSizeTextView = findViewById(R.id.fileSizeTextView);
        addressEditText = findViewById(R.id.addressEditText);
        fileTypeEditText = findViewById(R.id.fileTypeEditText);
        fileSizeEditText = findViewById(R.id.fileSizeEditText);
        downloadedBytesEditText = findViewById(R.id.downloadedBytesEditText);
        downloadInfoButton = findViewById(R.id.downloadInfoButton);
        downloadFileButton = findViewById(R.id.downloadFileButton);
        progressBar = findViewById(R.id.progressBar);
    }

    private void addListenerToDownloadInfoButton() {
        downloadInfoButton.setOnClickListener(view -> {
            FileInfoReceiverTask fileInfoTask = new FileInfoReceiverTask();
            fileInfoTask.execute(addressEditText.getText().toString());
        });
    }

    private void addListenerToDownloadFileButton() {
        downloadFileButton.setOnClickListener(this::startDownloadProcess);

    }

    private void startDownloadProcess(View view) {
        Log.d(TAG, "Preparing to download file");

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            DownloadService.runService(this, addressEditText.getText().toString());
            ;
            Log.i(TAG, "Started downloading file");
        } else {
            askForPermission();
        }
    }

    private void askForPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Accept the permission", Toast.LENGTH_LONG).show();
        }

        ActivityCompat.requestPermissions(this,
                new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE },
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            checkPermissions(permissions, grantResults);
        } else {
            throw new UnsupportedOperationException("Unknown request code");
        }
    }

    private void checkPermissions( @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions.length > 0
                && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadFile(permissions, grantResults);
        } else {
            Log.i(TAG,"Permissions not granted");
        }
    }

    private void downloadFile(@NonNull String[] permissions, @NonNull int[] grantResults) {
        DownloadService.runService(this, addressEditText.getText().toString());
        Log.i(TAG, "Service working");
    }

    class FileInfoReceiverTask extends AsyncTask<String, Void, FileInfo> {
        @Override
        protected FileInfo doInBackground(String... strings) {
            HttpsURLConnection urlConnection = null;
            try {
                URL url = new URL(strings[0]);
                urlConnection = (HttpsURLConnection) url.openConnection();
                return new FileInfo(urlConnection.getContentLength(), urlConnection.getContentType());

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(FileInfo fileInfo) {
            fileSizeEditText.setText(String.valueOf(fileInfo.getSize()));
            fileTypeEditText.setText(fileInfo.getType());
        }
    }

    class DownloadInfoBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            ProgressInfo progressInfo = intent.getParcelableExtra(DownloadService.FILE_EXTRA);

            if (DownloadService.IN_PROGRESS.equals(progressInfo.status)) {
                downloadedBytesEditText.setText(String.valueOf(progressInfo.downloadedBytes));
                progressBar.setMax(progressInfo.size);
                progressBar.setProgress(progressInfo.downloadedBytes);
            }

//            if (DownloadService.FINISHED.equals(progressInfo.status)) {
//
//            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(broadcastReceiver, new IntentFilter(DownloadService.ACTION_BROADCAST));
    }

    @Override
    protected void onStop() {
        unregisterReceiver(broadcastReceiver);
        super.onStop();
    }
}