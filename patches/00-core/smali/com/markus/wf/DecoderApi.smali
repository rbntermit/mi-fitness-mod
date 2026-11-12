.class public Lcom/markus/wf/DecoderApi;
.super Ljava/lang/Object;
.source "r8-map-id-339144b03bc704df7f72f5fb6d7a25592d5bbc656a9037a663fcda542ffbf712"


# direct methods
.method public static decode([B)Lcom/markus/wf/DecoderApi$Result;
    .locals 8

    .line 1
    :try_start_0
    new-instance v0, Lcom/markus/wf/e;

    .line 2
    invoke-direct {v0, p0}, Lcom/markus/wf/e;-><init>([B)V

    .line 3
    invoke-virtual {v0}, Lcom/markus/wf/e;->a()V

    .line 4
    new-instance p0, Lcom/markus/wf/DecoderApi$Result;

    invoke-direct {p0}, Lcom/markus/wf/DecoderApi$Result;-><init>()V

    .line 5
    invoke-virtual {v0}, Lcom/markus/wf/e;->c()Ljava/lang/String;

    move-result-object v1

    iput-object v1, p0, Lcom/markus/wf/DecoderApi$Result;->name:Ljava/lang/String;

    .line 6
    invoke-virtual {v0}, Lcom/markus/wf/e;->b()Ljava/util/ArrayList;

    move-result-object v0

    invoke-virtual {v0}, Ljava/util/ArrayList;->size()I

    move-result v1

    const/4 v2, 0x0

    :goto_0
    if-ge v2, v1, :cond_0

    invoke-virtual {v0, v2}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v3

    add-int/lit8 v2, v2, 0x1

    check-cast v3, Lcom/markus/wf/d;

    .line 7
    iget-object v4, p0, Lcom/markus/wf/DecoderApi$Result;->preview:Ljava/util/List;

    new-instance v5, Lcom/markus/wf/DecoderApi$ResultRow;

    iget-object v6, v3, Lcom/markus/wf/d;->a:[B

    iget-char v7, v3, Lcom/markus/wf/d;->b:C

    iget-char v3, v3, Lcom/markus/wf/d;->c:C

    invoke-direct {v5, v6, v7, v3}, Lcom/markus/wf/DecoderApi$ResultRow;-><init>([BII)V

    invoke-interface {v4, v5}, Ljava/util/List;->add(Ljava/lang/Object;)Z
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :cond_0
    return-object p0

    .line 8
    :catch_0
    new-instance p0, Lcom/markus/wf/DecoderApi$Result;

    invoke-direct {p0}, Lcom/markus/wf/DecoderApi$Result;-><init>()V

    return-object p0
.end method
