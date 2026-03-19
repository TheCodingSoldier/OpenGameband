package org.opengameband.launcher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.opengameband.util.MountPoint;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BasicLauncherPathsTest {

    @Test
    void launcherDirUsesMountPointAsParent() {
        BasicLauncher launcher = new BasicLauncher();
        File mountPoint = MountPoint.GetMountPoint();

        assertEquals(new File(mountPoint, "Launchers/Official").getAbsolutePath(), launcher.getLauncherDir().getAbsolutePath());
    }

    @Test
    void windowsInstallDirIsRelativeToLauncherDir() {
        String originalOsName = System.getProperty("os.name");
        try {
            System.setProperty("os.name", "Windows 11");
            BasicLauncher launcher = new BasicLauncher();

            assertEquals(new File(launcher.getLauncherDir(), "win").getAbsolutePath(), launcher.getInstallDir().getAbsolutePath());
        } finally {
            System.setProperty("os.name", originalOsName);
        }
    }

    @Test
    void mountPointResolvesToDirectory() {
        File mountPoint = MountPoint.GetMountPoint();
        assertTrue(mountPoint.exists());
        assertTrue(mountPoint.isDirectory());
    }

    @Test
    void resolveMountedMinecraftAppParsesVolumePath() {
        String hdiutilOutput = """
                /dev/disk4\tGUID_partition_scheme
                /dev/disk4s1\tApple_HFS\t/Volumes/Minecraft Installer\t(extra metadata)
                """;

        Path mountedPath = BasicLauncher.resolveMountedMinecraftApp(hdiutilOutput);

        assertEquals(Paths.get("/Volumes/Minecraft Installer", "Minecraft.app"), mountedPath);
    }

    @Test
    void resolveMountedMinecraftAppReturnsNullWithoutVolume() {
        assertNull(BasicLauncher.resolveMountedMinecraftApp("/dev/disk4\tGUID_partition_scheme\n"));
    }

    @Test
    void resolveInstalledMacAppBundleFindsAnyAppBundleWhenMinecraftAppMissing(@TempDir Path tempDir) throws IOException {
        Files.createDirectory(tempDir.resolve("Minecraft Launcher.app"));

        Path resolved = BasicLauncher.resolveInstalledMacAppBundle(tempDir.resolve("Minecraft.app"));

        assertEquals(tempDir.resolve("Minecraft Launcher.app"), resolved);
    }
}
