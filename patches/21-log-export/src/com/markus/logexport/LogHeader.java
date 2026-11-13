package com.markus.logexport;

/**
 * The first lines of an exported text log: what was recorded, on what. Plain Java so it is tested.
 * Files start with a UTF-8 byte order mark: Windows editors that guess the code page otherwise show the Russian text
 * as «РЎРєСЂ…».
 */
public final class LogHeader {
    public static final String BOM = "﻿";

    private LogHeader() {
    }

    /**
     * @param modVersion "3.60.0i, сборка мода 4"
     * @param band       "miwear.watch.q66tc, прошивка 4.200.078" or null when no band is bound
     * @param phone      "OnePlus CPH2581, Android 16 (API 36)"
     * @param period     "с 11-13 12:00:00 до 11-13 12:10:00" or "последние 15 минут, с 11-13 11:59:50"
     * @param processes  how many processes of the app were followed
     */
    public static String build(String modVersion, String band, String phone, String period, int processes) {
        StringBuilder sb = new StringBuilder(BOM);
        sb.append("# Журнал приложения Mi Fitness (мод)\n");
        sb.append("# Версия: ").append(orDash(modVersion)).append('\n');
        sb.append("# Браслет: ").append(band == null || band.trim().isEmpty() ? "не привязан" : band).append('\n');
        sb.append("# Телефон: ").append(orDash(phone)).append('\n');
        sb.append("# Время: ").append(orDash(period)).append(", процессов приложения: ").append(processes).append('\n');
        sb.append("# Токены, идентификаторы аккаунта и устройства, серийные номера, MAC, e-mail и телефоны заменены на ")
                .append(LogMasker.HIDDEN).append(", ").append(LogMasker.MAC).append(", ").append(LogMasker.EMAIL)
                .append(", ").append(LogMasker.PHONE).append(". Строки мода — с тегом MOD.\n");
        sb.append("# Отправляйте только в личные сообщения.\n\n");
        return sb.toString();
    }

    private static String orDash(String s) {
        return s == null || s.trim().isEmpty() ? "—" : s;
    }
}
