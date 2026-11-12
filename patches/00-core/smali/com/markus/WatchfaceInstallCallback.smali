.class public Lcom/markus/WatchfaceInstallCallback;
.super Ljava/lang/Object;
.source "WatchfaceInstallCallback.java"

# interfaces
.implements Lcom/xiaomi/fitness/watch/face/install/FaceInstallPushCallback;


# instance fields
.field private final activity:Landroidx/fragment/app/FragmentActivity;

.field private final faceDialog:Lcom/xiaomi/fitness/watch/face/widget/FaceDialog;


# direct methods
.method public constructor <init>(Lcom/xiaomi/fitness/watch/face/widget/FaceDialog;Landroidx/fragment/app/FragmentActivity;)V
    .locals 0

    .line 17
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 18
    iput-object p1, p0, Lcom/markus/WatchfaceInstallCallback;->faceDialog:Lcom/xiaomi/fitness/watch/face/widget/FaceDialog;

    .line 19
    iput-object p2, p0, Lcom/markus/WatchfaceInstallCallback;->activity:Landroidx/fragment/app/FragmentActivity;

    return-void
.end method


# virtual methods
.method synthetic lambda$onFinish$0$com-markus-WatchfaceInstallCallback(ZI)V
    .locals 4

    .line 26
    iget-object v0, p0, Lcom/markus/WatchfaceInstallCallback;->faceDialog:Lcom/xiaomi/fitness/watch/face/widget/FaceDialog;

    invoke-virtual {v0}, Lcom/xiaomi/fitness/watch/face/widget/FaceDialog;->dismiss()V

    .line 27
    iget-object v0, p0, Lcom/markus/WatchfaceInstallCallback;->activity:Landroidx/fragment/app/FragmentActivity;

    invoke-virtual {v0}, Landroidx/fragment/app/FragmentActivity;->finish()V

    const/4 v0, 0x1

    if-eqz p1, :cond_0

    .line 29
    iget-object p1, p0, Lcom/markus/WatchfaceInstallCallback;->activity:Landroidx/fragment/app/FragmentActivity;

    invoke-virtual {p1}, Landroidx/fragment/app/FragmentActivity;->getApplicationContext()Landroid/content/Context;

    move-result-object p1

    iget-object p2, p0, Lcom/markus/WatchfaceInstallCallback;->activity:Landroidx/fragment/app/FragmentActivity;

    const-string v1, "device_settings_watchface_installed"

    invoke-static {p2, v1}, Lcom/markus/a;->getResourceString(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;

    move-result-object p2

    invoke-static {p1, p2, v0}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p1

    invoke-virtual {p1}, Landroid/widget/Toast;->show()V

    goto :goto_0

    .line 31
    :cond_0
    iget-object p1, p0, Lcom/markus/WatchfaceInstallCallback;->activity:Landroidx/fragment/app/FragmentActivity;

    invoke-virtual {p1}, Landroidx/fragment/app/FragmentActivity;->getApplicationContext()Landroid/content/Context;

    move-result-object p1

    iget-object v1, p0, Lcom/markus/WatchfaceInstallCallback;->activity:Landroidx/fragment/app/FragmentActivity;

    const-string v2, "device_settings_watchface_installed_error"

    invoke-static {v1, v2}, Lcom/markus/a;->getResourceString(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v1

    invoke-static {p2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object p2

    new-array v2, v0, [Ljava/lang/Object;

    const/4 v3, 0x0

    aput-object p2, v2, v3

    invoke-static {v1, v2}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object p2

    invoke-static {p1, p2, v0}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p1

    invoke-virtual {p1}, Landroid/widget/Toast;->show()V

    :goto_0
    return-void
.end method

.method synthetic lambda$onProgress$1$com-markus-WatchfaceInstallCallback(I)V
    .locals 1

    .line 41
    iget-object v0, p0, Lcom/markus/WatchfaceInstallCallback;->faceDialog:Lcom/xiaomi/fitness/watch/face/widget/FaceDialog;

    invoke-virtual {v0, p1}, Lcom/xiaomi/fitness/watch/face/widget/FaceDialog;->setProgress(I)V

    return-void
.end method

.method public onFinish(ZI)V
    .locals 2

    .line 24
    new-instance v0, Landroid/os/Handler;

    iget-object v1, p0, Lcom/markus/WatchfaceInstallCallback;->activity:Landroidx/fragment/app/FragmentActivity;

    invoke-virtual {v1}, Landroidx/fragment/app/FragmentActivity;->getMainLooper()Landroid/os/Looper;

    move-result-object v1

    invoke-direct {v0, v1}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V

    .line 25
    new-instance v1, Lcom/markus/WatchfaceInstallCallback$$ExternalSyntheticLambda0;

    invoke-direct {v1, p0, p1, p2}, Lcom/markus/WatchfaceInstallCallback$$ExternalSyntheticLambda0;-><init>(Lcom/markus/WatchfaceInstallCallback;ZI)V

    .line 34
    invoke-virtual {v0, v1}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z

    return-void
.end method

.method public onProgress(I)V
    .locals 2

    .line 40
    new-instance v0, Landroid/os/Handler;

    iget-object v1, p0, Lcom/markus/WatchfaceInstallCallback;->activity:Landroidx/fragment/app/FragmentActivity;

    invoke-virtual {v1}, Landroidx/fragment/app/FragmentActivity;->getMainLooper()Landroid/os/Looper;

    move-result-object v1

    invoke-direct {v0, v1}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V

    .line 41
    new-instance v1, Lcom/markus/WatchfaceInstallCallback$$ExternalSyntheticLambda1;

    invoke-direct {v1, p0, p1}, Lcom/markus/WatchfaceInstallCallback$$ExternalSyntheticLambda1;-><init>(Lcom/markus/WatchfaceInstallCallback;I)V

    .line 42
    invoke-virtual {v0, v1}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z

    return-void
.end method

.method public onStart()V
    .locals 0

    return-void
.end method
