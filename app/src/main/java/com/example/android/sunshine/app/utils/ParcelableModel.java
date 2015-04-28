package com.example.android.sunshine.app.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.lang.reflect.Array;

/**
 * Created by tlnacl on 17/12/14.
 */
public abstract class ParcelableModel implements Parcelable {

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        Gson gson = new Gson();
        out.writeString(gson.toJson(this));
    }

    public static class ParcelableCreator<T> implements Creator<T> {
        private final Class<T> mCls;

        public ParcelableCreator(Class<T> cls) {
            mCls = cls;
        }

        @Override
        public T createFromParcel(Parcel in) {
            Gson gson = new Gson();
            return gson.fromJson(in.readString(), mCls);
        }

        @SuppressWarnings("unchecked")
        @Override
        public T[] newArray(int size) {
            return (T[]) Array.newInstance(mCls, size);
        }
    }
}

