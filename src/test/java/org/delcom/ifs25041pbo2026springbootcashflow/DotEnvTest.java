package org.delcom.ifs25041pbo2026springbootcashflow;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DotEnvTest {

    private static void assertParsed(String key, String value, String line) {
        Optional<Map.Entry<String, String>> parsed = DotEnv.parseLine(line);
        assertTrue(parsed.isPresent(), "baris: " + line);
        assertEquals(key, parsed.get().getKey());
        assertEquals(value, parsed.get().getValue());
    }

    @Test
    void parsesPlainAndDecoratedLines() {
        assertParsed("APP_PORT", "8080", "APP_PORT=8080");
        assertParsed("APP_PORT", "9090", "  APP_PORT = 9090  ");
        assertParsed("APP_PORT", "8080", "export APP_PORT=8080");
        assertParsed("NAME", "nilai dengan spasi", "NAME=\"nilai dengan spasi\"");
        assertParsed("NAME", "single", "NAME='single'");
        assertParsed("NAME", "nilai", "NAME=nilai # komentar");
        assertParsed("EMPTY", "", "EMPTY=");
        assertParsed("URL", "a=b=c", "URL=a=b=c");
    }

    @Test
    void stripsByteOrderMarkLeftByWindowsEditors() {
        assertParsed("APP_PORT", "8080", "﻿APP_PORT=8080");
    }

    @Test
    void ignoresCommentsBlankAndInvalidLines() {
        assertFalse(DotEnv.parseLine(null).isPresent());
        assertFalse(DotEnv.parseLine("").isPresent());
        assertFalse(DotEnv.parseLine("   ").isPresent());
        assertFalse(DotEnv.parseLine("# komentar").isPresent());
        assertFalse(DotEnv.parseLine("tanpa-pemisah").isPresent());
        assertFalse(DotEnv.parseLine("=tanpa-kunci").isPresent());
        assertFalse(DotEnv.parseLine("  = x").isPresent());
    }

    @Test
    void loadSetsSystemPropertiesButKeepsExistingOnes() throws IOException {
        String fresh = "CASHFLOW_TEST_FRESH_KEY";
        String existing = "CASHFLOW_TEST_EXISTING_KEY";
        Path file = Files.createTempFile("cashflow", ".env");
        try {
            System.setProperty(existing, "dari-sistem");
            Files.writeString(file, "﻿# komentar\n" + fresh + "=dari-env\n" + existing + "=dari-env\n",
                    StandardCharsets.UTF_8);

            DotEnv.load(file);

            assertEquals("dari-env", System.getProperty(fresh));
            assertEquals("dari-sistem", System.getProperty(existing));
        } finally {
            System.clearProperty(fresh);
            System.clearProperty(existing);
            Files.deleteIfExists(file);
        }
    }

    @Test
    void keepsMismatchedQuotesAsIs() {
        assertParsed("NAME", "\"abc", "NAME=\"abc");
        assertParsed("NAME", "'abc\"", "NAME='abc\"");
        assertParsed("NAME", "\"", "NAME=\"");
    }

    @Test
    void loadDoesNotOverrideRealEnvironmentVariables() throws IOException {
        String key = System.getenv().keySet().stream()
                .filter(k -> k.matches("[A-Za-z_][A-Za-z0-9_]*"))
                .findFirst()
                .orElse(null);
        Assumptions.assumeTrue(key != null, "tidak ada environment variable yang bisa dipakai");

        String before = System.getProperty(key);
        Path file = Files.createTempFile("cashflow", ".env");
        try {
            Files.writeString(file, key + "=dari-berkas\n", StandardCharsets.UTF_8);

            DotEnv.load(file);

            assertEquals(before, System.getProperty(key));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void loadIgnoresMissingFileAndNullPath() {
        assertDoesNotThrow(() -> DotEnv.load(Path.of("tidak-ada-" + System.nanoTime() + ".env")));
        assertDoesNotThrow(() -> DotEnv.load((Path) null));
        assertNull(System.getProperty("TIDAK_PERNAH_ADA_CASHFLOW"));
    }

    @Test
    void loadIgnoresUnreadableFile() throws IOException {
        Path file = Files.createTempFile("cashflow", ".env");
        try {
            // Byte tidak valid UTF-8 -> MalformedInputException (subclass IOException), harus ditelan.
            Files.write(file, new byte[]{(byte) 0xC3, (byte) 0x28, '\n'});
            assertDoesNotThrow(() -> DotEnv.load(file));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    void loadWithoutArgumentReadsDefaultFileSafely() {
        String before = System.getProperty("APP_PORT");
        try {
            assertDoesNotThrow(() -> DotEnv.load());
        } finally {
            if (before == null) {
                System.clearProperty("APP_PORT");
            }
        }
    }
}
