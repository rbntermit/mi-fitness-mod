.class public Lcom/markus/wf/DecoderApi$Result;
.super Ljava/lang/Object;
.source "r8-map-id-339144b03bc704df7f72f5fb6d7a25592d5bbc656a9037a663fcda542ffbf712"


# instance fields
.field public name:Ljava/lang/String;

.field public preview:Ljava/util/List;


# direct methods
.method public constructor <init>()V
    .locals 1

    .line 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 2
    .line 3
    .line 4
    new-instance v0, Ljava/util/ArrayList;

    .line 5
    .line 6
    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    .line 7
    .line 8
    .line 9
    iput-object v0, p0, Lcom/markus/wf/DecoderApi$Result;->preview:Ljava/util/List;

    .line 10
    .line 11
    const-string v0, ""

    .line 12
    .line 13
    iput-object v0, p0, Lcom/markus/wf/DecoderApi$Result;->name:Ljava/lang/String;

    .line 14
    .line 15
    return-void
.end method
