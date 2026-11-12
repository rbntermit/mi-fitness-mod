.class public final Lcom/markus/Settings;
.super Ljava/lang/Object;
.source "Settings.java"


# static fields
.field public static final HEALTH_SUMMARY_HIDE:Ljava/lang/String; = "health_summary_hide"

.field public static final OVERRIDE_LOCALE:Ljava/lang/String; = "override_locale"

.field private static final PREFS_NAME:Ljava/lang/String; = "markus_settings"

.field public static final WEATHER_MANUAL:Ljava/lang/String; = "weather_manual"

.field public static final WEATHER_MANUAL_LAT:Ljava/lang/String; = "weather_manual_lat"

.field public static final WEATHER_MANUAL_LON:Ljava/lang/String; = "weather_manual_lon"

.field private static prefs:Landroid/content/SharedPreferences;


# direct methods
.method private constructor <init>()V
    .locals 0

    .line 18
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static getBoolean(Ljava/lang/String;Z)Z
    .locals 1

    .line 38
    invoke-static {}, Lcom/markus/Settings;->prefs()Landroid/content/SharedPreferences;

    move-result-object v0

    invoke-interface {v0, p0, p1}, Landroid/content/SharedPreferences;->getBoolean(Ljava/lang/String;Z)Z

    move-result p0

    return p0
.end method

.method public static getDouble(Ljava/lang/String;D)D
    .locals 1

    .line 59
    invoke-static {p1, p2}, Ljava/lang/Double;->doubleToRawLongBits(D)J

    move-result-wide p1

    .line 60
    invoke-static {}, Lcom/markus/Settings;->prefs()Landroid/content/SharedPreferences;

    move-result-object v0

    invoke-interface {v0, p0, p1, p2}, Landroid/content/SharedPreferences;->getLong(Ljava/lang/String;J)J

    move-result-wide p0

    invoke-static {p0, p1}, Ljava/lang/Double;->longBitsToDouble(J)D

    move-result-wide p0

    return-wide p0
.end method

.method public static getInt(Ljava/lang/String;I)I
    .locals 1

    .line 49
    invoke-static {}, Lcom/markus/Settings;->prefs()Landroid/content/SharedPreferences;

    move-result-object v0

    invoke-interface {v0, p0, p1}, Landroid/content/SharedPreferences;->getInt(Ljava/lang/String;I)I

    move-result p0

    return p0
.end method

.method public static getManualWeatherLocationLat()D
    .locals 3

    .line 92
    const-string v0, "weather_manual_lat"

    const-wide/16 v1, 0x0

    invoke-static {v0, v1, v2}, Lcom/markus/Settings;->getDouble(Ljava/lang/String;D)D

    move-result-wide v0

    return-wide v0
.end method

.method public static getManualWeatherLocationLon()D
    .locals 3

    .line 96
    const-string v0, "weather_manual_lon"

    const-wide/16 v1, 0x0

    invoke-static {v0, v1, v2}, Lcom/markus/Settings;->getDouble(Ljava/lang/String;D)D

    move-result-wide v0

    return-wide v0
.end method

.method public static getOverrideLocale()Ljava/lang/String;
    .locals 2

    .line 108
    const-string v0, "override_locale"

    const-string v1, ""

    invoke-static {v0, v1}, Lcom/markus/Settings;->getString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    return-object v0
.end method

.method public static getString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    .locals 1

    .line 73
    invoke-static {}, Lcom/markus/Settings;->prefs()Landroid/content/SharedPreferences;

    move-result-object v0

    invoke-interface {v0, p0, p1}, Landroid/content/SharedPreferences;->getString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object p0

    return-object p0
.end method

.method public static init(Landroid/content/Context;)V
    .locals 2

    .line 22
    sget-object v0, Lcom/markus/Settings;->prefs:Landroid/content/SharedPreferences;

    if-nez v0, :cond_0

    .line 23
    invoke-virtual {p0}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;

    move-result-object p0

    const-string v0, "markus_settings"

    const/4 v1, 0x0

    invoke-virtual {p0, v0, v1}, Landroid/content/Context;->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;

    move-result-object p0

    sput-object p0, Lcom/markus/Settings;->prefs:Landroid/content/SharedPreferences;

    :cond_0
    return-void
.end method

.method public static isHealthSummaryHidden()Z
    .locals 2

    .line 84
    const-string v0, "health_summary_hide"

    const/4 v1, 0x0

    invoke-static {v0, v1}, Lcom/markus/Settings;->getBoolean(Ljava/lang/String;Z)Z

    move-result v0

    return v0
.end method

.method public static isManualWeatherLocation()Z
    .locals 2

    .line 88
    const-string v0, "weather_manual"

    const/4 v1, 0x0

    invoke-static {v0, v1}, Lcom/markus/Settings;->getBoolean(Ljava/lang/String;Z)Z

    move-result v0

    return v0
.end method

.method public static isOverrideLocale()Z
    .locals 2

    .line 100
    const-string v0, "override_locale"

    const-string v1, ""

    invoke-static {v0, v1}, Lcom/markus/Settings;->getString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 101
    invoke-virtual {v1, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v0

    if-eqz v0, :cond_0

    const/4 v0, 0x0

    return v0

    :cond_0
    const/4 v0, 0x1

    return v0
.end method

.method private static prefs()Landroid/content/SharedPreferences;
    .locals 2

    .line 28
    sget-object v0, Lcom/markus/Settings;->prefs:Landroid/content/SharedPreferences;

    if-eqz v0, :cond_0

    return-object v0

    .line 29
    :cond_0
    new-instance v0, Ljava/lang/IllegalStateException;

    const-string v1, "Settings.init() was not called"

    invoke-direct {v0, v1}, Ljava/lang/IllegalStateException;-><init>(Ljava/lang/String;)V

    throw v0
.end method

.method public static setBoolean(Ljava/lang/String;Z)V
    .locals 1

    .line 42
    invoke-static {}, Lcom/markus/Settings;->prefs()Landroid/content/SharedPreferences;

    move-result-object v0

    invoke-interface {v0}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;

    move-result-object v0

    invoke-interface {v0, p0, p1}, Landroid/content/SharedPreferences$Editor;->putBoolean(Ljava/lang/String;Z)Landroid/content/SharedPreferences$Editor;

    move-result-object p0

    invoke-interface {p0}, Landroid/content/SharedPreferences$Editor;->apply()V

    return-void
.end method

.method public static setDouble(Ljava/lang/String;D)V
    .locals 1

    .line 64
    invoke-static {}, Lcom/markus/Settings;->prefs()Landroid/content/SharedPreferences;

    move-result-object v0

    invoke-interface {v0}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;

    move-result-object v0

    .line 65
    invoke-static {p1, p2}, Ljava/lang/Double;->doubleToRawLongBits(D)J

    move-result-wide p1

    invoke-interface {v0, p0, p1, p2}, Landroid/content/SharedPreferences$Editor;->putLong(Ljava/lang/String;J)Landroid/content/SharedPreferences$Editor;

    move-result-object p0

    .line 66
    invoke-interface {p0}, Landroid/content/SharedPreferences$Editor;->apply()V

    return-void
.end method

.method public static setInt(Ljava/lang/String;I)V
    .locals 1

    .line 53
    invoke-static {}, Lcom/markus/Settings;->prefs()Landroid/content/SharedPreferences;

    move-result-object v0

    invoke-interface {v0}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;

    move-result-object v0

    invoke-interface {v0, p0, p1}, Landroid/content/SharedPreferences$Editor;->putInt(Ljava/lang/String;I)Landroid/content/SharedPreferences$Editor;

    move-result-object p0

    invoke-interface {p0}, Landroid/content/SharedPreferences$Editor;->apply()V

    return-void
.end method

.method public static setString(Ljava/lang/String;Ljava/lang/String;)V
    .locals 1

    .line 77
    invoke-static {}, Lcom/markus/Settings;->prefs()Landroid/content/SharedPreferences;

    move-result-object v0

    invoke-interface {v0}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;

    move-result-object v0

    invoke-interface {v0, p0, p1}, Landroid/content/SharedPreferences$Editor;->putString(Ljava/lang/String;Ljava/lang/String;)Landroid/content/SharedPreferences$Editor;

    move-result-object p0

    invoke-interface {p0}, Landroid/content/SharedPreferences$Editor;->apply()V

    return-void
.end method
