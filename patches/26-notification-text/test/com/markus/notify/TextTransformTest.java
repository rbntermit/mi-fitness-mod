package com.markus.notify;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Locale;
import org.junit.jupiter.api.Test;

class TextTransformTest {
    @Test void offPreservesExactObject() {
        String input = new String("Привет 😀 👨‍👩‍👧‍👦");
        assertSame(input, TextTransform.apply(input, false, false));
    }
    @Test void nullAndEmpty() {
        assertNull(TextTransform.apply(null, true, true));
        assertEquals("", TextTransform.apply("", true, true));
    }
    @Test void emojiAndCyrillic() {
        assertEquals("Привет  мир", TextTransform.apply("Привет 😀 мир", true, false));
    }
    @Test void nonBmpLettersSurvive() {
        assertEquals("𠀀 𐐨", TextTransform.apply("𠀀😀 𐐨", true, false));
    }
    @Test void numbersHashAndAsteriskSurvive() {
        assertEquals("Счёт #123 * 45.67", TextTransform.apply("Счёт #123 * 45.67", true, false));
    }
    @Test void keycapWithAndWithoutSelector() {
        assertEquals("123", TextTransform.apply("1️⃣2⃣#️⃣*⃣123", true, false));
    }
    @Test void flagPairs() {
        assertEquals(" / ", TextTransform.apply("🇷🇺 / 🇫🇷", true, false));
    }
    @Test void skinTone() { assertEquals("OK", TextTransform.apply("👍🏽OK", true, false)); }
    @Test void skinToneMakesTextDefaultBaseEmoji() {
        assertEquals("OK", TextTransform.apply("✌🏽🖐🏾☝🏻OK", true, false));
    }
    @Test void familyJoinerSequence() { assertEquals("AB", TextTransform.apply("A👨‍👩‍👧‍👦B", true, false)); }
    @Test void professionJoiner() { assertEquals("AB", TextTransform.apply("A👩🏽‍⚕️B", true, false)); }
    @Test void textSymbolsStayText() {
        assertEquals("© ® ™ ♥ ↔ + − =", TextTransform.apply("© ® ™ ♥ ↔ + − =", true, false));
    }
    @Test void explicitEmojiSymbols() {
        assertEquals("", TextTransform.apply("©️®️™️♥️↔️", true, false));
    }
    @Test void textPresentationSelector() {
        assertEquals("☀︎⌚︎", TextTransform.apply("☀︎⌚︎", true, false));
    }
    @Test void combiningMarksAndOtherJoinersSurvive() {
        assertEquals("й á क्‍ष", TextTransform.apply("й á क्‍ष", true, false));
    }
    @Test void malformedSurrogatesAreNotEaten() {
        assertEquals("A\ud83dB\udc00", TextTransform.apply("A\ud83dB\udc00", true, false));
    }
    @Test void tagFlag() {
        String england = "\ud83c\udff4\udb40\udc67\udb40\udc62\udb40\udc65\udb40\udc6e\udb40\udc67\udb40\udc7f";
        assertEquals("AB", TextTransform.apply("A" + england + "B", true, false));
    }
    @Test void whitespaceIsNotNormalized() {
        assertEquals(" A\n \tB  ", TextTransform.apply(" A\n😀 \tB  ", true, false));
    }
    @Test void uppercaseOnlyKeepsEmoji() {
        assertEquals("ПРИВЕТ 😀 ЁЖ", TextTransform.apply("Привет 😀 ёж", false, true));
    }
    @Test void bothSwitches() { assertEquals("ПРИВЕТ ", TextTransform.apply("Привет 👋🏾", true, true)); }
    @Test void uppercaseIsIndependentOfPhoneLocale() {
        Locale saved = Locale.getDefault();
        try {
            Locale.setDefault(new Locale("tr", "TR"));
            assertEquals("I STRASSE", TextTransform.apply("i straße", false, true));
        } finally { Locale.setDefault(saved); }
    }
    @Test void veryLongNotification() {
        StringBuilder input = new StringBuilder(), expected = new StringBuilder();
        for (int i = 0; i < 10000; i++) { input.append("а😀"); expected.append('А'); }
        assertEquals(expected.toString(), TextTransform.apply(input.toString(), true, true));
    }
    @Test void optionsAreSeparateStableKeys() {
        assertTrue(NotificationText.isOption("notification_no_emoji"));
        assertTrue(NotificationText.isOption("notification_uppercase"));
        assertTrue(NotificationText.isOption("notification_pause_on_phone_dnd"));
        assertFalse(NotificationText.isOption("simple_zen_sync"));
        assertFalse(NotificationText.isOption(null));
    }
    @Test void initialOptionsAreOff() { assertEquals(0, NotificationText.options()); }
}
