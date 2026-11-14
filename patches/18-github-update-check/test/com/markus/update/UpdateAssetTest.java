package com.markus.update;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.zip.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UpdateAssetTest {
    static final String TAG="v3.60.0i-band-7", NAME="MiFitness.3.60.0i-band-7.apk";
    static final String URL=UpdateAsset.DOWNLOADS+TAG+"/"+NAME;
    static final String SHA="0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
    static UpdateAsset asset(){return UpdateAsset.of(TAG,NAME,URL,"sha256:"+SHA,100);}
    @TempDir Path dir;

    @Test void exactAssetIsSelected() {
        String json="{\"tag_name\":\""+TAG+"\",\"assets\":["+entry(NAME)+"]}";
        ReleaseJson r=ReleaseJson.parse(json);
        assertNotNull(r);assertEquals(1,r.assets.size());assertEquals(100,r.assets.get(0).size);
        UpdateAsset a=UpdateAsset.select(r);assertNotNull(a);assertEquals(SHA,a.sha256);
        assertEquals(URL,a.url);
    }
    static String entry(String name){return "{\"name\":\""+name+"\",\"browser_download_url\":\""+URL
            +"\",\"digest\":\"sha256:"+SHA+"\",\"state\":\"uploaded\",\"size\":100}";}
    @Test void ambiguousAssetsAreRejected(){assertNull(UpdateAsset.select(ReleaseJson.parse("{\"tag_name\":\""+TAG+"\",\"assets\":["+entry(NAME)+","+entry(NAME)+"]}")));}
    @Test void wrongTagFileHashAndSizeAreRejected() {
        assertNull(UpdateAsset.of(TAG,"MiFitness.3.60.0i-band-6.apk",URL,"sha256:"+SHA,100));
        assertNull(UpdateAsset.of(TAG,NAME,URL,null,100));
        assertNull(UpdateAsset.of(TAG,NAME,URL,"sha256:abc",100));
        assertNull(UpdateAsset.of(TAG,NAME,URL,"sha256:"+SHA,0));
        assertNull(UpdateAsset.of(TAG,NAME,URL,"sha256:"+SHA,UpdateAsset.MAX_SIZE+1));
        assertNull(UpdateAsset.of(TAG,NAME,URL+"?token=anything","sha256:"+SHA,100));
    }
    @ParameterizedTest @ValueSource(strings={"http://github.com/a","https://evil.invalid/a", "file:///sdcard/a.apk",
            "https://github.com.evil.invalid/a", "https://release-assets.githubusercontent.com.evil.invalid/a",
            "https://user:password"+"@"+"release-assets.githubusercontent.com/a", "https://release-assets.githubusercontent.com:8443/a",
            "https://release-assets.githubusercontent.com/a#fragment", "https://github.com/other/repo/releases/download/a/b.apk"})
    void redirectsCannotLeaveGitHubStorage(String u){assertFalse(asset().permits(u));}
    @Test void legitimateRedirectsWork(){assertTrue(asset().permits(URL));assertTrue(asset().permits("https://release-assets.githubusercontent.com/a?signature=public"));assertTrue(asset().permits("https://objects.githubusercontent.com/a"));}
    @Test void truncatedOrTrailingJsonIsRejected(){assertNull(ReleaseJson.parse("{\"assets\":[{"));assertNull(ReleaseJson.parse("{} garbage"));}
    @Test void nestedAssetsHaveDepthLimit(){String s="{}";for(int i=0;i<70;i++)s="{\"assets\":["+s+"]}";assertNull(ReleaseJson.parse(s));}

    File apk(String tag) throws Exception {
        File f=dir.resolve("sample.apk").toFile();
        try(ZipOutputStream out=new ZipOutputStream(new FileOutputStream(f))){
            out.putNextEntry(new ZipEntry(ApkFiles.MARKER));out.write((tag+"\n").getBytes(StandardCharsets.US_ASCII));out.closeEntry();
            out.putNextEntry(new ZipEntry("classes.dex"));out.write(new byte[]{1,2,3});out.closeEntry();
        }return f;
    }
    UpdateAsset forFile(File f) throws Exception{return UpdateAsset.of(TAG,NAME,URL,"sha256:"+ApkFiles.sha256(f),f.length());}
    @Test void completeHashAndMarkerPass() throws Exception{File f=apk(TAG);ApkFiles.verify(f,forFile(f));}
    @Test void sameSizeTamperingIsRejected() throws Exception{
        File f=apk(TAG);UpdateAsset a=forFile(f);
        try(RandomAccessFile out=new RandomAccessFile(f,"rw")){out.seek(15);out.write(7);}
        assertThrows(IOException.class,()->ApkFiles.verify(f,a));
    }
    @Test void wrongBuildIsRejectedEvenWithMatchingHash() throws Exception{File f=apk("v3.60.0i-band-6");UpdateAsset a=forFile(f);assertThrows(IOException.class,()->ApkFiles.verify(f,a));}
    @Test void truncatedApkIsRejected() throws Exception{File f=apk(TAG);UpdateAsset a=forFile(f);try(RandomAccessFile out=new RandomAccessFile(f,"rw")){out.setLength(12);}assertThrows(IOException.class,()->ApkFiles.verify(f,a));}
    @Test void cancellationDoesNotCreateAnApk() throws Exception{
        ApkDownload d=new ApkDownload();d.cancel();
        assertThrows(IOException.class,()->d.run(asset(),dir.toFile(),(a,b)->{}));
        assertEquals(0,dir.toFile().listFiles().length);
    }
}
