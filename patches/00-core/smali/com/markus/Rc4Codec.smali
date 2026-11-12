.class public final Lcom/markus/Rc4Codec;
.super Ljava/lang/Object;
.source "Rc4Codec.java"


# direct methods
.method private constructor <init>()V
    .locals 0

    .line 11
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method public static crypt(Ljava/lang/String;Ljava/lang/String;[B)[B
    .locals 3

    .line 51
    :try_start_0
    invoke-static {}, Ljava/util/Base64;->getDecoder()Ljava/util/Base64$Decoder;

    move-result-object v0

    invoke-virtual {v0, p0}, Ljava/util/Base64$Decoder;->decode(Ljava/lang/String;)[B

    move-result-object p0

    .line 52
    invoke-static {}, Ljava/util/Base64;->getDecoder()Ljava/util/Base64$Decoder;

    move-result-object v0

    invoke-virtual {v0, p1}, Ljava/util/Base64$Decoder;->decode(Ljava/lang/String;)[B

    move-result-object p1

    .line 53
    array-length v0, p0

    array-length v1, p1

    add-int/2addr v0, v1

    new-array v0, v0, [B

    .line 55
    array-length v1, p0

    const/4 v2, 0x0

    invoke-static {p0, v2, v0, v2, v1}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V

    .line 56
    array-length p0, p0

    array-length v1, p1

    invoke-static {p1, v2, v0, p0, v1}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V

    .line 57
    const-string p0, "SHA-256"

    invoke-static {p0}, Ljava/security/MessageDigest;->getInstance(Ljava/lang/String;)Ljava/security/MessageDigest;

    move-result-object p0

    .line 59
    invoke-virtual {p0, v0}, Ljava/security/MessageDigest;->digest([B)[B

    move-result-object p0

    .line 60
    invoke-static {p2, p0}, Lcom/markus/Rc4Codec;->rc4Drop1024([B[B)[B

    move-result-object p0
    :try_end_0
    .catch Ljava/lang/Exception; {:try_start_0 .. :try_end_0} :catch_0

    return-object p0

    :catch_0
    move-exception p0

    .line 62
    new-instance p1, Ljava/lang/RuntimeException;

    const-string p2, "Failed to encrypt/decrypt data"

    invoke-direct {p1, p2, p0}, Ljava/lang/RuntimeException;-><init>(Ljava/lang/String;Ljava/lang/Throwable;)V

    throw p1
.end method

.method public static decode(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    .locals 5

    const/4 v0, 0x1

    .line 26
    new-array v1, v0, [Ljava/lang/Object;

    const/4 v2, 0x0

    aput-object p0, v1, v2

    const-string v3, "ssecurity: %s"

    invoke-static {v3, v1}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v1

    const-string v3, "Rc4Decode"

    invoke-static {v3, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 27
    new-array v1, v0, [Ljava/lang/Object;

    aput-object p1, v1, v2

    const-string v4, "nonce: %s"

    invoke-static {v4, v1}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v1

    invoke-static {v3, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 28
    new-array v0, v0, [Ljava/lang/Object;

    aput-object p2, v0, v2

    const-string v1, "encodedData: %s"

    invoke-static {v1, v0}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object v0

    invoke-static {v3, v0}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 29
    invoke-static {}, Ljava/util/Base64;->getDecoder()Ljava/util/Base64$Decoder;

    move-result-object v0

    invoke-virtual {v0, p2}, Ljava/util/Base64$Decoder;->decode(Ljava/lang/String;)[B

    move-result-object p2

    .line 30
    invoke-static {p0, p1, p2}, Lcom/markus/Rc4Codec;->crypt(Ljava/lang/String;Ljava/lang/String;[B)[B

    move-result-object p0

    .line 32
    new-instance p1, Ljava/lang/String;

    sget-object p2, Ljava/nio/charset/StandardCharsets;->UTF_8:Ljava/nio/charset/Charset;

    invoke-direct {p1, p0, p2}, Ljava/lang/String;-><init>([BLjava/nio/charset/Charset;)V

    return-object p1
.end method

.method public static encode(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
    .locals 1

    .line 39
    sget-object v0, Ljava/nio/charset/StandardCharsets;->UTF_8:Ljava/nio/charset/Charset;

    invoke-virtual {p2, v0}, Ljava/lang/String;->getBytes(Ljava/nio/charset/Charset;)[B

    move-result-object p2

    .line 40
    invoke-static {p0, p1, p2}, Lcom/markus/Rc4Codec;->crypt(Ljava/lang/String;Ljava/lang/String;[B)[B

    move-result-object p0

    .line 41
    invoke-static {}, Ljava/util/Base64;->getEncoder()Ljava/util/Base64$Encoder;

    move-result-object p1

    invoke-virtual {p1, p0}, Ljava/util/Base64$Encoder;->encodeToString([B)Ljava/lang/String;

    move-result-object p0

    return-object p0
.end method

.method public static rc4Drop1024([B[B)[B
    .locals 7

    const/16 v0, 0x100

    .line 70
    new-array v1, v0, [I

    const/4 v2, 0x0

    const/4 v3, 0x0

    :goto_0
    if-ge v3, v0, :cond_0

    .line 73
    aput v3, v1, v3

    add-int/lit8 v3, v3, 0x1

    goto :goto_0

    :cond_0
    const/4 v3, 0x0

    const/4 v4, 0x0

    :goto_1
    if-ge v3, v0, :cond_1

    .line 80
    aget v5, v1, v3

    add-int/2addr v4, v5

    array-length v6, p1

    rem-int v6, v3, v6

    aget-byte v6, p1, v6

    and-int/lit16 v6, v6, 0xff

    add-int/2addr v4, v6

    and-int/lit16 v4, v4, 0xff

    .line 82
    aget v6, v1, v4

    aput v6, v1, v3

    .line 83
    aput v5, v1, v4

    add-int/lit8 v3, v3, 0x1

    goto :goto_1

    :cond_1
    const/4 p1, 0x0

    const/4 v0, 0x0

    const/4 v3, 0x0

    :goto_2
    const/16 v4, 0x400

    if-ge p1, v4, :cond_2

    add-int/lit8 v0, v0, 0x1

    and-int/lit16 v0, v0, 0xff

    .line 93
    aget v4, v1, v0

    add-int/2addr v3, v4

    and-int/lit16 v3, v3, 0xff

    .line 96
    aget v5, v1, v3

    aput v5, v1, v0

    .line 97
    aput v4, v1, v3

    .line 100
    aget v5, v1, v0

    add-int/2addr v5, v4

    and-int/lit16 v4, v5, 0xff

    aget v4, v1, v4

    add-int/lit8 p1, p1, 0x1

    goto :goto_2

    .line 103
    :cond_2
    array-length p1, p0

    new-array p1, p1, [B

    .line 105
    :goto_3
    array-length v4, p0

    if-ge v2, v4, :cond_3

    add-int/lit8 v0, v0, 0x1

    and-int/lit16 v0, v0, 0xff

    .line 107
    aget v4, v1, v0

    add-int/2addr v3, v4

    and-int/lit16 v3, v3, 0xff

    .line 110
    aget v5, v1, v3

    aput v5, v1, v0

    .line 111
    aput v4, v1, v3

    .line 113
    aget v5, v1, v0

    add-int/2addr v5, v4

    and-int/lit16 v4, v5, 0xff

    aget v4, v1, v4

    .line 114
    aget-byte v5, p0, v2

    and-int/lit16 v5, v5, 0xff

    xor-int/2addr v4, v5

    int-to-byte v4, v4

    aput-byte v4, p1, v2

    add-int/lit8 v2, v2, 0x1

    goto :goto_3

    :cond_3
    return-object p1
.end method
