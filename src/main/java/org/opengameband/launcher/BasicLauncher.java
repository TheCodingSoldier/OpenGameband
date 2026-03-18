package org.opengameband.launcher;

import org.opengameband.Main;
import org.opengameband.exceptions.LauncherFailiure;
import org.opengameband.util.DownloadURLs;
import org.opengameband.util.Downloader;
import org.opengameband.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.opengameband.util.MountPoint.GetMountPoint;

/**
 * @author Zaprit
 * This is a launcher that just gets the launcher from Minecraft.net
 */
public class BasicLauncher implements Launcher {

    public BasicLauncher() {
    }

    private String getOSName() {
        return System.getProperty("os.name").toLowerCase();
    }

    @Override
    public void start() throws LauncherFailiure {
        File installDir = getInstallDir();
        File gameDataDir = getGameDataDir();
        if (installDir == null || gameDataDir == null) {
            throw new LauncherFailiure();
        }
        try {
            String osName = getOSName();
            if (osName.startsWith("mac")) {
                Runtime.getRuntime().exec(new String[]{new File(installDir, "Contents/MacOS/launcher").getAbsolutePath(),
                        "--workDir", gameDataDir.getAbsolutePath()});
            } else if (osName.startsWith("windows")) {
                Runtime.getRuntime().exec(new String[]{new File(installDir, "Minecraft.exe").getAbsolutePath(),
                        "--workDir", gameDataDir.getAbsolutePath()});
            } else {
                throw new LauncherFailiure();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            throw new LauncherFailiure();
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
        String osName = getOSName();
        if (osName.startsWith("mac")) {
                try {
                    Process p = Runtime.getRuntime().exec(new String[]{"hdiutil", "attach", file});
                    byte[] inputBytes = p.getInputStream().readAllBytes();
                    String stdin = new String(inputBytes);
                    System.out.println(stdin);
                    Path mountedMinecraftApp = resolveMountedMinecraftApp(stdin);
                    if (mountedMinecraftApp == null) {
                        throw new IOException("Could not find mounted Minecraft.app path");
                    }
                    Thread.sleep(1000);
                    FileUtils.CopyDir(mountedMinecraftApp, getInstallDir().toPath());

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();

                }
        } else if (osName.startsWith("windows")) {
            // Windows launcher is downloaded as an executable and does not require extraction.
        } else if (osName.startsWith("linux")) {
            throw new UnsupportedOperationException("Linux launcher extraction is not yet implemented");
        }
    }

    static Path resolveMountedMinecraftApp(String hdiutilOutput) {
        for (String line : hdiutilOutput.split("\\R")) {
            for (String field : line.split("\\t")) {
                String trimmedField = field.trim();
                if (trimmedField.startsWith("/Volumes/")) {
                    return Paths.get(trimmedField, "Minecraft.app");
                }
            }
        }
        return null;
    }

    @Override
    public File getLauncherDir() {
        return new File(GetMountPoint(), "Launchers/Official");
    }

    /**
     * @return The launcher location or null if the working dir is missing.
     */
    @Override
    public File getInstallDir() {
        if (GetMountPoint().exists() && GetMountPoint().isDirectory() && GetMountPoint().canRead()) {
            String osName = getOSName();
            if (osName.startsWith("windows")) {
                return new File(getLauncherDir(), "win");
            }
            if (osName.startsWith("mac")) {
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
        File[] files = installDir == null ? null : installDir.listFiles();
        return installDir != null && installDir.exists() && files != null && files.length > 0;
    }
}
