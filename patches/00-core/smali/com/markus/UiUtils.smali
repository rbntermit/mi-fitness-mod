.class public Lcom/markus/UiUtils;
.super Ljava/lang/Object;
.source "UiUtils.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lcom/markus/UiUtils$CoordinatesCallback;,
        Lcom/markus/UiUtils$OnChoiceListener;
    }
.end annotation


# direct methods
.method public constructor <init>()V
    .locals 0

    .line 10
    invoke-direct {p0}, Ljava/lang/Object;-><init>()V

    return-void
.end method

.method static synthetic lambda$showCoordinatesDialog$0(Landroid/widget/EditText;Landroid/widget/EditText;Lcom/markus/UiUtils$CoordinatesCallback;Landroid/app/AlertDialog;Landroid/content/Context;Landroid/view/View;)V
    .locals 2

    .line 53
    invoke-virtual {p0}, Landroid/widget/EditText;->getText()Landroid/text/Editable;

    move-result-object p0

    invoke-virtual {p0}, Ljava/lang/Object;->toString()Ljava/lang/String;

    move-result-object p0

    invoke-virtual {p0}, Ljava/lang/String;->trim()Ljava/lang/String;

    move-result-object p0

    .line 54
    invoke-virtual {p1}, Landroid/widget/EditText;->getText()Landroid/text/Editable;

    move-result-object p1

    invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;

    move-result-object p1

    invoke-virtual {p1}, Ljava/lang/String;->trim()Ljava/lang/String;

    move-result-object p1

    .line 57
    :try_start_0
    invoke-static {p0}, Ljava/lang/Double;->parseDouble(Ljava/lang/String;)D

    move-result-wide v0

    .line 58
    invoke-static {p1}, Ljava/lang/Double;->parseDouble(Ljava/lang/String;)D

    move-result-wide p0

    if-eqz p2, :cond_0

    .line 61
    invoke-interface {p2, v0, v1, p0, p1}, Lcom/markus/UiUtils$CoordinatesCallback;->onOk(DD)V

    .line 64
    :cond_0
    invoke-virtual {p3}, Landroid/app/AlertDialog;->dismiss()V
    :try_end_0
    .catch Ljava/lang/NumberFormatException; {:try_start_0 .. :try_end_0} :catch_0

    goto :goto_0

    .line 67
    :catch_0
    const-string p0, "\u0412\u0432\u0435\u0434\u0438\u0442\u0435 \u043a\u043e\u0440\u0440\u0435\u043a\u0442\u043d\u044b\u0435 lon \u0438 lat"

    const/4 p1, 0x0

    invoke-static {p4, p0, p1}, Landroid/widget/Toast;->makeText(Landroid/content/Context;Ljava/lang/CharSequence;I)Landroid/widget/Toast;

    move-result-object p0

    .line 71
    invoke-virtual {p0}, Landroid/widget/Toast;->show()V

    :goto_0
    return-void
.end method

.method static synthetic lambda$showCoordinatesDialog$1(Landroid/app/AlertDialog;Landroid/widget/EditText;Landroid/widget/EditText;Lcom/markus/UiUtils$CoordinatesCallback;Landroid/content/Context;Landroid/content/DialogInterface;)V
    .locals 7

    const/4 p5, -0x1

    .line 52
    invoke-virtual {p0, p5}, Landroid/app/AlertDialog;->getButton(I)Landroid/widget/Button;

    move-result-object p5

    new-instance v6, Lcom/markus/UiUtils$$ExternalSyntheticLambda0;

    move-object v0, v6

    move-object v1, p1

    move-object v2, p2

    move-object v3, p3

    move-object v4, p0

    move-object v5, p4

    invoke-direct/range {v0 .. v5}, Lcom/markus/UiUtils$$ExternalSyntheticLambda0;-><init>(Landroid/widget/EditText;Landroid/widget/EditText;Lcom/markus/UiUtils$CoordinatesCallback;Landroid/app/AlertDialog;Landroid/content/Context;)V

    invoke-virtual {p5, v6}, Landroid/widget/Button;->setOnClickListener(Landroid/view/View$OnClickListener;)V

    return-void
.end method

.method static synthetic lambda$showLanguageDialog$2([Ljava/lang/String;Lcom/markus/UiUtils$OnChoiceListener;Landroid/content/DialogInterface;I)V
    .locals 0

    if-lez p3, :cond_0

    .line 132
    aget-object p0, p0, p3

    goto :goto_0

    :cond_0
    const-string p0, ""

    .line 133
    :goto_0
    invoke-interface {p1, p0}, Lcom/markus/UiUtils$OnChoiceListener;->onChoice(Ljava/lang/String;)V

    .line 134
    invoke-interface {p2}, Landroid/content/DialogInterface;->dismiss()V

    return-void
.end method

.method public static showCoordinatesDialog(Landroid/content/Context;DDLcom/markus/UiUtils$CoordinatesCallback;)V
    .locals 10

    .line 17
    new-instance v0, Landroid/widget/LinearLayout;

    invoke-direct {v0, p0}, Landroid/widget/LinearLayout;-><init>(Landroid/content/Context;)V

    const/4 v1, 0x1

    .line 18
    invoke-virtual {v0, v1}, Landroid/widget/LinearLayout;->setOrientation(I)V

    .line 20
    invoke-virtual {p0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;

    move-result-object v1

    invoke-virtual {v1}, Landroid/content/res/Resources;->getDisplayMetrics()Landroid/util/DisplayMetrics;

    move-result-object v1

    iget v1, v1, Landroid/util/DisplayMetrics;->density:F

    const/high16 v2, 0x41a00000    # 20.0f

    mul-float v1, v1, v2

    float-to-int v1, v1

    .line 21
    div-int/lit8 v2, v1, 0x2

    const/4 v3, 0x0

    invoke-virtual {v0, v1, v2, v1, v3}, Landroid/widget/LinearLayout;->setPadding(IIII)V

    .line 23
    new-instance v6, Landroid/widget/EditText;

    invoke-direct {v6, p0}, Landroid/widget/EditText;-><init>(Landroid/content/Context;)V

    .line 24
    const-string v1, "lon"

    invoke-virtual {v6, v1}, Landroid/widget/EditText;->setHint(Ljava/lang/CharSequence;)V

    const/16 v1, 0x3002

    .line 25
    invoke-virtual {v6, v1}, Landroid/widget/EditText;->setInputType(I)V

    .line 30
    invoke-static {p1, p2}, Ljava/lang/String;->valueOf(D)Ljava/lang/String;

    move-result-object p1

    invoke-virtual {v6, p1}, Landroid/widget/EditText;->setText(Ljava/lang/CharSequence;)V

    .line 32
    new-instance v7, Landroid/widget/EditText;

    invoke-direct {v7, p0}, Landroid/widget/EditText;-><init>(Landroid/content/Context;)V

    .line 33
    const-string p1, "lat"

    invoke-virtual {v7, p1}, Landroid/widget/EditText;->setHint(Ljava/lang/CharSequence;)V

    .line 34
    invoke-virtual {v7, v1}, Landroid/widget/EditText;->setInputType(I)V

    .line 39
    invoke-static {p3, p4}, Ljava/lang/String;->valueOf(D)Ljava/lang/String;

    move-result-object p1

    invoke-virtual {v7, p1}, Landroid/widget/EditText;->setText(Ljava/lang/CharSequence;)V

    .line 41
    invoke-virtual {v0, v6}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    .line 42
    invoke-virtual {v0, v7}, Landroid/widget/LinearLayout;->addView(Landroid/view/View;)V

    .line 44
    new-instance p1, Landroid/app/AlertDialog$Builder;

    invoke-direct {p1, p0}, Landroid/app/AlertDialog$Builder;-><init>(Landroid/content/Context;)V

    const-string p2, "\u041a\u043e\u043e\u0440\u0434\u0438\u043d\u0430\u0442\u044b"

    .line 45
    invoke-virtual {p1, p2}, Landroid/app/AlertDialog$Builder;->setTitle(Ljava/lang/CharSequence;)Landroid/app/AlertDialog$Builder;

    move-result-object p1

    .line 46
    invoke-virtual {p1, v0}, Landroid/app/AlertDialog$Builder;->setView(Landroid/view/View;)Landroid/app/AlertDialog$Builder;

    move-result-object p1

    .line 47
    const-string p2, "\u041e\u0442\u043c\u0435\u043d\u0430"

    const/4 p3, 0x0

    invoke-virtual {p1, p2, p3}, Landroid/app/AlertDialog$Builder;->setNegativeButton(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;

    move-result-object p1

    const-string p2, "OK"

    .line 48
    invoke-virtual {p1, p2, p3}, Landroid/app/AlertDialog$Builder;->setPositiveButton(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;

    move-result-object p1

    .line 49
    invoke-virtual {p1}, Landroid/app/AlertDialog$Builder;->create()Landroid/app/AlertDialog;

    move-result-object p1

    .line 51
    new-instance p2, Lcom/markus/UiUtils$$ExternalSyntheticLambda2;

    move-object v4, p2

    move-object v5, p1

    move-object v8, p5

    move-object v9, p0

    invoke-direct/range {v4 .. v9}, Lcom/markus/UiUtils$$ExternalSyntheticLambda2;-><init>(Landroid/app/AlertDialog;Landroid/widget/EditText;Landroid/widget/EditText;Lcom/markus/UiUtils$CoordinatesCallback;Landroid/content/Context;)V

    invoke-virtual {p1, p2}, Landroid/app/AlertDialog;->setOnShowListener(Landroid/content/DialogInterface$OnShowListener;)V

    .line 76
    invoke-virtual {p1}, Landroid/app/AlertDialog;->show()V

    return-void
.end method

.method public static showLanguageDialog(Landroid/content/Context;Lcom/markus/UiUtils$OnChoiceListener;)V
    .locals 3

    const/16 v0, 0x22

    .line 91
    new-array v0, v0, [Ljava/lang/String;

    const/4 v1, 0x0

    const-string v2, "\u041f\u043e \u0443\u043c\u043e\u043b\u0447\u0430\u043d\u0438\u044e"

    aput-object v2, v0, v1

    const/4 v1, 0x1

    const-string v2, "en_US"

    aput-object v2, v0, v1

    const/4 v1, 0x2

    const-string v2, "zh_CN"

    aput-object v2, v0, v1

    const/4 v1, 0x3

    const-string v2, "zh_TW"

    aput-object v2, v0, v1

    const/4 v1, 0x4

    const-string v2, "ja_JP"

    aput-object v2, v0, v1

    const/4 v1, 0x5

    const-string v2, "es_ES"

    aput-object v2, v0, v1

    const/4 v1, 0x6

    const-string v2, "fr_FR"

    aput-object v2, v0, v1

    const/4 v1, 0x7

    const-string v2, "de_DE"

    aput-object v2, v0, v1

    const/16 v1, 0x8

    const-string v2, "ru_RU"

    aput-object v2, v0, v1

    const/16 v1, 0x9

    const-string v2, "pt_BR"

    aput-object v2, v0, v1

    const/16 v1, 0xa

    const-string v2, "pt_PT"

    aput-object v2, v0, v1

    const/16 v1, 0xb

    const-string v2, "it_IT"

    aput-object v2, v0, v1

    const/16 v1, 0xc

    const-string v2, "ko_KR"

    aput-object v2, v0, v1

    const/16 v1, 0xd

    const-string v2, "tr_TR"

    aput-object v2, v0, v1

    const/16 v1, 0xe

    const-string v2, "nl_NL"

    aput-object v2, v0, v1

    const/16 v1, 0xf

    const-string v2, "th_TH"

    aput-object v2, v0, v1

    const/16 v1, 0x10

    const-string v2, "sv_SE"

    aput-object v2, v0, v1

    const/16 v1, 0x11

    const-string v2, "da_DK"

    aput-object v2, v0, v1

    const/16 v1, 0x12

    const-string v2, "vi_VN"

    aput-object v2, v0, v1

    const/16 v1, 0x13

    const-string v2, "nb_NO"

    aput-object v2, v0, v1

    const/16 v1, 0x14

    const-string v2, "pl_PL"

    aput-object v2, v0, v1

    const/16 v1, 0x15

    const-string v2, "fi_FI"

    aput-object v2, v0, v1

    const/16 v1, 0x16

    const-string v2, "in_ID"

    aput-object v2, v0, v1

    const/16 v1, 0x17

    const-string v2, "el_GR"

    aput-object v2, v0, v1

    const/16 v1, 0x18

    const-string v2, "ro_RO"

    aput-object v2, v0, v1

    const/16 v1, 0x19

    const-string v2, "cs_CZ"

    aput-object v2, v0, v1

    const/16 v1, 0x1a

    const-string v2, "uk_UA"

    aput-object v2, v0, v1

    const/16 v1, 0x1b

    const-string v2, "sk_SK"

    aput-object v2, v0, v1

    const/16 v1, 0x1c

    const-string v2, "hu_HU"

    aput-object v2, v0, v1

    const/16 v1, 0x1d

    const-string v2, "ar_EG"

    aput-object v2, v0, v1

    const/16 v1, 0x1e

    const-string v2, "iw_IL"

    aput-object v2, v0, v1

    const/16 v1, 0x1f

    const-string v2, "zh_HK"

    aput-object v2, v0, v1

    const/16 v1, 0x20

    const-string v2, "lt_LT"

    aput-object v2, v0, v1

    const/16 v1, 0x21

    const-string v2, "bg_BG"

    aput-object v2, v0, v1

    .line 129
    new-instance v1, Landroid/app/AlertDialog$Builder;

    invoke-direct {v1, p0}, Landroid/app/AlertDialog$Builder;-><init>(Landroid/content/Context;)V

    const-string p0, "\u0412\u044b\u0431\u0435\u0440\u0438\u0442\u0435 \u0437\u043d\u0430\u0447\u0435\u043d\u0438\u0435"

    .line 130
    invoke-virtual {v1, p0}, Landroid/app/AlertDialog$Builder;->setTitle(Ljava/lang/CharSequence;)Landroid/app/AlertDialog$Builder;

    move-result-object p0

    new-instance v1, Lcom/markus/UiUtils$$ExternalSyntheticLambda1;

    invoke-direct {v1, v0, p1}, Lcom/markus/UiUtils$$ExternalSyntheticLambda1;-><init>([Ljava/lang/String;Lcom/markus/UiUtils$OnChoiceListener;)V

    .line 131
    invoke-virtual {p0, v0, v1}, Landroid/app/AlertDialog$Builder;->setItems([Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;

    move-result-object p0

    const-string p1, "\u041e\u0442\u043c\u0435\u043d\u0430"

    const/4 v0, 0x0

    .line 136
    invoke-virtual {p0, p1, v0}, Landroid/app/AlertDialog$Builder;->setNegativeButton(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;

    move-result-object p0

    .line 137
    invoke-virtual {p0}, Landroid/app/AlertDialog$Builder;->show()Landroid/app/AlertDialog;

    return-void
.end method
