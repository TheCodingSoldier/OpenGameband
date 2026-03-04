package org.opengameband.exceptions;

public class LauncherFailiure extends Exception {
    public LauncherFailiure() {
        super();
    }

    public LauncherFailiure(String message) {
        super(message);
    }

    public LauncherFailiure(Throwable cause) {
        super(cause);
    }
}
