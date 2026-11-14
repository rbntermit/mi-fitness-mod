package com.markus.update;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.cert.Certificate;
import java.util.*;
import java.util.zip.*;
import javax.net.ssl.HttpsURLConnection;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class ApkDownloadTest {
    @TempDir Path temp;
    byte[] bytes() throws Exception {
        ByteArrayOutputStream b=new ByteArrayOutputStream();
        try(ZipOutputStream z=new ZipOutputStream(b)) {
            z.putNextEntry(new ZipEntry(ApkFiles.MARKER));z.write((UpdateAssetTest.TAG+"\n").getBytes(StandardCharsets.US_ASCII));z.closeEntry();
        }return b.toByteArray();
    }
    UpdateAsset asset(byte[] data) throws Exception {
        Path f=temp.resolve("hash-input");Files.write(f,data);
        return UpdateAsset.of(UpdateAssetTest.TAG,UpdateAssetTest.NAME,UpdateAssetTest.URL,"sha256:"+ApkFiles.sha256(f.toFile()),data.length);
    }
    File dir(){return temp.resolve("downloads").toFile();}
    static final class Reply extends HttpsURLConnection {
        final byte[] data;final int code;final String location;
        Reply(URL url,byte[] data,int code,String location){super(url);this.data=data;this.code=code;this.location=location;}
        public int getResponseCode(){return code;}
        public String getHeaderField(String n){return "Location".equals(n)?location:null;}
        public long getContentLengthLong(){return -1;}
        public InputStream getInputStream(){return new ByteArrayInputStream(data);}
        public void connect(){}public void disconnect(){}public boolean usingProxy(){return false;}
        public String getCipherSuite(){return "test";}public Certificate[] getLocalCertificates(){return null;}public Certificate[] getServerCertificates(){return null;}
    }
    @Test void verifiedDownloadThroughGitHubRedirect() throws Exception {
        byte[] bytes=bytes();UpdateAsset a=asset(bytes);List<Reply> replies=new ArrayList<>();
        ApkDownload d=new ApkDownload(u->{Reply r=new Reply(u,bytes,replies.isEmpty()?302:200,"https://release-assets.githubusercontent.com/example");replies.add(r);return r;});
        File result=d.run(a,dir(),(n,total)->{});
        assertEquals(2,replies.size());assertEquals(a.name,result.getName());ApkFiles.verify(result,a);
        for(Reply r:replies){assertEquals("",r.getRequestProperty("Cookie"));assertEquals("",r.getRequestProperty("Authorization"));assertEquals("mi-fitness-mod-updater",r.getRequestProperty("User-Agent"));}
    }
    @Test void foreignRedirectIsNeverOpened() throws Exception {
        byte[] data=bytes();UpdateAsset a=asset(data);List<URL> requests=new ArrayList<>();
        ApkDownload d=new ApkDownload(u->{requests.add(u);return new Reply(u,data,302,"https://other.invalid/evil.apk");});
        assertThrows(IOException.class,()->d.run(a,dir(),(n,total)->{}));assertEquals(1,requests.size());assertEquals(0,dir().listFiles().length);
    }
    @Test void truncatedTransferIsRemoved() throws Exception {
        byte[] data=bytes();UpdateAsset a=asset(data);
        ApkDownload d=new ApkDownload(u->new Reply(u,Arrays.copyOf(data,data.length-3),200,null));
        assertThrows(IOException.class,()->d.run(a,dir(),(n,total)->{}));assertEquals(0,dir().listFiles().length);
    }
    @Test void oversizedTransferIsRemoved() throws Exception {
        byte[] data=bytes();UpdateAsset a=asset(data);
        ApkDownload d=new ApkDownload(u->new Reply(u,Arrays.copyOf(data,data.length+1),200,null));
        assertThrows(IOException.class,()->d.run(a,dir(),(n,total)->{}));assertEquals(0,dir().listFiles().length);
    }
    @Test void cancelledTransferIsRemoved() throws Exception {
        byte[] data=bytes();UpdateAsset a=asset(data);
        ApkDownload d=new ApkDownload(u->new Reply(u,data,200,null));
        assertThrows(IOException.class,()->d.run(a,dir(),(n,total)->d.cancel()));assertEquals(0,dir().listFiles().length);
    }
    @Test void redirectLoopsStop() throws Exception {
        byte[] data=bytes();UpdateAsset a=asset(data);List<URL> requests=new ArrayList<>();
        ApkDownload d=new ApkDownload(u->{requests.add(u);return new Reply(u,data,302,a.url);});
        assertThrows(IOException.class,()->d.run(a,dir(),(n,total)->{}));assertEquals(6,requests.size());assertEquals(0,dir().listFiles().length);
    }
}
