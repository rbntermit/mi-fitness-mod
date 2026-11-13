package com.markus.logexport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class LogMaskerTest {
    // test addresses are assembled at run time so that scripts/check-repo.sh does not take them for real ones
    static final String AT = "@";

    static String mac(String sep, String... parts) {
        return String.join(sep, parts);
    }

    static String m(String s) {
        return LogMasker.mask(s);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "serviceToken=AbC123/xyz+987==",
        "passToken: abcDEF123",
        "\"ssecurity\":\"q1w2e3r4t5y6\"",
        "ssecurity = Zx9",
        "access_token=tok123",
        "refreshToken\":\"r-123\"",
        "cUserId=Ab12Cd",
        "userId=1234567",
        "did=987654",
        "\"did\": \"blt.3.abc\"",
        "deviceId=x1y2",
        "sn=54713/F4ZA0012",
        "serialNumber: 12AB34",
        "imei=356938035643809",
        "macAddress=a1b2c3",
        "phone=+79161234567",
        "incomingNumber = 89161234567",
        "password=hunter2",
        "nonce=n0nc3",
        "signature=s1g",
    })
    void secretValuesAreHidden(String line) {
        String out = m(line);
        assertTrue(out.contains(LogMasker.HIDDEN) || out.contains(LogMasker.MAC) || out.contains(LogMasker.PHONE), out);
        String value = line.replaceFirst("^[^:=]*[:=]\\s*\"?", "").replace("\"", "");
        assertFalse(out.contains(value), out);
    }

    @Test
    void keysStayReadable() {
        assertEquals("serviceToken=" + LogMasker.HIDDEN + "&sid=miothealth", m("serviceToken=AbC123&sid=miothealth"));
        assertEquals("\"did\":\"" + LogMasker.HIDDEN + "\",\"model\":\"q66tc\"", m("\"did\":\"123abc\",\"model\":\"q66tc\""));
    }

    @Test
    void urlQueryParametersAreHidden() {
        String out = m("GET https://api.example.com/x?userId=42&serviceToken=abc%2Bdef&lang=ru");
        assertFalse(out.contains("abc%2Bdef"), out);
        assertFalse(out.contains("userId=42"), out);
        assertTrue(out.contains("lang=ru"), out);
    }

    @Test
    void cookiesAndBearerAreHidden() {
        assertEquals("Cookie: " + LogMasker.HIDDEN, m("Cookie: serviceToken=abc; userId=1; cUserId=2"));
        assertEquals("Authorization: Bearer " + LogMasker.HIDDEN, m("Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.abc"));
    }

    @Test
    void macAddressesAreHidden() {
        assertEquals("mac=" + LogMasker.HIDDEN, m("mac=" + mac(":", "11", "22", "33", "44", "55", "66")));
        assertEquals("connect " + LogMasker.MAC + " ok", m("connect " + mac(":", "A4", "C1", "38", "0F", "1E", "2D") + " ok"));
        assertEquals("addr " + LogMasker.MAC, m("addr " + mac("-", "a4", "c1", "38", "0f", "1e", "2d")));
        assertEquals("masked " + LogMasker.MAC, m("masked " + mac(":", "XX", "XX", "XX", "XX", "1E", "2D")));
    }

    @Test
    void emailsAreHidden() {
        assertEquals("account " + LogMasker.EMAIL + " signed in", m("account " + "ivan.petrov+mi" + AT + "example.co.uk" + " signed in"));
    }

    @Test
    void phoneNumbersAreHidden() {
        assertEquals("call from " + LogMasker.PHONE, m("call from +7 (916) 123-45-67"));
        assertEquals("call from " + LogMasker.PHONE, m("call from +79161234567"));
        assertEquals("sms " + LogMasker.PHONE + " text", m("sms 8 916 123 45 67 text"));
        assertEquals("sms " + LogMasker.PHONE, m("sms 79161234567"));
        assertEquals("uk " + LogMasker.PHONE, m("uk +44 20 7946 0958"));
    }

    @Test
    void longTokensAndIdsAreHidden() {
        assertEquals("Report{sid = " + LogMasker.HIDDEN + ", bedTime = 1794517200123}",
                m("Report{sid = 1234567890, bedTime = 1794517200123}"));
        assertEquals("called did " + LogMasker.HIDDEN + ", isDual", m("called did 1234567890, isDual"));
        // times in seconds after a time key stay: they are what a log is read for
        String times = "StepItem(sid=" + LogMasker.HIDDEN + ", time=1794558600, startTime = 1794550000, timestamp:1794517200)";
        assertEquals(times, m("StepItem(sid=1234567890, time=1794558600, startTime = 1794550000, timestamp:1794517200)"));
        assertEquals("devicelog/" + LogMasker.HIDDEN + "_miwear.watch.q66tc",
                m("devicelog/1234567890_miwear.watch.q66tc"));
        assertEquals("key " + LogMasker.HIDDEN, m("key 9f86d081884c7d659a2feaa0c55ad015"));
        assertEquals("tok " + LogMasker.HIDDEN, m("tok V2hhdEFuZEFyZVlvdURvaW5nSGVyZTEyMzQ1Ng=="));
        assertEquals("user " + LogMasker.HIDDEN + " logged", m("user 1234567890 logged"));
    }

    @Test
    void ordinaryLinesStay() {
        String[] keep = {
            "11-13 00:12:25.855 17816 18135 I NotifySyncService: onNotificationPosted Id:1 IsOngoing:true",
            "stock OTA blocked: offer 4.100.139 is not newer than 4.200.078",
            "PostTime:1794518744056 currentDeviceModel.connected: true",
            "MSG_NOTIFY_ADD_TIMEOUT mNotifyDataList size1",
            "did not answer in 5000 ms",
            "battery 23 %, version 3.60.0i",
            "com.xiaomi.fitness.device.manager.ui.tab.DeviceTabV4ViewModel.fillBannerList",
        };
        for (String s : keep) {
            assertEquals(s, m(s));
        }
    }

    @Test
    void tagsAndPathsStayReadable() {
        String tag = "11-13 12:13:15.622 16208 16208 I XiaomiFit_DeviceManager_16208_16208: onCoreServiceState()";
        assertEquals(tag, m(tag));
        String dashed = "11-13 12:40:33.313 12686 12686 I AutoPageTrack-Activity_12686_12686: onActivity visible";
        assertEquals(dashed, m(dashed));
        assertEquals("on device start sync:" + LogMasker.HIDDEN + " main 1794562844419",
                m("on device start sync:1234567890 main 1794562844419"));
        String path = "open /data/user/0/com.xiaomi.wearable/files/mmkv/MiFitnessKv2 ok";
        assertEquals(path, m(path));
        // a random base64 token with slashes is still hidden
        assertEquals("t " + LogMasker.HIDDEN, m("t aB3dE9f/Gh2jK4l+Mn5pQ7r/St8uV1w=="));
        assertEquals("t " + LogMasker.HIDDEN, m("t Ab3dE_f9Gh2jK4lMn5pQ7rSt8uV1wXyZ"));
    }

    @Test
    void zipEntryNamesDropTheDid() {
        assertEquals("miwear.watch.q66tc/_data_tmp_34836_log.tar.gz",
                LogLines.zipEntryName("1234567890_miwear.watch.q66tc/_data_tmp_34836_log.tar.gz"));
        assertEquals("a/miwear.watch.o61/log.txt", LogLines.zipEntryName("a/123456789_miwear.watch.o61/log.txt"));
        assertEquals("hmDeviceLog.log", LogLines.zipEntryName("hmDeviceLog.log"));
    }

    @Test
    void nullAndEmpty() {
        assertEquals(null, m(null));
        assertEquals("", m(""));
    }

    // ---------------------------------------------------------------- logcat lines

    @Test
    void pidOfThreadtimeLine() {
        assertEquals(17816, LogLines.pidOf("11-13 00:12:25.855 17816 18135 I Tag: text"));
        assertEquals(812, LogLines.pidOf("11-13 00:12:25.855   812   900 W Tag: text"));
        assertEquals(-1, LogLines.pidOf("--------- beginning of main"));
        assertEquals(-1, LogLines.pidOf("continuation of a message"));
    }

    @Test
    void onlyOwnProcessesAreKeptAndMasked() throws Exception {
        String in = "--------- beginning of main\n"
                + "11-13 00:12:25.855 17816 18135 I A: userId=12345\n"
                + "11-13 00:12:25.856  1813  1813 D System: other app serviceToken=zzz\n"
                + "11-13 00:12:25.857 18034 18034 I B: hello\n";
        StringWriter out = new StringWriter();
        LogLines.Result r = LogLines.filter(new BufferedReader(new StringReader(in)), out,
                new HashSet<>(Arrays.asList(17816, 18034)), LogLines.MAX_CHARS);
        assertEquals(2, r.kept);
        assertEquals(2, r.dropped);
        assertFalse(r.truncated);
        assertEquals("11-13 00:12:25.855 17816 18135 I A: userId=" + LogMasker.HIDDEN + "\n"
                + "11-13 00:12:25.857 18034 18034 I B: hello\n", out.toString());
    }

    @Test
    void outputIsCapped() throws Exception {
        StringBuilder in = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            in.append("11-13 00:12:25.855 100 100 I A: line ").append(i).append('\n');
        }
        StringWriter out = new StringWriter();
        LogLines.Result r = LogLines.filter(new BufferedReader(new StringReader(in.toString())), out,
                new HashSet<>(Arrays.asList(100)), 200);
        assertTrue(r.truncated);
        assertTrue(out.toString().length() <= 200);
        assertTrue(r.kept > 0);
    }
}
