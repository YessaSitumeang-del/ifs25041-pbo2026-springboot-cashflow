package org.delcom.ifs25041pbo2026springbootcashflow;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Memuat berkas {@code .env} (format {@code KEY=VALUE}) menjadi system property
 * agar bisa dibaca Spring lewat placeholder seperti {@code ${APP_PORT:8080}}.
 *
 * <p>Environment variable asli dan system property yang sudah ada tidak ditimpa.</p>
 */
public final class DotEnv {

    private static final Logger LOGGER = Logger.getLogger(DotEnv.class.getName());
    private static final String DEFAULT_FILE = ".env";

    private DotEnv() {
    }

    /** Memuat {@code .env} dari direktori kerja saat ini (diabaikan jika tidak ada). */
    public static void load() {
        load(Path.of(DEFAULT_FILE));
    }

    /** Memuat berkas env tertentu (diabaikan jika tidak ada atau tidak bisa dibaca). */
    public static void load(Path file) {
        if (file == null || !Files.isRegularFile(file)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (String line : lines) {
                parseLine(line).ifPresent(entry -> apply(entry.getKey(), entry.getValue()));
            }
        } catch (IOException ex) {
            LOGGER.log(Level.WARNING, "Gagal membaca berkas env: " + file, ex);
        }
    }

    /** Mem-parse satu baris env. Mengembalikan kosong untuk baris komentar/kosong/tidak valid. */
    static Optional<Map.Entry<String, String>> parseLine(String rawLine) {
        if (rawLine == null) {
            return Optional.empty();
        }
        // Notepad di Windows dapat menyimpan BOM di awal berkas.
        String line = rawLine.replace("﻿", "").trim();
        if (line.isEmpty() || line.startsWith("#")) {
            return Optional.empty();
        }
        if (line.startsWith("export ")) {
            line = line.substring("export ".length()).trim();
        }
        int separator = line.indexOf('=');
        if (separator <= 0) {
            return Optional.empty();
        }
        String key = line.substring(0, separator).trim();
        if (key.isEmpty()) {
            return Optional.empty();
        }
        String value = cleanValue(line.substring(separator + 1).trim());
        return Optional.of(new AbstractMap.SimpleImmutableEntry<>(key, value));
    }

    private static String cleanValue(String value) {
        if (value.length() >= 2) {
            char first = value.charAt(0);
            char last = value.charAt(value.length() - 1);
            if ((first == '"' || first == '\'') && first == last) {
                return value.substring(1, value.length() - 1);
            }
        }
        int comment = value.indexOf(" #");
        return comment >= 0 ? value.substring(0, comment).trim() : value;
    }

    private static void apply(String key, String value) {
        if (System.getenv(key) == null && System.getProperty(key) == null) {
            System.setProperty(key, value);
        }
    }
}
