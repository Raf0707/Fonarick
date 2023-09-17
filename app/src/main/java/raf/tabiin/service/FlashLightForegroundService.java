package raf.tabiin.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import io.reactivex.rxjava3.subjects.PublishSubject;
import raf.tabiin.flashlight.MainActivity;
import raf.tabiin.flashlight.R;
import raf.tabiin.utils.SharedPreferencesUtils;

import com.google.android.material.slider.Slider;

public class FlashLightForegroundService extends Service {
    private static final String CHANNEL_ID = "ForegroundServiceChannel";
    private static final int NOTIFICATION_ID = 1;

    private CameraManager cameraManager;
    private String cameraId;
    private boolean isFlashlightOn = false;
    private Slider brightnessSlider;
    private PublishSubject<Float> brightnessSubject = PublishSubject.create();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        try {
            cameraId = cameraManager.getCameraIdList()[0];
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /*@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, buildForegroundNotification());

        brightnessSubject.subscribe(brightness -> {
            // Изменение яркости фонарика с использованием значения brightness
            // ...
        });

        // Включение и выключение фонарика
        toggleFlashlight();

        return START_STICKY;
    }*/

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, buildForegroundNotification());

        brightnessSubject.subscribe(brightness -> {
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
                }

                try {
                    cameraManager.setTorchMode(cameraId, true);
                    cameraManager.turnOnTorchWithStrengthLevel(cameraId, Integer.parseInt(String.valueOf(brightness)));
                    cameraManager.setTorchMode(cameraId, false);
                } catch (CameraAccessException e) {
                    throw new RuntimeException(e);
                }
            }
            SharedPreferencesUtils.saveInteger(getApplicationContext(), "brightnessSlider", Integer.parseInt(String.valueOf(brightness)));
        });

        // Включение и выключение фонарика
        toggleFlashlight();

        return START_STICKY;
    }

    private void toggleFlashlight() {
        if (isFlashlightOn) {
            turnOffFlashlight();
        } else {
            turnOnFlashlight();
        }
    }

    private void turnOnFlashlight() {
        try {
            cameraManager.setTorchMode(cameraId, true);
            isFlashlightOn = true;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void turnOffFlashlight() {
        try {
            cameraManager.setTorchMode(cameraId, false);
            isFlashlightOn = false;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Notification buildForegroundNotification() {
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Flashlight Service")
                .setContentText("Фонарик работает в фоновом режиме")
                .setSmallIcon(R.drawable.fonaric)
                .setContentIntent(pendingIntent)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setLightColor(Color.BLUE);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }
}