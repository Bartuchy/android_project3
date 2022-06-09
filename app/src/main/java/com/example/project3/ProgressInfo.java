package com.example.project3;

import android.os.Parcel;
import android.os.Parcelable;

public class ProgressInfo implements Parcelable {
    public int downloadedBytes;
    public int size;
    public String status;


    protected ProgressInfo(Parcel in) {
        downloadedBytes = in.readInt();
        size = in.readInt();
        status = in.readString();
    }

    public static final Creator<ProgressInfo> CREATOR = new Creator<ProgressInfo>() {
        @Override
        public ProgressInfo createFromParcel(Parcel in) {
            return new ProgressInfo(in);
        }

        @Override
        public ProgressInfo[] newArray(int size) {
            return new ProgressInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(downloadedBytes);
        dest.writeInt(size);
        dest.writeString(status);
    }
}
