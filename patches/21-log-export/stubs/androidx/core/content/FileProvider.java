package androidx.core.content;

import android.content.Context;
import android.net.Uri;
import java.io.File;

/** Compile-time stand-in for the app's androidx FileProvider (declared as com.xiaomi.wearable.fileProvider). */
public class FileProvider {
    public static Uri getUriForFile(Context context, String authority, File file) {
        throw new UnsupportedOperationException("stub");
    }
}
