package org.opengameband.util;

import java.io.File;
import java.net.URISyntaxException;

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
            File codeSourceLocation = new File(MountPoint.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            if (codeSourceLocation.isFile()) {
                return codeSourceLocation.getParentFile();
            }
            return codeSourceLocation;
        } catch (URISyntaxException e) {
            return new File(MountPoint.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        }
    }
}
