package dev.pucky.util.logs;

public record LogEntry(long time, Logger.Level level, String message) {}
