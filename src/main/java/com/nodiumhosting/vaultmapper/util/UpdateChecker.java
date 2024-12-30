package com.nodiumhosting.vaultmapper.util;

import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.gui.ToastMessageManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateChecker {
    public static void checkForUpdates() {
        ToastMessageManager.displayToast("Checking for updates...", 5000, 5, 5);

        if (isLatestVersion()) {
            return;
        }

        ToastMessageManager.displayToast("New version of VaultMapper available! Your version: " + CURRENT_VERSION + ", Latest version: " + LATEST_VERSION, 10000, 5, 5);

        System.out.println("There is a new version of VaultMapper available! Your version: " + CURRENT_VERSION + ", Latest version: " + LATEST_VERSION);
    }

    public static final String CURRENT_VERSION = VaultMapper.getVersion();
    public static final String LATEST_VERSION = getLatestVersion();

    private static String getLatestVersion() {
        try {
            URL api = new URL("http://version.vaultmapper.site/");
            URLConnection apiCon = api.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            apiCon.getInputStream()));

            return in.readLine();
        } catch (Exception e) {
            VaultMapper.LOGGER.error("Error checking for updates: " + e.getMessage());
        }

        return "Unknown Latest Version";
    }

    public static boolean isLatestVersion() {
        return CURRENT_VERSION.equals(LATEST_VERSION);
    }
}