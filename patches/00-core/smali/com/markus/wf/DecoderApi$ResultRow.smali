.class public Lcom/markus/wf/DecoderApi$ResultRow;
.super Ljava/lang/Object;
.source "r8-map-id-339144b03bc704df7f72f5fb6d7a25592d5bbc656a9037a663fcda542ffbf712"


# instance fields
.field public height:I

.field public raw:[B

.field public width:I


# direct methods
.method public constructor <init>([BII)V
    .locals 0

    .line 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 2
    .line 3
    .line 4
    iput-object p1, p0, Lcom/markus/wf/DecoderApi$ResultRow;->raw:[B

    .line 5
    .line 6
    iput p2, p0, Lcom/markus/wf/DecoderApi$ResultRow;->width:I

    .line 7
    .line 8
    iput p3, p0, Lcom/markus/wf/DecoderApi$ResultRow;->height:I

    .line 9
    .line 10
    return-void
.end method
