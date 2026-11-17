import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class MarkusNotificationTextTest {
    @Test void disabledKeepsPayloadAndIdentity() {
        exi data = new exi(); data.c = "Привет 😀"; data.e = "текст"; data.a = "org.example.app";
        MarkusNotificationText.format(Arrays.asList(data), 0);
        assertEquals("Привет 😀", data.c); assertEquals("текст", data.e); assertEquals("org.example.app", data.a);
    }
    @Test void onlyDisplayTextChanges() {
        exi data = new exi();
        data.a = "org.example.Mixed"; data.b = "App"; data.c = "Привет 😀"; data.e = "текст 👍🏽";
        data.d = "chatKey"; data.i = "notifyKey"; data.g = 42; data.f = "12:34";
        MarkusNotificationText.format(Arrays.asList(data), 3);
        assertEquals("ПРИВЕТ ", data.c); assertEquals("ТЕКСТ ", data.e);
        assertEquals("org.example.Mixed", data.a); assertEquals("App", data.b);
        assertEquals("chatKey", data.d); assertEquals("notifyKey", data.i);
        assertEquals(42, data.g); assertEquals("12:34", data.f);
    }
    @Test void structuredMessagesKeepPersonKeysAndTimestamps() {
        exi data = new exi(); data.r = new exi.h(); data.r.c = "Семья 👨‍👩‍👧‍👦";
        data.r.a = new exi.i(); data.r.a.a = "Я 😀"; data.r.a.b = "selfKey";
        exi.g message = new exi.g(); message.a = 123456; message.b = "привет 👋";
        message.c = new exi.i(); message.c.a = "Маша ❤️"; message.c.b = "personKey";
        data.r.d = new exi.g[]{message, null};
        MarkusNotificationText.format(Arrays.asList(data), 3);
        assertEquals("СЕМЬЯ ", data.r.c); assertEquals("Я ", data.r.a.a); assertEquals("selfKey", data.r.a.b);
        assertEquals("ПРИВЕТ ", message.b); assertEquals("МАША ", message.c.a);
        assertEquals("personKey", message.c.b); assertEquals(123456, message.a);
    }
    @Test void nullPayloadsAndFields() {
        MarkusNotificationText.format(null, 3);
        exi data = new exi(); data.r = new exi.h();
        MarkusNotificationText.format(Arrays.asList(null, data), 3);
        assertNull(data.c); assertNull(data.e); assertNull(data.r.c); assertNull(data.r.a);
    }
    @Test void togglesWorkIndependently() {
        exi data = new exi(); data.c = "Привет 😀";
        MarkusNotificationText.format(Arrays.asList(data), 1); assertEquals("Привет ", data.c);
        data.c = "Привет 😀";
        MarkusNotificationText.format(Arrays.asList(data), 2); assertEquals("ПРИВЕТ 😀", data.c);
    }
}
