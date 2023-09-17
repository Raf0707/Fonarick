package raf.tabiin.flashlight;

import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.View;
import android.widget.CompoundButton;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.HashMap;
import java.util.Objects;

import raf.tabiin.flashlight.databinding.ActivitySettingsBinding;
import raf.tabiin.utils.CustomTabUtil;
import raf.tabiin.utils.SharedPreferencesUtils;

public class SettingsActivity extends AppCompatActivity {
    ActivitySettingsBinding b;
    private SwitchMaterial switchMaterial;
    private boolean promoCode = false;
    private boolean myPromo = false;
    private String cameraId;
    private Vibrator vibrator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());
        //Objects.requireNonNull(getActionBar()).hide();

        Intent intent = getIntent();
        promoCode = intent.getBooleanExtra("promoCode", false);

        myPromo = promoCode;
        SharedPreferencesUtils.saveBoolean(getApplicationContext(), "promo", myPromo);

        switchMaterial = b.dynamicColorsSwitch;
        vibrator = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);
        b.appThemeRadioGroup.check(SharedPreferencesUtils.getInteger(getApplicationContext(), "checkedButton", R.id.setFollowSystemTheme));
        b.dynamicColorsSwitch.setEnabled(DynamicColors.isDynamicColorAvailable());
        switchMaterial.setChecked(SharedPreferencesUtils.getBoolean(getApplicationContext(), "useDynamicColors"));
        b.brightnessSwitch.setChecked(SharedPreferencesUtils.getBoolean(getApplicationContext(), "brightnessSwitch"));
        b.brightnessSlider.setValue(SharedPreferencesUtils.getInteger(getApplicationContext(), "brightnessSlider", 1));

        b.appThemeRadioGroup.check(SharedPreferencesUtils.getInteger(getApplicationContext(), "checkedButton", R.id.setFollowSystemTheme));
        promoCode = SharedPreferencesUtils.getBoolean(getApplicationContext(), "promo", false);

        if (b.brightnessSwitch.isChecked()) b.brightnessSlider.setVisibility(View.VISIBLE);
        else b.brightnessSlider.setVisibility(View.GONE);

        b.appThemeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.setFollowSystemTheme:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                    SharedPreferencesUtils.saveInteger(getApplicationContext(), "checkedButton", R.id.setFollowSystemTheme);
                    SharedPreferencesUtils.saveInteger(getApplicationContext(), "nightMode", 0);
                    this.recreate();
                    break;
                case R.id.setLightTheme:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    SharedPreferencesUtils.saveInteger(getApplicationContext(), "checkedButton", R.id.setLightTheme);
                    SharedPreferencesUtils.saveInteger(getApplicationContext(), "nightMode", 1);
                    this.recreate();
                    break;
                case R.id.setNightTheme:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    SharedPreferencesUtils.saveInteger(getApplicationContext(), "checkedButton", R.id.setNightTheme);
                    SharedPreferencesUtils.saveInteger(getApplicationContext(), "nightMode", 2);
                    this.recreate();
                    break;
            }
        });


        b.dynamicColorsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferencesUtils.saveBoolean(getApplicationContext(), "useDynamicColors", isChecked);
            this.recreate();
        });

        b.brightnessSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                b.brightnessSlider.setVisibility(View.VISIBLE);
            } else {
                b.brightnessSlider.setVisibility(View.GONE);
                flashOffPromoCode();
            }

            SharedPreferencesUtils.saveBoolean(getApplicationContext(), "brightnessSwitch", isChecked);
        });

        b.brightnessSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {

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
                        cameraManager.setTorchMode(cameraId, true);
                        cameraManager.turnOnTorchWithStrengthLevel(cameraId, (int) slider.getValue());
                        cameraManager.setTorchMode(cameraId, false);
                    } catch (CameraAccessException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalStateException e) {
                        slider.setValue(1);
                    }
                }
                SharedPreferencesUtils.saveInteger(getApplicationContext(), "brightnessSlider", (int) slider.getValue());
            }
        });

        b.rafConsoleCard.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), RafConsoleActivity.class));
        });

        b.backMainCard.setOnClickListener(v -> {
            Intent intent1 = new Intent(getApplicationContext(), MainActivity.class);
            if (b.brightnessSwitch.isChecked()) {
                intent1.putExtra("brightnessSlider",
                        SharedPreferencesUtils.getInteger(
                                getApplicationContext(), "brightnessSlider", 1));
            } else if (!b.brightnessSwitch.isChecked()) {
                intent1.putExtra("brightnessSlider", 1);
            }

            startActivity(intent1);
        });
    }

    public void brightnessAlert() {
        new MaterialAlertDialogBuilder(SettingsActivity.this,
                R.style.AlertDialogTheme)
                .setTitle("Использовать яркость фонарика?")
                .setMessage("Яркость фонарика - платная услуга. Оплатите 350 рублей, чтобы выключить функцию яркости или воспользуйтесь промокодами")
                .setPositiveButton("Да", (dialogInterface, i) -> {


                    Snackbar.make(b.getRoot(),
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
                            Snackbar.make(b.getRoot(),
                                            "Не можете оплатить подписку? Воспользуйтесь бесплатным промокодом",
                                            Snackbar.LENGTH_LONG)
                                    .setAction("Перейти к промокоду", v -> {
                                        startActivity(new Intent(getApplicationContext(), RafConsoleActivity.class));
                                    })
                                    .show();
                        })

                .show();

        b.brightnessSwitch.setChecked(false);
    }

    public void flashOffPromoCode() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            cameraManager.setTorchMode(cameraId, false);
        }
        catch (CameraAccessException e) {

        }
    }

    public void flashOnPromoCode() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = cameraManager.getCameraIdList()[0];
            System.out.println(cameraManager.getCameraIdList().length);
            cameraManager.setTorchMode(cameraId, true);
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
}