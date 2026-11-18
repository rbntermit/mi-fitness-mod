package com.markus.logexport;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * «Начать запись» … «Остановить и сохранить»: logcat of the app's own processes streamed into a file for the whole
 * time, masked line by line. Some phones (OnePlus/ColorOS: LOG_FLOWCTRL … DROPPED) keep only the last minute in the
 * system buffer, so a snapshot afterwards misses the problem. Lives in the app process, not in the screen, so the
 * person can go and repeat the problem elsewhere in the app.
 */
public final class LogRecorder {
    static final long MAX_MS = 30L * 60 * 1000;
    private static final long PID_REFRESH_MS = 5000;
    private static final String TAG = "MOD";
    private static LogRecorder current;

    private final Context app;
    final File file;
    final long startedAt;
    private final String startText;
    private volatile boolean stopRequested;
    private volatile java.lang.Process proc;
    private Thread thread;
    volatile int kept;
    volatile boolean truncated;
    volatile String error;
    volatile boolean finished;
    volatile long stoppedAt;

    private LogRecorder(Context app, File file) {
        this.app = app;
        this.file = file;
        this.startedAt = System.currentTimeMillis();
        this.startText = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US).format(new Date(startedAt));
    }

    static synchronized LogRecorder current() {
        return current;
    }

    /** Starts a new recording unless one is running; returns the running one. */
    static synchronized LogRecorder start(Context context, File file, String headerWithoutPeriod) {
        if (current != null && !current.finished) {
            return current;
        }
        LogRecorder r = new LogRecorder(context.getApplicationContext(), file);
        current = r;
        r.thread = new Thread(() -> r.run(headerWithoutPeriod), "markus-log-recorder");
        r.thread.setDaemon(true);
        r.thread.start();
        Log.i(TAG, "log: recording started");
        return r;
    }

    /** Stops and waits up to a few seconds for the file to be closed. */
    void stop() {
        stopRequested = true;
        java.lang.Process p = proc;
        if (p != null) {
            p.destroy();
        }
        Thread t = thread;
        if (t != null) {
            try {
                t.join(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    long elapsedMs() {
        return (finished ? stoppedAt : System.currentTimeMillis()) - startedAt;
    }

    private void run(String header) {
        Set<Integer> pids = ownPids();
        long nextRefresh = System.currentTimeMillis() + PID_REFRESH_MS;
        long written = 0;
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write(header.replace("@PERIOD@", "запись с " + startText));
            // Persist a fresh cache snapshot even if the same SDK state was logged before recording began.
            w.write("# Диагностика Wear OS на начало записи: " + com.markus.facefix.WatchCompat.logSnapshot() + "\n");
            // -T: from now on, without the old buffer; the stream ends when the process is destroyed
            proc = new ProcessBuilder("logcat", "-v", "threadtime", "-T", startText).redirectErrorStream(true).start();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while (!stopRequested && (line = in.readLine()) != null) {
                    long now = System.currentTimeMillis();
                    if (now > nextRefresh) {
                        // processes of the app may restart while the person repeats the problem
                        pids.addAll(ownPids());
                        nextRefresh = now + PID_REFRESH_MS;
                    }
                    if (now - startedAt > MAX_MS) {
                        truncated = true;
                        break;
                    }
                    int pid = LogLines.pidOf(line);
                    if (pid < 0 || !pids.contains(pid)) {
                        continue;
                    }
                    String masked = LogMasker.mask(line);
                    if (written + masked.length() > LogLines.MAX_CHARS * 4) {
                        truncated = true;
                        break;
                    }
                    w.write(masked);
                    w.write('\n');
                    written += masked.length() + 1;
                    kept++;
                    if ((kept & 63) == 0) {
                        w.flush();
                    }
                }
            }
            w.write("\n# Запись остановлена " + new SimpleDateFormat("MM-dd HH:mm:ss", Locale.US).format(new Date())
                    + (truncated ? " (по ограничению времени или размера)" : "") + ", строк: " + kept + "\n");
        } catch (IOException | RuntimeException e) {
            if (!stopRequested) {
                error = e.getMessage();
                Log.w(TAG, "log: recording failed", e);
            }
        } finally {
            java.lang.Process p = proc;
            if (p != null) {
                p.destroy();
            }
            stoppedAt = System.currentTimeMillis();
            finished = true;
            Log.i(TAG, "log: recording stopped, lines " + kept);
        }
    }

    private Set<Integer> ownPids() {
        Set<Integer> pids = new HashSet<>();
        pids.add(Process.myPid());
        ActivityManager am = (ActivityManager) app.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> list = am == null ? null : am.getRunningAppProcesses();
        if (list != null) {
            int uid = Process.myUid();
            for (ActivityManager.RunningAppProcessInfo i : list) {
                if (i.uid == uid) {
                    pids.add(i.pid);
                }
            }
        }
        return pids;
    }
}
