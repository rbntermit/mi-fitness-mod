package com.markus.logexport;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import com.google.android.gms.common.api.ApiException;
import com.xiaomi.fitness.wearos.utils.SetupNavigator;
import java.lang.ref.WeakReference;

/** Reports the SDK's terminal terms failure and lets the user save evidence before leaving setup. */
public final class WearSetupFailure {
    private static final String TAG = "MOD";
    // LiveData dispatches the setup observer on the main thread. Do not retain an old activity.
    private static WeakReference<AlertDialog> shown = new WeakReference<>(null);

    private WearSetupFailure() {}

    public static void noteActivityResult(int resultCode) {
        Log.i(TAG, "wear setup: terms activity result=" + resultCode);
    }

    public static void noteLaunchUnavailable() {
        Log.w(TAG, "wear setup: terms activity unavailable");
    }

    public static void noteConsentFailure(Exception error) {
        String details = error instanceof ApiException ? ", status=" + ((ApiException) error).getStatusCode() : "";
        if (error instanceof SecurityException) {
            details += ", reason=" + ConsentFailureReason.classify(error.getMessage()).name();
        }
        Log.w(TAG, "wear setup: consent check failed, type=" + error.getClass().getSimpleName() + details);
    }

    public static void show(Activity activity, SetupNavigator navigator) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;
        AlertDialog current = shown.get();
        if (current != null && current.isShowing() && current.getOwnerActivity() == activity) return;

        Log.w(TAG, "wear setup: terms failed");
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Не удалось завершить настройку Wear OS")
                .setMessage("Google не подтвердил завершение этапа условий использования. "
                        + "Откройте журнал, чтобы сохранить сведения об ошибке, или выйдите из настройки.")
                .setCancelable(false)
                .setPositiveButton("Журнал приложения", null)
                .setNegativeButton("Выйти из настройки", (d, which) -> {
                    try {
                        navigator.abort();
                    } catch (RuntimeException e) {
                        Log.w(TAG, "wear setup: abort failed, type=" + e.getClass().getSimpleName());
                    } finally {
                        activity.setResult(Activity.RESULT_CANCELED);
                        activity.finish();
                    }
                })
                .create();
        dialog.setOwnerActivity(activity);
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> LogExport.openAppLog(activity));
        shown = new WeakReference<>(dialog);
    }
}
