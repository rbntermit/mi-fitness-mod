package com.markus.update;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Checks the exact downloaded bytes and signed build marker before Android examines the APK. */
final class ApkFiles {
    static final String MARKER="assets/markus-mod-build.txt";
    static String sha256(File f) throws IOException {
        try {
            MessageDigest d=MessageDigest.getInstance("SHA-256");
            try(InputStream in=new FileInputStream(f)) {
                byte[] b=new byte[65536]; int n;
                while((n=in.read(b))!=-1) { if(Thread.currentThread().isInterrupted()) throw new IOException("Проверка отменена"); d.update(b,0,n); }
            }
            StringBuilder out=new StringBuilder();
            for(byte b:d.digest()) out.append(String.format(java.util.Locale.ROOT,"%02x",b&255));
            return out.toString();
        } catch(NoSuchAlgorithmException e) { throw new IOException("SHA-256 unavailable",e); }
    }
    static void verify(File f, UpdateAsset a) throws IOException {
        if (f.length()!=a.size) throw new IOException("Размер APK не совпадает с релизом");
        if (!a.sha256.equals(sha256(f))) throw new IOException("SHA-256 APK не совпадает с релизом");
        try(ZipFile zip=new ZipFile(f)) {
            ZipEntry marker=null; int count=0;
            for(Enumeration<? extends ZipEntry> e=zip.entries();e.hasMoreElements();) {
                ZipEntry entry=e.nextElement();
                if(MARKER.equals(entry.getName())) { marker=entry;count++; }
            }
            if(count!=1 || marker.isDirectory() || marker.getSize()>128) throw new IOException("Нет однозначного номера сборки мода в APK");
            try(InputStream in=zip.getInputStream(marker)) {
                ByteArrayOutputStream out=new ByteArrayOutputStream(); int b;
                while((b=in.read())!=-1) { if(out.size()>=128) throw new IOException("Неверный номер сборки");out.write(b); }
                String tag=new String(out.toByteArray(),StandardCharsets.US_ASCII).trim();
                if(!a.version.tag().equals(tag)) throw new IOException("Номер сборки APK не соответствует релизу");
            }
        }
    }
}
