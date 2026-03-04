package org.opengameband.launcher;

import org.opengameband.*;
import org.opengameband.exceptions.LauncherFailiure;
import org.opengameband.util.DownloadURLs;
import org.opengameband.util.Downloader;
import org.opengameband.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.opengameband.util.MountPoint.GetMountPoint;

/**
 * @author Zaprit
 * This is a launcher that just gets the launcher from Minecraft.net
 */
public class BasicLauncher implements Launcher {

    public BasicLauncher() {
    }

    @Override
    public void start() throws LauncherFailiure {
        File installDir = getInstallDir();
        File gameDataDir = getGameDataDir();
        if (installDir == null || gameDataDir == null) {
            throw new LauncherFailiure("Launcher directories are not available");
        }
        try {
            switch (getOsFamily()) {
                case "Mac": {
                    Runtime.getRuntime().exec(new String[]{new File(installDir, "Contents/MacOS/launcher").getAbsolutePath(),
                            "--workDir", getGameDataDir().getAbsolutePath()});
                    break;
                }
                case "Windows": {
                    Runtime.getRuntime().exec(new String[]{new File(installDir, "Minecraft.exe").getAbsolutePath(),
                            "--workDir", getGameDataDir().getAbsolutePath()});
                    break;
                }
                default:
                    throw new LauncherFailiure("Unsupported OS: " + System.getProperty("os.name"));
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new LauncherFailiure(e);
        }
    }

    @Override
    public String getName() {
        return "Minecraft Official Launcher";
    }

    @Override
    public void install() {

        if (!getInstallDir().mkdirs() && !getInstallDir().exists()) {
            System.out.println("Not all subdirectories were created, some directories may already exist, or there might be permissions issues");
        }
        System.out.println("Downloading launcher from " + Objects.requireNonNull(DownloadURLs.getOSDownloadURL()).getURL());
        Downloader downloader = new Downloader(this::extract, DownloadURLs.getOSDownloadURL().getURL(), new File(getInstallDir(), DownloadURLs.getOSDownloadURL().getFile()).getAbsolutePath());
        downloader.addPropertyChangeListener(Main.getWindow());
        downloader.execute();
    }

    private void extract(String file) {
        switch (getOsFamily()) {
            case "Mac":
                try {
                    Process p = Runtime.getRuntime().exec("hdiutil attach " + file);
                    byte[] inputBytes = p.getInputStream().readAllBytes();
                    String stdin = new String(inputBytes);
                    System.out.println(stdin);
                    System.out.println(stdin.substring(stdin.indexOf("/dev/disk")));
                    Thread.sleep(1000);
                    FileUtils.CopyDir(Paths.get("/Volumes/Minecraft/Minecraft.app"), getInstallDir().toPath());

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();

                }
                break;
            case "Linux":
                throw new RuntimeException("Not Yet Implemented");
            default:
                break;
        }
    }

    @Override
    public File getLauncherDir() {
        return Path.of(GetMountPoint().getAbsolutePath(), "Launchers", "Official").toFile();
    }

    /**
     * @return The launcher location or null if the working dir is missing.
     */
    @Override
    public File getInstallDir() {
        if (GetMountPoint().exists() && GetMountPoint().isDirectory() && GetMountPoint().canRead()) {
            switch (getOsFamily()) {
                case "Windows":
                    return new File(getLauncherDir(), "win");
                case "Mac":
                    return new File(getLauncherDir(), "Minecraft.app");
            }
        }
        return null;
    }

    @Override
    public File getGameDataDir() {
        if (GetMountPoint().exists() && GetMountPoint().isDirectory()) {
            return new File(getLauncherDir(), "Game");
        }
        return null;
    }

    @Override
    public boolean isInstalled() {
        File installDir = getInstallDir();
        return installDir != null && installDir.exists() && installDir.listFiles() != null && installDir.listFiles().length > 0;
    }

    private String getOsFamily() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            return "Windows";
        }
        if (os.contains("mac")) {
            return "Mac";
        }
        if (os.contains("linux")) {
            return "Linux";
        }
        return "Unknown";
    }
}
