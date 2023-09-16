package raf.tabiin.flashlight;
import static androidx.core.content.ContentProviderCompat.requireContext;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

    private boolean myPromo = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        state = SharedPreferencesUtils.getBoolean(getApplicationContext(), "stateFlash");
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();



        if (state) {
            flashOnPromoCode();
        } else {
            flashOffPromoCode();
        }

        Intent intent = getIntent();
        promoCode = intent.getBooleanExtra("promoCode", false);

        myPromo = promoCode;

        SharedPreferencesUtils.saveBoolean(getApplicationContext(), "promo", myPromo);

        promoCode = SharedPreferencesUtils.getBoolean(getApplicationContext(), "promo", false);

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

        binding.torchbtn.setOnLongClickListener(v -> {new CustomTabUtil().openCustomTab(this,
                "https://t.me/+OoI8UWDVVm0yMDNi", R.color.black);
            return true;
        });

        binding.openSettingsBtn.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        });

    }

    private void runFlashlight() {

        binding.torchbtn.setOnClickListener(v -> {
            if (!state)
            {
                if (promoCode || myPromo) flashOnPromoCode();
                else flashLightOnAlert();
            }
            else
            {
                if (promoCode || myPromo) flashOffPromoCode();
                else flashLightOffAlert();
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
            cameraManager.setTorchMode(cameraId, true);
            state = true;
            SharedPreferencesUtils.saveBoolean(getApplicationContext(), "stateFlash", state);
            binding.torchbtn.setText("On");
        }
        catch (CameraAccessException e)
        {}
    }
}