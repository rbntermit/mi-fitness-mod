.class public final Lcom/markus/Compat;
.super Ljava/lang/Object;
.source "Compat.java"

# List.getFirst() exists only on Android 15 (API 35); minSdk is 26.
# Calling it on Android 14 and older throws NoSuchMethodError while installing a watchface.

# direct methods
.method public static first(Ljava/util/List;)Ljava/lang/Object;
    .locals 1

    const/4 v0, 0x0

    invoke-interface {p0, v0}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v0

    return-object v0
.end method
