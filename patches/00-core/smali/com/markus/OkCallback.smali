.class public Lcom/markus/OkCallback;
.super Ljava/lang/Object;
.source "OkCallback.java"

# interfaces
.implements Lokhttp3/Callback;


# instance fields
.field fmt_animated:Ljava/lang/String;

.field fmt_aod:Ljava/lang/String;

.field fmt_static:Ljava/lang/String;

.field public target:Lokhttp3/Callback;


# direct methods
.method public constructor <init>()V
    .locals 1

    .line 20
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    const/4 v0, 0x0

    .line 21
    iput-object v0, p0, Lcom/markus/OkCallback;->target:Lokhttp3/Callback;

    .line 23
    const-string v0, "files/WatchFace/%s/%s/local/preview/style_%d_static.png"

    iput-object v0, p0, Lcom/markus/OkCallback;->fmt_static:Ljava/lang/String;

    .line 24
    const-string v0, "files/WatchFace/%s/%s/local/preview/style_%d_aod.png"

    iput-object v0, p0, Lcom/markus/OkCallback;->fmt_aod:Ljava/lang/String;

    .line 25
    const-string v0, "files/WatchFace/%s/%s/local/preview/style_%d_animated.webp"

    iput-object v0, p0, Lcom/markus/OkCallback;->fmt_animated:Ljava/lang/String;

    return-void
.end method

.method private createEmptyBleExt()Lorg/json/JSONObject;
    .locals 4
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Lorg/json/JSONException;
        }
    .end annotation

    .line 34
    new-instance v0, Lorg/json/JSONObject;

    invoke-direct {v0}, Lorg/json/JSONObject;-><init>()V

    .line 35
    const-string v1, "type"

    const/4 v2, 0x0

    invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;I)Lorg/json/JSONObject;

    .line 36
    const-string v1, "album_picture_type"

    invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;I)Lorg/json/JSONObject;

    .line 37
    const-string v1, "album_portrait_type"

    invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;I)Lorg/json/JSONObject;

    .line 38
    const-string v1, "bottom_background"

    const-string v3, ""

    invoke-virtual {v0, v1, v3}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 39
    const-string v1, "upper_background"

    invoke-virtual {v0, v1, v3}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 40
    const-string v1, "bg_img"

    invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;I)Lorg/json/JSONObject;

    .line 41
    const-string v1, "support_element_colour"

    invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;I)Lorg/json/JSONObject;

    return-object v0
.end method

.method private createEmptyWatchface()Lorg/json/JSONObject;
    .locals 4
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Lorg/json/JSONException;
        }
    .end annotation

    .line 46
    new-instance v0, Lorg/json/JSONObject;

    invoke-direct {v0}, Lorg/json/JSONObject;-><init>()V

    .line 47
    const-string v1, "id"

    const-string v2, ""

    invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 48
    const-string v1, "display_name"

    invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 49
    const-string v1, "icon"

    invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 50
    const-string v1, "aod_icon"

    invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 51
    const-string v1, "config_file"

    invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 52
    const-string v1, "file_hash"

    invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 53
    const-string v1, "file_size"

    const/4 v3, 0x0

    invoke-virtual {v0, v1, v3}, Lorg/json/JSONObject;->put(Ljava/lang/String;I)Lorg/json/JSONObject;

    .line 54
    const-string v1, "download_count"

    invoke-virtual {v0, v1, v3}, Lorg/json/JSONObject;->put(Ljava/lang/String;I)Lorg/json/JSONObject;

    .line 55
    const-string v1, "spu_id"

    invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 56
    const-string v1, "spu_version"

    invoke-virtual {v0, v1, v3}, Lorg/json/JSONObject;->put(Ljava/lang/String;I)Lorg/json/JSONObject;

    .line 57
    const-string v1, "sku_id"

    invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 58
    const-string v1, "purchase_type"

    invoke-virtual {v0, v1, v3}, Lorg/json/JSONObject;->put(Ljava/lang/String;I)Lorg/json/JSONObject;

    .line 59
    const-string v1, "purchase_status"

    invoke-virtual {v0, v1, v3}, Lorg/json/JSONObject;->put(Ljava/lang/String;I)Lorg/json/JSONObject;

    .line 60
    const-string v1, "price"

    invoke-virtual {v0, v1, v3}, Lorg/json/JSONObject;->put(Ljava/lang/String;I)Lorg/json/JSONObject;

    .line 61
    const-string v1, "status"

    invoke-virtual {v0, v1, v3}, Lorg/json/JSONObject;->put(Ljava/lang/String;I)Lorg/json/JSONObject;

    .line 62
    const-string v1, "is_multi_icon"

    invoke-virtual {v0, v1, v3}, Lorg/json/JSONObject;->put(Ljava/lang/String;Z)Lorg/json/JSONObject;

    .line 63
    const-string v1, "acq_method"

    invoke-virtual {v0, v1, v3}, Lorg/json/JSONObject;->put(Ljava/lang/String;I)Lorg/json/JSONObject;

    .line 64
    const-string v1, "file_hash_v2"

    invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 65
    const-string v1, "file_size_v2"

    invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 67
    const-string v1, "ble_ext"

    invoke-direct {p0}, Lcom/markus/OkCallback;->createEmptyBleExt()Lorg/json/JSONObject;

    move-result-object v2

    invoke-virtual {v0, v1, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    return-object v0
.end method

.method private getIconList(Ljava/lang/String;Ljava/lang/String;)Lorg/json/JSONArray;
    .locals 11
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Lorg/json/JSONException;
        }
    .end annotation

    .line 110
    new-instance v0, Lorg/json/JSONArray;

    invoke-direct {v0}, Lorg/json/JSONArray;-><init>()V

    const/4 v1, 0x1

    const/4 v2, 0x1

    .line 114
    :cond_0
    new-instance v3, Lorg/json/JSONObject;

    invoke-direct {v3}, Lorg/json/JSONObject;-><init>()V

    .line 115
    iget-object v4, p0, Lcom/markus/OkCallback;->fmt_animated:Ljava/lang/String;

    invoke-static {v2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v5

    const/4 v6, 0x3

    new-array v7, v6, [Ljava/lang/Object;

    const/4 v8, 0x0

    aput-object p1, v7, v8

    aput-object p2, v7, v1

    const/4 v9, 0x2

    aput-object v5, v7, v9

    invoke-static {v4, v7}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v4

    .line 116
    iget-object v5, p0, Lcom/markus/OkCallback;->fmt_static:Ljava/lang/String;

    invoke-static {v2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v7

    new-array v10, v6, [Ljava/lang/Object;

    aput-object p1, v10, v8

    aput-object p2, v10, v1

    aput-object v7, v10, v9

    invoke-static {v5, v10}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v5

    .line 117
    iget-object v7, p0, Lcom/markus/OkCallback;->fmt_aod:Ljava/lang/String;

    invoke-static {v2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v10

    new-array v6, v6, [Ljava/lang/Object;

    aput-object p1, v6, v8

    aput-object p2, v6, v1

    aput-object v10, v6, v9

    invoke-static {v7, v6}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v6

    .line 118
    new-array v7, v1, [Ljava/lang/String;

    aput-object v4, v7, v8

    invoke-static {v7}, Lcom/markus/a;->fileExists([Ljava/lang/String;)Z

    move-result v7

    const-string v9, "icon"

    const-string v10, "https://device.local/%s"

    if-eqz v7, :cond_1

    .line 120
    new-array v5, v1, [Ljava/lang/Object;

    aput-object v4, v5, v8

    invoke-static {v10, v5}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v4

    invoke-virtual {v3, v9, v4}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    :goto_0
    const/4 v4, 0x1

    goto :goto_1

    .line 121
    :cond_1
    new-array v4, v1, [Ljava/lang/String;

    aput-object v5, v4, v8

    invoke-static {v4}, Lcom/markus/a;->fileExists([Ljava/lang/String;)Z

    move-result v4

    if-eqz v4, :cond_2

    .line 123
    new-array v4, v1, [Ljava/lang/Object;

    aput-object v5, v4, v8

    invoke-static {v10, v4}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v4

    invoke-virtual {v3, v9, v4}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    goto :goto_0

    :cond_2
    const/4 v4, 0x0

    .line 126
    :goto_1
    new-array v5, v1, [Ljava/lang/String;

    aput-object v6, v5, v8

    invoke-static {v5}, Lcom/markus/a;->fileExists([Ljava/lang/String;)Z

    move-result v5

    if-eqz v5, :cond_3

    .line 128
    new-array v4, v1, [Ljava/lang/Object;

    aput-object v6, v4, v8

    invoke-static {v10, v4}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v4

    const-string v5, "aod_icon"

    invoke-virtual {v3, v5, v4}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    const/4 v4, 0x1

    :cond_3
    if-eqz v4, :cond_4

    .line 132
    invoke-virtual {v0, v3}, Lorg/json/JSONArray;->put(Ljava/lang/Object;)Lorg/json/JSONArray;

    :cond_4
    add-int/lit8 v2, v2, 0x1

    if-nez v4, :cond_0

    return-object v0
.end method

.method private getMultiIconList(Ljava/lang/String;Ljava/lang/String;)Lorg/json/JSONArray;
    .locals 11
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Lorg/json/JSONException;
        }
    .end annotation

    .line 76
    new-instance v0, Lorg/json/JSONArray;

    invoke-direct {v0}, Lorg/json/JSONArray;-><init>()V

    const/4 v1, 0x1

    const/4 v2, 0x1

    .line 80
    :cond_0
    new-instance v3, Lorg/json/JSONObject;

    invoke-direct {v3}, Lorg/json/JSONObject;-><init>()V

    .line 81
    iget-object v4, p0, Lcom/markus/OkCallback;->fmt_animated:Ljava/lang/String;

    invoke-static {v2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v5

    const/4 v6, 0x3

    new-array v7, v6, [Ljava/lang/Object;

    const/4 v8, 0x0

    aput-object p1, v7, v8

    aput-object p2, v7, v1

    const/4 v9, 0x2

    aput-object v5, v7, v9

    invoke-static {v4, v7}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v4

    .line 82
    iget-object v5, p0, Lcom/markus/OkCallback;->fmt_static:Ljava/lang/String;

    invoke-static {v2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v7

    new-array v10, v6, [Ljava/lang/Object;

    aput-object p1, v10, v8

    aput-object p2, v10, v1

    aput-object v7, v10, v9

    invoke-static {v5, v10}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v5

    .line 83
    iget-object v7, p0, Lcom/markus/OkCallback;->fmt_aod:Ljava/lang/String;

    invoke-static {v2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v10

    new-array v6, v6, [Ljava/lang/Object;

    aput-object p1, v6, v8

    aput-object p2, v6, v1

    aput-object v10, v6, v9

    invoke-static {v7, v6}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v6

    .line 84
    new-array v7, v1, [Ljava/lang/String;

    aput-object v4, v7, v8

    invoke-static {v7}, Lcom/markus/a;->fileExists([Ljava/lang/String;)Z

    move-result v7

    const-string v9, "https://device.local/%s"

    if-eqz v7, :cond_1

    .line 86
    new-array v7, v1, [Ljava/lang/Object;

    aput-object v4, v7, v8

    invoke-static {v9, v7}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v4

    const-string v7, "animated_icon"

    invoke-virtual {v3, v7, v4}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    const/4 v4, 0x1

    goto :goto_0

    :cond_1
    const/4 v4, 0x0

    .line 88
    :goto_0
    new-array v7, v1, [Ljava/lang/String;

    aput-object v5, v7, v8

    invoke-static {v7}, Lcom/markus/a;->fileExists([Ljava/lang/String;)Z

    move-result v7

    if-eqz v7, :cond_2

    .line 90
    new-array v4, v1, [Ljava/lang/Object;

    aput-object v5, v4, v8

    invoke-static {v9, v4}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v4

    const-string v5, "static_icon"

    invoke-virtual {v3, v5, v4}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    const/4 v4, 0x1

    .line 93
    :cond_2
    new-array v5, v1, [Ljava/lang/String;

    aput-object v6, v5, v8

    invoke-static {v5}, Lcom/markus/a;->fileExists([Ljava/lang/String;)Z

    move-result v5

    if-eqz v5, :cond_3

    .line 95
    new-array v4, v1, [Ljava/lang/Object;

    aput-object v6, v4, v8

    invoke-static {v9, v4}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v4

    const-string v5, "aod_icon"

    invoke-virtual {v3, v5, v4}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    const/4 v4, 0x1

    :cond_3
    if-eqz v4, :cond_4

    .line 99
    invoke-virtual {v0, v3}, Lorg/json/JSONArray;->put(Ljava/lang/Object;)Lorg/json/JSONArray;

    :cond_4
    add-int/lit8 v2, v2, 0x1

    if-nez v4, :cond_0

    return-object v0
.end method

.method private handleOwnedTab(Lokhttp3/Call;Lokhttp3/Response;)Lokhttp3/Response;
    .locals 10
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;,
            Lorg/json/JSONException;
        }
    .end annotation

    .line 314
    invoke-virtual {p2}, Lokhttp3/Response;->request()Lokhttp3/Request;

    move-result-object p1

    invoke-virtual {p1}, Lokhttp3/Request;->url()Lokhttp3/HttpUrl;

    move-result-object p1

    .line 315
    const-string v0, "_nonce"

    invoke-virtual {p1, v0}, Lokhttp3/HttpUrl;->queryParameter(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 316
    const-string v1, "data"

    invoke-virtual {p1, v1}, Lokhttp3/HttpUrl;->queryParameter(Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    .line 317
    invoke-static {}, Lcom/markus/a;->getSecurity()Ljava/lang/String;

    move-result-object v1

    if-nez p1, :cond_0

    return-object p2

    .line 323
    :cond_0
    invoke-static {v1, v0, p1}, Lcom/markus/Rc4Codec;->decode(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    .line 324
    new-instance v2, Lorg/json/JSONObject;

    invoke-direct {v2, p1}, Lorg/json/JSONObject;-><init>(Ljava/lang/String;)V

    const-wide v3, 0x7fffffffffffffffL

    .line 326
    invoke-virtual {p2, v3, v4}, Lokhttp3/Response;->peekBody(J)Lokhttp3/ResponseBody;

    move-result-object p1

    if-nez p1, :cond_1

    return-object p2

    .line 331
    :cond_1
    new-instance v3, Lorg/json/JSONObject;

    invoke-virtual {p1}, Lokhttp3/ResponseBody;->string()Ljava/lang/String;

    move-result-object v4

    invoke-static {v1, v0, v4}, Lcom/markus/Rc4Codec;->decode(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v4

    invoke-direct {v3, v4}, Lorg/json/JSONObject;-><init>(Ljava/lang/String;)V

    .line 332
    const-string v4, "result"

    invoke-virtual {v3, v4}, Lorg/json/JSONObject;->getJSONObject(Ljava/lang/String;)Lorg/json/JSONObject;

    move-result-object v4

    if-nez v4, :cond_2

    return-object p2

    .line 337
    :cond_2
    new-instance v5, Ljava/util/ArrayList;

    invoke-direct {v5}, Ljava/util/ArrayList;-><init>()V

    .line 338
    const-string v6, "owned_ids"

    invoke-virtual {v2, v6}, Lorg/json/JSONObject;->optJSONArray(Ljava/lang/String;)Lorg/json/JSONArray;

    move-result-object v2

    if-nez v2, :cond_3

    return-object p2

    :cond_3
    const/4 v6, 0x0

    const/4 v7, 0x0

    .line 343
    :goto_0
    invoke-virtual {v2}, Lorg/json/JSONArray;->length()I

    move-result v8

    if-ge v7, v8, :cond_4

    .line 344
    invoke-virtual {v2, v7}, Lorg/json/JSONArray;->getString(I)Ljava/lang/String;

    move-result-object v8

    invoke-interface {v5, v8}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    add-int/lit8 v7, v7, 0x1

    goto :goto_0

    .line 347
    :cond_4
    invoke-interface {v5}, Ljava/util/List;->isEmpty()Z

    move-result v2

    if-eqz v2, :cond_5

    return-object p2

    .line 353
    :cond_5
    const-string v2, "watchface_list"

    invoke-virtual {v4, v2}, Lorg/json/JSONObject;->getJSONArray(Ljava/lang/String;)Lorg/json/JSONArray;

    move-result-object v2

    const/4 v4, 0x0

    .line 354
    :goto_1
    invoke-virtual {v2}, Lorg/json/JSONArray;->length()I

    move-result v7

    const/4 v8, 0x1

    if-ge v6, v7, :cond_7

    .line 355
    invoke-virtual {v2, v6}, Lorg/json/JSONArray;->getJSONObject(I)Lorg/json/JSONObject;

    move-result-object v7

    .line 356
    const-string v9, "id"

    invoke-virtual {v7, v9}, Lorg/json/JSONObject;->getString(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v9

    .line 357
    invoke-interface {v5, v9}, Ljava/util/List;->remove(Ljava/lang/Object;)Z

    .line 358
    invoke-direct {p0, v9, v7}, Lcom/markus/OkCallback;->patchEntry(Ljava/lang/String;Lorg/json/JSONObject;)Z

    move-result v7

    if-eqz v7, :cond_6

    const/4 v4, 0x1

    :cond_6
    add-int/lit8 v6, v6, 0x1

    goto :goto_1

    .line 363
    :cond_7
    invoke-interface {v5}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v5

    :cond_8
    :goto_2
    invoke-interface {v5}, Ljava/util/Iterator;->hasNext()Z

    move-result v6

    if-eqz v6, :cond_9

    invoke-interface {v5}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v6

    check-cast v6, Ljava/lang/String;

    .line 364
    invoke-direct {p0}, Lcom/markus/OkCallback;->createEmptyWatchface()Lorg/json/JSONObject;

    move-result-object v7

    .line 365
    invoke-direct {p0, v6, v7}, Lcom/markus/OkCallback;->patchEntry(Ljava/lang/String;Lorg/json/JSONObject;)Z

    move-result v6

    if-eqz v6, :cond_8

    .line 367
    invoke-virtual {v2, v7}, Lorg/json/JSONArray;->put(Ljava/lang/Object;)Lorg/json/JSONArray;

    const/4 v4, 0x1

    goto :goto_2

    :cond_9
    if-nez v4, :cond_a

    return-object p2

    .line 382
    :cond_a
    invoke-virtual {p1}, Lokhttp3/ResponseBody;->contentType()Lokhttp3/MediaType;

    move-result-object p1

    .line 383
    invoke-virtual {v3}, Lorg/json/JSONObject;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v0, v2}, Lcom/markus/Rc4Codec;->encode(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 381
    invoke-static {p1, v0}, Lokhttp3/ResponseBody;->create(Lokhttp3/MediaType;Ljava/lang/String;)Lokhttp3/ResponseBody;

    move-result-object p1

    .line 386
    invoke-virtual {p2}, Lokhttp3/Response;->newBuilder()Lokhttp3/Response$Builder;

    move-result-object p2

    invoke-virtual {p2, p1}, Lokhttp3/Response$Builder;->body(Lokhttp3/ResponseBody;)Lokhttp3/Response$Builder;

    move-result-object p1

    invoke-virtual {p1}, Lokhttp3/Response$Builder;->build()Lokhttp3/Response;

    move-result-object p1

    return-object p1
.end method

.method private handlePaidWatchface(Lokhttp3/Call;Lokhttp3/Response;)Lokhttp3/Response;
    .locals 4
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;,
            Lorg/json/JSONException;
        }
    .end annotation

    .line 212
    invoke-virtual {p2}, Lokhttp3/Response;->request()Lokhttp3/Request;

    move-result-object p1

    invoke-virtual {p1}, Lokhttp3/Request;->url()Lokhttp3/HttpUrl;

    move-result-object p1

    .line 213
    const-string v0, "_nonce"

    invoke-virtual {p1, v0}, Lokhttp3/HttpUrl;->queryParameter(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 214
    const-string v1, "data"

    invoke-virtual {p1, v1}, Lokhttp3/HttpUrl;->queryParameter(Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    .line 215
    invoke-static {}, Lcom/markus/a;->getSecurity()Ljava/lang/String;

    move-result-object v1

    if-nez p1, :cond_0

    return-object p2

    .line 221
    :cond_0
    invoke-static {v1, v0, p1}, Lcom/markus/Rc4Codec;->decode(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    .line 222
    new-instance v0, Lorg/json/JSONObject;

    invoke-direct {v0, p1}, Lorg/json/JSONObject;-><init>(Ljava/lang/String;)V

    .line 223
    const-string p1, "id"

    invoke-virtual {v0, p1}, Lorg/json/JSONObject;->getString(Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    const-wide v0, 0x7fffffffffffffffL

    .line 225
    invoke-virtual {p2, v0, v1}, Lokhttp3/Response;->peekBody(J)Lokhttp3/ResponseBody;

    move-result-object v0

    if-nez v0, :cond_1

    return-object p2

    .line 230
    :cond_1
    new-instance v1, Lorg/json/JSONObject;

    invoke-virtual {v0}, Lokhttp3/ResponseBody;->string()Ljava/lang/String;

    move-result-object v2

    invoke-direct {v1, v2}, Lorg/json/JSONObject;-><init>(Ljava/lang/String;)V

    .line 231
    const-string v2, "result"

    invoke-virtual {v1, v2}, Lorg/json/JSONObject;->getJSONObject(Ljava/lang/String;)Lorg/json/JSONObject;

    move-result-object v2

    if-nez v2, :cond_2

    return-object p2

    .line 236
    :cond_2
    const-string v3, "watch_face"

    invoke-virtual {v2, v3}, Lorg/json/JSONObject;->getJSONObject(Ljava/lang/String;)Lorg/json/JSONObject;

    move-result-object v3

    if-nez v3, :cond_3

    .line 238
    const-string v3, "watchface"

    invoke-virtual {v2, v3}, Lorg/json/JSONObject;->getJSONObject(Ljava/lang/String;)Lorg/json/JSONObject;

    move-result-object v3

    :cond_3
    if-nez v3, :cond_4

    return-object p2

    .line 244
    :cond_4
    invoke-direct {p0, p1, v3}, Lcom/markus/OkCallback;->patchEntry(Ljava/lang/String;Lorg/json/JSONObject;)Z

    move-result p1

    if-nez p1, :cond_5

    return-object p2

    .line 255
    :cond_5
    invoke-virtual {v0}, Lokhttp3/ResponseBody;->contentType()Lokhttp3/MediaType;

    move-result-object p1

    .line 256
    invoke-virtual {v1}, Lorg/json/JSONObject;->toString()Ljava/lang/String;

    move-result-object v0

    .line 254
    invoke-static {p1, v0}, Lokhttp3/ResponseBody;->create(Lokhttp3/MediaType;Ljava/lang/String;)Lokhttp3/ResponseBody;

    move-result-object p1

    .line 259
    invoke-virtual {p2}, Lokhttp3/Response;->newBuilder()Lokhttp3/Response$Builder;

    move-result-object p2

    invoke-virtual {p2, p1}, Lokhttp3/Response$Builder;->body(Lokhttp3/ResponseBody;)Lokhttp3/Response$Builder;

    move-result-object p1

    invoke-virtual {p1}, Lokhttp3/Response$Builder;->build()Lokhttp3/Response;

    move-result-object p1

    return-object p1
.end method

.method private handleWatchface(Lokhttp3/Call;Lokhttp3/Response;)Lokhttp3/Response;
    .locals 6
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;,
            Lorg/json/JSONException;
        }
    .end annotation

    .line 263
    invoke-virtual {p2}, Lokhttp3/Response;->request()Lokhttp3/Request;

    move-result-object p1

    invoke-virtual {p1}, Lokhttp3/Request;->url()Lokhttp3/HttpUrl;

    move-result-object p1

    .line 264
    const-string v0, "_nonce"

    invoke-virtual {p1, v0}, Lokhttp3/HttpUrl;->queryParameter(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 265
    const-string v1, "data"

    invoke-virtual {p1, v1}, Lokhttp3/HttpUrl;->queryParameter(Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    .line 266
    invoke-static {}, Lcom/markus/a;->getSecurity()Ljava/lang/String;

    move-result-object v1

    if-nez p1, :cond_0

    return-object p2

    .line 272
    :cond_0
    invoke-static {v1, v0, p1}, Lcom/markus/Rc4Codec;->decode(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    .line 273
    new-instance v2, Lorg/json/JSONObject;

    invoke-direct {v2, p1}, Lorg/json/JSONObject;-><init>(Ljava/lang/String;)V

    .line 274
    const-string p1, "id"

    invoke-virtual {v2, p1}, Lorg/json/JSONObject;->getString(Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    const-wide v2, 0x7fffffffffffffffL

    .line 276
    invoke-virtual {p2, v2, v3}, Lokhttp3/Response;->peekBody(J)Lokhttp3/ResponseBody;

    move-result-object v2

    if-nez v2, :cond_1

    return-object p2

    .line 281
    :cond_1
    new-instance v3, Lorg/json/JSONObject;

    invoke-virtual {v2}, Lokhttp3/ResponseBody;->string()Ljava/lang/String;

    move-result-object v4

    invoke-static {v1, v0, v4}, Lcom/markus/Rc4Codec;->decode(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v4

    invoke-direct {v3, v4}, Lorg/json/JSONObject;-><init>(Ljava/lang/String;)V

    .line 282
    const-string v4, "result"

    invoke-virtual {v3, v4}, Lorg/json/JSONObject;->getJSONObject(Ljava/lang/String;)Lorg/json/JSONObject;

    move-result-object v4

    if-nez v4, :cond_2

    return-object p2

    .line 287
    :cond_2
    const-string v5, "watch_face"

    invoke-virtual {v4, v5}, Lorg/json/JSONObject;->optJSONObject(Ljava/lang/String;)Lorg/json/JSONObject;

    move-result-object v5

    if-nez v5, :cond_3

    .line 289
    const-string v5, "watchface"

    invoke-virtual {v4, v5}, Lorg/json/JSONObject;->optJSONObject(Ljava/lang/String;)Lorg/json/JSONObject;

    move-result-object v5

    :cond_3
    if-nez v5, :cond_4

    return-object p2

    .line 295
    :cond_4
    invoke-direct {p0, p1, v5}, Lcom/markus/OkCallback;->patchEntry(Ljava/lang/String;Lorg/json/JSONObject;)Z

    move-result p1

    if-nez p1, :cond_5

    return-object p2

    .line 306
    :cond_5
    invoke-virtual {v2}, Lokhttp3/ResponseBody;->contentType()Lokhttp3/MediaType;

    move-result-object p1

    .line 307
    invoke-virtual {v3}, Lorg/json/JSONObject;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v0, v2}, Lcom/markus/Rc4Codec;->encode(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 305
    invoke-static {p1, v0}, Lokhttp3/ResponseBody;->create(Lokhttp3/MediaType;Ljava/lang/String;)Lokhttp3/ResponseBody;

    move-result-object p1

    .line 310
    invoke-virtual {p2}, Lokhttp3/Response;->newBuilder()Lokhttp3/Response$Builder;

    move-result-object p2

    invoke-virtual {p2, p1}, Lokhttp3/Response$Builder;->body(Lokhttp3/ResponseBody;)Lokhttp3/Response$Builder;

    move-result-object p1

    invoke-virtual {p1}, Lokhttp3/Response$Builder;->build()Lokhttp3/Response;

    move-result-object p1

    return-object p1
.end method

.method private handleWatchfaceList(Lokhttp3/Call;Lokhttp3/Response;)Lokhttp3/Response;
    .locals 10
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;,
            Lorg/json/JSONException;
        }
    .end annotation

    .line 390
    invoke-virtual {p2}, Lokhttp3/Response;->request()Lokhttp3/Request;

    move-result-object p1

    invoke-virtual {p1}, Lokhttp3/Request;->url()Lokhttp3/HttpUrl;

    move-result-object p1

    .line 391
    const-string v0, "_nonce"

    invoke-virtual {p1, v0}, Lokhttp3/HttpUrl;->queryParameter(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 392
    const-string v1, "data"

    invoke-virtual {p1, v1}, Lokhttp3/HttpUrl;->queryParameter(Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    .line 393
    invoke-static {}, Lcom/markus/a;->getSecurity()Ljava/lang/String;

    move-result-object v1

    if-nez p1, :cond_0

    return-object p2

    .line 399
    :cond_0
    invoke-static {v1, v0, p1}, Lcom/markus/Rc4Codec;->decode(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    .line 400
    new-instance v2, Lorg/json/JSONObject;

    invoke-direct {v2, p1}, Lorg/json/JSONObject;-><init>(Ljava/lang/String;)V

    const-wide v3, 0x7fffffffffffffffL

    .line 403
    invoke-virtual {p2, v3, v4}, Lokhttp3/Response;->peekBody(J)Lokhttp3/ResponseBody;

    move-result-object p1

    if-nez p1, :cond_1

    return-object p2

    .line 409
    :cond_1
    invoke-virtual {p1}, Lokhttp3/ResponseBody;->string()Ljava/lang/String;

    move-result-object v3

    .line 410
    invoke-static {v3}, Lcom/markus/a;->isBase64(Ljava/lang/String;)Z

    move-result v4

    if-eqz v4, :cond_2

    .line 411
    invoke-static {v1, v0, v3}, Lcom/markus/Rc4Codec;->decode(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v3

    .line 418
    :cond_2
    new-instance v4, Lorg/json/JSONObject;

    invoke-direct {v4, v3}, Lorg/json/JSONObject;-><init>(Ljava/lang/String;)V

    .line 419
    const-string v3, "result"

    invoke-virtual {v4, v3}, Lorg/json/JSONObject;->getJSONObject(Ljava/lang/String;)Lorg/json/JSONObject;

    move-result-object v3

    if-nez v3, :cond_3

    return-object p2

    .line 424
    :cond_3
    new-instance v5, Ljava/util/ArrayList;

    invoke-direct {v5}, Ljava/util/ArrayList;-><init>()V

    .line 425
    const-string v6, "id_list"

    invoke-virtual {v2, v6}, Lorg/json/JSONObject;->optJSONArray(Ljava/lang/String;)Lorg/json/JSONArray;

    move-result-object v2

    if-nez v2, :cond_4

    return-object p2

    :cond_4
    const/4 v6, 0x0

    const/4 v7, 0x0

    .line 430
    :goto_0
    invoke-virtual {v2}, Lorg/json/JSONArray;->length()I

    move-result v8

    if-ge v7, v8, :cond_5

    .line 431
    invoke-virtual {v2, v7}, Lorg/json/JSONArray;->getString(I)Ljava/lang/String;

    move-result-object v8

    invoke-interface {v5, v8}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    add-int/lit8 v7, v7, 0x1

    goto :goto_0

    .line 434
    :cond_5
    invoke-interface {v5}, Ljava/util/List;->isEmpty()Z

    move-result v2

    if-eqz v2, :cond_6

    return-object p2

    .line 440
    :cond_6
    const-string v2, "watchface_list"

    invoke-virtual {v3, v2}, Lorg/json/JSONObject;->getJSONArray(Ljava/lang/String;)Lorg/json/JSONArray;

    move-result-object v2

    const/4 v3, 0x0

    .line 441
    :goto_1
    invoke-virtual {v2}, Lorg/json/JSONArray;->length()I

    move-result v7

    const/4 v8, 0x1

    if-ge v6, v7, :cond_8

    .line 442
    invoke-virtual {v2, v6}, Lorg/json/JSONArray;->getJSONObject(I)Lorg/json/JSONObject;

    move-result-object v7

    .line 443
    const-string v9, "id"

    invoke-virtual {v7, v9}, Lorg/json/JSONObject;->getString(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v9

    .line 444
    invoke-interface {v5, v9}, Ljava/util/List;->remove(Ljava/lang/Object;)Z

    .line 445
    invoke-direct {p0, v9, v7}, Lcom/markus/OkCallback;->patchEntry(Ljava/lang/String;Lorg/json/JSONObject;)Z

    move-result v7

    if-eqz v7, :cond_7

    const/4 v3, 0x1

    :cond_7
    add-int/lit8 v6, v6, 0x1

    goto :goto_1

    .line 450
    :cond_8
    invoke-interface {v5}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v5

    :cond_9
    :goto_2
    invoke-interface {v5}, Ljava/util/Iterator;->hasNext()Z

    move-result v6

    if-eqz v6, :cond_a

    invoke-interface {v5}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v6

    check-cast v6, Ljava/lang/String;

    .line 451
    invoke-direct {p0}, Lcom/markus/OkCallback;->createEmptyWatchface()Lorg/json/JSONObject;

    move-result-object v7

    .line 452
    invoke-direct {p0, v6, v7}, Lcom/markus/OkCallback;->patchEntry(Ljava/lang/String;Lorg/json/JSONObject;)Z

    move-result v6

    if-eqz v6, :cond_9

    .line 454
    invoke-virtual {v2, v7}, Lorg/json/JSONArray;->put(Ljava/lang/Object;)Lorg/json/JSONArray;

    const/4 v3, 0x1

    goto :goto_2

    :cond_a
    if-nez v3, :cond_b

    return-object p2

    .line 469
    :cond_b
    invoke-virtual {p1}, Lokhttp3/ResponseBody;->contentType()Lokhttp3/MediaType;

    move-result-object p1

    .line 470
    invoke-virtual {v4}, Lorg/json/JSONObject;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v0, v2}, Lcom/markus/Rc4Codec;->encode(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    .line 468
    invoke-static {p1, v0}, Lokhttp3/ResponseBody;->create(Lokhttp3/MediaType;Ljava/lang/String;)Lokhttp3/ResponseBody;

    move-result-object p1

    .line 473
    invoke-virtual {p2}, Lokhttp3/Response;->newBuilder()Lokhttp3/Response$Builder;

    move-result-object p2

    invoke-virtual {p2, p1}, Lokhttp3/Response$Builder;->body(Lokhttp3/ResponseBody;)Lokhttp3/Response$Builder;

    move-result-object p1

    invoke-virtual {p1}, Lokhttp3/Response$Builder;->build()Lokhttp3/Response;

    move-result-object p1

    return-object p1
.end method

.method private patchEntry(Ljava/lang/String;Lorg/json/JSONObject;)Z
    .locals 18
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Lorg/json/JSONException;
        }
    .end annotation

    move-object/from16 v1, p0

    move-object/from16 v2, p1

    move-object/from16 v3, p2

    .line 140
    const-string v4, "OkCallback"

    const-string v0, "id"

    invoke-virtual {v3, v0, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 142
    invoke-static {}, Lcom/markus/a;->getCurrentDevice()Lcom/xiaomi/fitness/device/manager/DeviceModel;

    move-result-object v0

    invoke-interface {v0}, Lcom/xiaomi/fitness/device/manager/DeviceModel;->getDid()Ljava/lang/String;

    move-result-object v5

    const/4 v6, 0x5

    .line 143
    new-array v0, v6, [Ljava/lang/String;

    const/4 v7, 0x0

    const-string v8, "files"

    aput-object v8, v0, v7

    const/4 v9, 0x1

    const-string v10, "WatchFace"

    aput-object v10, v0, v9

    const/4 v11, 0x2

    aput-object v5, v0, v11

    const/4 v12, 0x3

    aput-object v2, v0, v12

    const/4 v13, 0x4

    const-string v14, "local"

    aput-object v14, v0, v13

    invoke-static {v0}, Lcom/markus/a;->fileExists([Ljava/lang/String;)Z

    move-result v0

    if-nez v0, :cond_0

    return v7

    :cond_0
    const/4 v15, 0x6

    .line 147
    new-array v0, v15, [Ljava/lang/String;

    aput-object v8, v0, v7

    aput-object v10, v0, v9

    aput-object v5, v0, v11

    aput-object v2, v0, v12

    aput-object v14, v0, v13

    const-string v16, "resource.bin"

    aput-object v16, v0, v6

    invoke-static {v0}, Lcom/markus/a;->getFile([Ljava/lang/String;)Ljava/io/File;

    move-result-object v0

    .line 148
    invoke-virtual {v0}, Ljava/io/File;->exists()Z

    move-result v16

    if-eqz v16, :cond_1

    .line 150
    :try_start_0
    invoke-virtual {v0}, Ljava/io/File;->toPath()Ljava/nio/file/Path;

    move-result-object v0
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_0

    move-object/from16 v17, v14

    :try_start_1
    invoke-static {v0}, Ljava/nio/file/Files;->size(Ljava/nio/file/Path;)J

    move-result-wide v13

    .line 151
    const-string v0, "file_size"

    invoke-virtual {v3, v0, v13, v14}, Lorg/json/JSONObject;->put(Ljava/lang/String;J)Lorg/json/JSONObject;

    .line 152
    const-string v0, "file_size_v2"

    invoke-virtual {v3, v0, v13, v14}, Lorg/json/JSONObject;->put(Ljava/lang/String;J)Lorg/json/JSONObject;
    :try_end_1
    .catch Ljava/io/IOException; {:try_start_1 .. :try_end_1} :catch_1

    goto :goto_0

    :catch_0
    move-object/from16 v17, v14

    :catch_1
    nop

    goto :goto_0

    :cond_1
    move-object/from16 v17, v14

    .line 158
    :goto_0
    new-array v0, v15, [Ljava/lang/String;

    aput-object v8, v0, v7

    aput-object v10, v0, v9

    aput-object v5, v0, v11

    aput-object v2, v0, v12

    const/4 v13, 0x4

    aput-object v17, v0, v13

    const-string v13, "description.xml"

    aput-object v13, v0, v6

    invoke-static {v0}, Lcom/markus/a;->getFile([Ljava/lang/String;)Ljava/io/File;

    move-result-object v0

    .line 160
    invoke-virtual {v0}, Ljava/io/File;->exists()Z

    move-result v13

    if-eqz v13, :cond_6

    .line 161
    :try_start_2
    invoke-virtual {v0}, Ljava/io/File;->toPath()Ljava/nio/file/Path;

    move-result-object v0

    new-array v13, v7, [Ljava/nio/file/OpenOption;

    invoke-static {v0, v13}, Ljava/nio/file/Files;->newInputStream(Ljava/nio/file/Path;[Ljava/nio/file/OpenOption;)Ljava/io/InputStream;

    move-result-object v13
    :try_end_2
    .catch Ljava/io/IOException; {:try_start_2 .. :try_end_2} :catch_3
    .catch Ljava/lang/Exception; {:try_start_2 .. :try_end_2} :catch_2

    .line 162
    :try_start_3
    invoke-static {}, Landroid/util/Xml;->newPullParser()Lorg/xmlpull/v1/XmlPullParser;

    move-result-object v0

    .line 163
    const-string v14, "UTF-8"

    invoke-interface {v0, v13, v14}, Lorg/xmlpull/v1/XmlPullParser;->setInput(Ljava/io/InputStream;Ljava/lang/String;)V

    .line 165
    invoke-interface {v0}, Lorg/xmlpull/v1/XmlPullParser;->getEventType()I

    move-result v14

    :goto_1
    if-eq v14, v9, :cond_4

    if-ne v14, v11, :cond_3

    .line 169
    invoke-interface {v0}, Lorg/xmlpull/v1/XmlPullParser;->getName()Ljava/lang/String;

    move-result-object v14

    .line 172
    const-string v15, "name"

    invoke-virtual {v15, v14}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v15

    if-eqz v15, :cond_2

    .line 173
    const-string v14, "display_name"

    invoke-interface {v0}, Lorg/xmlpull/v1/XmlPullParser;->nextText()Ljava/lang/String;

    move-result-object v15

    invoke-virtual {v3, v14, v15}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    goto :goto_2

    .line 174
    :cond_2
    const-string v15, "author"

    invoke-virtual {v15, v14}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v14

    if-eqz v14, :cond_3

    .line 175
    const-string v14, "publish_name"

    invoke-interface {v0}, Lorg/xmlpull/v1/XmlPullParser;->nextText()Ljava/lang/String;

    move-result-object v15

    invoke-virtual {v3, v14, v15}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 179
    :cond_3
    :goto_2
    invoke-interface {v0}, Lorg/xmlpull/v1/XmlPullParser;->next()I

    move-result v14
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_0

    const/4 v15, 0x6

    goto :goto_1

    :cond_4
    if-eqz v13, :cond_6

    .line 181
    :try_start_4
    invoke-virtual {v13}, Ljava/io/InputStream;->close()V
    :try_end_4
    .catch Ljava/io/IOException; {:try_start_4 .. :try_end_4} :catch_3
    .catch Ljava/lang/Exception; {:try_start_4 .. :try_end_4} :catch_2

    goto :goto_4

    :catchall_0
    move-exception v0

    move-object v14, v0

    if-eqz v13, :cond_5

    .line 161
    :try_start_5
    invoke-virtual {v13}, Ljava/io/InputStream;->close()V
    :try_end_5
    .catchall {:try_start_5 .. :try_end_5} :catchall_1

    goto :goto_3

    :catchall_1
    move-exception v0

    move-object v13, v0

    :try_start_6
    invoke-static {v14, v13}, Lcom/markus/FilePickerProxyActivity$$ExternalSyntheticBackport0;->m(Ljava/lang/Throwable;Ljava/lang/Throwable;)V

    :cond_5
    :goto_3
    throw v14
    :try_end_6
    .catch Ljava/io/IOException; {:try_start_6 .. :try_end_6} :catch_3
    .catch Ljava/lang/Exception; {:try_start_6 .. :try_end_6} :catch_2

    :catch_2
    move-exception v0

    .line 184
    const-string v13, "failed to parse description.xml with unknown exception"

    invoke-static {v4, v13, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    goto :goto_4

    :catch_3
    move-exception v0

    .line 182
    const-string v13, "failed to parse description.xml"

    invoke-static {v4, v13, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 189
    :cond_6
    :goto_4
    const-string v0, "file_hash"

    move-object/from16 v4, v17

    invoke-virtual {v3, v0, v4}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 190
    const-string v0, "file_hash_v2"

    invoke-virtual {v3, v0, v4}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    const/4 v0, 0x7

    .line 192
    new-array v13, v0, [Ljava/lang/String;

    aput-object v8, v13, v7

    aput-object v10, v13, v9

    aput-object v5, v13, v11

    aput-object v2, v13, v12

    const/4 v14, 0x4

    aput-object v4, v13, v14

    const-string v14, "preview"

    aput-object v14, v13, v6

    const-string v15, "market-preview.png"

    const/16 v17, 0x6

    aput-object v15, v13, v17

    invoke-static {v13}, Lcom/markus/a;->fileExists([Ljava/lang/String;)Z

    move-result v13

    if-eqz v13, :cond_7

    .line 193
    new-array v13, v11, [Ljava/lang/Object;

    aput-object v5, v13, v7

    aput-object v2, v13, v9

    const-string v15, "https://device.local/files/WatchFace/%s/%s/local/preview/market-preview.png"

    invoke-static {v15, v13}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v13

    const-string v15, "icon"

    invoke-virtual {v3, v15, v13}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 196
    :cond_7
    new-array v0, v0, [Ljava/lang/String;

    aput-object v8, v0, v7

    aput-object v10, v0, v9

    aput-object v5, v0, v11

    aput-object v2, v0, v12

    const/4 v8, 0x4

    aput-object v4, v0, v8

    aput-object v14, v0, v6

    const-string v4, "aod-preview.png"

    const/4 v6, 0x6

    aput-object v4, v0, v6

    invoke-static {v0}, Lcom/markus/a;->fileExists([Ljava/lang/String;)Z

    move-result v0

    if-eqz v0, :cond_8

    .line 197
    new-array v0, v11, [Ljava/lang/Object;

    aput-object v5, v0, v7

    aput-object v2, v0, v9

    const-string v4, "https://device.local/files/WatchFace/%s/%s/local/preview/aod-preview.png"

    invoke-static {v4, v0}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v0

    const-string v4, "aod_icon"

    invoke-virtual {v3, v4, v0}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 200
    :cond_8
    const-string v0, "is_multi_icon"

    invoke-virtual {v3, v0, v7}, Lorg/json/JSONObject;->put(Ljava/lang/String;Z)Lorg/json/JSONObject;

    .line 201
    const-string v0, "icon_list"

    invoke-direct {v1, v5, v2}, Lcom/markus/OkCallback;->getIconList(Ljava/lang/String;Ljava/lang/String;)Lorg/json/JSONArray;

    move-result-object v4

    invoke-virtual {v3, v0, v4}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 202
    const-string v0, "multi_icon_item_list"

    invoke-direct {v1, v5, v2}, Lcom/markus/OkCallback;->getMultiIconList(Ljava/lang/String;Ljava/lang/String;)Lorg/json/JSONArray;

    move-result-object v2

    invoke-virtual {v3, v0, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    .line 204
    const-string v0, "status"

    const/16 v2, 0x65

    invoke-virtual {v3, v0, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;I)Lorg/json/JSONObject;

    .line 206
    const-string v0, "ble_ext"

    invoke-direct/range {p0 .. p0}, Lcom/markus/OkCallback;->createEmptyBleExt()Lorg/json/JSONObject;

    move-result-object v2

    invoke-virtual {v3, v0, v2}, Lorg/json/JSONObject;->put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;

    return v9
.end method


# virtual methods
.method public onFailure(Lokhttp3/Call;Ljava/io/IOException;)V
    .locals 1

    .line 30
    iget-object v0, p0, Lcom/markus/OkCallback;->target:Lokhttp3/Callback;

    invoke-interface {v0, p1, p2}, Lokhttp3/Callback;->onFailure(Lokhttp3/Call;Ljava/io/IOException;)V

    return-void
.end method

.method public onResponse(Lokhttp3/Call;Lokhttp3/Response;)V
    .locals 3
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    .line 482
    :try_start_0
    invoke-virtual {p2}, Lokhttp3/Response;->request()Lokhttp3/Request;

    move-result-object v0

    invoke-virtual {v0}, Lokhttp3/Request;->url()Lokhttp3/HttpUrl;

    move-result-object v0

    .line 483
    invoke-virtual {v0}, Ljava/lang/Object;->toString()Ljava/lang/String;

    move-result-object v1

    const-string v2, "paidwatchface/detail"

    invoke-virtual {v1, v2}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v1

    if-eqz v1, :cond_0

    .line 484
    invoke-direct {p0, p1, p2}, Lcom/markus/OkCallback;->handlePaidWatchface(Lokhttp3/Call;Lokhttp3/Response;)Lokhttp3/Response;

    move-result-object p2

    .line 487
    :cond_0
    invoke-virtual {v0}, Ljava/lang/Object;->toString()Ljava/lang/String;

    move-result-object v1

    const-string v2, "/watchface/detail"

    invoke-virtual {v1, v2}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v1

    if-eqz v1, :cond_1

    .line 488
    invoke-direct {p0, p1, p2}, Lcom/markus/OkCallback;->handleWatchface(Lokhttp3/Call;Lokhttp3/Response;)Lokhttp3/Response;

    move-result-object p2

    .line 491
    :cond_1
    invoke-virtual {v0}, Ljava/lang/Object;->toString()Ljava/lang/String;

    move-result-object v1

    const-string v2, "/watchface/tab"

    invoke-virtual {v1, v2}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v1

    if-eqz v1, :cond_2

    .line 492
    invoke-direct {p0, p1, p2}, Lcom/markus/OkCallback;->handleOwnedTab(Lokhttp3/Call;Lokhttp3/Response;)Lokhttp3/Response;

    move-result-object p2

    .line 495
    :cond_2
    invoke-virtual {v0}, Ljava/lang/Object;->toString()Ljava/lang/String;

    move-result-object v0

    const-string v1, "paidwatchface/list"

    invoke-virtual {v0, v1}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v0

    if-eqz v0, :cond_3

    .line 496
    invoke-direct {p0, p1, p2}, Lcom/markus/OkCallback;->handleWatchfaceList(Lokhttp3/Call;Lokhttp3/Response;)Lokhttp3/Response;

    move-result-object p2
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception v0

    .line 500
    const-string v1, "OkCallback"

    const-string v2, "Failed to parse"

    invoke-static {v1, v2, v0}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 503
    :cond_3
    :goto_0
    iget-object v0, p0, Lcom/markus/OkCallback;->target:Lokhttp3/Callback;

    invoke-interface {v0, p1, p2}, Lokhttp3/Callback;->onResponse(Lokhttp3/Call;Lokhttp3/Response;)V

    return-void
.end method
