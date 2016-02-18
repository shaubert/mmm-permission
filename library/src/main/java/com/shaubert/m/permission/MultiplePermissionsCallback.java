package com.shaubert.m.permission;

import android.support.annotation.NonNull;

import java.util.Collection;

public interface MultiplePermissionsCallback {

    void onPermissionsResult(PermissionsRequest request,
                             @NonNull Collection<String> granted,
                             @NonNull Collection<String> denied);

}
