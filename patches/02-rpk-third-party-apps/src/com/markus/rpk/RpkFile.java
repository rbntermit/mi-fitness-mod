package com.markus.rpk;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 * A third-party app for Vela bands (.rpk): a ZIP with manifest.json that names the package and its version.
 * Plain Java (own small JSON reader), so every check is tested without a phone.
 */
public final class RpkFile {
    public static final long MAX_SIZE = 50L * 1024 * 1024;
    static final int MAX_MANIFEST = 512 * 1024;
    private static final Pattern PACKAGE = Pattern.compile("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z0-9_]+)+$");

    /** A check that failed; the message is shown to the person as it is. */
    public static final class Problem extends Exception {
        Problem(String message) {
            super(message);
        }
    }

    public final String packageName;
    public final String name;
    public final String versionName;
    public final int versionCode;
    public final long size;

    private RpkFile(String packageName, String name, String versionName, int versionCode, long size) {
        this.packageName = packageName;
        this.name = name;
        this.versionName = versionName;
        this.versionCode = versionCode;
        this.size = size;
    }

    public static RpkFile read(File f) throws Problem {
        long size = f.length();
        if (size <= 0) {
            throw new Problem("Файл пустой.");
        }
        if (size > MAX_SIZE) {
            throw new Problem("Файл больше " + MAX_SIZE / 1024 / 1024 + " МБ — это не похоже на приложение для браслета.");
        }
        byte[] head = new byte[4];
        try (InputStream in = new FileInputStream(f)) {
            if (in.read(head) != 4 || head[0] != 'P' || head[1] != 'K' || head[2] != 3 || head[3] != 4) {
                throw new Problem("Это не файл .rpk: внутри должен быть ZIP-архив.");
            }
        } catch (IOException e) {
            throw new Problem("Файл не читается: " + e.getMessage());
        }
        try (ZipFile zip = new ZipFile(f)) {
            ZipEntry m = zip.getEntry("manifest.json");
            if (m == null) {
                throw new Problem("В файле нет manifest.json — это не приложение для браслета (.rpk).");
            }
            if (m.getSize() > MAX_MANIFEST) {
                throw new Problem("manifest.json слишком большой.");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (InputStream in = zip.getInputStream(m)) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = in.read(buf)) != -1) {
                    out.write(buf, 0, r);
                    if (out.size() > MAX_MANIFEST) {
                        throw new Problem("manifest.json слишком большой.");
                    }
                }
            }
            return parseManifest(new String(out.toByteArray(), StandardCharsets.UTF_8), size);
        } catch (ZipException e) {
            throw new Problem("Архив повреждён: " + e.getMessage());
        } catch (IOException e) {
            throw new Problem("Архив не читается: " + e.getMessage());
        }
    }

    static RpkFile parseManifest(String json, long size) throws Problem {
        Map<String, Object> m;
        try {
            Object v = new Reader(json.startsWith("﻿") ? json.substring(1) : json).value();
            if (!(v instanceof Map)) {
                throw new Problem("manifest.json — не объект JSON.");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> mm = (Map<String, Object>) v;
            m = mm;
        } catch (IllegalArgumentException e) {
            throw new Problem("manifest.json не читается: " + e.getMessage());
        }
        Object pkg = m.get("package");
        if (!(pkg instanceof String) || !PACKAGE.matcher((String) pkg).matches()) {
            throw new Problem("В manifest.json нет имени пакета (package) вида com.example.app.");
        }
        int code = versionCode(m.get("versionCode"));
        if (code <= 0) {
            throw new Problem("В manifest.json нет номера версии (versionCode) — целого числа больше нуля.");
        }
        Object name = m.get("name");
        Object vName = m.get("versionName");
        String n = name instanceof String && !((String) name).trim().isEmpty() ? ((String) name).trim() : (String) pkg;
        return new RpkFile((String) pkg, n, vName instanceof String ? (String) vName : String.valueOf(vName == null ? code : vName),
                code, size);
    }

    private static int versionCode(Object v) {
        if (v instanceof Double) {
            double d = (Double) v;
            return d == Math.rint(d) && d > 0 && d <= Integer.MAX_VALUE ? (int) d : -1;
        }
        if (v instanceof String && ((String) v).trim().matches("[0-9]{1,9}")) {
            return Integer.parseInt(((String) v).trim());
        }
        return -1;
    }

    // ---------------------------------------------------------------- minimal JSON reader

    static final class Reader {
        private final String s;
        private int i;

        Reader(String s) {
            this.s = s;
        }

        Object value() {
            Object v = any();
            ws();
            if (i != s.length()) {
                throw new IllegalArgumentException("лишние символы в позиции " + i);
            }
            return v;
        }

        private void ws() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
                i++;
            }
        }

        private Object any() {
            ws();
            if (i >= s.length()) {
                throw new IllegalArgumentException("файл оборван");
            }
            char c = s.charAt(i);
            switch (c) {
                case '{':
                    return object();
                case '[':
                    return array();
                case '"':
                    return string();
                case 't':
                    return word("true", Boolean.TRUE);
                case 'f':
                    return word("false", Boolean.FALSE);
                case 'n':
                    return word("null", null);
                default:
                    return number();
            }
        }

        private Object word(String w, Object v) {
            if (!s.startsWith(w, i)) {
                throw new IllegalArgumentException("ошибка в позиции " + i);
            }
            i += w.length();
            return v;
        }

        private Map<String, Object> object() {
            Map<String, Object> m = new LinkedHashMap<>();
            i++;
            ws();
            if (i < s.length() && s.charAt(i) == '}') {
                i++;
                return m;
            }
            while (true) {
                ws();
                if (i >= s.length() || s.charAt(i) != '"') {
                    throw new IllegalArgumentException("ожидалось имя поля в позиции " + i);
                }
                String k = string();
                ws();
                if (i >= s.length() || s.charAt(i) != ':') {
                    throw new IllegalArgumentException("ожидалось «:» в позиции " + i);
                }
                i++;
                Object v = any();
                if (!m.containsKey(k)) {
                    m.put(k, v);
                }
                ws();
                if (i < s.length() && s.charAt(i) == ',') {
                    i++;
                    continue;
                }
                if (i < s.length() && s.charAt(i) == '}') {
                    i++;
                    return m;
                }
                throw new IllegalArgumentException("ожидалось «,» или «}» в позиции " + i);
            }
        }

        private java.util.List<Object> array() {
            java.util.List<Object> a = new java.util.ArrayList<>();
            i++;
            ws();
            if (i < s.length() && s.charAt(i) == ']') {
                i++;
                return a;
            }
            while (true) {
                a.add(any());
                ws();
                if (i < s.length() && s.charAt(i) == ',') {
                    i++;
                    continue;
                }
                if (i < s.length() && s.charAt(i) == ']') {
                    i++;
                    return a;
                }
                throw new IllegalArgumentException("ожидалось «,» или «]» в позиции " + i);
            }
        }

        private String string() {
            StringBuilder b = new StringBuilder();
            i++;
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (c == '"') {
                    return b.toString();
                }
                if (c == '\\') {
                    if (i >= s.length()) {
                        break;
                    }
                    char e = s.charAt(i++);
                    switch (e) {
                        case 'n': b.append('\n'); break;
                        case 't': b.append('\t'); break;
                        case 'r': b.append('\r'); break;
                        case 'b': b.append('\b'); break;
                        case 'f': b.append('\f'); break;
                        case 'u':
                            if (i + 4 > s.length()) {
                                throw new IllegalArgumentException("неверный \\u в позиции " + i);
                            }
                            b.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                            i += 4;
                            break;
                        default: b.append(e);
                    }
                } else {
                    b.append(c);
                }
            }
            throw new IllegalArgumentException("строка не закрыта");
        }

        private Double number() {
            int start = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) {
                i++;
            }
            if (start == i) {
                throw new IllegalArgumentException("неожиданный символ в позиции " + i);
            }
            try {
                return Double.valueOf(s.substring(start, i));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("неверное число в позиции " + start);
            }
        }
    }
}
