package org.opengameband.launcher;

import org.junit.jupiter.api.Test;
import org.opengameband.util.MountPoint;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
