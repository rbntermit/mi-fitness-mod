.class public Lcom/markus/a;
.super Ljava/lang/Object;
.source "a.java"


# static fields
.field private static final EXECUTOR:Ljava/util/concurrent/ExecutorService;

.field private static final ICON:I = 0x7f08038b

.field private static final TYPE_DEBUG_MENU:I = 0x115c

.field private static final TYPE_DOWNLOAD_LOGS:I = 0x115e

.field private static final TYPE_ENCRYPT_KEY:I = 0x115f

.field private static final TYPE_INSTALL_FIRMWARE:I = 0x1160

.field private static final TYPE_INSTALL_WATCHFACE:I = 0x115d

.field private static final TYPE_SETTINGS:I = 0x7000


# direct methods
.method static constructor <clinit>()V
    .locals 1

    .line 59
    invoke-static {}, Ljava/util/concurrent/Executors;->newCachedThreadPool()Ljava/util/concurrent/ExecutorService;

    move-result-object v0

    sput-object v0, Lcom/markus/a;->EXECUTOR:Ljava/util/concurrent/ExecutorService;

    return-void
.end method

.method public constructor <init>()V
    .locals 0

    .line 58
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static createRow(Landroid/content/Context;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;)Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;
    .locals 22
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Landroid/content/Context;",
            "I",
            "Ljava/lang/String;",
            "Ljava/lang/String;",
            "Ljava/lang/String;",
            "Lkotlin/jvm/functions/Function1<",
            "-",
            "Landroid/content/Context;",
            "Lkotlin/Unit;",
            ">;)",
            "Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;"
        }
    .end annotation

    move-object/from16 v0, p4

    .line 127
    invoke-virtual/range {p0 .. p0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    const-string v2, "drawable"

    invoke-virtual/range {p0 .. p0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object v3

    move-object/from16 v4, p2

    invoke-virtual {v1, v4, v2, v3}, Landroid/content/res/Resources;->getIdentifier(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I

    move-result v7

    .line 132
    invoke-virtual/range {p0 .. p0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    invoke-virtual/range {p0 .. p0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object v2

    const-string v3, "string"

    move-object/from16 v4, p3

    invoke-virtual {v1, v4, v3, v2}, Landroid/content/res/Resources;->getIdentifier(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I

    move-result v1

    if-eqz v1, :cond_0

    .line 134
    invoke-virtual/range {p0 .. p0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v2

    invoke-virtual {v2, v1}, Landroid/content/res/Resources;->getString(I)Ljava/lang/String;

    move-result-object v1

    invoke-static {v1}, Lcom/xiaomi/fitness/common/extensions/StringExtKt;->toLiveData(Ljava/lang/String;)Landroidx/lifecycle/MutableLiveData;

    move-result-object v1

    goto :goto_0

    .line 136
    :cond_0
    invoke-static/range {p3 .. p3}, Lcom/xiaomi/fitness/common/extensions/StringExtKt;->toLiveData(Ljava/lang/String;)Landroidx/lifecycle/MutableLiveData;

    move-result-object v1

    :goto_0
    move-object v8, v1

    if-eqz v0, :cond_1

    .line 140
    invoke-virtual/range {p0 .. p0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    invoke-virtual/range {p0 .. p0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v1, v0, v3, v2}, Landroid/content/res/Resources;->getIdentifier(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I

    move-result v0

    if-eqz v0, :cond_1

    .line 142
    invoke-virtual/range {p0 .. p0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    invoke-virtual {v1, v0}, Landroid/content/res/Resources;->getString(I)Ljava/lang/String;

    move-result-object v0

    invoke-static {v0}, Lcom/xiaomi/fitness/common/extensions/StringExtKt;->toLiveData(Ljava/lang/String;)Landroidx/lifecycle/MutableLiveData;

    move-result-object v0

    goto :goto_1

    :cond_1
    const/4 v0, 0x0

    :goto_1
    move-object v9, v0

    .line 146
    new-instance v0, Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;

    move-object v4, v0

    const/16 v19, 0x0

    const/16 v20, 0x0

    const/16 v5, 0xb

    const/4 v10, 0x1

    const/4 v11, 0x0

    const/4 v12, 0x0

    const/4 v13, 0x0

    const/4 v15, 0x0

    const/16 v16, 0x0

    const/16 v17, 0x0

    const/16 v18, 0x1

    move/from16 v6, p1

    move-object/from16 v14, p5

    # 3.60.0i: one more (nullable) MutableLiveData at the end of the constructor; an empty one
    new-instance v21, Landroidx/lifecycle/MutableLiveData;

    invoke-direct/range {v21 .. v21}, Landroidx/lifecycle/MutableLiveData;-><init>()V

    invoke-direct/range {v4 .. v21}, Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;-><init>(IIILandroidx/lifecycle/MutableLiveData;Landroidx/lifecycle/MutableLiveData;ZLandroidx/databinding/ObservableBoolean;Landroidx/lifecycle/MutableLiveData;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Ljava/lang/String;Lkotlin/jvm/functions/Function1;ZLkotlin/jvm/functions/Function1;Landroidx/lifecycle/MutableLiveData;Landroidx/lifecycle/MutableLiveData;)V

    return-object v0
.end method

.method public static createSwitchRow(Landroid/content/Context;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcom/xiaomi/fitness/device/manager/export/bean/TabContentSwitchItem;
    .locals 18

    move-object/from16 v0, p4

    move-object/from16 v1, p5

    .line 82
    invoke-virtual/range {p0 .. p0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v2

    const-string v3, "drawable"

    invoke-virtual/range {p0 .. p0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object v4

    move-object/from16 v5, p2

    invoke-virtual {v2, v5, v3, v4}, Landroid/content/res/Resources;->getIdentifier(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I

    move-result v7

    .line 87
    invoke-virtual/range {p0 .. p0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v2

    invoke-virtual/range {p0 .. p0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object v3

    const-string v4, "string"

    move-object/from16 v5, p3

    invoke-virtual {v2, v5, v4, v3}, Landroid/content/res/Resources;->getIdentifier(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I

    move-result v2

    if-eqz v2, :cond_0

    .line 89
    invoke-virtual/range {p0 .. p0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v3

    invoke-virtual {v3, v2}, Landroid/content/res/Resources;->getString(I)Ljava/lang/String;

    move-result-object v2

    invoke-static {v2}, Lcom/xiaomi/fitness/common/extensions/StringExtKt;->toLiveData(Ljava/lang/String;)Landroidx/lifecycle/MutableLiveData;

    move-result-object v2

    goto :goto_0

    .line 91
    :cond_0
    invoke-static/range {p3 .. p3}, Lcom/xiaomi/fitness/common/extensions/StringExtKt;->toLiveData(Ljava/lang/String;)Landroidx/lifecycle/MutableLiveData;

    move-result-object v2

    :goto_0
    move-object v8, v2

    if-eqz v0, :cond_1

    .line 95
    invoke-virtual/range {p0 .. p0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v2

    invoke-virtual/range {p0 .. p0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v2, v0, v4, v3}, Landroid/content/res/Resources;->getIdentifier(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I

    move-result v0

    if-eqz v0, :cond_1

    .line 97
    invoke-virtual/range {p0 .. p0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v2

    invoke-virtual {v2, v0}, Landroid/content/res/Resources;->getString(I)Ljava/lang/String;

    move-result-object v0

    invoke-static {v0}, Lcom/xiaomi/fitness/common/extensions/StringExtKt;->toLiveData(Ljava/lang/String;)Landroidx/lifecycle/MutableLiveData;

    move-result-object v0

    goto :goto_1

    :cond_1
    const/4 v0, 0x0

    :goto_1
    move-object v10, v0

    .line 101
    new-instance v12, Landroidx/lifecycle/MutableLiveData;

    invoke-direct {v12}, Landroidx/lifecycle/MutableLiveData;-><init>()V

    const/4 v0, 0x0

    .line 102
    invoke-static {v1, v0}, Lcom/markus/Settings;->getBoolean(Ljava/lang/String;Z)Z

    move-result v0

    invoke-static {v0}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;

    move-result-object v0

    invoke-virtual {v12, v0}, Landroidx/lifecycle/MutableLiveData;->postValue(Ljava/lang/Object;)V

    .line 104
    new-instance v14, Lcom/markus/a$$ExternalSyntheticLambda2;

    invoke-direct {v14, v1, v12}, Lcom/markus/a$$ExternalSyntheticLambda2;-><init>(Ljava/lang/String;Landroidx/lifecycle/MutableLiveData;)V

    .line 110
    new-instance v0, Lcom/xiaomi/fitness/device/manager/export/bean/TabContentSwitchItem;

    const/16 v16, 0x0

    const/16 v17, 0x0

    const/16 v9, 0xd

    const/4 v11, 0x0

    const/4 v13, 0x0

    const/4 v15, 0x0

    move-object v5, v0

    move/from16 v6, p1

    invoke-direct/range {v5 .. v17}, Lcom/xiaomi/fitness/device/manager/export/bean/TabContentSwitchItem;-><init>(IILandroidx/lifecycle/MutableLiveData;ILandroidx/lifecycle/MutableLiveData;Lkotlin/jvm/functions/Function1;Landroidx/lifecycle/MutableLiveData;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;Lkotlin/jvm/functions/Function1;)V

    return-object v0
.end method

.method public static varargs fileExists([Ljava/lang/String;)Z
    .locals 0

    .line 418
    invoke-static {p0}, Lcom/markus/a;->getFile([Ljava/lang/String;)Ljava/io/File;

    move-result-object p0

    .line 419
    invoke-virtual {p0}, Ljava/io/File;->exists()Z

    move-result p0

    return p0
.end method

.method public static getCurrentDevice()Lcom/xiaomi/fitness/device/manager/DeviceModel;
    .locals 2

    .line 321
    invoke-static {}, Lcom/markus/a;->getDeviceManager()Lcom/xiaomi/fitness/device/manager/DeviceManager;

    move-result-object v0

    .line 322
    invoke-interface {v0}, Lcom/xiaomi/fitness/device/manager/DeviceManager;->getCurrentDeviceModel()Lcom/xiaomi/fitness/device/manager/DeviceModel;

    move-result-object v0

    if-eqz v0, :cond_1

    .line 323
    invoke-interface {v0}, Lcom/xiaomi/fitness/device/manager/DeviceModel;->isDeviceConnected()Z

    move-result v1

    if-nez v1, :cond_0

    goto :goto_0

    :cond_0
    return-object v0

    :cond_1
    :goto_0
    const/4 v0, 0x0

    return-object v0
.end method

.method public static getDeviceManager()Lcom/xiaomi/fitness/device/manager/DeviceManager;
    .locals 1

    .line 317
    sget-object v0, Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager;->Companion:Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager$Companion;

    invoke-static {v0}, Lcom/xiaomi/fitness/device/manager/export/DeviceManagerExtKt;->getInstance(Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager$Companion;)Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager;

    move-result-object v0

    return-object v0
.end method

.method public static varargs getFile([Ljava/lang/String;)Ljava/io/File;
    .locals 2

    .line 412
    invoke-static {}, Landroid/app/ActivityThread;->currentApplication()Landroid/app/Application;

    move-result-object v0

    invoke-virtual {v0}, Landroid/app/Application;->getApplicationContext()Landroid/content/Context;

    move-result-object v0

    const/4 v1, 0x0

    invoke-virtual {v0, v1}, Landroid/content/Context;->getExternalFilesDir(Ljava/lang/String;)Ljava/io/File;

    move-result-object v0

    invoke-virtual {v0}, Ljava/io/File;->getParentFile()Ljava/io/File;

    move-result-object v0

    .line 413
    new-instance v1, Ljava/io/File;

    invoke-static {p0}, Lcom/markus/a;->strFromArgs([Ljava/lang/String;)Ljava/lang/String;

    move-result-object p0

    invoke-direct {v1, v0, p0}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    return-object v1
.end method

.method private static getLogDir()Ljava/lang/String;
    .locals 1

    .line 355
    sget-object v0, Lcom/xiaomi/fitness/feedback/util/FeedbackFileUtils;->INSTANCE:Lcom/xiaomi/fitness/feedback/util/FeedbackFileUtils;

    invoke-virtual {v0}, Lcom/xiaomi/fitness/feedback/util/FeedbackFileUtils;->getDebiceLogDirPath()Ljava/lang/String;

    move-result-object v0

    return-object v0
.end method

.method public static getResourceString(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;
    .locals 3

    if-nez p0, :cond_0

    return-object p1

    .line 74
    :cond_0
    invoke-virtual {p0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    const-string v1, "string"

    invoke-virtual {p0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v0, p1, v1, v2}, Landroid/content/res/Resources;->getIdentifier(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I

    move-result v0

    if-nez v0, :cond_1

    return-object p1

    .line 78
    :cond_1
    invoke-virtual {p0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object p0

    invoke-virtual {p0, v0}, Landroid/content/res/Resources;->getString(I)Ljava/lang/String;

    move-result-object p0

    return-object p0
.end method

.method public static getSecurity()Ljava/lang/String;
    .locals 3

    .line 380
    sget-object v0, Lcom/xiaomi/fitness/account/token/TokenManager;->Companion:Lcom/xiaomi/fitness/account/token/TokenManager$Companion;

    invoke-static {v0}, Lcom/xiaomi/fitness/account/extensions/AccountManagerExtKt;->getInstance(Lcom/xiaomi/fitness/account/token/TokenManager$Companion;)Lcom/xiaomi/fitness/account/token/TokenManager;

    move-result-object v0

    .line 381
    const-string v1, "miothealth"

    const/4 v2, 0x0

    invoke-interface {v0, v1, v2, v2}, Lcom/xiaomi/fitness/account/token/TokenManager;->getServiceToken(Ljava/lang/String;ZI)Lcom/xiaomi/fitness/account/token/MiAccessToken;

    move-result-object v0

    if-nez v0, :cond_0

    const/4 v0, 0x0

    return-object v0

    .line 385
    :cond_0
    invoke-virtual {v0}, Lcom/xiaomi/fitness/account/token/MiAccessToken;->getSecurity()Ljava/lang/String;

    move-result-object v0

    return-object v0
.end method

.method public static getTitle(I)I
    .locals 3

    const/16 v0, 0x7000

    if-ne p0, v0, :cond_0

    .line 360
    invoke-static {}, Landroid/app/ActivityThread;->currentApplication()Landroid/app/Application;

    move-result-object p0

    invoke-virtual {p0}, Landroid/app/Application;->getApplicationContext()Landroid/content/Context;

    move-result-object p0

    .line 361
    invoke-virtual {p0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    const-string v1, "string"

    invoke-virtual {p0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;

    move-result-object p0

    const-string v2, "device_settings_settings_title"

    invoke-virtual {v0, v2, v1, p0}, Landroid/content/res/Resources;->getIdentifier(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I

    move-result p0

    return p0

    :cond_0
    const/4 p0, -0x1

    return p0
.end method

.method private static handleDebugMenu(Landroid/app/Activity;Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;)Z
    .locals 1

    .line 330
    invoke-static {}, Lcom/markus/a;->getCurrentDevice()Lcom/xiaomi/fitness/device/manager/DeviceModel;

    move-result-object p1

    const/4 v0, 0x1

    if-eqz p1, :cond_0

    # 3.60.0i: Xiaomi removed the third-party app debug page (XmsManager.gotoDebugPage, ThirdAppDebugFragment)
    const-string p1, "Установка сторонних приложений из файла недоступна в этой версии Mi Fitness: Xiaomi убрала страницу отладки"

    invoke-static {p0, p1, v0}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p1

    invoke-virtual {p1}, Landroid/widget/Toast;->show()V

    goto :goto_0

    .line 335
    :cond_0
    const-string p1, "device_settings_not_connected"

    invoke-static {p0, p1}, Lcom/markus/a;->getResourceString(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    invoke-static {p0, p1, v0}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p0

    invoke-virtual {p0}, Landroid/widget/Toast;->show()V

    :goto_0
    return v0
.end method

.method public static handleDeviceLocal(Lokhttp3/internal/connection/RealCall;Lokhttp3/Callback;)V
    .locals 1

    .line 423
    new-instance v0, Lcom/markus/OkRunnable;

    invoke-direct {v0, p0, p1}, Lcom/markus/OkRunnable;-><init>(Lokhttp3/internal/connection/RealCall;Lokhttp3/Callback;)V

    .line 424
    sget-object p0, Lcom/markus/a;->EXECUTOR:Ljava/util/concurrent/ExecutorService;

    invoke-interface {p0, v0}, Ljava/util/concurrent/ExecutorService;->execute(Ljava/lang/Runnable;)V

    return-void
.end method

.method private static handleInstallWatchface(Landroid/app/Activity;Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;)Z
    .locals 3

    .line 341
    invoke-static {}, Lcom/markus/a;->getCurrentDevice()Lcom/xiaomi/fitness/device/manager/DeviceModel;

    move-result-object p1

    const/4 v0, 0x1

    if-nez p1, :cond_0

    .line 343
    const-string p1, "device_settings_not_connected"

    invoke-static {p0, p1}, Lcom/markus/a;->getResourceString(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    invoke-static {p0, p1, v0}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p0

    invoke-virtual {p0}, Landroid/widget/Toast;->show()V

    return v0

    .line 347
    :cond_0
    new-instance p1, Landroid/content/Intent;

    const-class v1, Lcom/markus/FilePickerProxyActivity;

    invoke-direct {p1, p0, v1}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V

    const/high16 v1, 0x10000000

    .line 348
    invoke-virtual {p1, v1}, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;

    .line 349
    const-string v1, "request_id"

    const/4 v2, 0x0

    invoke-virtual {p1, v1, v2}, Landroid/content/Intent;->putExtra(Ljava/lang/String;I)Landroid/content/Intent;

    .line 350
    invoke-virtual {p0, p1}, Landroid/app/Activity;->startActivity(Landroid/content/Intent;)V

    return v0
.end method

.method public static handleMenuClick(Landroid/app/Activity;Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;Lcom/xiaomi/fitness/device/manager/ui/tab/DeviceTabFragmentV4;)Z
    .locals 2

    .line 367
    invoke-virtual {p1}, Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;->getType()I

    move-result v0

    const/16 v1, 0x115c

    if-ne v0, v1, :cond_0

    .line 368
    invoke-static {p0, p1}, Lcom/markus/a;->handleDebugMenu(Landroid/app/Activity;Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;)Z

    move-result p0

    return p0

    .line 369
    :cond_0
    invoke-virtual {p1}, Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;->getType()I

    move-result v0

    const/16 v1, 0x115d

    if-ne v0, v1, :cond_1

    .line 370
    invoke-static {p0, p1}, Lcom/markus/a;->handleInstallWatchface(Landroid/app/Activity;Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;)Z

    move-result p0

    return p0

    .line 371
    :cond_1
    invoke-virtual {p1}, Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;->getType()I

    move-result p0

    const/16 v0, 0x7000

    if-ne p0, v0, :cond_2

    .line 372
    new-instance p0, Landroid/os/Bundle;

    invoke-direct {p0}, Landroid/os/Bundle;-><init>()V

    .line 373
    const-string v0, "device_tab_type"

    invoke-virtual {p1}, Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;->getType()I

    move-result p1

    invoke-virtual {p0, v0, p1}, Landroid/os/Bundle;->putInt(Ljava/lang/String;I)V

    .line 374
    const-class p1, Lcom/xiaomi/fitness/device/manager/ui/tab/DeviceSettingListFragment;

    invoke-static {p2, p1, p0}, Lcom/xiaomi/fitness/baseui/common/BaseFragmentExKt;->gotoPage(Lcom/xiaomi/fitness/baseui/view/BaseFragment;Ljava/lang/Class;Landroid/os/Bundle;)V

    :cond_2
    const/4 p0, 0x0

    return p0
.end method

.method public static insertMenus(Landroidx/databinding/ObservableArrayList;Landroidx/databinding/ObservableArrayList;Landroidx/databinding/ObservableArrayList;)V
    .locals 6
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Landroidx/databinding/ObservableArrayList<",
            "Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;",
            ">;",
            "Landroidx/databinding/ObservableArrayList<",
            "Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;",
            ">;",
            "Landroidx/databinding/ObservableArrayList<",
            "Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;",
            ">;)V"
        }
    .end annotation

    .line 167
    invoke-static {}, Landroid/app/ActivityThread;->currentApplication()Landroid/app/Application;

    move-result-object p1

    if-nez p1, :cond_0

    return-void

    :cond_0
    const/4 v4, 0x0

    const/4 v5, 0x0

    const/16 v1, 0x115d

    .line 172
    const-string v2, "device_icon_dial_market"

    const-string v3, "device_settings_install_watchface"

    move-object v0, p1

    invoke-static/range {v0 .. v5}, Lcom/markus/a;->createRow(Landroid/content/Context;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;)Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;

    move-result-object p2

    const/4 v0, 0x2

    invoke-virtual {p0, v0, p2}, Landroidx/databinding/ObservableArrayList;->add(ILjava/lang/Object;)V

    const/16 v1, 0x115c

    .line 173
    const-string v2, "device_third_app_market"

    const-string v3, "device_settings_app_menu"

    move-object v0, p1

    invoke-static/range {v0 .. v5}, Lcom/markus/a;->createRow(Landroid/content/Context;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;)Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;

    move-result-object p2

    const/4 v0, 0x3

    invoke-virtual {p0, v0, p2}, Landroidx/databinding/ObservableArrayList;->add(ILjava/lang/Object;)V

    const/16 v1, 0x7000

    .line 175
    const-string v2, "device_third_app_market"

    const-string v3, "device_settings_settings_title"

    move-object v0, p1

    invoke-static/range {v0 .. v5}, Lcom/markus/a;->createRow(Landroid/content/Context;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;)Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;

    move-result-object p1

    .line 177
    invoke-virtual {p0, p1}, Landroidx/databinding/ObservableArrayList;->add(Ljava/lang/Object;)Z

    return-void
.end method

.method public static insertSubMenus(Landroidx/databinding/ObservableArrayList;I)V
    .locals 7
    .annotation system Ldalvik/annotation/Signature;
        value = {
            "(",
            "Landroidx/databinding/ObservableArrayList<",
            "Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;",
            ">;I)V"
        }
    .end annotation

    .line 181
    invoke-static {}, Landroid/app/ActivityThread;->currentApplication()Landroid/app/Application;

    move-result-object v6

    if-nez v6, :cond_0

    return-void

    :cond_0
    const/16 v0, 0xb

    if-ne p1, v0, :cond_1

    .line 186
    new-instance v5, Lcom/markus/a$$ExternalSyntheticLambda3;

    invoke-direct {v5}, Lcom/markus/a$$ExternalSyntheticLambda3;-><init>()V

    .line 202
    const-string v3, "device_settings_dump_device_log"

    const/4 v4, 0x0

    const/16 v1, 0x115e

    const-string v2, "device_tab_setting_icon_safe"

    move-object v0, v6

    invoke-static/range {v0 .. v5}, Lcom/markus/a;->createRow(Landroid/content/Context;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;)Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;

    move-result-object p1

    invoke-virtual {p0, p1}, Landroidx/databinding/ObservableArrayList;->add(Ljava/lang/Object;)Z

    .line 204
    new-instance v5, Lcom/markus/a$$ExternalSyntheticLambda4;

    invoke-direct {v5}, Lcom/markus/a$$ExternalSyntheticLambda4;-><init>()V

    .line 236
    const-string v3, "device_settings_connection_key"

    const-string v4, "device_settings_connection_key_subtitle"

    const/16 v1, 0x115f

    const-string v2, "device_settings_track_set_item_icon"

    invoke-static/range {v0 .. v5}, Lcom/markus/a;->createRow(Landroid/content/Context;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;)Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;

    move-result-object p1

    invoke-virtual {p0, p1}, Landroidx/databinding/ObservableArrayList;->add(Ljava/lang/Object;)Z

    goto :goto_0

    :cond_1
    const/16 v0, 0x7000

    if-ne p1, v0, :cond_2

    const/4 v4, 0x0

    .line 256
    const-string v5, "health_summary_hide"

    const/4 v1, 0x0

    const-string v2, "device_tab_setting_icon_system"

    const-string v3, "device_settings_settings_hide_health_summary"

    move-object v0, v6

    invoke-static/range {v0 .. v5}, Lcom/markus/a;->createSwitchRow(Landroid/content/Context;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcom/xiaomi/fitness/device/manager/export/bean/TabContentSwitchItem;

    move-result-object p1

    invoke-virtual {p0, p1}, Landroidx/databinding/ObservableArrayList;->add(Ljava/lang/Object;)Z

    .line 257
    const-string v5, "weather_manual"

    const-string v2, "ic_home_weather_setting"

    const-string v3, "device_settings_settings_manual_weather"

    invoke-static/range {v0 .. v5}, Lcom/markus/a;->createSwitchRow(Landroid/content/Context;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcom/xiaomi/fitness/device/manager/export/bean/TabContentSwitchItem;

    move-result-object p1

    invoke-virtual {p0, p1}, Landroidx/databinding/ObservableArrayList;->add(Ljava/lang/Object;)Z

    .line 258
    new-instance v5, Lcom/markus/a$$ExternalSyntheticLambda5;

    invoke-direct {v5}, Lcom/markus/a$$ExternalSyntheticLambda5;-><init>()V

    .line 299
    const-string v3, "device_settings_settings_manual_weather_set"

    const-string v2, "ic_weather_location"

    invoke-static/range {v0 .. v5}, Lcom/markus/a;->createRow(Landroid/content/Context;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;)Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;

    move-result-object p1

    invoke-virtual {p0, p1}, Landroidx/databinding/ObservableArrayList;->add(Ljava/lang/Object;)Z

    .line 301
    new-instance v5, Lcom/markus/a$$ExternalSyntheticLambda6;

    invoke-direct {v5}, Lcom/markus/a$$ExternalSyntheticLambda6;-><init>()V

    .line 312
    const-string v3, "device_settings_language_setting"

    const-string v2, "ic_home_band_language"

    invoke-static/range {v0 .. v5}, Lcom/markus/a;->createRow(Landroid/content/Context;ILjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/jvm/functions/Function1;)Lcom/xiaomi/fitness/device/manager/export/bean/TabContentItem;

    move-result-object p1

    invoke-virtual {p0, p1}, Landroidx/databinding/ObservableArrayList;->add(Ljava/lang/Object;)Z

    :cond_2
    :goto_0
    return-void
.end method

.method public static isBase64(Ljava/lang/String;)Z
    .locals 2

    const/4 v0, 0x0

    if-eqz p0, :cond_1

    .line 389
    invoke-virtual {p0}, Ljava/lang/String;->isEmpty()Z

    move-result v1

    if-eqz v1, :cond_0

    goto :goto_0

    .line 393
    :cond_0
    :try_start_0
    invoke-static {}, Ljava/util/Base64;->getDecoder()Ljava/util/Base64$Decoder;

    move-result-object v1

    invoke-virtual {v1, p0}, Ljava/util/Base64$Decoder;->decode(Ljava/lang/String;)[B
    :try_end_0
    .catch Ljava/lang/IllegalArgumentException; {:try_start_0 .. :try_end_0} :catch_0

    const/4 p0, 0x1

    return p0

    :catch_0
    :cond_1
    :goto_0
    return v0
.end method

.method static synthetic lambda$createSwitchRow$0(Ljava/lang/String;Landroidx/lifecycle/MutableLiveData;Ljava/lang/Boolean;)Lkotlin/Unit;
    .locals 1

    .line 105
    invoke-virtual {p2}, Ljava/lang/Boolean;->booleanValue()Z

    move-result v0

    invoke-static {p0, v0}, Lcom/markus/Settings;->setBoolean(Ljava/lang/String;Z)V

    .line 106
    invoke-virtual {p1, p2}, Landroidx/lifecycle/MutableLiveData;->postValue(Ljava/lang/Object;)V

    .line 107
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    return-object p0
.end method

.method static synthetic lambda$insertSubMenus$1(Landroid/content/Context;)Lkotlin/Unit;
    .locals 4

    .line 187
    invoke-static {}, Lcom/markus/a;->getCurrentDevice()Lcom/xiaomi/fitness/device/manager/DeviceModel;

    move-result-object v0

    const/4 v1, 0x1

    if-nez v0, :cond_0

    .line 189
    const-string v0, "device_settings_not_connected"

    invoke-static {p0, v0}, Lcom/markus/a;->getResourceString(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    invoke-static {p0, v0, v1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p0

    invoke-virtual {p0}, Landroid/widget/Toast;->show()V

    .line 190
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    return-object p0

    .line 193
    :cond_0
    new-instance v2, Lcom/markus/LogSyncCallback;

    invoke-static {}, Lcom/markus/a;->getLogDir()Ljava/lang/String;

    move-result-object v3

    invoke-direct {v2, p0, v3}, Lcom/markus/LogSyncCallback;-><init>(Landroid/content/Context;Ljava/lang/String;)V

    .line 194
    instance-of v3, v0, Lcom/xiaomi/fitness/device/manager/export/WearableDeviceModel;

    if-eqz v3, :cond_1

    .line 195
    sget-object v3, Lcom/xiaomi/fitness/feedback/bugreport/GetDeviceLog;->INSTANCE:Lcom/xiaomi/fitness/feedback/bugreport/GetDeviceLog;

    check-cast v0, Lcom/xiaomi/fitness/device/manager/export/WearableDeviceModel;

    invoke-virtual {v3, v0, v2}, Lcom/xiaomi/fitness/feedback/bugreport/GetDeviceLog;->syncLogFromBleDevice(Lcom/xiaomi/fitness/device/manager/export/WearableDeviceModel;Lcom/xiaomi/fitness/device/contact/export/OnSyncCallback;)V

    .line 196
    const-string v0, "device_settings_receiving_logs"

    invoke-static {p0, v0}, Lcom/markus/a;->getResourceString(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    invoke-static {p0, v0, v1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p0

    invoke-virtual {p0}, Landroid/widget/Toast;->show()V

    goto :goto_0

    .line 198
    :cond_1
    const-string v0, "device_settings_uncompatible_device"

    invoke-static {p0, v0}, Lcom/markus/a;->getResourceString(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    invoke-static {p0, v0, v1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p0

    invoke-virtual {p0}, Landroid/widget/Toast;->show()V

    .line 200
    :goto_0
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    return-object p0
.end method

.method static synthetic lambda$insertSubMenus$2(Landroid/content/Context;)Lkotlin/Unit;
    .locals 5

    .line 206
    invoke-static {}, Lcom/markus/a;->getDeviceManager()Lcom/xiaomi/fitness/device/manager/DeviceManager;

    move-result-object v0

    const/4 v1, 0x1

    if-nez v0, :cond_0

    .line 208
    const-string v0, "device_settings_unable_to_get_info"

    invoke-static {p0, v0}, Lcom/markus/a;->getResourceString(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    invoke-static {p0, v0, v1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p0

    .line 209
    invoke-virtual {p0}, Landroid/widget/Toast;->show()V

    .line 210
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    return-object p0

    .line 213
    :cond_0
    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2}, Ljava/lang/StringBuilder;-><init>()V

    .line 214
    invoke-interface {v0}, Lcom/xiaomi/fitness/device/manager/DeviceManager;->getDeviceList()Ljava/util/List;

    move-result-object v0

    invoke-interface {v0}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v0

    :cond_1
    :goto_0
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z

    move-result v3

    if-eqz v3, :cond_2

    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v3

    check-cast v3, Lcom/xiaomi/fitness/device/manager/bean/Device;

    .line 215
    instance-of v4, v3, Lcom/xiaomi/fitness/device/manager/export/bean/WearableDevice;

    if-eqz v4, :cond_1

    .line 216
    invoke-virtual {v3}, Lcom/xiaomi/fitness/device/manager/bean/Device;->getName()Ljava/lang/String;

    move-result-object v4

    invoke-virtual {v2, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 217
    const-string v4, " ("

    invoke-virtual {v2, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 218
    invoke-virtual {v3}, Lcom/xiaomi/fitness/device/manager/bean/Device;->getModel()Ljava/lang/String;

    move-result-object v4

    invoke-virtual {v2, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 219
    const-string v4, ", "

    invoke-virtual {v2, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 220
    invoke-virtual {v3}, Lcom/xiaomi/fitness/device/manager/bean/Device;->getDid()Ljava/lang/String;

    move-result-object v4

    invoke-virtual {v2, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 221
    const-string v4, "): "

    invoke-virtual {v2, v4}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 222
    invoke-virtual {v3}, Lcom/xiaomi/fitness/device/manager/bean/Device;->getDetail()Lcom/xiaomi/fitness/device/manager/bean/Device$Detail;

    move-result-object v3

    invoke-virtual {v3}, Lcom/xiaomi/fitness/device/manager/bean/Device$Detail;->getEncryptKey()Ljava/lang/String;

    move-result-object v3

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 223
    const-string v3, "\n"

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    goto :goto_0

    .line 227
    :cond_2
    const-string v0, "clipboard"

    invoke-virtual {p0, v0}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Landroid/content/ClipboardManager;

    .line 228
    const-string v3, "Encrypt Key"

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v3, v2}, Landroid/content/ClipData;->newPlainText(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Landroid/content/ClipData;

    move-result-object v2

    .line 229
    invoke-virtual {v0, v2}, Landroid/content/ClipboardManager;->setPrimaryClip(Landroid/content/ClipData;)V

    .line 231
    const-string v0, "device_settings_copied_to_clipboard"

    invoke-static {p0, v0}, Lcom/markus/a;->getResourceString(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    invoke-static {p0, v0, v1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p0

    .line 232
    invoke-virtual {p0}, Landroid/widget/Toast;->show()V

    .line 234
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    return-object p0
.end method

.method static synthetic lambda$insertSubMenus$3(DD)V
    .locals 9

    .line 277
    const-string v0, "weather_manual_lat"

    invoke-static {v0, p2, p3}, Lcom/markus/Settings;->setDouble(Ljava/lang/String;D)V

    .line 278
    const-string v0, "weather_manual_lon"

    invoke-static {v0, p0, p1}, Lcom/markus/Settings;->setDouble(Ljava/lang/String;D)V

    .line 279
    sget-object v0, Lcom/xiaomi/fitness/sync/export/di/WeatherSyncManager;->Companion:Lcom/xiaomi/fitness/sync/export/di/WeatherSyncManager$Companion;

    invoke-static {v0}, Lcom/xiaomi/fitness/sync/export/di/SyncExtKt;->getInstance(Lcom/xiaomi/fitness/sync/export/di/WeatherSyncManager$Companion;)Lcom/xiaomi/fitness/sync/export/di/WeatherSyncManager;

    move-result-object v0

    .line 280
    sget-object v1, Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager;->Companion:Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager$Companion;

    invoke-static {v1}, Lcom/xiaomi/fitness/device/manager/export/DeviceManagerExtKt;->getInstance(Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager$Companion;)Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager;

    move-result-object v1

    invoke-virtual {v1}, Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager;->getCurrentDeviceModel()Lcom/xiaomi/fitness/device/manager/DeviceModel;

    move-result-object v1

    move-object v7, v1

    check-cast v7, Lcom/xiaomi/fitness/device/manager/export/WearableDeviceModel;

    .line 282
    invoke-static {v7}, Ljava/util/Collections;->singletonList(Ljava/lang/Object;)Ljava/util/List;

    move-result-object v2

    const/4 v5, 0x0

    const/4 v6, 0x0

    const/4 v3, 0x0

    const/4 v4, 0x0

    move-object v1, v0

    .line 281
    invoke-interface/range {v1 .. v6}, Lcom/xiaomi/fitness/sync/export/di/WeatherSyncManager;->requestLocation(Ljava/util/List;Lcom/xiaomi/fitness/sync/export/MyAction1;Lcom/xiaomi/fitness/sync/export/model/LocationKey;ZZ)V

    const/4 v8, 0x0

    move-object v2, v7

    move-wide v3, p0

    move-wide v5, p2

    move-object v7, v8

    .line 288
    invoke-interface/range {v1 .. v7}, Lcom/xiaomi/fitness/sync/export/di/WeatherSyncManager;->syncWeatherToDeviceForTest(Lcom/xiaomi/fitness/device/manager/export/WearableDeviceModel;DDLcom/xiaomi/fitness/sync/export/MyAction1;)V

    return-void
.end method

.method static synthetic lambda$insertSubMenus$4(Landroid/content/Context;)Lkotlin/Unit;
    .locals 7

    .line 259
    invoke-static {}, Lcom/markus/Settings;->isManualWeatherLocation()Z

    move-result v0

    if-nez v0, :cond_0

    .line 260
    sget-object v0, Lcom/xiaomi/fitness/sync/export/di/WeatherSyncManager;->Companion:Lcom/xiaomi/fitness/sync/export/di/WeatherSyncManager$Companion;

    invoke-static {v0}, Lcom/xiaomi/fitness/sync/export/di/SyncExtKt;->getInstance(Lcom/xiaomi/fitness/sync/export/di/WeatherSyncManager$Companion;)Lcom/xiaomi/fitness/sync/export/di/WeatherSyncManager;

    move-result-object v1

    .line 261
    sget-object v0, Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager;->Companion:Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager$Companion;

    invoke-static {v0}, Lcom/xiaomi/fitness/device/manager/export/DeviceManagerExtKt;->getInstance(Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager$Companion;)Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager;

    move-result-object v0

    invoke-virtual {v0}, Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager;->getCurrentDeviceModel()Lcom/xiaomi/fitness/device/manager/DeviceModel;

    move-result-object v0

    check-cast v0, Lcom/xiaomi/fitness/device/manager/export/WearableDeviceModel;

    .line 263
    invoke-static {v0}, Ljava/util/Collections;->singletonList(Ljava/lang/Object;)Ljava/util/List;

    move-result-object v2

    const/4 v5, 0x0

    const/4 v6, 0x0

    const/4 v3, 0x0

    const/4 v4, 0x0

    .line 262
    invoke-interface/range {v1 .. v6}, Lcom/xiaomi/fitness/sync/export/di/WeatherSyncManager;->requestLocation(Ljava/util/List;Lcom/xiaomi/fitness/sync/export/MyAction1;Lcom/xiaomi/fitness/sync/export/model/LocationKey;ZZ)V

    .line 269
    const-string v0, "Updated!"

    const/4 v1, 0x1

    invoke-static {p0, v0, v1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    .line 270
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    return-object p0

    .line 274
    :cond_0
    invoke-static {}, Lcom/markus/Settings;->getManualWeatherLocationLon()D

    move-result-wide v1

    .line 275
    invoke-static {}, Lcom/markus/Settings;->getManualWeatherLocationLat()D

    move-result-wide v3

    new-instance v5, Lcom/markus/a$$ExternalSyntheticLambda0;

    invoke-direct {v5}, Lcom/markus/a$$ExternalSyntheticLambda0;-><init>()V

    move-object v0, p0

    .line 272
    invoke-static/range {v0 .. v5}, Lcom/markus/UiUtils;->showCoordinatesDialog(Landroid/content/Context;DDLcom/markus/UiUtils$CoordinatesCallback;)V

    .line 297
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    return-object p0
.end method

.method static synthetic lambda$insertSubMenus$5(Ljava/lang/String;)V
    .locals 1

    .line 305
    const-string v0, "override_locale"

    invoke-static {v0, p0}, Lcom/markus/Settings;->setString(Ljava/lang/String;Ljava/lang/String;)V

    .line 306
    sget-object p0, Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager;->Companion:Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager$Companion;

    invoke-static {p0}, Lcom/xiaomi/fitness/device/manager/export/DeviceManagerExtKt;->getInstance(Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager$Companion;)Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager;

    move-result-object p0

    invoke-virtual {p0}, Lcom/xiaomi/fitness/device/manager/export/WearableDeviceManager;->getCurrentDeviceModel()Lcom/xiaomi/fitness/device/manager/DeviceModel;

    move-result-object p0

    check-cast p0, Lcom/xiaomi/fitness/device/manager/export/WearableDeviceModel;

    .line 307
    sget-object v0, Lcom/xiaomi/fitness/devicesettings/DeviceSettingsSyncer;->INSTANCE:Lcom/xiaomi/fitness/devicesettings/DeviceSettingsSyncer;

    invoke-virtual {v0, p0}, Lcom/xiaomi/fitness/devicesettings/DeviceSettingsSyncer;->syncBluetoothLanguage(Lcom/xiaomi/fitness/device/manager/export/WearableDeviceModel;)V

    return-void
.end method

.method static synthetic lambda$insertSubMenus$6(Landroid/content/Context;)Lkotlin/Unit;
    .locals 1

    .line 302
    new-instance v0, Lcom/markus/a$$ExternalSyntheticLambda1;

    invoke-direct {v0}, Lcom/markus/a$$ExternalSyntheticLambda1;-><init>()V

    invoke-static {p0, v0}, Lcom/markus/UiUtils;->showLanguageDialog(Landroid/content/Context;Lcom/markus/UiUtils$OnChoiceListener;)V

    .line 310
    sget-object p0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    return-object p0
.end method

.method public static overrideFeature(Lcom/xiaomi/fitness/device/manager/DeviceModel;Ljava/lang/String;)Z
    .locals 2

    const/4 v0, 0x0

    .line 430
    :try_start_0
    const-string v1, "third_party_no_market"

    invoke-virtual {p1, v1}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result p1

    if-eqz p1, :cond_1

    .line 431
    invoke-static {p0}, Lcom/xiaomi/fitness/device/manager/DeviceModelExtKt;->isWearOS(Lcom/xiaomi/fitness/device/manager/DeviceModel;)Z

    move-result p0
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    if-eqz p0, :cond_0

    return v0

    :cond_0
    const/4 p0, 0x1

    return p0

    :cond_1
    return v0

    .line 438
    :catch_0
    const-string p0, "isSupportFeature"

    const-string p1, "Override fail"

    invoke-static {p0, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I

    return v0
.end method

.method private static varargs strFromArgs([Ljava/lang/String;)Ljava/lang/String;
    .locals 3

    .line 401
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    const/4 v1, 0x0

    .line 402
    :goto_0
    array-length v2, p0

    if-ge v1, v2, :cond_1

    if-eqz v1, :cond_0

    .line 404
    const-string v2, "/"

    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    .line 406
    :cond_0
    aget-object v2, p0, v1

    invoke-virtual {v0, v2}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    .line 408
    :cond_1
    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p0

    return-object p0
.end method
