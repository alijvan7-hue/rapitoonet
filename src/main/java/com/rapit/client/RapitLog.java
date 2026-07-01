package com.rapit.client;

import java.util.logging.Logger;

/**
 * Thin wrapper around java.util.logging so every part of the client
 * logs under a single consistent "RapitClient" name instead of
 * scattering System.out.println calls everywhere.
 */
public final class RapitLog {

    private static final Logger LOGGER = Logger.getLogger("RapitClient");

    private RapitLog() {
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void warn(String message) {
        LOGGER.warning(message);
    }

    public static void error(String message, Throwable t) {
        LOGGER.severe(message + " - " + t);
    }
}
