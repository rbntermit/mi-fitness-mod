.class public Lcom/markus/PrepareInstallCallback;
.super Ljava/lang/Object;
.source "PrepareInstallCallback.java"

# interfaces
.implements Lkotlin/jvm/functions/Function3;


# annotations
.annotation system Ldalvik/annotation/Signature;
    value = {
        "Ljava/lang/Object;",
        "Lkotlin/jvm/functions/Function3<",
        "Ljava/lang/Boolean;",
        "Ljava/lang/Integer;",
        "Ljava/lang/Integer;",
        "Lkotlin/Unit;",
        ">;"
    }
.end annotation


# instance fields
.field private final absolutePath:Ljava/lang/String;

.field private final activity:Landroidx/fragment/app/FragmentActivity;

.field private final callback:Lcom/markus/WatchfaceInstallCallback;

.field private final controller:Lcom/xiaomi/fitness/watch/face/install/FaceInstallController;

.field private final watchfaceId:Ljava/lang/String;


# direct methods
.method public static synthetic $r8$lambda$ZdIq855RzeeCWv8JsT2R8mOFEko(Landroidx/fragment/app/FragmentActivity;)V
    .locals 0

    invoke-virtual {p0}, Landroid/app/Activity;->finish()V

    return-void
.end method

.method public constructor <init>(Lcom/xiaomi/fitness/watch/face/install/FaceInstallController;Ljava/lang/String;Ljava/lang/String;Lcom/markus/WatchfaceInstallCallback;Landroidx/fragment/app/FragmentActivity;)V
    .locals 0

    .line 17
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 18
    iput-object p1, p0, Lcom/markus/PrepareInstallCallback;->controller:Lcom/xiaomi/fitness/watch/face/install/FaceInstallController;

    .line 19
    iput-object p2, p0, Lcom/markus/PrepareInstallCallback;->absolutePath:Ljava/lang/String;

    .line 20
    iput-object p3, p0, Lcom/markus/PrepareInstallCallback;->watchfaceId:Ljava/lang/String;

    .line 21
    iput-object p4, p0, Lcom/markus/PrepareInstallCallback;->callback:Lcom/markus/WatchfaceInstallCallback;

    .line 22
    iput-object p5, p0, Lcom/markus/PrepareInstallCallback;->activity:Landroidx/fragment/app/FragmentActivity;

    return-void
.end method


# virtual methods
.method public bridge synthetic invoke(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    .locals 0
    .annotation system Ldalvik/annotation/MethodParameters;
        accessFlags = {
            0x1000,
            0x1000,
            0x1000
        }
        names = {
            null,
            null,
            null
        }
    .end annotation

    .line 9
    check-cast p1, Ljava/lang/Boolean;

    check-cast p2, Ljava/lang/Integer;

    check-cast p3, Ljava/lang/Integer;

    invoke-virtual {p0, p1, p2, p3}, Lcom/markus/PrepareInstallCallback;->invoke(Ljava/lang/Boolean;Ljava/lang/Integer;Ljava/lang/Integer;)Lkotlin/Unit;

    move-result-object p1

    return-object p1
.end method

.method public invoke(Ljava/lang/Boolean;Ljava/lang/Integer;Ljava/lang/Integer;)Lkotlin/Unit;
    .locals 2

    .line 27
    invoke-virtual {p1}, Ljava/lang/Boolean;->booleanValue()Z

    move-result p1

    if-eqz p1, :cond_0

    .line 28
    iget-object p1, p0, Lcom/markus/PrepareInstallCallback;->controller:Lcom/xiaomi/fitness/watch/face/install/FaceInstallController;

    iget-object p2, p0, Lcom/markus/PrepareInstallCallback;->absolutePath:Ljava/lang/String;

    iget-object p3, p0, Lcom/markus/PrepareInstallCallback;->watchfaceId:Ljava/lang/String;

    const/4 v0, 0x0

    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v0

    iget-object v1, p0, Lcom/markus/PrepareInstallCallback;->callback:Lcom/markus/WatchfaceInstallCallback;

    invoke-interface {p1, p2, p3, v0, v1}, Lcom/xiaomi/fitness/watch/face/install/FaceInstallController;->doInstall(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Lcom/xiaomi/fitness/watch/face/install/FaceInstallPushCallback;)V

    goto :goto_0

    .line 30
    :cond_0
    new-instance p1, Landroid/os/Handler;

    iget-object p2, p0, Lcom/markus/PrepareInstallCallback;->activity:Landroidx/fragment/app/FragmentActivity;

    invoke-virtual {p2}, Landroidx/fragment/app/FragmentActivity;->getMainLooper()Landroid/os/Looper;

    move-result-object p2

    invoke-direct {p1, p2}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V

    .line 31
    iget-object p2, p0, Lcom/markus/PrepareInstallCallback;->activity:Landroidx/fragment/app/FragmentActivity;

    invoke-virtual {p2}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    new-instance p3, Lcom/markus/PrepareInstallCallback$$ExternalSyntheticLambda0;

    invoke-direct {p3, p2}, Lcom/markus/PrepareInstallCallback$$ExternalSyntheticLambda0;-><init>(Landroidx/fragment/app/FragmentActivity;)V

    .line 32
    invoke-virtual {p1, p3}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z

    .line 34
    :goto_0
    sget-object p1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;

    return-object p1
.end method
