package org.opengameband.launcher;

import org.opengameband.Main;
import org.opengameband.exceptions.LauncherFailiure;
import org.opengameband.util.DownloadURLs;
import org.opengameband.util.Downloader;
import org.opengameband.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
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
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
    }

    @Override
    public void start() throws LauncherFailiure {
        File installDir = getInstallDir();
        File gameDataDir = getGameDataDir();
        if (installDir == null || gameDataDir == null) {
            throw new LauncherFailiure();
        }
        if (!gameDataDir.exists() && !gameDataDir.mkdirs()) {
            throw new LauncherFailiure();
        }
        try {
            String osName = getOSName();
            ProcessBuilder processBuilder;
            if (osName.startsWith("mac")) {
                processBuilder = new ProcessBuilder(
                        new File(installDir, "Contents/MacOS/launcher").getAbsolutePath(),
                        "--workDir", gameDataDir.getAbsolutePath()
                );
            } else if (osName.startsWith("windows")) {
                processBuilder = new ProcessBuilder(
                        new File(installDir, "Minecraft.exe").getAbsolutePath(),
                        "--workDir", gameDataDir.getAbsolutePath()
                );
            } else {
                throw new LauncherFailiure();
            }
            processBuilder.directory(installDir);
            processBuilder.start();
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
        File installDir = getInstallDir();
        if (installDir == null) {
            return;
        }
        if (!installDir.mkdirs() && !installDir.exists()) {
            System.out.println("Not all subdirectories were created, some directories may already exist, or there might be permissions issues");
        }
        DownloadURLs downloadURL = Objects.requireNonNull(DownloadURLs.getOSDownloadURL());
        System.out.println("Downloading launcher from " + downloadURL.getURL());
        Downloader downloader = new Downloader(this::extract, downloadURL.getURL(), new File(installDir, downloadURL.getFile()).getAbsolutePath());
        downloader.addPropertyChangeListener(Main.getWindow());
        downloader.execute();
    }

    private void extract(String file) {
        String osName = getOSName();
        if (osName.startsWith("mac")) {
                try {
                    Path downloadedImage = Paths.get(file);
                    String downloadedImageName = downloadedImage.getFileName() == null ? "" : downloadedImage.getFileName().toString().toLowerCase();
                    if (!Files.isRegularFile(downloadedImage) || !downloadedImageName.endsWith(".dmg")) {
                        throw new IOException("Invalid DMG file: " + file);
                    }
                    Process p = Runtime.getRuntime().exec(new String[]{"hdiutil", "attach", file});
                    byte[] inputBytes = p.getInputStream().readAllBytes();
                    String stdin = new String(inputBytes);
                    System.out.println(stdin);
                    Path mountedMinecraftApp = resolveMountedMinecraftApp(stdin);
                    if (mountedMinecraftApp == null) {
                        throw new IOException("Could not find mounted Minecraft.app path");
                    }
                    mountedMinecraftApp = resolveInstalledMacAppBundle(mountedMinecraftApp);
                    if (mountedMinecraftApp == null) {
                        throw new IOException("Could not find a .app bundle in mounted volume");
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

    static Path resolveInstalledMacAppBundle(Path preferredMinecraftApp) {
        if (preferredMinecraftApp.toFile().exists()) {
            return preferredMinecraftApp;
        }

        Path parentPath = preferredMinecraftApp.getParent();
        if (parentPath == null) {
            return null;
        }
        File mountDir = parentPath.toFile();
        if (!mountDir.isDirectory()) {
            return null;
        }
        File[] appBundles = mountDir.listFiles((dir, name) -> name.endsWith(".app"));
        if (appBundles == null || appBundles.length == 0) {
            return null;
        }
        return appBundles[0].toPath();
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
        File mountPoint = GetMountPoint();
        if (mountPoint.exists() && mountPoint.isDirectory() && mountPoint.canRead()) {
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
        File mountPoint = GetMountPoint();
        if (mountPoint.exists() && mountPoint.isDirectory() && mountPoint.canRead()) {
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
