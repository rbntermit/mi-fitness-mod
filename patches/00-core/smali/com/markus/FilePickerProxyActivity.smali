.class public Lcom/markus/FilePickerProxyActivity;
.super Landroidx/fragment/app/FragmentActivity;
.source "FilePickerProxyActivity.java"


# static fields
.field public static final ACTIVITY_UNIQUE_ID_INSTALL_FIRMWARE:I = 0x79bdf

.field public static final ACTIVITY_UNIQUE_ID_INSTALL_WATCHFACE:I = 0x79bde

.field private static final MAX_SINGLE_FILE_SIZE:J = 0x3200000L

.field private static final MAX_TOTAL_UNCOMPRESSED_SIZE:J = 0x6400000L

.field private static final WATCHFACE_HEADER:[B

.field private static final ZIP_HEADER:[B


# instance fields
.field private checkUpdate:Lcom/mi/fitness/checkupdate/export/CheckUpdateManager;


# direct methods
.method static constructor <clinit>()V
    .locals 2

    const/4 v0, 0x4

    .line 33
    new-array v1, v0, [B

    fill-array-data v1, :array_0

    sput-object v1, Lcom/markus/FilePickerProxyActivity;->ZIP_HEADER:[B

    .line 34
    new-array v0, v0, [B

    fill-array-data v0, :array_1

    sput-object v0, Lcom/markus/FilePickerProxyActivity;->WATCHFACE_HEADER:[B

    return-void

    :array_0
    .array-data 1
        0x50t
        0x4bt
        0x3t
        0x4t
    .end array-data

    :array_1
    .array-data 1
        0x5at
        -0x5bt
        0x34t
        0x12t
    .end array-data
.end method

.method public constructor <init>()V
    .locals 0

    .line 28
    invoke-direct {p0}, Landroidx/fragment/app/FragmentActivity;-><init>()V

    return-void
.end method

.method private static copyWithLimit(Ljava/io/InputStream;Ljava/io/OutputStream;J)J
    .locals 6
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    const/16 v0, 0x4000

    .line 261
    new-array v0, v0, [B

    const-wide/16 v1, 0x0

    .line 265
    :goto_0
    invoke-virtual {p0, v0}, Ljava/io/InputStream;->read([B)I

    move-result v3

    const/4 v4, -0x1

    if-ne v3, v4, :cond_0

    return-wide v1

    :cond_0
    int-to-long v4, v3

    add-long/2addr v1, v4

    cmp-long v4, v1, p2

    if-gtz v4, :cond_1

    const/4 v4, 0x0

    .line 275
    invoke-virtual {p1, v0, v4, v3}, Ljava/io/OutputStream;->write([BII)V

    goto :goto_0

    .line 272
    :cond_1
    new-instance p0, Ljava/io/IOException;

    const-string p1, "ZIP entry exceeds allowed uncompressed size"

    invoke-direct {p0, p1}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    goto :goto_2

    :goto_1
    throw p0

    :goto_2
    goto :goto_1
.end method

.method private static deleteQuietly(Ljava/io/File;)V
    .locals 0

    if-eqz p0, :cond_0

    .line 256
    invoke-virtual {p0}, Ljava/io/File;->delete()Z

    :cond_0
    return-void
.end method

.method private extractArchive(Ljava/util/zip/ZipFile;Ljava/io/File;)V
    .locals 12
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    .line 325
    new-instance v0, Ljava/lang/StringBuilder;

    invoke-direct {v0}, Ljava/lang/StringBuilder;-><init>()V

    invoke-virtual {p2}, Ljava/io/File;->getCanonicalPath()Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    sget-object v1, Ljava/io/File;->separator:Ljava/lang/String;

    invoke-virtual {v0, v1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v0

    invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v0

    .line 326
    invoke-virtual {p1}, Ljava/util/zip/ZipFile;->entries()Ljava/util/Enumeration;

    move-result-object v1

    const-wide/16 v2, 0x0

    move-wide v4, v2

    .line 329
    :cond_0
    :goto_0
    invoke-interface {v1}, Ljava/util/Enumeration;->hasMoreElements()Z

    move-result v6

    if-eqz v6, :cond_8

    .line 330
    invoke-interface {v1}, Ljava/util/Enumeration;->nextElement()Ljava/lang/Object;

    move-result-object v6

    check-cast v6, Ljava/util/zip/ZipEntry;

    .line 331
    invoke-virtual {v6}, Ljava/util/zip/ZipEntry;->getName()Ljava/lang/String;

    move-result-object v7

    invoke-static {v7}, Lcom/markus/FilePickerProxyActivity;->normalizeEntryName(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v7

    .line 333
    invoke-static {v7}, Lcom/markus/FilePickerProxyActivity;->validateEntryName(Ljava/lang/String;)V

    .line 335
    new-instance v7, Ljava/io/File;

    invoke-virtual {v6}, Ljava/util/zip/ZipEntry;->getName()Ljava/lang/String;

    move-result-object v8

    invoke-direct {v7, p2, v8}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 336
    invoke-virtual {v7}, Ljava/io/File;->getCanonicalPath()Ljava/lang/String;

    move-result-object v8

    .line 338
    invoke-virtual {v8, v0}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v8

    if-eqz v8, :cond_7

    .line 342
    invoke-virtual {v6}, Ljava/util/zip/ZipEntry;->isDirectory()Z

    move-result v8

    const-string v9, "Cannot create directory: "

    if-eqz v8, :cond_2

    .line 343
    invoke-virtual {v7}, Ljava/io/File;->isDirectory()Z

    move-result v6

    if-nez v6, :cond_0

    invoke-virtual {v7}, Ljava/io/File;->mkdirs()Z

    move-result v6

    if-eqz v6, :cond_1

    goto :goto_0

    .line 344
    :cond_1
    new-instance p1, Ljava/io/IOException;

    new-instance p2, Ljava/lang/StringBuilder;

    invoke-direct {p2, v9}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {p2, v7}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object p2

    invoke-virtual {p2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p2

    invoke-direct {p1, p2}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p1

    .line 350
    :cond_2
    invoke-virtual {v7}, Ljava/io/File;->getParentFile()Ljava/io/File;

    move-result-object v8

    if-eqz v8, :cond_4

    .line 352
    invoke-virtual {v8}, Ljava/io/File;->isDirectory()Z

    move-result v10

    if-nez v10, :cond_4

    invoke-virtual {v8}, Ljava/io/File;->mkdirs()Z

    move-result v10

    if-eqz v10, :cond_3

    goto :goto_1

    .line 353
    :cond_3
    new-instance p1, Ljava/io/IOException;

    new-instance p2, Ljava/lang/StringBuilder;

    invoke-direct {p2, v9}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {p2, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;

    move-result-object p2

    invoke-virtual {p2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p2

    invoke-direct {p1, p2}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p1

    :cond_4
    :goto_1
    const-wide/32 v8, 0x6400000

    sub-long/2addr v8, v4

    cmp-long v10, v8, v2

    if-lez v10, :cond_6

    const-wide/32 v10, 0x3200000

    .line 362
    invoke-static {v10, v11, v8, v9}, Ljava/lang/Math;->min(JJ)J

    move-result-wide v8

    .line 364
    invoke-virtual {p1, v6}, Ljava/util/zip/ZipFile;->getInputStream(Ljava/util/zip/ZipEntry;)Ljava/io/InputStream;

    move-result-object v6

    .line 365
    :try_start_0
    new-instance v10, Ljava/io/BufferedOutputStream;

    new-instance v11, Ljava/io/FileOutputStream;

    invoke-direct {v11, v7}, Ljava/io/FileOutputStream;-><init>(Ljava/io/File;)V

    invoke-direct {v10, v11}, Ljava/io/BufferedOutputStream;-><init>(Ljava/io/OutputStream;)V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_2

    .line 367
    :try_start_1
    invoke-static {v6, v10, v8, v9}, Lcom/markus/FilePickerProxyActivity;->copyWithLimit(Ljava/io/InputStream;Ljava/io/OutputStream;J)J

    move-result-wide v7
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    add-long/2addr v4, v7

    .line 369
    :try_start_2
    invoke-virtual {v10}, Ljava/io/OutputStream;->close()V
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_2

    if-eqz v6, :cond_0

    invoke-virtual {v6}, Ljava/io/InputStream;->close()V

    goto/16 :goto_0

    :catchall_0
    move-exception p1

    .line 364
    :try_start_3
    invoke-virtual {v10}, Ljava/io/OutputStream;->close()V
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_1

    goto :goto_2

    :catchall_1
    move-exception p2

    :try_start_4
    invoke-static {p1, p2}, Lcom/markus/FilePickerProxyActivity$$ExternalSyntheticBackport0;->m(Ljava/lang/Throwable;Ljava/lang/Throwable;)V

    :goto_2
    throw p1
    :try_end_4
    .catchall {:try_start_4 .. :try_end_4} :catchall_2

    :catchall_2
    move-exception p1

    if-eqz v6, :cond_5

    :try_start_5
    invoke-virtual {v6}, Ljava/io/InputStream;->close()V
    :try_end_5
    .catchall {:try_start_5 .. :try_end_5} :catchall_3

    goto :goto_3

    :catchall_3
    move-exception p2

    invoke-static {p1, p2}, Lcom/markus/FilePickerProxyActivity$$ExternalSyntheticBackport0;->m(Ljava/lang/Throwable;Ljava/lang/Throwable;)V

    :cond_5
    :goto_3
    throw p1

    .line 359
    :cond_6
    new-instance p1, Ljava/io/IOException;

    const-string p2, "Archive exceeds maximum uncompressed size"

    invoke-direct {p1, p2}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p1

    .line 339
    :cond_7
    new-instance p1, Ljava/io/IOException;

    new-instance p2, Ljava/lang/StringBuilder;

    const-string v0, "Unsafe ZIP entry: "

    invoke-direct {p2, v0}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v6}, Ljava/util/zip/ZipEntry;->getName()Ljava/lang/String;

    move-result-object v0

    invoke-virtual {p2, v0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object p2

    invoke-virtual {p2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p2

    invoke-direct {p1, p2}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p1

    :cond_8
    return-void
.end method

.method private static extractPreviewFromWatchface(Ljava/io/File;Ljava/lang/String;Ljava/lang/String;)V
    .locals 19
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    .line 176
    const-string v0, "author"

    const-string v1, "name"

    const-string v2, "watch"

    const-string v3, "UTF-8"

    invoke-virtual/range {p0 .. p0}, Ljava/io/File;->toPath()Ljava/nio/file/Path;

    move-result-object v4

    invoke-static {v4}, Ljava/nio/file/Files;->readAllBytes(Ljava/nio/file/Path;)[B

    move-result-object v4

    .line 177
    invoke-static {v4}, Lcom/markus/wf/DecoderApi;->decode([B)Lcom/markus/wf/DecoderApi$Result;

    move-result-object v5

    const/4 v6, 0x6

    .line 179
    new-array v7, v6, [Ljava/lang/String;

    const/4 v8, 0x0

    const-string v9, "files"

    aput-object v9, v7, v8

    const/4 v10, 0x1

    const-string v11, "WatchFace"

    aput-object v11, v7, v10

    const/4 v12, 0x2

    aput-object p1, v7, v12

    const/4 v13, 0x3

    aput-object p2, v7, v13

    const/4 v14, 0x4

    const-string v15, "local"

    aput-object v15, v7, v14

    const/16 v16, 0x5

    const-string v17, "preview"

    aput-object v17, v7, v16

    invoke-static {v7}, Lcom/markus/a;->getFile([Ljava/lang/String;)Ljava/io/File;

    move-result-object v7

    .line 180
    invoke-virtual {v7}, Ljava/io/File;->mkdirs()Z

    .line 182
    new-array v7, v6, [Ljava/lang/String;

    aput-object v9, v7, v8

    aput-object v11, v7, v10

    aput-object p1, v7, v12

    aput-object p2, v7, v13

    aput-object v15, v7, v14

    const-string v18, "resource.bin"

    aput-object v18, v7, v16

    invoke-static {v7}, Lcom/markus/a;->getFile([Ljava/lang/String;)Ljava/io/File;

    move-result-object v7

    .line 183
    invoke-virtual {v7}, Ljava/io/File;->toPath()Ljava/nio/file/Path;

    move-result-object v7

    new-array v14, v8, [Ljava/nio/file/OpenOption;

    invoke-static {v7, v4, v14}, Ljava/nio/file/Files;->write(Ljava/nio/file/Path;[B[Ljava/nio/file/OpenOption;)Ljava/nio/file/Path;

    .line 185
    invoke-static {}, Landroid/util/Xml;->newSerializer()Lorg/xmlpull/v1/XmlSerializer;

    move-result-object v4

    .line 186
    new-array v7, v6, [Ljava/lang/String;

    aput-object v9, v7, v8

    aput-object v11, v7, v10

    aput-object p1, v7, v12

    aput-object p2, v7, v13

    const/4 v14, 0x4

    aput-object v15, v7, v14

    const-string v14, "description.xml"

    aput-object v14, v7, v16

    invoke-static {v7}, Lcom/markus/a;->getFile([Ljava/lang/String;)Ljava/io/File;

    move-result-object v7

    .line 187
    new-instance v14, Ljava/io/FileOutputStream;

    invoke-direct {v14, v7}, Ljava/io/FileOutputStream;-><init>(Ljava/io/File;)V

    .line 188
    :try_start_0
    invoke-interface {v4, v14, v3}, Lorg/xmlpull/v1/XmlSerializer;->setOutput(Ljava/io/OutputStream;Ljava/lang/String;)V

    .line 190
    invoke-static {v10}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;

    move-result-object v7

    invoke-interface {v4, v3, v7}, Lorg/xmlpull/v1/XmlSerializer;->startDocument(Ljava/lang/String;Ljava/lang/Boolean;)V

    const/4 v3, 0x0

    .line 192
    invoke-interface {v4, v3, v2}, Lorg/xmlpull/v1/XmlSerializer;->startTag(Ljava/lang/String;Ljava/lang/String;)Lorg/xmlpull/v1/XmlSerializer;

    .line 194
    invoke-interface {v4, v3, v1}, Lorg/xmlpull/v1/XmlSerializer;->startTag(Ljava/lang/String;Ljava/lang/String;)Lorg/xmlpull/v1/XmlSerializer;

    .line 195
    iget-object v7, v5, Lcom/markus/wf/DecoderApi$Result;->name:Ljava/lang/String;

    invoke-interface {v4, v7}, Lorg/xmlpull/v1/XmlSerializer;->text(Ljava/lang/String;)Lorg/xmlpull/v1/XmlSerializer;

    .line 196
    invoke-interface {v4, v3, v1}, Lorg/xmlpull/v1/XmlSerializer;->endTag(Ljava/lang/String;Ljava/lang/String;)Lorg/xmlpull/v1/XmlSerializer;

    .line 198
    invoke-interface {v4, v3, v0}, Lorg/xmlpull/v1/XmlSerializer;->startTag(Ljava/lang/String;Ljava/lang/String;)Lorg/xmlpull/v1/XmlSerializer;

    .line 199
    const-string v1, "???"

    invoke-interface {v4, v1}, Lorg/xmlpull/v1/XmlSerializer;->text(Ljava/lang/String;)Lorg/xmlpull/v1/XmlSerializer;

    .line 200
    invoke-interface {v4, v3, v0}, Lorg/xmlpull/v1/XmlSerializer;->endTag(Ljava/lang/String;Ljava/lang/String;)Lorg/xmlpull/v1/XmlSerializer;

    .line 202
    invoke-interface {v4, v3, v2}, Lorg/xmlpull/v1/XmlSerializer;->endTag(Ljava/lang/String;Ljava/lang/String;)Lorg/xmlpull/v1/XmlSerializer;

    .line 204
    invoke-interface {v4}, Lorg/xmlpull/v1/XmlSerializer;->endDocument()V
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    .line 205
    invoke-virtual {v14}, Ljava/io/FileOutputStream;->close()V

    .line 207
    iget-object v0, v5, Lcom/markus/wf/DecoderApi$Result;->preview:Ljava/util/List;

    invoke-interface {v0}, Ljava/util/List;->isEmpty()Z

    move-result v0

    const/4 v1, 0x7

    if-nez v0, :cond_0

    .line 208
    iget-object v0, v5, Lcom/markus/wf/DecoderApi$Result;->preview:Ljava/util/List;

    invoke-interface {v0}, Ljava/util/List;->getFirst()Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Lcom/markus/wf/DecoderApi$ResultRow;

    .line 209
    new-array v2, v1, [Ljava/lang/String;

    aput-object v9, v2, v8

    aput-object v11, v2, v10

    aput-object p1, v2, v12

    aput-object p2, v2, v13

    const/4 v3, 0x4

    aput-object v15, v2, v3

    aput-object v17, v2, v16

    const-string v3, "market-preview.png"

    aput-object v3, v2, v6

    invoke-static {v2}, Lcom/markus/a;->getFile([Ljava/lang/String;)Ljava/io/File;

    move-result-object v2

    .line 210
    iget-object v3, v0, Lcom/markus/wf/DecoderApi$ResultRow;->raw:[B

    iget v4, v0, Lcom/markus/wf/DecoderApi$ResultRow;->width:I

    iget v7, v0, Lcom/markus/wf/DecoderApi$ResultRow;->height:I

    invoke-static {v3, v4, v7, v2}, Lcom/markus/ImageUtils;->saveRgbaToPng([BIILjava/io/File;)V

    .line 212
    new-array v2, v1, [Ljava/lang/String;

    aput-object v9, v2, v8

    aput-object v11, v2, v10

    aput-object p1, v2, v12

    aput-object p2, v2, v13

    const/4 v3, 0x4

    aput-object v15, v2, v3

    aput-object v17, v2, v16

    const-string v3, "preview.png"

    aput-object v3, v2, v6

    invoke-static {v2}, Lcom/markus/a;->getFile([Ljava/lang/String;)Ljava/io/File;

    move-result-object v2

    .line 213
    iget-object v3, v0, Lcom/markus/wf/DecoderApi$ResultRow;->raw:[B

    iget v4, v0, Lcom/markus/wf/DecoderApi$ResultRow;->width:I

    iget v0, v0, Lcom/markus/wf/DecoderApi$ResultRow;->height:I

    invoke-static {v3, v4, v0, v2}, Lcom/markus/ImageUtils;->saveRgbaToPng([BIILjava/io/File;)V

    :cond_0
    const/4 v0, 0x0

    .line 216
    :goto_0
    iget-object v2, v5, Lcom/markus/wf/DecoderApi$Result;->preview:Ljava/util/List;

    invoke-interface {v2}, Ljava/util/List;->size()I

    move-result v2

    if-ge v0, v2, :cond_1

    .line 217
    new-array v2, v1, [Ljava/lang/String;

    aput-object v9, v2, v8

    aput-object v11, v2, v10

    aput-object p1, v2, v12

    aput-object p2, v2, v13

    const/4 v3, 0x4

    aput-object v15, v2, v3

    aput-object v17, v2, v16

    add-int/lit8 v4, v0, 0x1

    invoke-static {v4}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v7

    new-array v14, v10, [Ljava/lang/Object;

    aput-object v7, v14, v8

    const-string v7, "style_%d_static.png"

    invoke-static {v7, v14}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v7

    aput-object v7, v2, v6

    invoke-static {v2}, Lcom/markus/a;->getFile([Ljava/lang/String;)Ljava/io/File;

    move-result-object v2

    .line 218
    iget-object v7, v5, Lcom/markus/wf/DecoderApi$Result;->preview:Ljava/util/List;

    invoke-interface {v7, v0}, Ljava/util/List;->get(I)Ljava/lang/Object;

    move-result-object v0

    check-cast v0, Lcom/markus/wf/DecoderApi$ResultRow;

    .line 220
    iget-object v7, v0, Lcom/markus/wf/DecoderApi$ResultRow;->raw:[B

    iget v14, v0, Lcom/markus/wf/DecoderApi$ResultRow;->width:I

    iget v0, v0, Lcom/markus/wf/DecoderApi$ResultRow;->height:I

    invoke-static {v7, v14, v0, v2}, Lcom/markus/ImageUtils;->saveRgbaToPng([BIILjava/io/File;)V

    move v0, v4

    goto :goto_0

    :cond_1
    return-void

    :catchall_0
    move-exception v0

    move-object v1, v0

    .line 187
    :try_start_1
    invoke-virtual {v14}, Ljava/io/FileOutputStream;->close()V
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_1

    goto :goto_1

    :catchall_1
    move-exception v0

    move-object v2, v0

    invoke-static {v1, v2}, Lcom/markus/FilePickerProxyActivity$$ExternalSyntheticBackport0;->m(Ljava/lang/Throwable;Ljava/lang/Throwable;)V

    :goto_1
    goto :goto_3

    :goto_2
    throw v1

    :goto_3
    goto :goto_2
.end method

.method private extractToTempFile(Ljava/util/zip/ZipFile;Ljava/util/zip/ZipEntry;)Ljava/io/File;
    .locals 4
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    const/4 v0, 0x0

    .line 281
    invoke-virtual {p0, v0}, Lcom/markus/FilePickerProxyActivity;->getExternalFilesDir(Ljava/lang/String;)Ljava/io/File;

    move-result-object v0

    .line 282
    new-instance v1, Ljava/io/File;

    const-string v2, "tempfile2"

    invoke-direct {v1, v0, v2}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    const/4 v0, 0x0

    .line 285
    :try_start_0
    invoke-virtual {p1, p2}, Ljava/util/zip/ZipFile;->getInputStream(Ljava/util/zip/ZipEntry;)Ljava/io/InputStream;

    move-result-object p1
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_4

    .line 286
    :try_start_1
    new-instance p2, Ljava/io/BufferedOutputStream;

    new-instance v2, Ljava/io/FileOutputStream;

    invoke-direct {v2, v1}, Ljava/io/FileOutputStream;-><init>(Ljava/io/File;)V

    invoke-direct {p2, v2}, Ljava/io/BufferedOutputStream;-><init>(Ljava/io/OutputStream;)V
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_2

    const-wide/32 v2, 0x3200000

    .line 288
    :try_start_2
    invoke-static {p1, p2, v2, v3}, Lcom/markus/FilePickerProxyActivity;->copyWithLimit(Ljava/io/InputStream;Ljava/io/OutputStream;J)J
    :try_end_2
    .catchall {:try_start_2 .. :try_end_2} :catchall_0

    const/4 v0, 0x1

    .line 291
    :try_start_3
    invoke-virtual {p2}, Ljava/io/OutputStream;->close()V
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_2

    if-eqz p1, :cond_0

    :try_start_4
    invoke-virtual {p1}, Ljava/io/InputStream;->close()V
    :try_end_4
    .catchall {:try_start_4 .. :try_end_4} :catchall_4

    :cond_0
    return-object v1

    :catchall_0
    move-exception v2

    .line 285
    :try_start_5
    invoke-virtual {p2}, Ljava/io/OutputStream;->close()V
    :try_end_5
    .catchall {:try_start_5 .. :try_end_5} :catchall_1

    goto :goto_0

    :catchall_1
    move-exception p2

    :try_start_6
    invoke-static {v2, p2}, Lcom/markus/FilePickerProxyActivity$$ExternalSyntheticBackport0;->m(Ljava/lang/Throwable;Ljava/lang/Throwable;)V

    :goto_0
    throw v2
    :try_end_6
    .catchall {:try_start_6 .. :try_end_6} :catchall_2

    :catchall_2
    move-exception p2

    if-eqz p1, :cond_1

    :try_start_7
    invoke-virtual {p1}, Ljava/io/InputStream;->close()V
    :try_end_7
    .catchall {:try_start_7 .. :try_end_7} :catchall_3

    goto :goto_1

    :catchall_3
    move-exception p1

    :try_start_8
    invoke-static {p2, p1}, Lcom/markus/FilePickerProxyActivity$$ExternalSyntheticBackport0;->m(Ljava/lang/Throwable;Ljava/lang/Throwable;)V

    :cond_1
    :goto_1
    throw p2
    :try_end_8
    .catchall {:try_start_8 .. :try_end_8} :catchall_4

    :catchall_4
    move-exception p1

    if-nez v0, :cond_2

    .line 293
    invoke-static {v1}, Lcom/markus/FilePickerProxyActivity;->deleteQuietly(Ljava/io/File;)V

    .line 295
    :cond_2
    throw p1
.end method

.method private getResourceString(Ljava/lang/String;)Ljava/lang/String;
    .locals 3

    .line 42
    invoke-virtual {p0}, Lcom/markus/FilePickerProxyActivity;->getResources()Landroid/content/res/Resources;

    move-result-object v0

    const-string v1, "string"

    invoke-virtual {p0}, Lcom/markus/FilePickerProxyActivity;->getPackageName()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v0, p1, v1, v2}, Landroid/content/res/Resources;->getIdentifier(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)I

    move-result v0

    if-nez v0, :cond_0

    return-object p1

    .line 46
    :cond_0
    invoke-virtual {p0}, Lcom/markus/FilePickerProxyActivity;->getResources()Landroid/content/res/Resources;

    move-result-object p1

    invoke-virtual {p1, v0}, Landroid/content/res/Resources;->getString(I)Ljava/lang/String;

    move-result-object p1

    return-object p1
.end method

.method private getWatchfaceId(Ljava/io/File;)Ljava/lang/String;
    .locals 1

    .line 166
    :try_start_0
    new-instance v0, Ljava/io/FileInputStream;

    invoke-direct {v0, p1}, Ljava/io/FileInputStream;-><init>(Ljava/io/File;)V
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_0

    .line 167
    :try_start_1
    invoke-direct {p0, v0}, Lcom/markus/FilePickerProxyActivity;->getWatchfaceId(Ljava/io/InputStream;)Ljava/lang/String;

    move-result-object p1
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 168
    :try_start_2
    invoke-virtual {v0}, Ljava/io/InputStream;->close()V
    :try_end_2
    .catch Ljava/io/IOException; {:try_start_2 .. :try_end_2} :catch_0

    return-object p1

    :catchall_0
    move-exception p1

    .line 166
    :try_start_3
    invoke-virtual {v0}, Ljava/io/InputStream;->close()V
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_1

    goto :goto_0

    :catchall_1
    move-exception v0

    :try_start_4
    invoke-static {p1, v0}, Lcom/markus/FilePickerProxyActivity$$ExternalSyntheticBackport0;->m(Ljava/lang/Throwable;Ljava/lang/Throwable;)V

    :goto_0
    throw p1
    :try_end_4
    .catch Ljava/io/IOException; {:try_start_4 .. :try_end_4} :catch_0

    .line 169
    :catch_0
    const-string p1, "device_settings_io_error"

    invoke-direct {p0, p1}, Lcom/markus/FilePickerProxyActivity;->getResourceString(Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    const/4 v0, 0x1

    invoke-static {p0, p1, v0}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p1

    invoke-virtual {p1}, Landroid/widget/Toast;->show()V

    .line 170
    invoke-virtual {p0}, Lcom/markus/FilePickerProxyActivity;->finish()V

    const/4 p1, 0x0

    return-object p1
.end method

.method private getWatchfaceId(Ljava/io/InputStream;)Ljava/lang/String;
    .locals 3
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    const/16 v0, 0x1000

    .line 152
    new-array v0, v0, [B

    .line 153
    invoke-virtual {p1, v0}, Ljava/io/InputStream;->read([B)I

    .line 155
    new-instance p1, Ljava/lang/StringBuilder;

    invoke-direct {p1}, Ljava/lang/StringBuilder;-><init>()V

    const/16 v1, 0x28

    :goto_0
    const/16 v2, 0x68

    if-ge v1, v2, :cond_1

    .line 158
    aget-byte v2, v0, v1

    int-to-char v2, v2

    if-nez v2, :cond_0

    goto :goto_1

    .line 160
    :cond_0
    invoke-virtual {p1, v2}, Ljava/lang/StringBuilder;->append(C)Ljava/lang/StringBuilder;

    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    .line 162
    :cond_1
    :goto_1
    invoke-virtual {p1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p1

    return-object p1
.end method

.method private handleInstallWatchfaceArchive(Ljava/io/File;)V
    .locals 12

    .line 374
    const-string v0, "local"

    const-string v1, "WatchFace"

    const-string v2, "files"

    const/4 v3, 0x0

    const/4 v4, 0x1

    :try_start_0
    new-instance v5, Ljava/util/zip/ZipFile;

    invoke-direct {v5, p1}, Ljava/util/zip/ZipFile;-><init>(Ljava/io/File;)V
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_0

    .line 375
    :try_start_1
    invoke-virtual {v5}, Ljava/util/zip/ZipFile;->entries()Ljava/util/Enumeration;

    move-result-object p1

    invoke-static {p1}, Ljava/util/Collections;->list(Ljava/util/Enumeration;)Ljava/util/ArrayList;

    move-result-object p1

    .line 377
    new-instance v6, Ljava/util/ArrayList;

    invoke-direct {v6}, Ljava/util/ArrayList;-><init>()V

    .line 379
    invoke-interface {p1}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object v7

    :cond_0
    :goto_0
    invoke-interface {v7}, Ljava/util/Iterator;->hasNext()Z

    move-result v8

    if-eqz v8, :cond_1

    invoke-interface {v7}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v8

    check-cast v8, Ljava/util/zip/ZipEntry;

    .line 380
    invoke-virtual {v8}, Ljava/util/zip/ZipEntry;->isDirectory()Z

    move-result v9

    if-nez v9, :cond_0

    .line 381
    invoke-interface {v6, v8}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    goto :goto_0

    .line 385
    :cond_1
    invoke-interface {p1}, Ljava/util/List;->size()I

    move-result v7

    if-ne v7, v4, :cond_3

    invoke-interface {v6}, Ljava/util/List;->size()I

    move-result v7

    if-ne v7, v4, :cond_3

    .line 386
    invoke-interface {v6}, Ljava/util/List;->getFirst()Ljava/lang/Object;

    move-result-object p1

    check-cast p1, Ljava/util/zip/ZipEntry;

    .line 388
    invoke-direct {p0, v5, p1}, Lcom/markus/FilePickerProxyActivity;->hasWatchfaceMagic(Ljava/util/zip/ZipFile;Ljava/util/zip/ZipEntry;)Z

    move-result v0

    if-eqz v0, :cond_2

    .line 392
    invoke-direct {p0, v5, p1}, Lcom/markus/FilePickerProxyActivity;->extractToTempFile(Ljava/util/zip/ZipFile;Ljava/util/zip/ZipEntry;)Ljava/io/File;

    move-result-object p1

    .line 393
    invoke-direct {p0, p1}, Lcom/markus/FilePickerProxyActivity;->handleInstallWatchfaceBinary(Ljava/io/File;)V
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_2

    .line 467
    :try_start_2
    invoke-virtual {v5}, Ljava/util/zip/ZipFile;->close()V
    :try_end_2
    .catch Ljava/io/IOException; {:try_start_2 .. :try_end_2} :catch_0

    return-void

    .line 389
    :cond_2
    :try_start_3
    new-instance p1, Ljava/io/IOException;

    const-string v0, "Single file in archive has invalid watchface signature"

    invoke-direct {p1, v0}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p1

    .line 401
    :cond_3
    invoke-interface {p1}, Ljava/util/List;->iterator()Ljava/util/Iterator;

    move-result-object p1

    const/4 v6, 0x0

    const/4 v7, 0x0

    const/4 v8, 0x0

    :cond_4
    :goto_1
    invoke-interface {p1}, Ljava/util/Iterator;->hasNext()Z

    move-result v9

    if-eqz v9, :cond_b

    invoke-interface {p1}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    move-result-object v9

    check-cast v9, Ljava/util/zip/ZipEntry;

    .line 402
    invoke-virtual {v9}, Ljava/util/zip/ZipEntry;->getName()Ljava/lang/String;

    move-result-object v10

    invoke-static {v10}, Lcom/markus/FilePickerProxyActivity;->normalizeEntryName(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v10

    .line 404
    invoke-virtual {v10}, Ljava/lang/String;->isEmpty()Z

    move-result v11

    if-nez v11, :cond_a

    .line 408
    invoke-static {v10}, Lcom/markus/FilePickerProxyActivity;->validateEntryName(Ljava/lang/String;)V

    .line 410
    invoke-static {v10, v9}, Lcom/markus/FilePickerProxyActivity;->isRootBin(Ljava/lang/String;Ljava/util/zip/ZipEntry;)Z

    move-result v11

    if-eqz v11, :cond_6

    if-nez v6, :cond_5

    move-object v6, v9

    goto :goto_1

    .line 412
    :cond_5
    new-instance p1, Ljava/io/IOException;

    const-string v0, "Archive contains more than one .bin file"

    invoke-direct {p1, v0}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p1

    .line 419
    :cond_6
    const-string v11, "description.xml"

    invoke-virtual {v11, v10}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v11

    if-eqz v11, :cond_8

    invoke-virtual {v9}, Ljava/util/zip/ZipEntry;->isDirectory()Z

    move-result v11

    if-nez v11, :cond_8

    if-nez v7, :cond_7

    const/4 v7, 0x1

    goto :goto_1

    .line 421
    :cond_7
    new-instance p1, Ljava/io/IOException;

    const-string v0, "Archive contains duplicate description.xml"

    invoke-direct {p1, v0}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p1

    .line 428
    :cond_8
    const-string v11, "preview"

    invoke-virtual {v11, v10}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z

    move-result v11

    if-eqz v11, :cond_9

    invoke-virtual {v9}, Ljava/util/zip/ZipEntry;->isDirectory()Z

    move-result v9

    if-eqz v9, :cond_9

    :goto_2
    const/4 v8, 0x1

    goto :goto_1

    .line 433
    :cond_9
    const-string v9, "preview/"

    invoke-virtual {v10, v9}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v9

    if-eqz v9, :cond_4

    goto :goto_2

    .line 405
    :cond_a
    new-instance p1, Ljava/io/IOException;

    const-string v0, "Invalid empty ZIP entry"

    invoke-direct {p1, v0}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p1

    :cond_b
    if-eqz v6, :cond_11

    if-eqz v7, :cond_10

    if-eqz v8, :cond_f

    .line 451
    invoke-direct {p0, v5, v6}, Lcom/markus/FilePickerProxyActivity;->hasWatchfaceMagic(Ljava/util/zip/ZipFile;Ljava/util/zip/ZipEntry;)Z

    move-result p1

    if-eqz p1, :cond_e

    .line 456
    invoke-virtual {v5, v6}, Ljava/util/zip/ZipFile;->getInputStream(Ljava/util/zip/ZipEntry;)Ljava/io/InputStream;

    move-result-object p1
    :try_end_3
    .catchall {:try_start_3 .. :try_end_3} :catchall_2

    .line 457
    :try_start_4
    invoke-direct {p0, p1}, Lcom/markus/FilePickerProxyActivity;->getWatchfaceId(Ljava/io/InputStream;)Ljava/lang/String;

    move-result-object v6
    :try_end_4
    .catchall {:try_start_4 .. :try_end_4} :catchall_0

    if-eqz p1, :cond_c

    .line 458
    :try_start_5
    invoke-virtual {p1}, Ljava/io/InputStream;->close()V

    .line 459
    :cond_c
    invoke-static {}, Lcom/markus/a;->getCurrentDevice()Lcom/xiaomi/fitness/device/manager/DeviceModel;

    move-result-object p1

    invoke-interface {p1}, Lcom/xiaomi/fitness/device/manager/DeviceModel;->getDid()Ljava/lang/String;

    move-result-object p1

    const/4 v7, 0x5

    .line 461
    new-array v8, v7, [Ljava/lang/String;

    aput-object v2, v8, v3

    aput-object v1, v8, v4

    const/4 v9, 0x2

    aput-object p1, v8, v9

    const/4 v10, 0x3

    aput-object v6, v8, v10

    const/4 v11, 0x4

    aput-object v0, v8, v11

    invoke-static {v8}, Lcom/markus/a;->getFile([Ljava/lang/String;)Ljava/io/File;

    move-result-object v8

    .line 462
    invoke-virtual {v8}, Ljava/io/File;->mkdirs()Z

    .line 463
    invoke-direct {p0, v5, v8}, Lcom/markus/FilePickerProxyActivity;->extractArchive(Ljava/util/zip/ZipFile;Ljava/io/File;)V

    const/4 v8, 0x6

    .line 465
    new-array v8, v8, [Ljava/lang/String;

    aput-object v2, v8, v3

    aput-object v1, v8, v4

    aput-object p1, v8, v9

    aput-object v6, v8, v10

    aput-object v0, v8, v11

    const-string p1, "resource.bin"

    aput-object p1, v8, v7

    invoke-static {v8}, Lcom/markus/a;->getFile([Ljava/lang/String;)Ljava/io/File;

    move-result-object p1

    .line 466
    invoke-direct {p0, p1, v6}, Lcom/markus/FilePickerProxyActivity;->installWatchface(Ljava/io/File;Ljava/lang/String;)V
    :try_end_5
    .catchall {:try_start_5 .. :try_end_5} :catchall_2

    .line 467
    :try_start_6
    invoke-virtual {v5}, Ljava/util/zip/ZipFile;->close()V
    :try_end_6
    .catch Ljava/io/IOException; {:try_start_6 .. :try_end_6} :catch_0

    goto :goto_5

    :catchall_0
    move-exception v0

    if-eqz p1, :cond_d

    .line 456
    :try_start_7
    invoke-virtual {p1}, Ljava/io/InputStream;->close()V
    :try_end_7
    .catchall {:try_start_7 .. :try_end_7} :catchall_1

    goto :goto_3

    :catchall_1
    move-exception p1

    :try_start_8
    invoke-static {v0, p1}, Lcom/markus/FilePickerProxyActivity$$ExternalSyntheticBackport0;->m(Ljava/lang/Throwable;Ljava/lang/Throwable;)V

    :cond_d
    :goto_3
    throw v0

    .line 452
    :cond_e
    new-instance p1, Ljava/io/IOException;

    const-string v0, ".bin file has invalid watchface signature"

    invoke-direct {p1, v0}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p1

    .line 448
    :cond_f
    new-instance p1, Ljava/io/IOException;

    const-string v0, "Archive does not contain preview directory"

    invoke-direct {p1, v0}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p1

    .line 444
    :cond_10
    new-instance p1, Ljava/io/IOException;

    const-string v0, "Archive does not contain description.xml"

    invoke-direct {p1, v0}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p1

    .line 440
    :cond_11
    new-instance p1, Ljava/io/IOException;

    const-string v0, "Archive does not contain exactly one .bin file"

    invoke-direct {p1, v0}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw p1
    :try_end_8
    .catchall {:try_start_8 .. :try_end_8} :catchall_2

    :catchall_2
    move-exception p1

    .line 374
    :try_start_9
    invoke-virtual {v5}, Ljava/util/zip/ZipFile;->close()V
    :try_end_9
    .catchall {:try_start_9 .. :try_end_9} :catchall_3

    goto :goto_4

    :catchall_3
    move-exception v0

    :try_start_a
    invoke-static {p1, v0}, Lcom/markus/FilePickerProxyActivity$$ExternalSyntheticBackport0;->m(Ljava/lang/Throwable;Ljava/lang/Throwable;)V

    :goto_4
    throw p1
    :try_end_a
    .catch Ljava/io/IOException; {:try_start_a .. :try_end_a} :catch_0

    :catch_0
    move-exception p1

    .line 469
    const-string v0, "FilePickerProxyActivity"

    const-string v1, "Unable to install wmz"

    invoke-static {v0, v1, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 470
    const-string p1, "device_settings_watchface_installed_error"

    invoke-direct {p0, p1}, Lcom/markus/FilePickerProxyActivity;->getResourceString(Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    const/4 v0, -0x1

    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v0

    new-array v1, v4, [Ljava/lang/Object;

    aput-object v0, v1, v3

    invoke-static {p1, v1}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object p1

    invoke-static {p0, p1, v4}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p1

    invoke-virtual {p1}, Landroid/widget/Toast;->show()V

    .line 471
    invoke-virtual {p0}, Lcom/markus/FilePickerProxyActivity;->finish()V

    :goto_5
    return-void
.end method

.method private handleInstallWatchfaceBinary(Ljava/io/File;)V
    .locals 4

    .line 225
    invoke-direct {p0, p1}, Lcom/markus/FilePickerProxyActivity;->getWatchfaceId(Ljava/io/File;)Ljava/lang/String;

    move-result-object v0

    .line 226
    invoke-static {}, Lcom/markus/a;->getCurrentDevice()Lcom/xiaomi/fitness/device/manager/DeviceModel;

    move-result-object v1

    invoke-interface {v1}, Lcom/xiaomi/fitness/device/manager/DeviceModel;->getDid()Ljava/lang/String;

    move-result-object v1

    .line 228
    :try_start_0
    invoke-static {p1, v1, v0}, Lcom/markus/FilePickerProxyActivity;->extractPreviewFromWatchface(Ljava/io/File;Ljava/lang/String;Ljava/lang/String;)V
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    :catch_0
    move-exception v1

    .line 230
    const-string v2, "FilePickerProxyActivity"

    const-string v3, "Unable to extract watchface previews"

    invoke-static {v2, v3, v1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)I

    .line 232
    :goto_0
    invoke-direct {p0, p1, v0}, Lcom/markus/FilePickerProxyActivity;->installWatchface(Ljava/io/File;Ljava/lang/String;)V

    return-void
.end method

.method private hasWatchfaceMagic(Ljava/util/zip/ZipFile;Ljava/util/zip/ZipEntry;)Z
    .locals 6
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    .line 236
    invoke-virtual {p2}, Ljava/util/zip/ZipEntry;->isDirectory()Z

    move-result v0

    const/4 v1, 0x0

    if-eqz v0, :cond_0

    return v1

    .line 240
    :cond_0
    invoke-virtual {p1, p2}, Ljava/util/zip/ZipFile;->getInputStream(Ljava/util/zip/ZipEntry;)Ljava/io/InputStream;

    move-result-object p1

    .line 241
    :try_start_0
    sget-object p2, Lcom/markus/FilePickerProxyActivity;->WATCHFACE_HEADER:[B

    array-length v0, p2

    const/4 v2, 0x0

    :goto_0
    if-ge v2, v0, :cond_4

    aget-byte v3, p2, v2

    .line 242
    invoke-virtual {p1}, Ljava/io/InputStream;->read()I

    move-result v4
    :try_end_0
    .catchall {:try_start_0 .. :try_end_0} :catchall_0

    const/4 v5, -0x1

    if-eq v4, v5, :cond_2

    and-int/lit16 v3, v3, 0xff

    if-eq v4, v3, :cond_1

    goto :goto_1

    :cond_1
    add-int/lit8 v2, v2, 0x1

    goto :goto_0

    :cond_2
    :goto_1
    if-eqz p1, :cond_3

    .line 250
    invoke-virtual {p1}, Ljava/io/InputStream;->close()V

    :cond_3
    return v1

    :cond_4
    if-eqz p1, :cond_5

    invoke-virtual {p1}, Ljava/io/InputStream;->close()V

    :cond_5
    const/4 p1, 0x1

    return p1

    :catchall_0
    move-exception p2

    if-eqz p1, :cond_6

    .line 240
    :try_start_1
    invoke-virtual {p1}, Ljava/io/InputStream;->close()V
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_1

    goto :goto_2

    :catchall_1
    move-exception p1

    invoke-static {p2, p1}, Lcom/markus/FilePickerProxyActivity$$ExternalSyntheticBackport0;->m(Ljava/lang/Throwable;Ljava/lang/Throwable;)V

    :cond_6
    :goto_2
    goto :goto_4

    :goto_3
    throw p2

    :goto_4
    goto :goto_3
.end method

.method private installWatchface(Ljava/io/File;Ljava/lang/String;)V
    .locals 19

    move-object/from16 v6, p0

    if-nez p2, :cond_0

    .line 123
    invoke-virtual/range {p0 .. p0}, Lcom/markus/FilePickerProxyActivity;->finish()V

    return-void

    .line 127
    :cond_0
    new-instance v0, Lcom/xiaomi/fitness/watch/face/viewmodel/FaceBleInfoViewModel;

    invoke-direct {v0}, Lcom/xiaomi/fitness/watch/face/viewmodel/FaceBleInfoViewModel;-><init>()V

    .line 130
    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;

    move-result-object v1

    const/4 v2, 0x1

    .line 133
    :try_start_0
    const-string v3, "faceInstallController"

    invoke-virtual {v1, v3}, Ljava/lang/Class;->getDeclaredField(Ljava/lang/String;)Ljava/lang/reflect/Field;

    move-result-object v1

    .line 134
    invoke-virtual {v1, v2}, Ljava/lang/reflect/Field;->setAccessible(Z)V

    .line 135
    invoke-virtual {v1, v0}, Ljava/lang/reflect/Field;->get(Ljava/lang/Object;)Ljava/lang/Object;

    move-result-object v0

    move-object v7, v0

    check-cast v7, Lcom/xiaomi/fitness/watch/face/install/FaceInstallBaseImpl;
    :try_end_0
    .catch Ljava/lang/NoSuchFieldException; {:try_start_0 .. :try_end_0} :catch_0
    .catch Ljava/lang/IllegalAccessException; {:try_start_0 .. :try_end_0} :catch_0

    .line 142
    new-instance v0, Lcom/xiaomi/fitness/watch/face/widget/FaceDialog$Builder;

    const-string v1, "watchface"

    invoke-direct {v0, v1}, Lcom/xiaomi/fitness/watch/face/widget/FaceDialog$Builder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v0}, Lcom/xiaomi/fitness/watch/face/widget/FaceDialog$Builder;->create()Lcom/xiaomi/fitness/baseui/dialog/CommonDialog;

    move-result-object v0

    check-cast v0, Lcom/xiaomi/fitness/watch/face/widget/FaceDialog;

    .line 143
    invoke-virtual/range {p0 .. p0}, Lcom/markus/FilePickerProxyActivity;->getSupportFragmentManager()Landroidx/fragment/app/FragmentManager;

    move-result-object v1

    const-string v2, "test"

    invoke-virtual {v0, v1, v2}, Lcom/xiaomi/fitness/watch/face/widget/FaceDialog;->show(Landroidx/fragment/app/FragmentManager;Ljava/lang/String;)V

    .line 145
    new-instance v4, Lcom/markus/WatchfaceInstallCallback;

    invoke-direct {v4, v0, v6}, Lcom/markus/WatchfaceInstallCallback;-><init>(Lcom/xiaomi/fitness/watch/face/widget/FaceDialog;Landroidx/fragment/app/FragmentActivity;)V

    .line 146
    new-instance v18, Lcom/markus/PrepareInstallCallback;

    invoke-virtual/range {p1 .. p1}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v2

    move-object/from16 v0, v18

    move-object v1, v7

    move-object/from16 v3, p2

    move-object/from16 v5, p0

    invoke-direct/range {v0 .. v5}, Lcom/markus/PrepareInstallCallback;-><init>(Lcom/xiaomi/fitness/watch/face/install/FaceInstallController;Ljava/lang/String;Ljava/lang/String;Lcom/markus/WatchfaceInstallCallback;Landroidx/fragment/app/FragmentActivity;)V

    .line 148
    invoke-virtual/range {p1 .. p1}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object v9

    invoke-virtual/range {p1 .. p1}, Ljava/io/File;->length()J

    move-result-wide v10

    const-string v16, ""

    const/16 v17, 0x0

    const-wide/16 v12, 0x0

    const/4 v14, 0x1

    const-string v15, ""

    move-object/from16 v8, p2

    invoke-virtual/range {v7 .. v18}, Lcom/xiaomi/fitness/watch/face/install/FaceInstallBaseImpl;->preInstall(Ljava/lang/String;Ljava/lang/String;JJZLjava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Lkotlin/jvm/functions/Function3;)V

    return-void

    .line 137
    :catch_0
    const-string v0, "\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0438 \u043f\u043e\u043b\u0443\u0447\u0435\u043d\u0438\u0438 \u043a\u043e\u043d\u0442\u0440\u043e\u043b\u043b\u0435\u0440\u0430!"

    invoke-static {v6, v0, v2}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object v0

    invoke-virtual {v0}, Landroid/widget/Toast;->show()V

    .line 138
    invoke-virtual/range {p0 .. p0}, Lcom/markus/FilePickerProxyActivity;->finish()V

    return-void
.end method

.method private static isRootBin(Ljava/lang/String;Ljava/util/zip/ZipEntry;)Z
    .locals 1

    .line 319
    invoke-virtual {p1}, Ljava/util/zip/ZipEntry;->isDirectory()Z

    move-result p1

    if-nez p1, :cond_0

    const/16 p1, 0x2f

    .line 320
    invoke-virtual {p0, p1}, Ljava/lang/String;->indexOf(I)I

    move-result p1

    const/4 v0, -0x1

    if-ne p1, v0, :cond_0

    sget-object p1, Ljava/util/Locale;->ROOT:Ljava/util/Locale;

    .line 321
    invoke-virtual {p0, p1}, Ljava/lang/String;->toLowerCase(Ljava/util/Locale;)Ljava/lang/String;

    move-result-object p0

    const-string p1, ".bin"

    invoke-virtual {p0, p1}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z

    move-result p0

    if-eqz p0, :cond_0

    const/4 p0, 0x1

    goto :goto_0

    :cond_0
    const/4 p0, 0x0

    :goto_0
    return p0
.end method

.method private static normalizeEntryName(Ljava/lang/String;)Ljava/lang/String;
    .locals 4

    const/16 v0, 0x5c

    const/16 v1, 0x2f

    .line 299
    invoke-virtual {p0, v0, v1}, Ljava/lang/String;->replace(CC)Ljava/lang/String;

    move-result-object p0

    .line 301
    :goto_0
    const-string v0, "/"

    invoke-virtual {p0, v0}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v1

    const/4 v2, 0x1

    if-eqz v1, :cond_0

    .line 302
    invoke-virtual {p0, v2}, Ljava/lang/String;->substring(I)Ljava/lang/String;

    move-result-object p0

    goto :goto_0

    .line 305
    :cond_0
    :goto_1
    invoke-virtual {p0, v0}, Ljava/lang/String;->endsWith(Ljava/lang/String;)Z

    move-result v1

    if-eqz v1, :cond_1

    .line 306
    invoke-virtual {p0}, Ljava/lang/String;->length()I

    move-result v1

    sub-int/2addr v1, v2

    const/4 v3, 0x0

    invoke-virtual {p0, v3, v1}, Ljava/lang/String;->substring(II)Ljava/lang/String;

    move-result-object p0

    goto :goto_1

    :cond_1
    return-object p0
.end method

.method private static startsWith([B[B)Z
    .locals 4

    .line 50
    array-length v0, p0

    array-length v1, p1

    const/4 v2, 0x0

    if-ge v0, v1, :cond_0

    return v2

    :cond_0
    const/4 v0, 0x0

    .line 53
    :goto_0
    array-length v1, p1

    if-ge v0, v1, :cond_2

    .line 54
    aget-byte v1, p0, v0

    aget-byte v3, p1, v0

    if-eq v1, v3, :cond_1

    return v2

    :cond_1
    add-int/lit8 v0, v0, 0x1

    goto :goto_0

    :cond_2
    const/4 p0, 0x1

    return p0
.end method

.method private static validateEntryName(Ljava/lang/String;)V
    .locals 3
    .annotation system Ldalvik/annotation/Throws;
        value = {
            Ljava/io/IOException;
        }
    .end annotation

    .line 313
    const-string v0, "/"

    invoke-virtual {p0, v0}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v0

    if-nez v0, :cond_0

    const-string v0, "\\"

    invoke-virtual {p0, v0}, Ljava/lang/String;->startsWith(Ljava/lang/String;)Z

    move-result v0

    if-nez v0, :cond_0

    const-string v0, "../"

    invoke-virtual {p0, v0}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v0

    if-nez v0, :cond_0

    const-string v0, "..\\"

    invoke-virtual {p0, v0}, Ljava/lang/String;->contains(Ljava/lang/CharSequence;)Z

    move-result v0

    if-nez v0, :cond_0

    return-void

    .line 314
    :cond_0
    new-instance v0, Ljava/io/IOException;

    new-instance v1, Ljava/lang/StringBuilder;

    const-string v2, "Unsafe ZIP entry name: "

    invoke-direct {v1, v2}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v1, p0}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object p0

    invoke-virtual {p0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object p0

    invoke-direct {v0, p0}, Ljava/io/IOException;-><init>(Ljava/lang/String;)V

    throw v0
.end method


# virtual methods
.method public copyFile(Landroid/content/Intent;Ljava/lang/String;)Ljava/io/File;
    .locals 6

    .line 80
    invoke-virtual {p0}, Lcom/markus/FilePickerProxyActivity;->getContentResolver()Landroid/content/ContentResolver;

    move-result-object v0

    const/4 v1, 0x0

    .line 83
    :try_start_0
    invoke-virtual {p1}, Landroid/content/Intent;->getData()Landroid/net/Uri;

    move-result-object p1

    invoke-virtual {v0, p1}, Landroid/content/ContentResolver;->openInputStream(Landroid/net/Uri;)Ljava/io/InputStream;

    move-result-object p1
    :try_end_0
    .catch Ljava/io/FileNotFoundException; {:try_start_0 .. :try_end_0} :catch_1

    const/4 v0, 0x1

    .line 88
    const-string v2, "device_settings_io_error"

    if-nez p1, :cond_0

    .line 89
    invoke-direct {p0, v2}, Lcom/markus/FilePickerProxyActivity;->getResourceString(Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    invoke-static {p0, p1, v0}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p1

    invoke-virtual {p1}, Landroid/widget/Toast;->show()V

    return-object v1

    .line 97
    :cond_0
    :try_start_1
    invoke-virtual {p0, v1}, Lcom/markus/FilePickerProxyActivity;->getExternalFilesDir(Ljava/lang/String;)Ljava/io/File;

    move-result-object v3

    .line 98
    new-instance v4, Ljava/io/File;

    invoke-direct {v4, v3, p2}, Ljava/io/File;-><init>(Ljava/io/File;Ljava/lang/String;)V

    .line 99
    new-instance p2, Ljava/io/FileOutputStream;

    invoke-direct {p2, v4}, Ljava/io/FileOutputStream;-><init>(Ljava/io/File;)V

    const/16 v3, 0x1000

    .line 101
    new-array v3, v3, [B

    .line 103
    :goto_0
    invoke-virtual {p1, v3}, Ljava/io/InputStream;->read([B)I

    move-result v5

    if-lez v5, :cond_1

    .line 104
    invoke-virtual {p2, v3}, Ljava/io/OutputStream;->write([B)V

    goto :goto_0

    .line 107
    :cond_1
    invoke-virtual {p2}, Ljava/io/OutputStream;->close()V

    .line 108
    invoke-virtual {p1}, Ljava/io/InputStream;->close()V
    :try_end_1
    .catch Ljava/io/IOException; {:try_start_1 .. :try_end_1} :catch_0

    return-object v4

    .line 110
    :catch_0
    invoke-direct {p0, v2}, Lcom/markus/FilePickerProxyActivity;->getResourceString(Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    invoke-static {p0, p1, v0}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p1

    invoke-virtual {p1}, Landroid/widget/Toast;->show()V

    :catch_1
    return-object v1
.end method

.method public onActivityResult(IILandroid/content/Intent;)V
    .locals 1

    .line 520
    invoke-super {p0, p1, p2, p3}, Landroidx/fragment/app/FragmentActivity;->onActivityResult(IILandroid/content/Intent;)V

    const/4 v0, -0x1

    if-ne p2, v0, :cond_2

    const p2, 0x79bde

    if-ne p1, p2, :cond_0

    .line 523
    invoke-virtual {p0, p3}, Lcom/markus/FilePickerProxyActivity;->onActivityResultInstallWatchface(Landroid/content/Intent;)V

    goto :goto_0

    :cond_0
    const p2, 0x79bdf

    if-ne p1, p2, :cond_1

    .line 525
    invoke-virtual {p0, p3}, Lcom/markus/FilePickerProxyActivity;->onActivityResultInstallFirmware(Landroid/content/Intent;)V

    .line 526
    invoke-virtual {p0}, Lcom/markus/FilePickerProxyActivity;->finish()V

    goto :goto_0

    .line 528
    :cond_1
    invoke-virtual {p0}, Lcom/markus/FilePickerProxyActivity;->finish()V

    goto :goto_0

    .line 532
    :cond_2
    invoke-virtual {p0}, Lcom/markus/FilePickerProxyActivity;->finish()V

    :goto_0
    return-void
.end method

.method public onActivityResultInstallFirmware(Landroid/content/Intent;)V
    .locals 2

    .line 510
    const-string v0, "tempfile"

    invoke-virtual {p0, p1, v0}, Lcom/markus/FilePickerProxyActivity;->copyFile(Landroid/content/Intent;Ljava/lang/String;)Ljava/io/File;

    move-result-object p1

    if-nez p1, :cond_0

    return-void

    .line 515
    :cond_0
    iget-object v0, p0, Lcom/markus/FilePickerProxyActivity;->checkUpdate:Lcom/mi/fitness/checkupdate/export/CheckUpdateManager;

    invoke-virtual {p1}, Ljava/io/File;->getAbsolutePath()Ljava/lang/String;

    move-result-object p1

    const/4 v1, 0x0

    invoke-interface {v0, p0, p1, v1}, Lcom/mi/fitness/checkupdate/export/CheckUpdateManager;->manualUpgrade(Landroid/content/Context;Ljava/lang/String;Z)V

    return-void
.end method

.method public onActivityResultInstallWatchface(Landroid/content/Intent;)V
    .locals 3

    .line 476
    const-string v0, "tempfile"

    invoke-virtual {p0, p1, v0}, Lcom/markus/FilePickerProxyActivity;->copyFile(Landroid/content/Intent;Ljava/lang/String;)Ljava/io/File;

    move-result-object p1

    if-nez p1, :cond_0

    return-void

    :cond_0
    const/16 v0, 0x1000

    .line 481
    new-array v0, v0, [B

    const/4 v1, 0x1

    .line 482
    :try_start_0
    new-instance v2, Ljava/io/FileInputStream;

    invoke-direct {v2, p1}, Ljava/io/FileInputStream;-><init>(Ljava/io/File;)V
    :try_end_0
    .catch Ljava/io/IOException; {:try_start_0 .. :try_end_0} :catch_1

    .line 483
    :try_start_1
    invoke-virtual {v2, v0}, Ljava/io/InputStream;->read([B)I
    :try_end_1
    .catchall {:try_start_1 .. :try_end_1} :catchall_0

    .line 484
    :try_start_2
    invoke-virtual {v2}, Ljava/io/InputStream;->close()V
    :try_end_2
    .catch Ljava/io/IOException; {:try_start_2 .. :try_end_2} :catch_1

    .line 491
    :try_start_3
    sget-object v2, Lcom/markus/FilePickerProxyActivity;->ZIP_HEADER:[B

    invoke-static {v0, v2}, Lcom/markus/FilePickerProxyActivity;->startsWith([B[B)Z

    move-result v2

    if-eqz v2, :cond_1

    .line 492
    invoke-direct {p0, p1}, Lcom/markus/FilePickerProxyActivity;->handleInstallWatchfaceArchive(Ljava/io/File;)V

    return-void

    .line 495
    :cond_1
    sget-object v2, Lcom/markus/FilePickerProxyActivity;->WATCHFACE_HEADER:[B

    invoke-static {v0, v2}, Lcom/markus/FilePickerProxyActivity;->startsWith([B[B)Z

    move-result v0

    if-eqz v0, :cond_2

    .line 496
    invoke-direct {p0, p1}, Lcom/markus/FilePickerProxyActivity;->handleInstallWatchfaceBinary(Ljava/io/File;)V
    :try_end_3
    .catch Ljava/lang/Exception; {:try_start_3 .. :try_end_3} :catch_0

    return-void

    .line 505
    :cond_2
    const-string p1, "device_settings_not_a_watchface"

    invoke-direct {p0, p1}, Lcom/markus/FilePickerProxyActivity;->getResourceString(Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    invoke-static {p0, p1, v1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p1

    invoke-virtual {p1}, Landroid/widget/Toast;->show()V

    .line 506
    invoke-virtual {p0}, Lcom/markus/FilePickerProxyActivity;->finish()V

    return-void

    :catch_0
    move-exception p1

    .line 500
    invoke-virtual {p1}, Ljava/lang/Exception;->getMessage()Ljava/lang/String;

    move-result-object p1

    invoke-static {p0, p1, v1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p1

    invoke-virtual {p1}, Landroid/widget/Toast;->show()V

    .line 501
    invoke-virtual {p0}, Lcom/markus/FilePickerProxyActivity;->finish()V

    return-void

    :catchall_0
    move-exception p1

    .line 482
    :try_start_4
    invoke-virtual {v2}, Ljava/io/InputStream;->close()V
    :try_end_4
    .catchall {:try_start_4 .. :try_end_4} :catchall_1

    goto :goto_0

    :catchall_1
    move-exception v0

    :try_start_5
    invoke-static {p1, v0}, Lcom/markus/FilePickerProxyActivity$$ExternalSyntheticBackport0;->m(Ljava/lang/Throwable;Ljava/lang/Throwable;)V

    :goto_0
    throw p1
    :try_end_5
    .catch Ljava/io/IOException; {:try_start_5 .. :try_end_5} :catch_1

    .line 485
    :catch_1
    const-string p1, "device_settings_io_error"

    invoke-direct {p0, p1}, Lcom/markus/FilePickerProxyActivity;->getResourceString(Ljava/lang/String;)Ljava/lang/String;

    move-result-object p1

    invoke-static {p0, p1, v1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p1

    invoke-virtual {p1}, Landroid/widget/Toast;->show()V

    .line 486
    invoke-virtual {p0}, Lcom/markus/FilePickerProxyActivity;->finish()V

    return-void
.end method

.method public onCreate(Landroid/os/Bundle;)V
    .locals 3

    .line 63
    invoke-super {p0, p1}, Landroidx/fragment/app/FragmentActivity;->onCreate(Landroid/os/Bundle;)V

    .line 65
    new-instance p1, Landroid/widget/FrameLayout;

    invoke-direct {p1, p0}, Landroid/widget/FrameLayout;-><init>(Landroid/content/Context;)V

    .line 66
    invoke-static {}, Landroid/view/View;->generateViewId()I

    move-result v0

    invoke-virtual {p1, v0}, Landroid/widget/FrameLayout;->setId(I)V

    .line 67
    invoke-virtual {p0, p1}, Lcom/markus/FilePickerProxyActivity;->setContentView(Landroid/view/View;)V

    .line 69
    new-instance p1, Lcom/mi/fitness/checkupdate/util/CheckUpdateManagerImpl;

    invoke-direct {p1}, Lcom/mi/fitness/checkupdate/util/CheckUpdateManagerImpl;-><init>()V

    iput-object p1, p0, Lcom/markus/FilePickerProxyActivity;->checkUpdate:Lcom/mi/fitness/checkupdate/export/CheckUpdateManager;

    .line 71
    new-instance p1, Landroid/content/Intent;

    invoke-direct {p1}, Landroid/content/Intent;-><init>()V

    const-string v0, "*/*"

    invoke-virtual {p1, v0}, Landroid/content/Intent;->setType(Ljava/lang/String;)Landroid/content/Intent;

    move-result-object p1

    const-string v0, "android.intent.action.GET_CONTENT"

    invoke-virtual {p1, v0}, Landroid/content/Intent;->setAction(Ljava/lang/String;)Landroid/content/Intent;

    move-result-object p1

    .line 72
    invoke-virtual {p0}, Lcom/markus/FilePickerProxyActivity;->getIntent()Landroid/content/Intent;

    move-result-object v0

    const-string v1, "request_id"

    const/4 v2, 0x0

    invoke-virtual {v0, v1, v2}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I

    move-result v0

    const/4 v1, 0x1

    if-ne v0, v1, :cond_0

    .line 73
    const-string v0, "device_settings_select_firmware"

    invoke-direct {p0, v0}, Lcom/markus/FilePickerProxyActivity;->getResourceString(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    invoke-static {p1, v0}, Landroid/content/Intent;->createChooser(Landroid/content/Intent;Ljava/lang/CharSequence;)Landroid/content/Intent;

    move-result-object p1

    const v0, 0x79bdf

    invoke-virtual {p0, p1, v0}, Lcom/markus/FilePickerProxyActivity;->startActivityForResult(Landroid/content/Intent;I)V

    goto :goto_0

    .line 75
    :cond_0
    const-string v0, "device_settings_select_watchface"

    invoke-direct {p0, v0}, Lcom/markus/FilePickerProxyActivity;->getResourceString(Ljava/lang/String;)Ljava/lang/String;

    move-result-object v0

    invoke-static {p1, v0}, Landroid/content/Intent;->createChooser(Landroid/content/Intent;Ljava/lang/CharSequence;)Landroid/content/Intent;

    move-result-object p1

    const v0, 0x79bde

    invoke-virtual {p0, p1, v0}, Lcom/markus/FilePickerProxyActivity;->startActivityForResult(Landroid/content/Intent;I)V

    :goto_0
    return-void
.end method
