package com.markus.notify;

import java.util.Locale;

/** Unicode 17 emoji presentation; ordinary text symbols and text-style variation selectors are retained. */
public final class TextTransform {
    private TextTransform() {}

    public static String apply(String text, boolean noEmoji, boolean uppercase) {
        if (text == null || (!noEmoji && !uppercase)) return text;
        String result = noEmoji ? stripEmoji(text) : text;
        return uppercase ? result.toUpperCase(Locale.ROOT) : result;
    }

    private static String stripEmoji(String text) {
        StringBuilder out = null;
        for (int i = 0; i < text.length();) {
            int end = atomEnd(text, i, false);
            if (end > i) {
                // Consume joiners only when followed by another emoji atom; preserve script-shaping ZWJ elsewhere.
                while (end < text.length() && text.codePointAt(end) == 0x200d) {
                    int next = atomEnd(text, end + 1, true);
                    if (next == end + 1) break;
                    end = next;
                }
                if (out == null) out = new StringBuilder(text.length()).append(text, 0, i);
                i = end;
            } else {
                int next = i + Character.charCount(text.codePointAt(i));
                if (out != null) out.append(text, i, next);
                i = next;
            }
        }
        return out == null ? text : out.toString();
    }

    private static int atomEnd(String text, int start, boolean joined) {
        if (start >= text.length()) return start;
        int cp = text.codePointAt(start);
        int end = start + Character.charCount(cp);
        if (end < text.length() && text.codePointAt(end) == 0xfe0e) return start;
        boolean selector = end < text.length() && text.codePointAt(end) == 0xfe0f;
        int afterSelector = selector ? end + 1 : end;
        int next = afterSelector < text.length() ? text.codePointAt(afterSelector) : -1;
        boolean modifier = next >= 0x1f3fb && next <= 0x1f3ff;
        if (cp == '#' || cp == '*' || (cp >= '0' && cp <= '9')) {
            return afterSelector < text.length() && text.codePointAt(afterSelector) == 0x20e3
                    ? afterSelector + 1 : start;
        }
        if (!EmojiRanges.contains(EmojiRanges.PRESENTATION, cp)
                && !((selector || joined || modifier) && EmojiRanges.contains(EmojiRanges.EMOJI, cp))) return start;
        end = afterSelector;
        if (modifier) end += Character.charCount(next);
        if (cp == 0x1f3f4) {
            int tagEnd = end;
            while (tagEnd < text.length()) {
                int tag = text.codePointAt(tagEnd);
                if (tag < 0xe0020 || tag > 0xe007e) break;
                tagEnd += Character.charCount(tag);
            }
            if (tagEnd > end && tagEnd < text.length() && text.codePointAt(tagEnd) == 0xe007f) end = tagEnd + 2;
        }
        return end;
    }
}
