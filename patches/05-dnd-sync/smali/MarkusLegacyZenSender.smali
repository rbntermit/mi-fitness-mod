.class public final LMarkusLegacyZenSender;
.super Ljava/lang/Object;

# Keep the obfuscated request type out of a packaged Java stub. Stock default linkType is -1.
.method public static send(Ljava/lang/String;Lt3u;Lcom/xiaomi/fitness/device/contact/export/OnSyncCallback;)I
    .locals 1
    const/4 v0, 0x0
    invoke-static {p0, p1, p2, v0}, LMarkusLegacyZenSender;->call(Ljava/lang/String;Lt3u;Lcom/xiaomi/fitness/device/contact/export/OnSyncCallback;Z)I
    move-result v0
    return v0
.end method

.method public static sendAod(Ljava/lang/String;Lt3u;Lcom/xiaomi/fitness/device/contact/export/OnSyncCallback;)I
    .locals 1
    # AOD requests use the flag from the stock AODSettingViewModel.
    const/4 v0, 0x1
    invoke-static {p0, p1, p2, v0}, LMarkusLegacyZenSender;->call(Ljava/lang/String;Lt3u;Lcom/xiaomi/fitness/device/contact/export/OnSyncCallback;Z)I
    move-result v0
    return v0
.end method

.method private static call(Ljava/lang/String;Lt3u;Lcom/xiaomi/fitness/device/contact/export/OnSyncCallback;Z)I
    .locals 6
    sget-object v0, Lcom/xiaomi/fitness/device/contact/export/DeviceContact;->Companion:Lcom/xiaomi/fitness/device/contact/export/DeviceContact$Companion;
    invoke-static {v0}, Lcom/xiaomi/fitness/device/contact/export/DeviceSyncExtKt;->getInstance(Lcom/xiaomi/fitness/device/contact/export/DeviceContact$Companion;)Lcom/xiaomi/fitness/device/contact/export/DeviceContact;
    move-result-object v0
    move-object v1, p0
    move-object v2, p1
    move v3, p3
    move-object v4, p2
    const/4 v5, -0x1
    invoke-interface/range {v0 .. v5}, Lcom/xiaomi/fitness/device/contact/export/DeviceContact;->call(Ljava/lang/String;Lt3u;ZLcom/xiaomi/fitness/device/contact/export/OnSyncCallback;I)I
    move-result v0
    return v0
.end method

.method public static aodResponse(Lcom/xiaomi/fitness/device/contact/export/SyncResult;)Lxir;
    .locals 1
    if-eqz p0, :missing
    invoke-virtual {p0}, Lcom/xiaomi/fitness/device/contact/export/SyncResult;->getPacket()Lt3u;
    move-result-object v0
    if-eqz v0, :missing
    invoke-virtual {v0}, Lt3u;->u()Lsir;
    move-result-object v0
    if-eqz v0, :missing
    invoke-virtual {v0}, Lsir;->B()Lxir;
    move-result-object v0
    return-object v0
    :missing
    const/4 v0, 0x0
    return-object v0
.end method
