package com.shaubert.m.permission;

import android.content.pm.PackageManager;
import com.shaubert.lifecycle.objects.LifecycleBasedObject;

import java.util.*;

public class PermissionsRequest extends LifecycleBasedObject {
    private int requestCode;
    private Set<String> permissionsSet;
    private PermissionRequester permissionRequester;
    private SinglePermissionCallback singlePermissionCallback;
    private MultiplePermissionsCallback multiplePermissionsCallback;

    public PermissionsRequest(Object activityOrFragment, String ... permissions) {
        this(activityOrFragment, requestCodeFromStringArray(permissions), permissions);
    }

    public PermissionsRequest(Object activityOrFragment, int requestCode, String ... permissions) {
        this.permissionRequester = new PermissionRequester(activityOrFragment);
        this.requestCode = requestCode;

        permissionsSet = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(permissions)));
    }

    public void setSinglePermissionCallback(SinglePermissionCallback singlePermissionCallback) {
        this.singlePermissionCallback = singlePermissionCallback;
    }

    public void setMultiplePermissionsCallback(MultiplePermissionsCallback multiplePermissionsCallback) {
        this.multiplePermissionsCallback = multiplePermissionsCallback;
    }

    public String[] getNotGranted() {
        if (permissionRequester.supported()) {
            return getPermissionsToRequest();
        } else {
            return new String[0];
        }
    }

    public boolean isGranted() {
        return getNotGranted().length == 0;
    }

    public void request() {
        if (!isAttached()) {
            throw new IllegalStateException("PermissionsRequest must be attached to LifecycleDelegate");
        }

        boolean hasAllPermissions = true;
        String[] permissionsToRequest = getNotGranted();
        if (permissionsToRequest.length > 0) {
            permissionRequester.requestPermissions(permissionsToRequest, requestCode);
            hasAllPermissions = false;
        }

        if (hasAllPermissions) {
            if (singlePermissionCallback != null) {
                for (String permission : permissionsSet) {
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

    private String[] getPermissionsToRequest() {
        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissionsSet) {
            if (permissionRequester.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        return permissionsToRequest.toArray(new String[permissionsToRequest.size()]);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (singlePermissionCallback == null && multiplePermissionsCallback == null) return;

        if (requestCode == this.requestCode) {
            Set<String> granted = new HashSet<>(permissionsSet.size());
            Set<String> denied = new HashSet<>(permissionsSet.size());
            for (String permission : permissionsSet) {
                if (permissionRequester.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
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

    static int requestCodeFromStringArray(String[] permissions) {
        int hashCode = 0;
        for (String permission : permissions) {
            hashCode += permission.hashCode();
        }
        return hashCode & 0x0000ffff;
    }
}
