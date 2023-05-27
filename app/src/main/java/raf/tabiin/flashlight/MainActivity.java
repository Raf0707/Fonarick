package raf.tabiin.flashlight;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.view.View;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import raf.tabiin.flashlight.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    boolean state;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Dexter.withContext(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(new PermissionListener() {
            @Override
            public void onPermissionGranted(
                    PermissionGrantedResponse permissionGrantedResponse) {
                runFlashlight();
            }

            @Override
            public void onPermissionDenied(
                    PermissionDeniedResponse permissionDeniedResponse) {
                Snackbar.make(binding.getRoot(), 
                        "Camera permission required.", 
                        Snackbar.LENGTH_LONG);

            }

            @Override
            public void onPermissionRationaleShouldBeShown(
                    PermissionRequest permissionRequest, PermissionToken permissionToken) {}
                    
        }).check();

    }

    private void runFlashlight() {

        binding.torchbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!state)
                {
                    CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

                    try {
                        String cameraId = cameraManager.getCameraIdList()[0];
                        cameraManager.setTorchMode(cameraId, true);
                        state = true;
                        binding.torchbtn.setImageResource(R.drawable.torch_on);
                    }
                    catch (CameraAccessException e)
                    {}
                }
                else
                {
                    CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

                    try {
                        String cameraId = cameraManager.getCameraIdList()[0];
                        cameraManager.setTorchMode(cameraId, false);
                        state = false;
                        binding.torchbtn.setImageResource(R.drawable.torch_off);
                    }
                    catch (CameraAccessException e)
                    {}
                }
            }
        });
    }
}