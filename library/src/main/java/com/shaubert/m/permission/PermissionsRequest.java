package com.shaubert.m.permission;

import android.content.pm.PackageManager;
import com.shaubert.lifecycle.objects.LifecycleBasedObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PermissionsRequest extends LifecycleBasedObject {
    private int requestCode;
    private String[] permissions;
    private Set<String> permissionsSet;
    private PermissionRequester permissionRequester;
    private SinglePermissionCallback singlePermissionCallback;
    private MultiplePermissionsCallback multiplePermissionsCallback;

    public PermissionsRequest(Object activityOrFragment, String ... permissions) {
        this(activityOrFragment, requestCodeFromStringArray(permissions), permissions);
    }

    public PermissionsRequest(Object activityOrFragment, int requestCode, String ... permissions) {
        this.permissionRequester = new PermissionRequester(activityOrFragment);
        this.permissions = permissions;
        this.requestCode = requestCode;

        permissionsSet = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(permissions)));
    }

    public void setSinglePermissionCallback(SinglePermissionCallback singlePermissionCallback) {
        this.singlePermissionCallback = singlePermissionCallback;
    }

    public void setMultiplePermissionsCallback(MultiplePermissionsCallback multiplePermissionsCallback) {
        this.multiplePermissionsCallback = multiplePermissionsCallback;
    }

    public void request() {
        if (!isAttached()) {
            throw new IllegalStateException("PermissionsRequest must be attached to LifecycleDelegate");
        }

        if (permissionRequester.supported()) {
            permissionRequester.requestPermissions(permissions, requestCode);
        } else {
            if (singlePermissionCallback != null) {
                for (String permission : permissions) {
                    singlePermissionCallback.onPermissionGranted(this, permission);
                }
            }

            if (multiplePermissionsCallback != null) {
                multiplePermissionsCallback.onPermissionsResult(this,
                        permissionsSet,
                        Collections.<String>emptySet());
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (singlePermissionCallback == null && multiplePermissionsCallback == null) return;

        if (requestCode == this.requestCode
                && equalElements(permissionsSet, permissions)
                && grantResults != null) {

            Set<String> granted = new HashSet<>(permissions.length);
            Set<String> denied = new HashSet<>(permissions.length);
            int minSize = Math.min(permissions.length, grantResults.length);
            for (int i = 0; i < minSize; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    granted.add(permission);
                    if (singlePermissionCallback != null) {
                        singlePermissionCallback.onPermissionGranted(this, permission);
                    }
                } else {
                    denied.add(permission);
                    if (singlePermissionCallback != null) {
                        singlePermissionCallback.onPermissionDenied(this, permission);
                    }
                }
            }

            if (multiplePermissionsCallback != null) {
                multiplePermissionsCallback.onPermissionsResult(this, granted, denied);
            }
        }
    }

    static boolean equalElements(Set<String> set, String[] arr) {
        if (arr == null || arr.length == 0) {
            return set == null || set.isEmpty();
        }
        if (arr.length != set.size()) {
            return false;
        }

        for (String el : arr) {
            if (!set.contains(el)) {
                return false;
            }
        }

        return true;
    }

    static int requestCodeFromStringArray(String[] permissions) {
        int hashCode = 0;
        for (String permission : permissions) {
            hashCode += permission.hashCode();
        }
        return hashCode & 0x0000ffff;
    }
}
