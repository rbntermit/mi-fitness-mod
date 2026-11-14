package com.markus.update;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads the top-level string and boolean fields of the GitHub "latest release" answer
 * (tag_name, name, body, html_url, draft, prerelease). Release assets retain only their names, download addresses, sizes and SHA-256; other nested metadata is skipped.
 * A small strict reader instead of org.json, so that it is testable off the device. Pure logic.
 */
final class ReleaseJson {
    final List<Asset> assets;
    static final class Asset {
        final String name,url,digest,state;
        final long size;
        Asset(Map<String,Object> f) {
            name=str(f.get("name"));url=str(f.get("browser_download_url"));
            digest=str(f.get("digest"));state=str(f.get("state"));
            size=f.get("size") instanceof Long?(Long)f.get("size"):-1;
        }
    }
    final String tag;
    final String name;
    final String body;
    final String htmlUrl;
    final boolean draft;
    final boolean prerelease;

    private ReleaseJson(Map<String, Object> f) {
        List<Asset> out=new ArrayList<>();
        Object raw=f.get("assets");
        if(raw instanceof List) {
            for(Object item:(List<?>)raw) {
                if(item instanceof Map) out.add(new Asset((Map<String,Object>)item));
            }
        }
        assets=Collections.unmodifiableList(out);
        tag = str(f.get("tag_name"));
        name = str(f.get("name"));
        body = str(f.get("body"));
        htmlUrl = str(f.get("html_url"));
        draft = Boolean.TRUE.equals(f.get("draft"));
        prerelease = Boolean.TRUE.equals(f.get("prerelease"));
    }

    /** Null when the text is not a JSON object. */
    static ReleaseJson parse(String json) {
        if (json == null) {
            return null;
        }
        try {
            Reader r = new Reader(json);
            Map<String, Object> f = r.topObject();
            r.ws();
            if(r.i!=json.length()) throw new IllegalArgumentException("trailing JSON");
            return new ReleaseJson(f);
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            return null;
        }
    }

    private static String str(Object o) {
        return o instanceof String ? (String) o : null;
    }

    private static final class Reader {
        private final String s;
        private int i;
        private int depth;

        Reader(String s) {
            this.s = s;
        }

        Map<String, Object> topObject() {
            ws();
            Map<String, Object> out = new HashMap<>();
            expect('{');
            ws();
            if (peek() == '}') {
                i++;
                return out;
            }
            while (true) {
                ws();
                String key = string();
                ws();
                expect(':');
                ws();
                Object v = "assets".equals(key) ? assetArray() : value(true);
                if (v != null) {
                    out.put(key, v);
                }
                ws();
                char c = next();
                if (c == '}') {
                    return out;
                }
                if (c != ',') {
                    throw new IllegalArgumentException("',' expected");
                }
            }
        }

        /** Scalar strings, booleans and integer metadata are returned; other containers are skipped here. */
        private Object value(boolean keep) {
            char c = peek();
            if (c == '"') {
                String v = string();
                return keep ? v : null;
            }
            if (c == '{' || c == '[') {
                skipContainer();
                return null;
            }
            if (s.startsWith("true", i)) {
                i += 4;
                return keep ? Boolean.TRUE : null;
            }
            if (s.startsWith("false", i)) {
                i += 5;
                return keep ? Boolean.FALSE : null;
            }
            if (s.startsWith("null", i)) {
                i += 4;
                return null;
            }
            int start = i;
            while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) {
                i++;
            }
            if (i == start) {
                throw new IllegalArgumentException("value expected");
            }
            return keep ? Long.valueOf(s.substring(start,i)) : null;
        }

        private Object assetArray() {
            if(peek()!='[') { value(false);return null; }
            expect('[');if(++depth>64) throw new IllegalArgumentException("too deep");ws();
            List<Map<String,Object>> out=new ArrayList<>();
            if(peek()==']'){i++;depth--;return out;}
            while(true) {
                if(out.size()>=64) throw new IllegalArgumentException("too many assets");
                out.add(topObject());ws();char c=next();
                if(c==']'){depth--;return out;}
                if(c!=',')throw new IllegalArgumentException("asset separator");ws();
            }
        }
        private void skipContainer() {
            char open = next();
            char close = open == '{' ? '}' : ']';
            if (++depth > 64) {
                throw new IllegalArgumentException("too deep");
            }
            ws();
            if (peek() == close) {
                i++;
                depth--;
                return;
            }
            while (true) {
                ws();
                if (open == '{') {
                    string();
                    ws();
                    expect(':');
                    ws();
                }
                value(false);
                ws();
                char c = next();
                if (c == close) {
                    depth--;
                    return;
                }
                if (c != ',') {
                    throw new IllegalArgumentException("',' expected");
                }
            }
        }

        private String string() {
            expect('"');
            StringBuilder b = new StringBuilder();
            while (true) {
                char c = next();
                if (c == '"') {
                    return b.toString();
                }
                if (c != '\\') {
                    b.append(c);
                    continue;
                }
                char e = next();
                switch (e) {
                    case '"': b.append('"'); break;
                    case '\\': b.append('\\'); break;
                    case '/': b.append('/'); break;
                    case 'b': b.append('\b'); break;
                    case 'f': b.append('\f'); break;
                    case 'n': b.append('\n'); break;
                    case 'r': b.append('\r'); break;
                    case 't': b.append('\t'); break;
                    case 'u':
                        if (i + 4 > s.length()) {
                            throw new IllegalArgumentException("bad escape");
                        }
                        b.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        i += 4;
                        break;
                    default:
                        throw new IllegalArgumentException("bad escape");
                }
            }
        }

        private void ws() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) {
                i++;
            }
        }

        private char peek() {
            if (i >= s.length()) {
                throw new IllegalArgumentException("unexpected end");
            }
            return s.charAt(i);
        }

        private char next() {
            char c = peek();
            i++;
            return c;
        }

        private void expect(char c) {
            if (next() != c) {
                throw new IllegalArgumentException("'" + c + "' expected");
            }
        }
    }
}
