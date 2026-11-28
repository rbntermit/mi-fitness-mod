package com.xiaomi.fitness.devicesettings.widget;

import android.content.Context;
import android.view.View;

/** Compile-only stub. The real widget is in the stock app. */
public abstract class TimePicker extends View {
    protected TimePicker(Context context) { super(context); }

    public abstract void setCurrentHour(Integer hour);
    public abstract void setCurrentMinute(Integer minute);
}
