package com.markus.logexport;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.SystemClock;
import android.view.View;
import java.util.ArrayDeque;

/** Records fixed checkpoints. No request, response or user-supplied value enters this trace. */
public final class LoginDiagnostics {
    private static final int MAX_EVENTS = 24;
    private static final ArrayDeque<String> events = new ArrayDeque<>(MAX_EVENTS);
    private static long started = SystemClock.elapsedRealtime();

    private LoginDiagnostics() {}

    public static synchronized void begin() {
        events.clear();
        started = SystemClock.elapsedRealtime();
        mark("passport_opened");
    }

    public static synchronized void mark(String stage) {
        if (events.size() == MAX_EVENTS) events.removeFirst();
        events.addLast((SystemClock.elapsedRealtime() - started) + " ms " + stage);
    }

    public static void result(int result) {
        mark(result == Activity.RESULT_OK ? "mi_fitness_login_result_ok"
                : result == Activity.RESULT_CANCELED ? "mi_fitness_login_result_cancelled" : "mi_fitness_login_result_other");
    }

    static synchronized String snapshot() {
        StringBuilder text = new StringBuilder();
        for (String event : events) text.append(event).append('\n');
        return text.toString();
    }

    public static View.OnClickListener help(Activity activity) {
        return view -> {
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;
            mark("login_help_opened");
            new AlertDialog.Builder(activity)
                    .setTitle("Помощь со входом")
                    .setMessage("В журнале есть только этапы входа и время. Можно сохранить его и при необходимости закрыть вход.")
                    .setPositiveButton("Журнал входа", (dialog, which) -> LogExport.openLoginLog(activity))
                    .setNegativeButton("Закрыть вход", (dialog, which) -> activity.finish())
                    .setNeutralButton("Вернуться", null)
                    .show();
        };
    }
}
