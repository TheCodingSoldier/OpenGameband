package org.opengameband.util;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Zaprit
 */
public class MountPoint {
    public MountPoint() {
    }

    /**
     * This gets the working directory of OpenGameband
     * TODO: Add the ability to change this as it may be needed later
     */
    public static File GetMountPoint(){
        try {
            URI codeLocation = MountPoint.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            Path path = Paths.get(codeLocation);
            File location = path.toFile();
            if (location.isFile()) {
                return location.getParentFile();
            }
            return location;
        } catch (Exception e) {
            return new File(System.getProperty("user.dir"));
        }
    }
}
