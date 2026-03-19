package org.opengameband.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

class DownloadURLsTest {

    @Test
    void downloadFilenamesAreRelativePaths() {
        assertFalse(DownloadURLs.MAC.getFile().startsWith("/"));
        assertFalse(DownloadURLs.LIN.getFile().startsWith("/"));
        assertFalse(DownloadURLs.WIN.getFile().startsWith("/"));
        assertFalse(DownloadURLs.WIN.getFile().startsWith("\\"));
    }

    @Test
    void windowsOsNameResolvesToWindowsDownload() {
        String originalOsName = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Windows 11");
            assertEquals(DownloadURLs.WIN, DownloadURLs.getOSDownloadURL());
        } finally {
            System.setProperty("os.name", originalOsName);
        }
    }

    @Test
    void macAndLinuxOsNamesResolveCorrectly() {
        String originalOsName = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Mac OS X");
            assertEquals(DownloadURLs.MAC, DownloadURLs.getOSDownloadURL());

            System.setProperty("os.name", "Linux");
            assertEquals(DownloadURLs.LIN, DownloadURLs.getOSDownloadURL());
        } finally {
            System.setProperty("os.name", originalOsName);
        }
    }

    @Test
    void unknownOsNameReturnsNull() {
        String originalOsName = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Plan9");
            assertNull(DownloadURLs.getOSDownloadURL());
        } finally {
            System.setProperty("os.name", originalOsName);
        }
    }

    @Test
    void missingOsNameReturnsNullInsteadOfThrowing() {
        String originalOsName = System.getProperty("os.name");
        try {
            System.clearProperty("os.name");
            assertNull(DownloadURLs.getOSDownloadURL());
        } finally {
            if (originalOsName == null) {
                System.clearProperty("os.name");
            } else {
                System.setProperty("os.name", originalOsName);
            }
        }
    }
}
