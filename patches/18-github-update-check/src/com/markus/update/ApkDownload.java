package com.markus.update;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.HttpsURLConnection;

/** Explicit user download only. Redirects are validated one by one; no account data or credentials. */
final class ApkDownload {
    interface Progress { void changed(long bytes,long total); }
    interface Connections { HttpsURLConnection open(URL url) throws IOException; }
    private final Connections connections;
    ApkDownload() { this(u -> (HttpsURLConnection)u.openConnection()); }
    ApkDownload(Connections connections) { this.connections=connections; }
    private final AtomicBoolean cancelled=new AtomicBoolean();
    private volatile HttpsURLConnection connection;
    void cancel() { cancelled.set(true); HttpsURLConnection c=connection;if(c!=null)c.disconnect(); }
    private void active() throws IOException {
        if(cancelled.get() || Thread.currentThread().isInterrupted()) throw new IOException("Загрузка отменена");
    }
    File run(UpdateAsset a,File dir,Progress progress) throws IOException {
        if(!dir.isDirectory() && !dir.mkdirs()) throw new IOException("Не удалось создать папку обновления");
        File[] old=dir.listFiles();
        if(old!=null) for(File f:old) if((f.getName().endsWith(".part") || f.getName().endsWith(".apk")) && System.currentTimeMillis()-f.lastModified()>86400000L) f.delete();
        if(dir.getUsableSpace()<a.size+16L*1024*1024) throw new IOException("Недостаточно места для обновления");
        File partial=File.createTempFile("update-", ".part",dir);
        boolean complete=false;
        try {
            String address=a.url;
            for(int hop=0;hop<=5;hop++) {
                active();
                if(!a.permits(address)) throw new IOException("Неподходящий адрес загрузки APK");
                HttpsURLConnection c=connections.open(new URL(address));connection=c;
                try {
                    c.setConnectTimeout(15000);c.setReadTimeout(15000);c.setUseCaches(false);
                    c.setInstanceFollowRedirects(false);
                    c.setRequestProperty("User-Agent","mi-fitness-mod-updater");
                    c.setRequestProperty("Accept","application/octet-stream");
                    c.setRequestProperty("Accept-Encoding","identity");
                    c.setRequestProperty("Cookie","");c.setRequestProperty("Authorization","");
                    int code=c.getResponseCode();
                    if(code==301 || code==302 || code==303 || code==307 || code==308) {
                        String next=c.getHeaderField("Location");
                        if(next==null) throw new IOException("Нет адреса перенаправления");
                        address=new URL(new URL(address),next).toExternalForm();continue;
                    }
                    if(code!=200) throw new IOException("GitHub не отдал APK (HTTP "+code+")");
                    long expected=c.getContentLengthLong();
                    if(expected>=0 && expected!=a.size) throw new IOException("Неверный размер ответа GitHub");
                    long count=0,last=System.nanoTime()-300000000L; long start=System.nanoTime();
                    try(InputStream in=c.getInputStream();OutputStream out=new FileOutputStream(partial)) {
                        byte[] buffer=new byte[65536];int n;
                        while((n=in.read(buffer))!=-1) {
                            active(); count+=n;
                            if(count>a.size) throw new IOException("APK больше заявленного размера");
                            if(System.nanoTime()-start>30L*60*1000000000L) throw new IOException("Загрузка заняла слишком много времени");
                            out.write(buffer,0,n);
                            long now=System.nanoTime();
                            if(now-last>250000000L){last=now;progress.changed(count,a.size);}
                        }
                    }
                    active(); ApkFiles.verify(partial,a);
                    File ready=new File(dir,a.name);
                    if(ready.exists() && !ready.delete()) throw new IOException("Не удалось заменить предыдущую копию APK");
                    if(!partial.renameTo(ready)) throw new IOException("Не удалось сохранить APK");
                    complete=true;progress.changed(a.size,a.size);return ready;
                } finally { c.disconnect();connection=null; }
            }
            throw new IOException("Слишком много перенаправлений GitHub");
        } finally { if(!complete) partial.delete(); }
    }
}
