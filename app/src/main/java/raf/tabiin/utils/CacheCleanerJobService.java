package raf.tabiin.utils;

import static android.content.Intent.getIntent;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PersistableBundle;
import android.util.Log;

public class CacheCleanerJobService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        PersistableBundle extras = params.getExtras();
        boolean shouldCleanCache = extras.getBoolean("cleanCache", false);

        if (shouldCleanCache) {
            clearCache(getApplicationContext());
        }

        jobFinished(params, false); // Уведомляем о завершении задачи.

        return true; // Возвращаем true, если задача выполняется асинхронно.
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true; // Возвращаем true, чтобы указать, что задача должна быть перепланирована в случае прерывания.
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

    // Метод для запуска задачи очистки кеша из другого компонента вашего приложения
    public static void scheduleCacheCleaning(Context context) {
        android.app.job.JobScheduler jobScheduler = (android.app.job.JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        if (jobScheduler != null) {
            android.app.job.JobInfo.Builder builder = new android.app.job.JobInfo.Builder(1,
                    new ComponentName(context, CacheCleanerJobService.class));
            builder.setExtras(new PersistableBundle());
            //builder.getExtras().putBoolean("cleanCache", true);

            // Устанавливаем периодичность выполнения задачи (например, каждые 24 часа).
            builder.setPeriodic(24 * 60 * 60 * 1000); // 24 часа в миллисекундах

            jobScheduler.schedule(builder.build());
        }
    }
}