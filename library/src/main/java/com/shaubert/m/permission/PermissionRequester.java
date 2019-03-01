package com.shaubert.m.permission;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class PermissionRequester {
    private Activity activity;
    private Fragment supportFragment;
    private android.app.Fragment fragment;

    public PermissionRequester(Object activityOrFragment) {
        if (activityOrFragment == null) throw new NullPointerException();
        if (activityOrFragment instanceof Activity) {
            this.activity = (Activity) activityOrFragment;
        } else if (activityOrFragment instanceof Fragment) {
            this.supportFragment = (Fragment) activityOrFragment;
        } else if (activityOrFragment instanceof android.app.Fragment) {
            this.fragment = (android.app.Fragment) activityOrFragment;
        } else {
            throw new IllegalArgumentException("Must be Activity or Fragment but: " + activityOrFragment);
        }
    }

    @SuppressLint("NewApi")
    public final void requestPermissions(@NonNull String[] permissions, int requestCode) {
        if (!supported()) {
            throw new IllegalStateException("requestPermissions supported only from Android SDK 23");
        }

        if (activity != null) {
            activity.requestPermissions(permissions, requestCode);
        } else if (supportFragment != null) {
            supportFragment.requestPermissions(permissions, requestCode);
        } else {
            fragment.requestPermissions(permissions, requestCode);
        }
    }

    @SuppressLint("NewApi")
    public final int checkSelfPermission(@NonNull String permissions) {
        if (!supported()) {
            throw new IllegalStateException("checkSelfPermission supported only from Android SDK 23");
        }

        if (activity != null) {
            return activity.checkSelfPermission(permissions);
        } else if (supportFragment != null) {
            return supportFragment.getActivity().checkSelfPermission(permissions);
        } else {
            return fragment.getActivity().checkSelfPermission(permissions);
        }
    }

    public boolean supported() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

}
