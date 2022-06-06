package com.example.project3;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

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
}