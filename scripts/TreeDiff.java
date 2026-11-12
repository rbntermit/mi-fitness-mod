import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.*;

/** Compare two apktool trees. Smali is keyed by class path (smali_classesN ignored) and
 *  compared without debug lines; everything else is compared byte for byte.
 *  Usage: java TreeDiff.java <a> <b> <outDir> */
public class TreeDiff {
    static final String[] DEBUG = {".line", ".source", ".local ", ".end local", ".restart local", ".prologue", ".param", ".end param"};

    static String hash(Path p, boolean smali) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        if (!smali) { md.update(Files.readAllBytes(p)); }
        else for (String l : Files.readAllLines(p)) {
            String s = l.strip();
            if (Arrays.stream(DEBUG).anyMatch(s::startsWith)) continue;
            md.update(l.stripTrailing().getBytes()); md.update((byte) 10);
        }
        return HexFormat.of().formatHex(md.digest());
    }

    static Map<String, Path>[] index(Path root) throws IOException {
        Map<String, Path> smali = new TreeMap<>(), other = new TreeMap<>();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(root)) {
            for (Path d : ds) {
                if (!Files.isDirectory(d)) continue;
                String n = d.getFileName().toString();
                boolean isSmali = n.equals("smali") || n.startsWith("smali_");
                try (Stream<Path> w = Files.walk(d)) {
                    for (Path f : (Iterable<Path>) w.filter(Files::isRegularFile)::iterator) {
                        String rel = (isSmali ? d : root).relativize(f).toString().replace(File.separatorChar, '/');
                        (isSmali ? smali : other).put(rel, f);
                    }
                }
            }
        }
        return new Map[]{smali, other};
    }

    public static void main(String[] a) throws Exception {
        Map<String, Path>[] ia = index(Path.of(a[0])), ib = index(Path.of(a[1]));
        Path out = Path.of(a[2]); Files.createDirectories(out);
        String[] kinds = {"smali", "other"};
        for (int k = 0; k < 2; k++) {
            Map<String, Path> x = ia[k], y = ib[k];
            List<String> added = new ArrayList<>(), removed = new ArrayList<>(), changed = Collections.synchronizedList(new ArrayList<>());
            for (String s : y.keySet()) if (!x.containsKey(s)) added.add(s);
            for (String s : x.keySet()) if (!y.containsKey(s)) removed.add(s);
            final int kk = k;
            x.keySet().parallelStream().filter(y::containsKey).forEach(s -> {
                try { if (!hash(x.get(s), kk == 0).equals(hash(y.get(s), kk == 0))) changed.add(s); }
                catch (Exception e) { throw new RuntimeException(s, e); }
            });
            Collections.sort(changed);
            Files.write(out.resolve(kinds[k] + "_added.txt"), added);
            Files.write(out.resolve(kinds[k] + "_removed.txt"), removed);
            Files.write(out.resolve(kinds[k] + "_changed.txt"), changed);
            System.out.printf("%s added=%d removed=%d changed=%d%n", kinds[k], added.size(), removed.size(), changed.size());
        }
    }
}
