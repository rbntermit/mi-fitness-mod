package com.markus.alarm;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.xiaomi.fitness.devicesettings.base.clock.AlarmClockAddFragment;
import com.xiaomi.fitness.devicesettings.widget.TimePicker;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public final class PhoneAlarmImport {
    private PhoneAlarmImport() {}

    public static void attach(AlarmClockAddFragment editor) {
        Bundle arguments = editor.getArguments();
        if (arguments != null && arguments.containsKey("key_clock_bean")) return;

        TimePicker picker = editor.getMTimePicker();
        if (picker == null || !(picker.getParent() instanceof LinearLayout)) return;
        LinearLayout content = (LinearLayout) picker.getParent();
        Context context = content.getContext();
        Button importTime = new Button(context, null, android.R.attr.borderlessButtonStyle);
        importTime.setAllCaps(false);
        importTime.setText(stringId(context, "markus_import_phone_alarm"));
        int textColor = context.getResources().getIdentifier("text_color", "color", context.getPackageName());
        if (textColor != 0) importTime.setTextColor(context.getColorStateList(textColor));
        importTime.setOnClickListener(view -> {
            AlarmManager alarms = context.getSystemService(AlarmManager.class);
            AlarmManager.AlarmClockInfo next = alarms == null ? null : alarms.getNextAlarmClock();
            Instant now = Instant.now();
            Instant trigger = next == null ? null : Instant.ofEpochMilli(next.getTriggerTime());
            if (trigger == null || !trigger.isAfter(now)) {
                Toast.makeText(context, stringId(context, "markus_import_phone_alarm_none"), Toast.LENGTH_SHORT).show();
                return;
            }

            ZoneId zone = ZoneId.systemDefault();
            LocalTime exactTime = BandAlarmTime.importableTime(now, trigger, zone);
            if (exactTime != null) {
                setTime(editor, picker, exactTime);
                return;
            }

            LocalTime time = LocalDateTime.ofInstant(trigger, zone).toLocalTime().truncatedTo(ChronoUnit.MINUTES);
            LocalDateTime bandTime = BandAlarmTime.nextOccurrence(LocalDateTime.ofInstant(now, zone), time);
            int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_TIME;
            String phoneDate = DateUtils.formatDateTime(context, trigger.toEpochMilli(), flags);
            String bandDate = DateUtils.formatDateTime(context, bandTime.atZone(zone).toInstant().toEpochMilli(), flags);
            String message = context.getString(stringId(context, "markus_import_phone_alarm_date_note"), phoneDate, bandDate);
            boolean night = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                    == Configuration.UI_MODE_NIGHT_YES;
            new AlertDialog.Builder(context, night ? android.R.style.Theme_Material_Dialog_Alert
                    : android.R.style.Theme_Material_Light_Dialog_Alert)
                    .setTitle(stringId(context, "markus_import_phone_alarm_later"))
                    .setMessage(message)
                    .setPositiveButton(stringId(context, "markus_import_phone_alarm_time_only"),
                            (dialog, which) -> setTime(editor, picker, time))
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });
        int afterTimeDescription = Math.min(content.indexOfChild(picker) + 2, content.getChildCount());
        content.addView(importTime, afterTimeDescription, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    private static void setTime(AlarmClockAddFragment editor, TimePicker picker, LocalTime time) {
        picker.setCurrentHour(time.getHour());
        picker.setCurrentMinute(time.getMinute());
        editor.onTimeChanged(picker, time.getHour(), time.getMinute());
    }

    private static int stringId(Context context, String name) {
        return context.getResources().getIdentifier(name, "string", context.getPackageName());
    }
}
