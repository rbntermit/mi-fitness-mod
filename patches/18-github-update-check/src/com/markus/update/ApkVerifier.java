package com.markus.update;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

final class ApkVerifier {
    static void verify(Context c,File file,UpdateAsset asset) throws Exception {
        ApkFiles.verify(file,asset);
        ModVersion current=ModUpdate.installed(c);
        if(!ModVersion.isNewer(asset.version,current)) throw new IOException("Эта сборка уже установлена или старее текущей");
        PackageManager pm=c.getPackageManager();
        int flags=Build.VERSION.SDK_INT>=28?PackageManager.GET_SIGNING_CERTIFICATES:PackageManager.GET_SIGNATURES;
        PackageInfo apk=pm.getPackageArchiveInfo(file.getAbsolutePath(),flags);
        PackageInfo installed=pm.getPackageInfo(c.getPackageName(),flags);
        String expected=asset.version.tag().substring(1).split("-band-",2)[0];
        if(apk==null || !c.getPackageName().equals(apk.packageName) || !expected.equals(apk.versionName))
            throw new IOException("Пакет или версия APK не соответствуют обновлению Mi Fitness");
        if(code(apk)<code(installed)) throw new IOException("Android не разрешает понижение versionCode");
        Signature[] a=signatures(apk),b=signatures(installed);
        if(a==null || b==null || a.length!=1 || b.length!=1 || !Arrays.equals(a[0].toByteArray(),b[0].toByteArray()))
            throw new IOException("Подпись APK отличается от установленного приложения");
    }
    private static long code(PackageInfo p) { return Build.VERSION.SDK_INT>=28?p.getLongVersionCode():p.versionCode; }
    /** API 26-27 have no SigningInfo; remove the legacy path only when minSdk reaches 28. */
    @SuppressWarnings("deprecation")
    private static Signature[] signatures(PackageInfo p) {
        if(Build.VERSION.SDK_INT>=28) return p.signingInfo==null?null:p.signingInfo.getApkContentsSigners();
        return p.signatures;
    }
}
