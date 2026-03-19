package org.opengameband.util;

import java.util.Locale;

/**
 * @author Zaprit
 */
public enum DownloadURLs {
    MAC("https://launcher.mojang.com/download/Minecraft.dmg", "Minecraft.dmg"),
    LIN("https://launcher.mojang.com/download/Minecraft.tar.gz", "Minecraft.tar.gz"),
    WIN("https://launcher.mojang.com/download/Minecraft.exe", "Minecraft.exe");

    private String url;
    private String file;

    DownloadURLs(String url, String file) {
        this.url = url;
        this.file = file;
    }

    public String getURL() {
        return url;
    }


    public String getFile() {
        return file;
    }

    public static DownloadURLs getOSDownloadURL() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (osName.contains("windows")) {
            return WIN;
        }
        if (osName.contains("mac")) {
            return MAC;
        }
        if (osName.contains("linux")) {
            return LIN;
        }
        return null;
    }
}
