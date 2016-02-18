package com.shaubert.m.permission;

public interface SinglePermissionCallback {

    void onPermissionGranted(PermissionsRequest request, String permission);

    void onPermissionDenied(PermissionsRequest request, String permission);

}
