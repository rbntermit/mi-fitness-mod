import com.markus.notify.NotificationText;
import com.markus.notify.TextTransform;
import java.util.List;

/** The 3.60 BLE/Huami payload only: do not change package names, keys, timestamps or reply actions. */
public final class MarkusNotificationText {
    private MarkusNotificationText() {}

    public static void beforeSend(List<exi> list) { format(list, NotificationText.options()); }

    static void format(List<exi> list, int options) {
        if (list == null || options == 0) return;
        for (exi data : list) {
            if (data == null) continue;
            data.c = text(data.c, options);
            data.e = text(data.e, options);
            exi.h style = data.r;
            if (style != null) {
                style.c = text(style.c, options);
                person(style.a, options);
                if (style.d != null) for (exi.g message : style.d) {
                    if (message == null) continue;
                    message.b = text(message.b, options);
                    person(message.c, options);
                }
            }
        }
    }

    private static void person(exi.i person, int options) {
        if (person != null) person.a = text(person.a, options);
    }

    private static String text(String value, int options) {
        return TextTransform.apply(value, (options & 1) != 0, (options & 2) != 0);
    }
}
