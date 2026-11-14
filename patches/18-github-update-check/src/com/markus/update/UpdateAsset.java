package com.markus.update;

import java.net.URI;
import java.util.Locale;

/** A single APK belonging to exactly this release, with GitHub's SHA-256 and size. */
final class UpdateAsset {
    static final long MAX_SIZE = 512L * 1024 * 1024;
    static final String DOWNLOADS = "https://github.com/rbntermit/mi-fitness-mod/releases/download/";
    final ModVersion version;
    final String name, url, sha256;
    final long size;

    private UpdateAsset(ModVersion version, String name, String url, String sha256, long size) {
        this.version=version; this.name=name; this.url=url; this.sha256=sha256; this.size=size;
    }

    static UpdateAsset of(String tag, String name, String url, String digest, long size) {
        ModVersion v=ModVersion.parseTag(tag);
        if (v==null || !v.tag().equals(tag) || size<=0 || size>MAX_SIZE) return null;
        String expected="MiFitness."+tag.substring(1)+".apk";
        if (!expected.equals(name) || !(DOWNLOADS+tag+"/"+name).equals(url)) return null;
        if (digest==null || !digest.matches("sha256:[0-9a-fA-F]{64}")) return null;
        return new UpdateAsset(v,name,url,digest.substring(7).toLowerCase(Locale.ROOT),size);
    }

    static UpdateAsset select(ReleaseJson release) {
        if (release==null || release.tag==null) return null;
        String expected="MiFitness."+release.tag.substring(Math.min(1,release.tag.length()))+".apk";
        UpdateAsset result=null;
        int matches=0;
        for (ReleaseJson.Asset a:release.assets) {
            if (!expected.equals(a.name)) continue;
            matches++;
            if (!"uploaded".equals(a.state)) continue;
            result=of(release.tag,a.name,a.url,a.digest,a.size);
        }
        return matches==1?result:null;
    }

    /** Only HTTPS GitHub release storage, without credentials, fragments or unusual ports. */
    boolean permits(String address) {
        try {
            if (address==null || address.length()>16384) return false;
            URI u=new URI(address);
            if (!"https".equalsIgnoreCase(u.getScheme()) || u.getUserInfo()!=null || u.getFragment()!=null
                    || (u.getPort()!=-1 && u.getPort()!=443)) return false;
            String host=u.getHost();
            if (host==null) return false;
            host=host.toLowerCase(Locale.ROOT);
            if ("github.com".equals(host)) return url.equals(address);
            return "release-assets.githubusercontent.com".equals(host) || "objects.githubusercontent.com".equals(host);
        } catch (Exception e) { return false; }
    }
}
