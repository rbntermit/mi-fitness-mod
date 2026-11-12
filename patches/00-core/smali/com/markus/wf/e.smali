.class public final Lcom/markus/wf/e;
.super Ljava/lang/Object;
.source "r8-map-id-339144b03bc704df7f72f5fb6d7a25592d5bbc656a9037a663fcda542ffbf712"


# instance fields
.field public final a:[B

.field public b:Lcom/markus/wf/o;


# direct methods
.method public constructor <init>([B)V
    .locals 1

    .line 1
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    .line 2
    .line 3
    .line 4
    const/4 v0, 0x0

    .line 5
    iput-object v0, p0, Lcom/markus/wf/e;->b:Lcom/markus/wf/o;

    .line 6
    .line 7
    iput-object p1, p0, Lcom/markus/wf/e;->a:[B

    .line 8
    .line 9
    return-void
.end method


# virtual methods
.method public final a()V
    .locals 54

    move-object/from16 v0, p0

    .line 1
    iget-object v1, v0, Lcom/markus/wf/e;->a:[B

    invoke-static {v1}, Ljava/nio/ByteBuffer;->wrap([B)Ljava/nio/ByteBuffer;

    move-result-object v2

    sget-object v3, Ljava/nio/ByteOrder;->LITTLE_ENDIAN:Ljava/nio/ByteOrder;

    invoke-virtual {v2, v3}, Ljava/nio/ByteBuffer;->order(Ljava/nio/ByteOrder;)Ljava/nio/ByteBuffer;

    move-result-object v2

    .line 2
    invoke-virtual {v2}, Ljava/nio/ByteBuffer;->getInt()I

    move-result v3

    invoke-static {v3}, Ljava/lang/Integer;->toUnsignedLong(I)J

    move-result-wide v3

    const-wide/32 v5, 0x1234a55a

    cmp-long v3, v3, v5

    if-nez v3, :cond_20

    const/16 v3, 0x10

    .line 3
    invoke-virtual {v2, v3}, Ljava/nio/Buffer;->position(I)Ljava/nio/Buffer;

    move-result-object v3

    check-cast v3, Ljava/nio/ByteBuffer;

    .line 4
    invoke-virtual {v2}, Ljava/nio/ByteBuffer;->getInt()I

    move-result v2

    invoke-static {v2}, Ljava/lang/Integer;->toUnsignedLong(I)J

    move-result-wide v2

    const/16 v4, 0x8

    shr-long/2addr v2, v4

    const-wide/16 v5, 0x9

    cmp-long v2, v2, v5

    if-nez v2, :cond_0

    .line 5
    new-instance v2, Lcom/markus/wf/b;

    .line 6
    invoke-direct {v2}, Ljava/lang/Object;-><init>()V

    goto :goto_0

    .line 7
    :cond_0
    new-instance v2, Lcom/markus/wf/a;

    .line 8
    invoke-direct {v2}, Ljava/lang/Object;-><init>()V

    .line 9
    :goto_0
    invoke-virtual {v2}, Lcom/markus/wf/a;->a()Lcom/markus/wf/l;

    move-result-object v2

    .line 10
    new-instance v3, Lcom/markus/wf/c;

    invoke-direct {v3, v1}, Lcom/markus/wf/c;-><init>([B)V

    .line 11
    new-instance v1, Lcom/markus/wf/o;

    .line 12
    invoke-direct {v1}, Ljava/lang/Object;-><init>()V

    const/4 v5, 0x0

    .line 13
    iput-object v5, v1, Lcom/markus/wf/o;->e:Ljava/lang/String;

    .line 14
    iput-object v5, v1, Lcom/markus/wf/o;->f:Lcom/markus/wf/n;

    .line 15
    new-instance v6, Ljava/util/ArrayList;

    invoke-direct {v6}, Ljava/util/ArrayList;-><init>()V

    iput-object v6, v1, Lcom/markus/wf/o;->g:Ljava/util/ArrayList;

    .line 16
    new-instance v6, Ljava/util/ArrayList;

    invoke-direct {v6}, Ljava/util/ArrayList;-><init>()V

    iput-object v6, v1, Lcom/markus/wf/o;->h:Ljava/util/ArrayList;

    .line 17
    invoke-virtual {v3}, Lcom/markus/wf/c;->d()J

    .line 18
    invoke-virtual {v3}, Lcom/markus/wf/c;->d()J

    .line 19
    invoke-virtual {v3}, Lcom/markus/wf/c;->d()J

    .line 20
    invoke-virtual {v3}, Lcom/markus/wf/c;->d()J

    .line 21
    invoke-virtual {v3}, Lcom/markus/wf/c;->d()J

    .line 22
    invoke-virtual {v3}, Lcom/markus/wf/c;->d()J

    .line 23
    invoke-virtual {v3}, Lcom/markus/wf/c;->d()J

    move-result-wide v6

    iput-wide v6, v1, Lcom/markus/wf/o;->a:J

    .line 24
    invoke-virtual {v3}, Lcom/markus/wf/c;->e()I

    move-result v6

    iput-short v6, v1, Lcom/markus/wf/o;->b:S

    .line 25
    invoke-virtual {v3}, Lcom/markus/wf/c;->e()I

    move-result v6

    iput-short v6, v1, Lcom/markus/wf/o;->c:S

    .line 26
    invoke-virtual {v3}, Lcom/markus/wf/c;->c()I

    .line 27
    invoke-virtual {v3}, Lcom/markus/wf/c;->d()J

    .line 28
    invoke-virtual {v3}, Lcom/markus/wf/c;->d()J

    const/16 v6, 0x40

    .line 29
    invoke-virtual {v3, v6}, Lcom/markus/wf/c;->b(I)Ljava/lang/String;

    .line 30
    invoke-virtual {v3, v6}, Lcom/markus/wf/c;->a(I)[B

    move-result-object v7

    iput-object v7, v1, Lcom/markus/wf/o;->d:[B

    .line 31
    iget-wide v7, v1, Lcom/markus/wf/o;->a:J

    iget-short v9, v1, Lcom/markus/wf/o;->c:S

    int-to-long v9, v9

    invoke-static {v7, v8, v9, v10}, Ljava/lang/Math;->max(JJ)J

    move-result-wide v7

    const-wide/16 v11, 0x0

    :goto_1
    cmp-long v13, v11, v7

    const-wide/16 v14, 0x1

    if-gez v13, :cond_1

    .line 32
    iget-object v13, v1, Lcom/markus/wf/o;->g:Ljava/util/ArrayList;

    invoke-virtual {v3}, Lcom/markus/wf/c;->d()J

    move-result-wide v16

    move/from16 v18, v4

    invoke-static/range {v16 .. v17}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;

    move-result-object v4

    invoke-virtual {v13, v4}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    add-long/2addr v11, v14

    move/from16 v4, v18

    goto :goto_1

    :cond_1
    move/from16 v18, v4

    .line 33
    new-instance v4, Ljava/util/ArrayList;

    invoke-direct {v4}, Ljava/util/ArrayList;-><init>()V

    const/4 v7, 0x0

    move v8, v7

    .line 34
    :goto_2
    iget-short v11, v1, Lcom/markus/wf/o;->b:S

    const/16 v16, 0x1

    const-wide/16 v19, 0x0

    iget-object v9, v3, Lcom/markus/wf/c;->a:Ljava/lang/Object;

    if-ge v8, v11, :cond_5

    .line 35
    new-instance v10, Ljava/util/ArrayList;

    invoke-direct {v10}, Ljava/util/ArrayList;-><init>()V

    invoke-virtual {v4, v10}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 36
    new-instance v10, Lcom/markus/wf/k;

    .line 37
    invoke-direct {v10}, Ljava/lang/Object;-><init>()V

    .line 38
    iput-object v5, v10, Lcom/markus/wf/k;->c:Lcom/markus/wf/f;

    .line 39
    new-instance v11, Ljava/util/ArrayList;

    invoke-direct {v11}, Ljava/util/ArrayList;-><init>()V

    .line 40
    new-instance v11, Ljava/util/ArrayList;

    invoke-direct {v11}, Ljava/util/ArrayList;-><init>()V

    iput-object v11, v10, Lcom/markus/wf/k;->f:Ljava/util/ArrayList;

    .line 41
    check-cast v9, Ljava/nio/ByteBuffer;

    invoke-virtual {v9}, Ljava/nio/ByteBuffer;->getInt()I

    move-result v9

    const/16 v11, 0x1f

    const/16 v17, 0x2

    int-to-long v12, v9

    .line 42
    iput-wide v12, v10, Lcom/markus/wf/k;->a:J

    .line 43
    invoke-virtual {v3}, Lcom/markus/wf/c;->d()J

    move-result-wide v12

    iput-wide v12, v10, Lcom/markus/wf/k;->b:J

    .line 44
    iget-wide v12, v10, Lcom/markus/wf/k;->a:J

    shr-long v21, v12, v11

    cmp-long v9, v21, v19

    if-eqz v9, :cond_2

    move/from16 v9, v16

    goto :goto_3

    :cond_2
    move v9, v7

    :goto_3
    iput-boolean v9, v10, Lcom/markus/wf/k;->d:Z

    const-wide/32 v21, 0xffffff

    and-long v11, v12, v21

    .line 45
    iput-wide v11, v10, Lcom/markus/wf/k;->a:J

    move v9, v7

    .line 46
    :goto_4
    invoke-virtual {v2}, Lcom/markus/wf/l;->a()I

    move-result v11

    if-ge v9, v11, :cond_3

    .line 47
    new-instance v11, Lcom/markus/wf/l;

    .line 48
    invoke-direct {v11}, Ljava/lang/Object;-><init>()V

    .line 49
    invoke-virtual {v3}, Lcom/markus/wf/c;->d()J

    .line 50
    invoke-virtual {v3}, Lcom/markus/wf/c;->d()J

    .line 51
    invoke-virtual {v4}, Ljava/util/ArrayList;->size()I

    move-result v12

    add-int/lit8 v12, v12, -0x1

    invoke-virtual {v4, v12}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v12

    check-cast v12, Ljava/util/List;

    invoke-interface {v12, v11}, Ljava/util/List;->add(Ljava/lang/Object;)Z

    add-int/lit8 v9, v9, 0x1

    goto :goto_4

    .line 52
    :cond_3
    iget-boolean v9, v10, Lcom/markus/wf/k;->d:Z

    if-eqz v9, :cond_4

    .line 53
    invoke-virtual {v3, v6}, Lcom/markus/wf/c;->b(I)Ljava/lang/String;

    .line 54
    invoke-virtual {v3}, Lcom/markus/wf/c;->d()J

    .line 55
    invoke-virtual {v3}, Lcom/markus/wf/c;->d()J

    move-result-wide v11

    shr-long v11, v11, v17

    long-to-int v9, v11

    .line 56
    iput v9, v10, Lcom/markus/wf/k;->e:I

    move v9, v7

    .line 57
    :goto_5
    iget v11, v10, Lcom/markus/wf/k;->e:I

    if-ge v9, v11, :cond_4

    .line 58
    invoke-virtual {v3}, Lcom/markus/wf/c;->d()J

    move-result-wide v11

    .line 59
    iget-object v13, v10, Lcom/markus/wf/k;->f:Ljava/util/ArrayList;

    invoke-static {v11, v12}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;

    move-result-object v11

    invoke-virtual {v13, v11}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    add-int/lit8 v9, v9, 0x1

    goto :goto_5

    .line 60
    :cond_4
    iget-object v9, v1, Lcom/markus/wf/o;->h:Ljava/util/ArrayList;

    invoke-virtual {v9, v10}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    add-int/lit8 v8, v8, 0x1

    goto/16 :goto_2

    :cond_5
    const/16 v11, 0x1f

    const/16 v17, 0x2

    .line 61
    iget-object v2, v1, Lcom/markus/wf/o;->d:[B

    .line 62
    invoke-static {v2}, Ljava/nio/ByteBuffer;->wrap([B)Ljava/nio/ByteBuffer;

    move-result-object v2

    sget-object v4, Ljava/nio/ByteOrder;->LITTLE_ENDIAN:Ljava/nio/ByteOrder;

    invoke-virtual {v2, v4}, Ljava/nio/ByteBuffer;->order(Ljava/nio/ByteOrder;)Ljava/nio/ByteBuffer;

    move-result-object v2

    .line 63
    invoke-virtual {v2}, Ljava/nio/ByteBuffer;->getInt()I

    move-result v4

    invoke-static {v4}, Ljava/lang/Integer;->toUnsignedLong(I)J

    move-result-wide v4

    const-wide v12, 0xffffffffL

    cmp-long v4, v4, v12

    if-eqz v4, :cond_7

    .line 64
    invoke-virtual {v2, v7}, Ljava/nio/Buffer;->position(I)Ljava/nio/Buffer;

    move-result-object v4

    check-cast v4, Ljava/nio/ByteBuffer;

    .line 65
    iget-object v4, v1, Lcom/markus/wf/o;->d:[B

    array-length v4, v4

    .line 66
    new-array v5, v4, [B

    .line 67
    invoke-virtual {v2, v5}, Ljava/nio/ByteBuffer;->get([B)Ljava/nio/ByteBuffer;

    move v2, v7

    :goto_6
    if-ge v2, v4, :cond_6

    .line 68
    aget-byte v6, v5, v2

    if-eqz v6, :cond_6

    add-int/lit8 v2, v2, 0x1

    goto :goto_6

    .line 69
    :cond_6
    new-instance v4, Ljava/lang/String;

    sget-object v6, Ljava/nio/charset/StandardCharsets;->UTF_8:Ljava/nio/charset/Charset;

    invoke-direct {v4, v5, v7, v2, v6}, Ljava/lang/String;-><init>([BIILjava/nio/charset/Charset;)V

    .line 70
    iput-object v4, v1, Lcom/markus/wf/o;->e:Ljava/lang/String;

    move/from16 v22, v11

    move-wide/from16 v23, v12

    goto/16 :goto_9

    .line 71
    :cond_7
    new-instance v4, Lcom/markus/wf/j;

    .line 72
    invoke-direct {v4}, Ljava/lang/Object;-><init>()V

    .line 73
    invoke-virtual {v2}, Ljava/nio/ByteBuffer;->getInt()I

    move-result v5

    invoke-static {v5}, Ljava/lang/Integer;->toUnsignedLong(I)J

    .line 74
    invoke-virtual {v2}, Ljava/nio/ByteBuffer;->getInt()I

    move-result v5

    invoke-static {v5}, Ljava/lang/Integer;->toUnsignedLong(I)J

    .line 75
    invoke-virtual {v2}, Ljava/nio/ByteBuffer;->getInt()I

    move-result v5

    invoke-static {v5}, Ljava/lang/Integer;->toUnsignedLong(I)J

    move-result-wide v5

    .line 76
    iput-wide v5, v4, Lcom/markus/wf/j;->a:J

    .line 77
    invoke-virtual {v2}, Ljava/nio/ByteBuffer;->getInt()I

    move-result v2

    invoke-static {v2}, Ljava/lang/Integer;->toUnsignedLong(I)J

    move-result-wide v5

    .line 78
    iput-wide v5, v4, Lcom/markus/wf/j;->b:J

    .line 79
    iget-wide v5, v4, Lcom/markus/wf/j;->a:J

    .line 80
    move-object v2, v9

    check-cast v2, Ljava/nio/ByteBuffer;

    long-to-int v5, v5

    invoke-virtual {v2, v5}, Ljava/nio/Buffer;->position(I)Ljava/nio/Buffer;

    move-result-object v2

    check-cast v2, Ljava/nio/ByteBuffer;

    .line 81
    iget-wide v4, v4, Lcom/markus/wf/j;->b:J

    long-to-int v2, v4

    .line 82
    invoke-virtual {v3, v2}, Lcom/markus/wf/c;->a(I)[B

    move-result-object v2

    .line 83
    new-instance v4, Lcom/markus/wf/c;

    invoke-direct {v4, v2}, Lcom/markus/wf/c;-><init>([B)V

    .line 84
    new-instance v2, Lcom/markus/wf/n;

    .line 85
    invoke-direct {v2}, Ljava/lang/Object;-><init>()V

    .line 86
    new-instance v5, Ljava/util/HashMap;

    invoke-direct {v5}, Ljava/util/HashMap;-><init>()V

    iput-object v5, v2, Lcom/markus/wf/n;->a:Ljava/util/HashMap;

    .line 87
    iget-object v5, v4, Lcom/markus/wf/c;->a:Ljava/lang/Object;

    check-cast v5, Ljava/nio/ByteBuffer;

    .line 88
    invoke-virtual {v5}, Ljava/nio/ByteBuffer;->getLong()J

    move-result-wide v5

    .line 89
    new-instance v8, Ljava/util/ArrayList;

    invoke-direct {v8}, Ljava/util/ArrayList;-><init>()V

    .line 90
    const-string v52, "lt_LT"

    const-string v53, "bg_BG"

    const-string v21, "en_US"

    const-string v22, "zh_CN"

    const-string v23, "zh_TW"

    const-string v24, "ja_JP"

    const-string v25, "es_ES"

    const-string v26, "fr_FR"

    const-string v27, "de_DE"

    const-string v28, "ru_RU"

    const-string v29, "pt_BR"

    const-string v30, "pt_PT"

    const-string v31, "it_IT"

    const-string v32, "ko_KR"

    const-string v33, "tr_TR"

    const-string v34, "nl_NL"

    const-string v35, "th_TH"

    const-string v36, "sv_SE"

    const-string v37, "da_DK"

    const-string v38, "vi_VN"

    const-string v39, "nb_NO"

    const-string v40, "pl_PL"

    const-string v41, "fi_FI"

    const-string v42, "in_ID"

    const-string v43, "el_GR"

    const-string v44, "ro_RO"

    const-string v45, "cs_CZ"

    const-string v46, "uk_UA"

    const-string v47, "hu_HU"

    const-string v48, "sk_SK"

    const-string v49, "zh_HK"

    const-string v50, "iw_IL"

    const-string v51, "ar_EG"

    filled-new-array/range {v21 .. v53}, [Ljava/lang/String;

    move-result-object v10

    move/from16 v22, v11

    :goto_7
    const/16 v11, 0x21

    if-ge v7, v11, :cond_9

    shl-long v23, v14, v7

    and-long v23, v23, v5

    cmp-long v11, v23, v19

    move-wide/from16 v23, v12

    if-eqz v11, :cond_8

    .line 91
    invoke-virtual {v4}, Lcom/markus/wf/c;->d()J

    move-result-wide v12

    long-to-int v11, v12

    invoke-static {v11}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v11

    invoke-virtual {v8, v11}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    :cond_8
    add-int/lit8 v7, v7, 0x1

    move-wide/from16 v12, v23

    goto :goto_7

    :cond_9
    move-wide/from16 v23, v12

    const/4 v7, 0x0

    const/4 v12, 0x0

    :goto_8
    if-ge v7, v11, :cond_b

    shl-long v25, v14, v7

    and-long v25, v25, v5

    cmp-long v13, v25, v19

    if-eqz v13, :cond_a

    .line 92
    iget-object v13, v2, Lcom/markus/wf/n;->a:Ljava/util/HashMap;

    aget-object v11, v10, v7

    invoke-virtual {v8, v12}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v26

    check-cast v26, Ljava/lang/Integer;

    invoke-virtual/range {v26 .. v26}, Ljava/lang/Integer;->intValue()I

    move-result v14

    invoke-virtual {v4, v14}, Lcom/markus/wf/c;->b(I)Ljava/lang/String;

    move-result-object v14

    invoke-virtual {v13, v11, v14}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    add-int/lit8 v12, v12, 0x1

    :cond_a
    add-int/lit8 v7, v7, 0x1

    const/16 v11, 0x21

    const-wide/16 v14, 0x1

    goto :goto_8

    .line 93
    :cond_b
    iput-object v2, v1, Lcom/markus/wf/o;->f:Lcom/markus/wf/n;

    .line 94
    :goto_9
    iget-object v2, v1, Lcom/markus/wf/o;->h:Ljava/util/ArrayList;

    invoke-virtual {v2}, Ljava/util/ArrayList;->size()I

    move-result v4

    const/4 v5, 0x0

    :goto_a
    if-ge v5, v4, :cond_1f

    invoke-virtual {v2, v5}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    move-result-object v6

    add-int/lit8 v5, v5, 0x1

    check-cast v6, Lcom/markus/wf/k;

    .line 95
    iget-wide v7, v6, Lcom/markus/wf/k;->b:J

    cmp-long v10, v7, v19

    if-eqz v10, :cond_c

    cmp-long v10, v7, v23

    if-nez v10, :cond_d

    :cond_c
    move-object/from16 v25, v2

    move-object/from16 v26, v3

    move/from16 v28, v4

    move/from16 v29, v5

    goto/16 :goto_16

    .line 96
    :cond_d
    move-object v10, v9

    check-cast v10, Ljava/nio/ByteBuffer;

    long-to-int v7, v7

    invoke-virtual {v10, v7}, Ljava/nio/Buffer;->position(I)Ljava/nio/Buffer;

    move-result-object v7

    check-cast v7, Ljava/nio/ByteBuffer;

    .line 97
    new-instance v7, Lcom/markus/wf/f;

    .line 98
    invoke-direct {v7}, Ljava/lang/Object;-><init>()V

    .line 99
    invoke-virtual {v3}, Lcom/markus/wf/c;->e()I

    move-result v8

    iput-short v8, v7, Lcom/markus/wf/f;->b:S

    .line 100
    invoke-virtual {v3}, Lcom/markus/wf/c;->e()I

    .line 101
    move-object v8, v9

    check-cast v8, Ljava/nio/ByteBuffer;

    invoke-virtual {v8}, Ljava/nio/ByteBuffer;->getShort()S

    .line 102
    invoke-virtual {v3}, Lcom/markus/wf/c;->c()I

    move-result v8

    iput-char v8, v7, Lcom/markus/wf/f;->c:C

    .line 103
    invoke-virtual {v3}, Lcom/markus/wf/c;->c()I

    move-result v8

    iput-char v8, v7, Lcom/markus/wf/f;->d:C

    .line 104
    invoke-virtual {v3}, Lcom/markus/wf/c;->d()J

    move-result-wide v10

    long-to-int v8, v10

    .line 105
    invoke-virtual {v3, v8}, Lcom/markus/wf/c;->a(I)[B

    move-result-object v8

    .line 106
    new-instance v10, Lcom/markus/wf/c;

    iget-char v11, v7, Lcom/markus/wf/f;->c:C

    iget-char v12, v7, Lcom/markus/wf/f;->d:C

    iget-short v13, v7, Lcom/markus/wf/f;->b:S

    invoke-direct {v10, v11, v12, v13}, Lcom/markus/wf/c;-><init>(III)V

    .line 107
    invoke-static {v8}, Ljava/nio/ByteBuffer;->wrap([B)Ljava/nio/ByteBuffer;

    move-result-object v11

    sget-object v12, Ljava/nio/ByteOrder;->LITTLE_ENDIAN:Ljava/nio/ByteOrder;

    invoke-virtual {v11, v12}, Ljava/nio/ByteBuffer;->order(Ljava/nio/ByteOrder;)Ljava/nio/ByteBuffer;

    move-result-object v11

    .line 108
    invoke-virtual {v11}, Ljava/nio/ByteBuffer;->getInt()I

    move-result v12

    .line 109
    iget-object v10, v10, Lcom/markus/wf/c;->a:Ljava/lang/Object;

    check-cast v10, Lcom/markus/wf/g;

    iget-byte v13, v10, Lcom/markus/wf/g;->c:B

    const/4 v14, 0x4

    packed-switch v13, :pswitch_data_0

    .line 110
    iget-char v13, v10, Lcom/markus/wf/g;->a:C

    mul-int/2addr v13, v14

    iget-char v15, v10, Lcom/markus/wf/g;->b:C

    :goto_b
    mul-int/2addr v13, v15

    goto :goto_c

    .line 111
    :pswitch_0
    iget-char v13, v10, Lcom/markus/wf/g;->a:C

    mul-int/lit8 v13, v13, 0x3

    iget-char v15, v10, Lcom/markus/wf/g;->b:C

    goto :goto_b

    .line 112
    :pswitch_1
    iget-char v13, v10, Lcom/markus/wf/g;->a:C

    mul-int/lit8 v13, v13, 0x2

    iget-char v15, v10, Lcom/markus/wf/g;->b:C

    goto :goto_b

    .line 113
    :pswitch_2
    iget-char v13, v10, Lcom/markus/wf/g;->a:C

    iget-char v15, v10, Lcom/markus/wf/g;->b:C

    mul-int/2addr v13, v15

    add-int/lit16 v13, v13, 0x400

    :goto_c
    const v15, 0x5aa521e0

    if-ne v12, v15, :cond_19

    .line 114
    invoke-virtual {v11}, Ljava/nio/ByteBuffer;->getInt()I

    move-result v12

    invoke-static {v12}, Ljava/lang/Integer;->toUnsignedLong(I)J

    move-result-wide v12

    const-wide/16 v25, 0xf

    move/from16 v27, v14

    and-long v14, v12, v25

    long-to-int v14, v14

    shr-long v12, v12, v27

    const-wide/32 v25, 0xfffff

    and-long v12, v12, v25

    long-to-int v12, v12

    .line 115
    array-length v8, v8

    add-int/lit8 v8, v8, -0x8

    .line 116
    new-array v13, v8, [B

    .line 117
    invoke-virtual {v11, v13}, Ljava/nio/ByteBuffer;->get([B)Ljava/nio/ByteBuffer;

    .line 118
    new-array v11, v12, [B

    if-lez v14, :cond_18

    if-ltz v8, :cond_17

    if-ltz v12, :cond_16

    move-object/from16 v25, v2

    const/4 v2, 0x0

    const/4 v15, 0x0

    :goto_d
    if-ge v15, v8, :cond_15

    move-object/from16 v26, v3

    .line 119
    aget-byte v3, v13, v15

    move/from16 v28, v4

    and-int/lit8 v4, v3, 0x7f

    add-int/lit8 v15, v15, 0x1

    if-eqz v4, :cond_14

    and-int/lit16 v3, v3, 0x80

    move/from16 v29, v3

    .line 120
    const-string v3, "Output buffer is too small"

    if-eqz v29, :cond_10

    mul-int/2addr v4, v14

    move/from16 v29, v5

    add-int v5, v15, v4

    if-gt v5, v8, :cond_f

    move/from16 v30, v5

    add-int v5, v2, v4

    if-gt v5, v12, :cond_e

    .line 121
    invoke-static {v13, v15, v11, v2, v4}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V

    move v2, v5

    move/from16 v15, v30

    goto :goto_f

    .line 122
    :cond_e
    new-instance v0, Ljava/lang/IllegalArgumentException;

    invoke-direct {v0, v3}, Ljava/lang/IllegalArgumentException;-><init>(Ljava/lang/String;)V

    throw v0

    .line 123
    :cond_f
    new-instance v0, Ljava/lang/IllegalArgumentException;

    const-string v1, "Invalid RLE stream: literal block exceeds input"

    invoke-direct {v0, v1}, Ljava/lang/IllegalArgumentException;-><init>(Ljava/lang/String;)V

    throw v0

    :cond_10
    move/from16 v29, v5

    add-int v5, v15, v14

    if-gt v5, v8, :cond_13

    mul-int v30, v4, v14

    move/from16 v31, v5

    add-int v5, v30, v2

    if-gt v5, v12, :cond_12

    const/4 v3, 0x0

    :goto_e
    if-ge v3, v4, :cond_11

    .line 124
    invoke-static {v13, v15, v11, v2, v14}, Ljava/lang/System;->arraycopy(Ljava/lang/Object;ILjava/lang/Object;II)V

    add-int/2addr v2, v14

    add-int/lit8 v3, v3, 0x1

    goto :goto_e

    :cond_11
    move/from16 v15, v31

    :goto_f
    move-object/from16 v3, v26

    move/from16 v4, v28

    move/from16 v5, v29

    goto :goto_d

    .line 125
    :cond_12
    new-instance v0, Ljava/lang/IllegalArgumentException;

    invoke-direct {v0, v3}, Ljava/lang/IllegalArgumentException;-><init>(Ljava/lang/String;)V

    throw v0

    .line 126
    :cond_13
    new-instance v0, Ljava/lang/IllegalArgumentException;

    const-string v1, "Invalid RLE stream: repeated block exceeds input"

    invoke-direct {v0, v1}, Ljava/lang/IllegalArgumentException;-><init>(Ljava/lang/String;)V

    throw v0

    .line 127
    :cond_14
    new-instance v0, Ljava/lang/IllegalArgumentException;

    const-string v1, "Invalid RLE stream: zero block count"

    invoke-direct {v0, v1}, Ljava/lang/IllegalArgumentException;-><init>(Ljava/lang/String;)V

    throw v0

    :cond_15
    move-object/from16 v26, v3

    move/from16 v28, v4

    move/from16 v29, v5

    if-eq v2, v12, :cond_1a

    .line 128
    invoke-static {v11, v2}, Ljava/util/Arrays;->copyOf([BI)[B

    move-result-object v11

    goto :goto_10

    .line 129
    :cond_16
    new-instance v0, Ljava/lang/IllegalArgumentException;

    const-string v1, "Invalid outputLen"

    invoke-direct {v0, v1}, Ljava/lang/IllegalArgumentException;-><init>(Ljava/lang/String;)V

    throw v0

    .line 130
    :cond_17
    new-instance v0, Ljava/lang/IllegalArgumentException;

    const-string v1, "Invalid inputLen"

    invoke-direct {v0, v1}, Ljava/lang/IllegalArgumentException;-><init>(Ljava/lang/String;)V

    throw v0

    .line 131
    :cond_18
    new-instance v0, Ljava/lang/IllegalArgumentException;

    const-string v1, "blockSize must be > 0"

    invoke-direct {v0, v1}, Ljava/lang/IllegalArgumentException;-><init>(Ljava/lang/String;)V

    throw v0

    :cond_19
    move-object/from16 v25, v2

    move-object/from16 v26, v3

    move/from16 v28, v4

    move/from16 v29, v5

    move/from16 v27, v14

    .line 132
    invoke-virtual {v11}, Ljava/nio/Buffer;->position()I

    move-result v2

    add-int/lit8 v2, v2, -0x4

    .line 133
    invoke-virtual {v11, v2}, Ljava/nio/Buffer;->position(I)Ljava/nio/Buffer;

    move-result-object v2

    check-cast v2, Ljava/nio/ByteBuffer;

    .line 134
    new-array v2, v13, [B

    .line 135
    invoke-virtual {v11, v2}, Ljava/nio/ByteBuffer;->get([B)Ljava/nio/ByteBuffer;

    move-object v11, v2

    .line 136
    :cond_1a
    :goto_10
    iget-byte v2, v10, Lcom/markus/wf/g;->c:B

    const-string v3, " bytes, got "

    const-string v4, "Input buffer is too small: expected at least "

    packed-switch v2, :pswitch_data_1

    .line 137
    array-length v2, v11

    new-array v2, v2, [B

    const/4 v3, 0x0

    .line 138
    :goto_11
    iget-char v4, v10, Lcom/markus/wf/g;->a:C

    iget-char v5, v10, Lcom/markus/wf/g;->b:C

    mul-int/2addr v4, v5

    if-ge v3, v4, :cond_1e

    mul-int/lit8 v4, v3, 0x4

    add-int/lit8 v5, v4, 0x2

    .line 139
    aget-byte v8, v11, v5

    aput-byte v8, v2, v4

    add-int/lit8 v8, v4, 0x1

    .line 140
    aget-byte v12, v11, v8

    aput-byte v12, v2, v8

    .line 141
    aget-byte v8, v11, v4

    aput-byte v8, v2, v5

    add-int/lit8 v4, v4, 0x3

    .line 142
    aget-byte v5, v11, v4

    aput-byte v5, v2, v4

    add-int/lit8 v3, v3, 0x1

    goto :goto_11

    .line 143
    :pswitch_3
    iget-char v2, v10, Lcom/markus/wf/g;->a:C

    iget-char v5, v10, Lcom/markus/wf/g;->b:C

    mul-int/2addr v2, v5

    .line 144
    array-length v5, v11

    mul-int/lit8 v8, v2, 0x3

    if-lt v5, v8, :cond_1c

    mul-int/lit8 v3, v2, 0x4

    .line 145
    new-array v3, v3, [B

    const/4 v4, 0x0

    :goto_12
    if-ge v4, v2, :cond_1b

    mul-int/lit8 v5, v4, 0x3

    mul-int/lit8 v8, v4, 0x4

    .line 146
    aget-byte v10, v11, v5

    and-int/lit16 v10, v10, 0xff

    add-int/lit8 v12, v5, 0x1

    aget-byte v12, v11, v12

    and-int/lit16 v12, v12, 0xff

    shl-int/lit8 v12, v12, 0x8

    or-int/2addr v10, v12

    and-int/lit8 v12, v10, 0x1f

    ushr-int/lit8 v13, v10, 0x5

    and-int/lit8 v13, v13, 0x3f

    ushr-int/lit8 v10, v10, 0xb

    and-int/lit8 v10, v10, 0x1f

    mul-int/lit16 v10, v10, 0xff

    add-int/lit8 v10, v10, 0xf

    .line 147
    div-int/lit8 v10, v10, 0x1f

    int-to-byte v10, v10

    aput-byte v10, v3, v8

    add-int/lit8 v10, v8, 0x1

    mul-int/lit16 v13, v13, 0xff

    add-int/lit8 v13, v13, 0x1f

    .line 148
    div-int/lit8 v13, v13, 0x3f

    int-to-byte v13, v13

    aput-byte v13, v3, v10

    add-int/lit8 v10, v8, 0x2

    mul-int/lit16 v12, v12, 0xff

    add-int/lit8 v12, v12, 0xf

    .line 149
    div-int/lit8 v12, v12, 0x1f

    int-to-byte v12, v12

    aput-byte v12, v3, v10

    add-int/lit8 v8, v8, 0x3

    add-int/lit8 v5, v5, 0x2

    .line 150
    aget-byte v5, v11, v5

    aput-byte v5, v3, v8

    add-int/lit8 v4, v4, 0x1

    goto :goto_12

    :cond_1b
    move-object v2, v3

    goto/16 :goto_15

    .line 151
    :cond_1c
    new-instance v0, Ljava/lang/IllegalArgumentException;

    array-length v1, v11

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2, v4}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v2, v8}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-direct {v0, v1}, Ljava/lang/IllegalArgumentException;-><init>(Ljava/lang/String;)V

    throw v0

    .line 152
    :pswitch_4
    iget-char v2, v10, Lcom/markus/wf/g;->a:C

    iget-char v5, v10, Lcom/markus/wf/g;->b:C

    mul-int/2addr v2, v5

    .line 153
    array-length v5, v11

    mul-int/lit8 v8, v2, 0x2

    if-lt v5, v8, :cond_1d

    mul-int/lit8 v3, v2, 0x4

    .line 154
    new-array v3, v3, [B

    const/4 v4, 0x0

    :goto_13
    if-ge v4, v2, :cond_1b

    mul-int/lit8 v5, v4, 0x2

    mul-int/lit8 v8, v4, 0x4

    .line 155
    aget-byte v10, v11, v5

    and-int/lit16 v10, v10, 0xff

    add-int/lit8 v5, v5, 0x1

    aget-byte v5, v11, v5

    and-int/lit16 v5, v5, 0xff

    shl-int/lit8 v5, v5, 0x8

    or-int/2addr v5, v10

    and-int/lit8 v10, v5, 0x1f

    ushr-int/lit8 v12, v5, 0x5

    and-int/lit8 v12, v12, 0x3f

    ushr-int/lit8 v5, v5, 0xb

    and-int/lit8 v5, v5, 0x1f

    mul-int/lit16 v5, v5, 0xff

    add-int/lit8 v5, v5, 0xf

    .line 156
    div-int/lit8 v5, v5, 0x1f

    int-to-byte v5, v5

    aput-byte v5, v3, v8

    add-int/lit8 v5, v8, 0x1

    mul-int/lit16 v12, v12, 0xff

    add-int/lit8 v12, v12, 0x1f

    .line 157
    div-int/lit8 v12, v12, 0x3f

    int-to-byte v12, v12

    aput-byte v12, v3, v5

    add-int/lit8 v5, v8, 0x2

    mul-int/lit16 v10, v10, 0xff

    add-int/lit8 v10, v10, 0xf

    .line 158
    div-int/lit8 v10, v10, 0x1f

    int-to-byte v10, v10

    aput-byte v10, v3, v5

    add-int/lit8 v8, v8, 0x3

    const/4 v5, -0x1

    .line 159
    aput-byte v5, v3, v8

    add-int/lit8 v4, v4, 0x1

    goto :goto_13

    .line 160
    :cond_1d
    new-instance v0, Ljava/lang/IllegalArgumentException;

    array-length v1, v11

    new-instance v2, Ljava/lang/StringBuilder;

    invoke-direct {v2, v4}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    invoke-virtual {v2, v8}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    invoke-virtual {v2, v1}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-direct {v0, v1}, Ljava/lang/IllegalArgumentException;-><init>(Ljava/lang/String;)V

    throw v0

    .line 161
    :pswitch_5
    iget-char v2, v10, Lcom/markus/wf/g;->a:C

    iget-char v3, v10, Lcom/markus/wf/g;->b:C

    mul-int/2addr v2, v3

    mul-int/lit8 v3, v2, 0x4

    .line 162
    new-array v3, v3, [B

    const/4 v4, 0x0

    :goto_14
    if-ge v4, v2, :cond_1b

    add-int/lit16 v5, v4, 0x400

    .line 163
    aget-byte v5, v11, v5

    and-int/lit16 v5, v5, 0xff

    mul-int/lit8 v5, v5, 0x4

    mul-int/lit8 v8, v4, 0x4

    add-int/lit8 v10, v5, 0x2

    .line 164
    aget-byte v10, v11, v10

    aput-byte v10, v3, v8

    add-int/lit8 v10, v8, 0x1

    add-int/lit8 v12, v5, 0x1

    .line 165
    aget-byte v12, v11, v12

    aput-byte v12, v3, v10

    add-int/lit8 v10, v8, 0x2

    .line 166
    aget-byte v12, v11, v5

    aput-byte v12, v3, v10

    add-int/lit8 v8, v8, 0x3

    add-int/lit8 v5, v5, 0x3

    .line 167
    aget-byte v5, v11, v5

    aput-byte v5, v3, v8

    add-int/lit8 v4, v4, 0x1

    goto :goto_14

    .line 168
    :cond_1e
    :goto_15
    iput-object v2, v7, Lcom/markus/wf/f;->a:[B

    .line 169
    iput-object v7, v6, Lcom/markus/wf/k;->c:Lcom/markus/wf/f;

    :goto_16
    move-object/from16 v2, v25

    move-object/from16 v3, v26

    move/from16 v4, v28

    move/from16 v5, v29

    goto/16 :goto_a

    .line 170
    :cond_1f
    iput-object v1, v0, Lcom/markus/wf/e;->b:Lcom/markus/wf/o;

    return-void

    .line 171
    :cond_20
    new-instance v0, Lcom/markus/wf/h;

    .line 172
    invoke-direct {v0}, Ljava/lang/Exception;-><init>()V

    .line 173
    throw v0

    :pswitch_data_0
    .packed-switch 0x0
        :pswitch_2
        :pswitch_1
        :pswitch_0
    .end packed-switch

    :pswitch_data_1
    .packed-switch 0x0
        :pswitch_5
        :pswitch_4
        :pswitch_3
    .end packed-switch
.end method

.method public final b()Ljava/util/ArrayList;
    .locals 7

    .line 1
    new-instance v0, Ljava/util/ArrayList;

    .line 2
    .line 3
    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V

    .line 4
    .line 5
    .line 6
    iget-object p0, p0, Lcom/markus/wf/e;->b:Lcom/markus/wf/o;

    .line 7
    .line 8
    iget-object p0, p0, Lcom/markus/wf/o;->h:Ljava/util/ArrayList;

    .line 9
    .line 10
    invoke-virtual {p0}, Ljava/util/ArrayList;->size()I

    .line 11
    .line 12
    .line 13
    move-result v1

    .line 14
    const/4 v2, 0x0

    .line 15
    :goto_0
    if-ge v2, v1, :cond_1

    .line 16
    .line 17
    invoke-virtual {p0, v2}, Ljava/util/ArrayList;->get(I)Ljava/lang/Object;

    .line 18
    .line 19
    .line 20
    move-result-object v3

    .line 21
    add-int/lit8 v2, v2, 0x1

    .line 22
    .line 23
    check-cast v3, Lcom/markus/wf/k;

    .line 24
    .line 25
    iget-object v3, v3, Lcom/markus/wf/k;->c:Lcom/markus/wf/f;

    .line 26
    .line 27
    if-nez v3, :cond_0

    .line 28
    .line 29
    goto :goto_0

    .line 30
    :cond_0
    new-instance v4, Lcom/markus/wf/d;

    .line 31
    .line 32
    iget-object v5, v3, Lcom/markus/wf/f;->a:[B

    .line 33
    .line 34
    iget-char v6, v3, Lcom/markus/wf/f;->c:C

    .line 35
    .line 36
    iget-char v3, v3, Lcom/markus/wf/f;->d:C

    .line 37
    .line 38
    invoke-direct {v4}, Ljava/lang/Object;-><init>()V

    .line 39
    .line 40
    .line 41
    iput-object v5, v4, Lcom/markus/wf/d;->a:[B

    .line 42
    .line 43
    iput-char v6, v4, Lcom/markus/wf/d;->b:C

    .line 44
    .line 45
    iput-char v3, v4, Lcom/markus/wf/d;->c:C

    .line 46
    .line 47
    invoke-virtual {v0, v4}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z

    .line 48
    .line 49
    .line 50
    goto :goto_0

    .line 51
    :cond_1
    return-object v0
.end method

.method public final c()Ljava/lang/String;
    .locals 2

    .line 1
    iget-object v0, p0, Lcom/markus/wf/e;->b:Lcom/markus/wf/o;

    .line 2
    .line 3
    iget-object v1, v0, Lcom/markus/wf/o;->e:Ljava/lang/String;

    .line 4
    .line 5
    if-eqz v1, :cond_0

    .line 6
    .line 7
    return-object v1

    .line 8
    :cond_0
    iget-object v0, v0, Lcom/markus/wf/o;->f:Lcom/markus/wf/n;

    .line 9
    .line 10
    if-eqz v0, :cond_1

    .line 11
    .line 12
    iget-object v0, v0, Lcom/markus/wf/n;->a:Ljava/util/HashMap;

    .line 13
    .line 14
    invoke-virtual {v0}, Ljava/util/HashMap;->values()Ljava/util/Collection;

    .line 15
    .line 16
    .line 17
    move-result-object v0

    .line 18
    invoke-interface {v0}, Ljava/util/Collection;->iterator()Ljava/util/Iterator;

    .line 19
    .line 20
    .line 21
    move-result-object v0

    .line 22
    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;

    .line 23
    .line 24
    .line 25
    move-result-object v0

    .line 26
    check-cast v0, Ljava/lang/String;

    .line 27
    .line 28
    iget-object p0, p0, Lcom/markus/wf/e;->b:Lcom/markus/wf/o;

    .line 29
    .line 30
    iget-object p0, p0, Lcom/markus/wf/o;->f:Lcom/markus/wf/n;

    .line 31
    .line 32
    iget-object p0, p0, Lcom/markus/wf/n;->a:Ljava/util/HashMap;

    .line 33
    .line 34
    const-string v1, "en_US"

    .line 35
    .line 36
    invoke-virtual {p0, v1, v0}, Ljava/util/HashMap;->getOrDefault(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;

    .line 37
    .line 38
    .line 39
    move-result-object p0

    .line 40
    check-cast p0, Ljava/lang/String;

    .line 41
    .line 42
    return-object p0

    .line 43
    :cond_1
    const-string p0, ""

    .line 44
    .line 45
    return-object p0
.end method
