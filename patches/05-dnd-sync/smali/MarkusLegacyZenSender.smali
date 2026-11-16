.class public final LMarkusLegacyZenSender;
.super Ljava/lang/Object;

# Keep the obfuscated request type out of a packaged Java stub. Stock default linkType is -1.
.method public static send(Ljava/lang/String;Lt3u;Lcom/xiaomi/fitness/device/contact/export/OnSyncCallback;)I
    .locals 6
    sget-object v0, Lcom/xiaomi/fitness/device/contact/export/DeviceContact;->Companion:Lcom/xiaomi/fitness/device/contact/export/DeviceContact$Companion;
    invoke-static {v0}, Lcom/xiaomi/fitness/device/contact/export/DeviceSyncExtKt;->getInstance(Lcom/xiaomi/fitness/device/contact/export/DeviceContact$Companion;)Lcom/xiaomi/fitness/device/contact/export/DeviceContact;
    move-result-object v0
    move-object v1, p0
    move-object v2, p1
    const/4 v3, 0x0
    move-object v4, p2
    const/4 v5, -0x1
    invoke-interface/range {v0 .. v5}, Lcom/xiaomi/fitness/device/contact/export/DeviceContact;->call(Ljava/lang/String;Lt3u;ZLcom/xiaomi/fitness/device/contact/export/OnSyncCallback;I)I
    move-result v0
    return v0
.end method
