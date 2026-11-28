package com.xiaomi.fitness.devicesettings.base.clock;

import android.os.Bundle;
import com.xiaomi.fitness.devicesettings.widget.TimePicker;

/** Compile-only stub. The real fragment is in the stock app. */
public abstract class AlarmClockAddFragment {
    public abstract Bundle getArguments();
    public abstract TimePicker getMTimePicker();
    public abstract void onTimeChanged(TimePicker picker, int hour, int minute);
}
