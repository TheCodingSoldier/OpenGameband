package org.opengameband.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
            assertNotNull(DownloadURLs.getOSDownloadURL());
        } finally {
            System.setProperty("os.name", originalOsName);
        }
    }
}
