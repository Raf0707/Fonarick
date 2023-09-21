package raf.tabiin.flashlight;
import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import raf.tabiin.flashlight.databinding.ActivityMainBinding;
import raf.tabiin.utils.CustomTabUtil;
import raf.tabiin.utils.SharedPreferencesUtils;

public class MainActivity extends AppCompatActivity {
    boolean state;
    ActivityMainBinding binding;

    int counterOn = 0;
    int accessSettingsCounter = 0;
    private boolean promoCode = false;

    private String cameraId;
    private int brightLight;
    private boolean myPromo = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        state = SharedPreferencesUtils.getBoolean(getApplicationContext(), "stateFlash");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        App.instance.setNightMode();

        if (SharedPreferencesUtils.getBoolean(this, "useDynamicColors"))
            DynamicColors.applyToActivityIfAvailable(this);



        brightLight = SharedPreferencesUtils.getInteger(getApplicationContext(), "brightnessSlider");
        binding.brightnessSlider.setValue(brightLight);

        if (savedInstanceState != null) App.instance.setNightMode();

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

        binding.brightnessSlider.setVisibility(View.GONE);

        binding.torchbtn.setOnLongClickListener(v -> {new CustomTabUtil().openCustomTab(this,
                "https://t.me/+OoI8UWDVVm0yMDNi", R.color.black);
            return true;
        });

        binding.openSettingsBtn.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        });

        binding.brightnessSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

                CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    try {
                        String[] cameraIds = cameraManager.getCameraIdList();
                        for (String id : cameraIds) {
                            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                            Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                            if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                                cameraId = id;
                                break;
                            }
                        }
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        slider.setValue(1);
                    }

                    try {
                        if (slider.getValue() != 1.0) {
                            binding.torchbtn.setText("On");
                            cameraManager.setTorchMode(cameraId, true);
                            cameraManager.turnOnTorchWithStrengthLevel(cameraId, (int) slider.getValue());
                        } else {
                            String cameraId = cameraManager.getCameraIdList()[0];
                            cameraManager.setTorchMode(cameraId, false);
                            binding.torchbtn.setText("OFF");
                        }

                    } catch (CameraAccessException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalStateException e) {
                        slider.setValue(1);
                    }
                }
                SharedPreferencesUtils.saveInteger(getApplicationContext(), "brightnessSlider", (int) slider.getValue());
                clearCache(getApplicationContext());
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                CameraManager cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    try {
                        String[] cameraIds = cameraManager.getCameraIdList();
                        for (String id : cameraIds) {
                            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                            Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                            if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                                cameraId = id;
                                break;
                            }
                        }
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    } catch (IllegalStateException e) {
                        slider.setValue(1);
                    }

                    try {
                        if (slider.getValue() != 1.0) {
                            binding.torchbtn.setText("On");
                            cameraManager.setTorchMode(cameraId, true);
                            cameraManager.turnOnTorchWithStrengthLevel(cameraId, (int) slider.getValue());
                        } else {
                            String cameraId = cameraManager.getCameraIdList()[0];
                            cameraManager.setTorchMode(cameraId, false);
                            binding.torchbtn.setText("OFF");
                        }
                    } catch (CameraAccessException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalStateException e) {
                        slider.setValue(1);
                    }
                }
                SharedPreferencesUtils.saveInteger(getApplicationContext(), "brightnessSlider", (int) slider.getValue());
                clearCache(getApplicationContext());
            }

        });

    }

    private void runFlashlight() {

        binding.torchbtn.setOnClickListener(v -> {
            if (!state)
            {
                flashOnPromoCode();
                binding.brightnessSlider.setVisibility(View.VISIBLE);
                clearCache(getApplicationContext());
            }
            else
            {
                flashOffPromoCode();
                binding.brightnessSlider.setVisibility(View.GONE);
                clearCache(getApplicationContext());
            }
        });
    }

    public void flashLightOnAlert() {
        new MaterialAlertDialogBuilder(MainActivity.this,
                R.style.AlertDialogTheme)
                .setTitle("Включить фонарик?")
                .setMessage("Вы уверены, что хотите включить фонарик? " +
                        "Это платное приложение, количество включений и выключений ограничено " +
                        "(3 включения в день бесплатно), далее по 50 рублей за каждое включение")
                .setPositiveButton("Да", (dialogInterface, i) -> {
                    counterOn++;
                    if (counterOn >= 3) {
                        Snackbar.make(binding.getRoot(),
                                        "Лимит исчерпан. Оплатите платную подписку, чтобы включать фонарик чаще или воспользуйтесь промокодами",
                                        Snackbar.LENGTH_LONG)
                                .setAction("Оплатить", v -> new CustomTabUtil().openCustomTab(this,
                                        "https://www.donationalerts.com/r/raf0707", R.color.black))
                                .show();
                    } else {
                        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

                        try {
                            String cameraId = cameraManager.getCameraIdList()[0];
                            cameraManager.setTorchMode(cameraId, true);
                            state = true;
                            binding.torchbtn.setText("On");
                        }
                        catch (CameraAccessException e)
                        {}
                    }

                })
                .setNeutralButton("Отмена",
                        (dialogInterface, i) ->
                                dialogInterface.cancel())
                .show();
    }

    public void flashLightOffAlert() {
        new MaterialAlertDialogBuilder(MainActivity.this,
                R.style.AlertDialogTheme)
                .setTitle("Выключить фонарик?")
                .setMessage("Выключение фонарика - платная услуга. Оплатите 120 рублей, чтобы выключить фонарик или воспользуйтесь промокодами")
                .setPositiveButton("Да", (dialogInterface, i) -> {


                    Snackbar.make(binding.getRoot(),
                                    "Оплатить подписку?",
                                    Snackbar.LENGTH_LONG)
                            .setAction("Оплатить", v -> { new CustomTabUtil().openCustomTab(this,
                                    "https://www.donationalerts.com/r/raf0707", R.color.black);
                                    })
                            .show();

                })
                .setNeutralButton("Отмена",
                        (dialogInterface, i) -> {
                            dialogInterface.cancel();
                            Snackbar.make(binding.getRoot(),
                                            "Не можете оплатить подписку? Воспользуйтесь бесплатным промокодом",
                                            Snackbar.LENGTH_LONG)
                                    .setAction("Перейти к промокоду", v -> {
                                        startActivity(new Intent(getApplicationContext(), RafConsoleActivity.class));
                                    })
                                    .show();
                        })

                .show();
    }

    public void flashOffPromoCode() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
            state = false;
            SharedPreferencesUtils.saveBoolean(getApplicationContext(), "stateFlash", state);
            binding.torchbtn.setText("OFF");

        }
        catch (CameraAccessException e) {
            Snackbar.make(binding.getRoot(),
                            "Дайте разрешение на камеру (бесплатно 3 раза, затем 50 рублей за вкл/откл разрешения на камеру)",
                            Snackbar.LENGTH_LONG)
                    .setAction("в настройки (бесплатно 3 раза)", v1 -> {
                        accessSettingsCounter++;
                        if (accessSettingsCounter >= 3) {
                            new CustomTabUtil().openCustomTab(this,
                                    "https://www.donationalerts.com/r/raf0707", R.color.black);
                        } else {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    }).show();
        }

    }

    public void flashOnPromoCode() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            System.out.println(cameraManager.getCameraIdList().length);
            cameraManager.setTorchMode(cameraId, true);
            state = true;
            SharedPreferencesUtils.saveBoolean(getApplicationContext(), "stateFlash", state);
            binding.torchbtn.setText("On");

        }
        catch (CameraAccessException e)
        {}

    }

    public void flash_effect(View view) {
        Camera camera;
        Camera.Parameters params;
        long delay = 50;
        camera = Camera.open();
        params = camera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);

        camera.setParameters(params);
        camera.startPreview();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(params);
                camera.stopPreview();
                camera.release();

            }
        }, delay);



    }

    public void clearCache(Context context) {
        try {
            // Очистка внутреннего кеша приложения
            context.getCacheDir().deleteOnExit();

            // Очистка внешнего кеша приложения, если он доступен
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                context.getExternalCacheDir().deleteOnExit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}