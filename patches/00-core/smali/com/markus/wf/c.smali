.class public final Lcom/markus/wf/c;
.super Ljava/lang/Object;
.source "r8-map-id-339144b03bc704df7f72f5fb6d7a25592d5bbc656a9037a663fcda542ffbf712"


# instance fields
.field public final a:Ljava/lang/Object;


# direct methods
.method public constructor <init>(III)V
    .locals 1

    .line 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    const/4 v0, 0x3

    if-eqz p3, :cond_3

    if-eq p3, v0, :cond_2

    const/4 v0, 0x6

    if-eq p3, v0, :cond_1

    const/16 v0, 0x10

    if-ne p3, v0, :cond_0

    .line 2
    new-instance p3, Lcom/markus/wf/g;

    const/4 v0, 0x0

    .line 3
    invoke-direct {p3, p1, p2, v0}, Lcom/markus/wf/g;-><init>(IIB)V

    .line 4
    iput-object p3, p0, Lcom/markus/wf/c;->a:Ljava/lang/Object;

    return-void

    .line 5
    :cond_0
    new-instance p0, Lcom/markus/wf/h;

    invoke-static {p3}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object p1

    filled-new-array {p1}, [Ljava/lang/Object;

    move-result-object p1

    const-string p2, "Color format %d is unknown!"

    invoke-static {p2, p1}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;

    move-result-object p1

    .line 6
    invoke-direct {p0, p1}, Ljava/lang/Exception;-><init>(Ljava/lang/String;)V

    .line 7
    throw p0

    .line 8
    :cond_1
    new-instance p3, Lcom/markus/wf/g;

    const/4 v0, 0x2

    .line 9
    invoke-direct {p3, p1, p2, v0}, Lcom/markus/wf/g;-><init>(IIB)V

    .line 10
    iput-object p3, p0, Lcom/markus/wf/c;->a:Ljava/lang/Object;

    return-void

    .line 11
    :cond_2
    new-instance p3, Lcom/markus/wf/g;

    const/4 v0, 0x1

    .line 12
    invoke-direct {p3, p1, p2, v0}, Lcom/markus/wf/g;-><init>(IIB)V

    .line 13
    iput-object p3, p0, Lcom/markus/wf/c;->a:Ljava/lang/Object;

    return-void

    .line 14
    :cond_3
    new-instance p3, Lcom/markus/wf/g;

    .line 15
    invoke-direct {p3, p1, p2, v0}, Lcom/markus/wf/g;-><init>(IIB)V

    .line 16
    iput-object p3, p0, Lcom/markus/wf/c;->a:Ljava/lang/Object;

    return-void
.end method

.method public constructor <init>([B)V
    .locals 1

    .line 17
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 18
    invoke-static {p1}, Ljava/nio/ByteBuffer;->wrap([B)Ljava/nio/ByteBuffer;

    move-result-object p1

    sget-object v0, Ljava/nio/ByteOrder;->LITTLE_ENDIAN:Ljava/nio/ByteOrder;

    invoke-virtual {p1, v0}, Ljava/nio/ByteBuffer;->order(Ljava/nio/ByteOrder;)Ljava/nio/ByteBuffer;

    move-result-object p1

    iput-object p1, p0, Lcom/markus/wf/c;->a:Ljava/lang/Object;

    return-void
.end method


# virtual methods
.method public a(I)[B
    .locals 0

    .line 1
    new-array p1, p1, [B

    .line 2
    .line 3
    iget-object p0, p0, Lcom/markus/wf/c;->a:Ljava/lang/Object;

    .line 4
    .line 5
    check-cast p0, Ljava/nio/ByteBuffer;

    .line 6
    .line 7
    invoke-virtual {p0, p1}, Ljava/nio/ByteBuffer;->get([B)Ljava/nio/ByteBuffer;

    .line 8
    .line 9
    .line 10
    return-object p1
.end method

.method public b(I)Ljava/lang/String;
    .locals 3

    .line 1
    new-array v0, p1, [B

    .line 2
    iget-object p0, p0, Lcom/markus/wf/c;->a:Ljava/lang/Object;

    check-cast p0, Ljava/nio/ByteBuffer;

    invoke-virtual {p0, v0}, Ljava/nio/ByteBuffer;->get([B)Ljava/nio/ByteBuffer;

    const/4 p0, 0x0

    move v1, p0

    :goto_0
    if-ge v1, p1, :cond_0

    .line 3
    aget-byte v2, v0, v1

    if-eqz v2, :cond_0

    add-int/lit8 v1, v1, 0x1

    goto :goto_0

    .line 4
    :cond_0
    new-instance p1, Ljava/lang/String;

    sget-object v2, Ljava/nio/charset/StandardCharsets;->UTF_8:Ljava/nio/charset/Charset;

    invoke-direct {p1, v0, p0, v1, v2}, Ljava/lang/String;-><init>([BIILjava/nio/charset/Charset;)V

    return-object p1
.end method

.method public c()I
    .locals 1

    .line 1
    iget-object p0, p0, Lcom/markus/wf/c;->a:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Ljava/nio/ByteBuffer;

    .line 4
    .line 5
    invoke-virtual {p0}, Ljava/nio/ByteBuffer;->getShort()S

    .line 6
    .line 7
    .line 8
    move-result p0

    .line 9
    invoke-static {p0}, Ljava/lang/Short;->toUnsignedInt(S)I

    .line 10
    .line 11
    .line 12
    move-result p0

    .line 13
    const v0, 0xffff

    .line 14
    .line 15
    .line 16
    and-int/2addr p0, v0

    .line 17
    return p0
.end method

.method public d()J
    .locals 2

    .line 1
    iget-object p0, p0, Lcom/markus/wf/c;->a:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Ljava/nio/ByteBuffer;

    .line 4
    .line 5
    invoke-virtual {p0}, Ljava/nio/ByteBuffer;->getInt()I

    .line 6
    .line 7
    .line 8
    move-result p0

    .line 9
    invoke-static {p0}, Ljava/lang/Integer;->toUnsignedLong(I)J

    .line 10
    .line 11
    .line 12
    move-result-wide v0

    .line 13
    return-wide v0
.end method

.method public e()I
    .locals 0

    .line 1
    iget-object p0, p0, Lcom/markus/wf/c;->a:Ljava/lang/Object;

    .line 2
    .line 3
    check-cast p0, Ljava/nio/ByteBuffer;

    .line 4
    .line 5
    invoke-virtual {p0}, Ljava/nio/ByteBuffer;->get()B

    .line 6
    .line 7
    .line 8
    move-result p0

    .line 9
    invoke-static {p0}, Ljava/lang/Byte;->toUnsignedInt(B)I

    .line 10
    .line 11
    .line 12
    move-result p0

    .line 13
    and-int/lit16 p0, p0, 0xff

    .line 14
    .line 15
    return p0
.end method
