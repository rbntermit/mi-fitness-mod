.class public Lcom/markus/naming/WfPaidNaming;
.super Lcom/markus/naming/WfNaming;
.source "WfPaidNaming.java"


# static fields
.field public static AOD_ICON:Ljava/lang/String; = "aod_icon"

.field public static ICON:Ljava/lang/String; = "icon"

.field public static WATCHFACE:Ljava/lang/String; = "watch_face"


# direct methods
.method static constructor <clinit>()V
    .locals 0

    return-void
.end method

.method public constructor <init>()V
    .locals 0

    .line 3
    invoke-direct {p0}, Lcom/markus/naming/WfNaming;-><init>()V

    return-void
.end method


# virtual methods
.method public getWatchface()Ljava/lang/String;
    .locals 1

    .line 5
    const-string v0, "watch_face"

    return-object v0
.end method
