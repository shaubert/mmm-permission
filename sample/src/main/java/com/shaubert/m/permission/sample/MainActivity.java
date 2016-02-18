package com.shaubert.m.permission.sample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import com.shaubert.lifecycle.objects.dispatchers.support.LifecycleDispatcherAppCompatActivity;
import com.shaubert.m.permission.MultiplePermissionsCallback;
import com.shaubert.m.permission.PermissionsRequest;
import com.shaubert.m.permission.SinglePermissionCallback;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

public class MainActivity extends LifecycleDispatcherAppCompatActivity {

    private static final String[] TEST_PERMISSIONS = new String[]{
            "android.permission.ACCESS_FINE_LOCATION",
            "android.permission.READ_CONTACTS",
            "android.permission.VIBRATE",
    };
    
    private static final String[][] TEST_PERMISSIONS_GROUPS = new String[][]{
            new String[]{
                    "android.permission.ACCESS_WIFI_STATE",
                    "android.permission.CAMERA",
            },
            new String[]{
                    "android.permission.READ_SMS",
                    "android.permission.CALL_PHONE",
                    "android.permission.GET_ACCOUNTS",
            },
            new String[]{
                    "android.permission.BLUETOOTH",
                    "android.permission.READ_LOGS",
                    "android.permission.CHANGE_NETWORK_STATE",
            },
    };

    private TextView log;
    private PermissionsRequest[] permissionsRequests;

    private SinglePermissionCallback singlePermissionCallback = new SinglePermissionCallback() {
        @Override
        public void onPermissionGranted(PermissionsRequest request, String permission) {
            log.append("\n[SC] Permission Granted: " + permission);
        }

        @Override
        public void onPermissionDenied(PermissionsRequest request, String permission) {
            log.append("\n[SC] Permission Denied: " + permission);
        }
    };

    private MultiplePermissionsCallback multiplePermissionsCallback = new MultiplePermissionsCallback() {
        @Override
        public void onPermissionsResult(PermissionsRequest request, @NonNull Collection<String> granted, @NonNull Collection<String> denied) {
            log.append("\n[MC] Permissions Granted: " + Arrays.toString(granted.toArray(new String[granted.size()])));
            log.append("\n[MC] Permissions Denied: " + Arrays.toString(denied.toArray(new String[denied.size()])));
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.ab_toolbar));

        log = (TextView) findViewById(R.id.log);

        findViewById(R.id.request_permission_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestRandomPermission();
            }
        });

        createPermissionRequests();
    }

    private void createPermissionRequests() {
        permissionsRequests = new PermissionsRequest[TEST_PERMISSIONS.length + TEST_PERMISSIONS_GROUPS.length];
        int pos = 0;
        for (String permission : TEST_PERMISSIONS) {
            PermissionsRequest permissionsRequest = new PermissionsRequest(this, permission);
            permissionsRequest.setSinglePermissionCallback(singlePermissionCallback);
            permissionsRequest.setMultiplePermissionsCallback(multiplePermissionsCallback);
            attachToLifecycle(permissionsRequest);
            permissionsRequests[pos++] = permissionsRequest;
        }

        for (String permissions[] : TEST_PERMISSIONS_GROUPS) {
            PermissionsRequest permissionsRequest = new PermissionsRequest(this, permissions);
            permissionsRequest.setSinglePermissionCallback(singlePermissionCallback);
            permissionsRequest.setMultiplePermissionsCallback(multiplePermissionsCallback);
            attachToLifecycle(permissionsRequest);
            permissionsRequests[pos++] = permissionsRequest;
        }
    }

    private void requestRandomPermission() {
        permissionsRequests[new Random().nextInt(permissionsRequests.length)].request();
    }

}
