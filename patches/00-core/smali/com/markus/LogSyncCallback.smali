.class public Lcom/markus/LogSyncCallback;
.super Ljava/lang/Object;
.source "LogSyncCallback.java"

# interfaces
.implements Lcom/xiaomi/fitness/device/contact/export/OnSyncCallback;


# instance fields
.field private final context:Landroid/content/Context;

.field private final path:Ljava/lang/String;


# direct methods
.method public constructor <init>(Landroid/content/Context;Ljava/lang/String;)V
    .locals 0

    .line 13
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 14
    iput-object p1, p0, Lcom/markus/LogSyncCallback;->context:Landroid/content/Context;

    .line 15
    iput-object p2, p0, Lcom/markus/LogSyncCallback;->path:Ljava/lang/String;

    return-void
.end method


# virtual methods
.method public onError(Ljava/lang/String;II)V
    .locals 0

    .line 20
    iget-object p1, p0, Lcom/markus/LogSyncCallback;->context:Landroid/content/Context;

    const-string p2, "device_settings_log_collection_error"

    invoke-static {p1, p2}, Lcom/markus/a;->getResourceString(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;

    move-result-object p2

    const/4 p3, 0x1

    invoke-static {p1, p2, p3}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p1

    invoke-virtual {p1}, Landroid/widget/Toast;->show()V

    return-void
.end method

.method public onSuccess(Ljava/lang/String;ILcom/xiaomi/fitness/device/contact/export/SyncResult;)V
    .locals 2

    .line 25
    new-instance p1, Landroid/app/AlertDialog$Builder;

    iget-object p2, p0, Lcom/markus/LogSyncCallback;->context:Landroid/content/Context;

    invoke-direct {p1, p2}, Landroid/app/AlertDialog$Builder;-><init>(Landroid/content/Context;)V

    .line 26
    iget-object p2, p0, Lcom/markus/LogSyncCallback;->context:Landroid/content/Context;

    const-string p3, "device_settings_log_dialog_title"

    invoke-static {p2, p3}, Lcom/markus/a;->getResourceString(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;

    move-result-object p2

    invoke-virtual {p1, p2}, Landroid/app/AlertDialog$Builder;->setTitle(Ljava/lang/CharSequence;)Landroid/app/AlertDialog$Builder;

    .line 27
    iget-object p2, p0, Lcom/markus/LogSyncCallback;->context:Landroid/content/Context;

    const-string p3, "device_settings_log_dialog_subtitle"

    invoke-static {p2, p3}, Lcom/markus/a;->getResourceString(Landroid/content/Context;Ljava/lang/String;)Ljava/lang/String;

    move-result-object p2

    iget-object p3, p0, Lcom/markus/LogSyncCallback;->path:Ljava/lang/String;

    const/4 v0, 0x1

    new-array v0, v0, [Ljava/lang/Object;

    const/4 v1, 0x0

    aput-object p3, v0, v1

    invoke-static {p2, v0}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object p2

    invoke-virtual {p1, p2}, Landroid/app/AlertDialog$Builder;->setMessage(Ljava/lang/CharSequence;)Landroid/app/AlertDialog$Builder;

    .line 28
    const-string p2, "OK"

    const/4 p3, 0x0

    invoke-virtual {p1, p2, p3}, Landroid/app/AlertDialog$Builder;->setNegativeButton(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;

    .line 29
    invoke-virtual {p1}, Landroid/app/AlertDialog$Builder;->create()Landroid/app/AlertDialog;

    move-result-object p1

    invoke-virtual {p1}, Landroid/app/AlertDialog;->show()V

    return-void
.end method
