package dev.pucky.util.logs;

import dev.pucky.util.MMUtil;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.ArrayDeque;
import java.util.Deque;

public class Logger {
    @Getter private static final Deque<LogEntry> logHistory = new ArrayDeque<>();
    @Getter private static final int maxLogs = 200;

    @Getter
    public enum Level {
        SUCCESS("<dark_green>", "SUCCESS", "<s>Başarılı İşlem"),
        FAIL("<red>", "FAIL", "<#993535>Hata"),
        FATAL("<dark_red>", "FATAL", "<#722626>Kritik Hata"),
        WARN("<yellow>", "WARN", "<n>Uyarı"),
        INFO("<blue>", "INFO", "<#362672>Bilgilendirme");

        private final String color;
        private final String label;
        private final String name;

        Level(String color, String label, String name) {
            this.color = color;
            this.label = label;
            this.name = name;
        }
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(MMUtil.deserialize(message));
    }

    private static void write(Level level, String message) {
        logHistory.addFirst(new LogEntry(System.currentTimeMillis(), level, message));
        if (logHistory.size() > maxLogs) logHistory.removeLast();

        log("<yellow>[PDungeon] " + level.color + "[" + level.label + "] <reset>" + message);
    }

    public static void success(String message) {
        write(Level.SUCCESS, message);
    }

    public static void fail(String message) {
        write(Level.FAIL, message);
    }

    public static void fatal(String message) {
        write(Level.FATAL, message);
    }

    public static void warn(String message) {
        write(Level.WARN, message);
    }

    public static void inform(String message) {
        write(Level.INFO, message);
    }
}
