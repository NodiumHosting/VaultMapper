package com.nodiumhosting.vaultmapper.util;

import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.gui.ToastMessageManager;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class UpdateChecker {
    public static final String CURRENT_VERSION = VaultMapper.getVersion();
    public static final String LATEST_VERSION = getLatestVersion();
    public static final boolean IS_RUNNING_LATEST = isLatestVersion();

    public static void checkForUpdates() {
//        ToastMessageManager.displayToast("Checking for updates...");
//
//        if (isLatestVersion()) {
//            return;
//        }
//
//        ToastMessageManager.displayToast("New version of VaultMapper available! Your version: " + CURRENT_VERSION + ", Latest version: " + LATEST_VERSION);
    }

    private static String getLatestVersion() {
        try {
            URL api = new URL("https://vmver.ndmh.xyz/");
            URLConnection apiCon = api.openConnection();
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            apiCon.getInputStream()));

            String ver = in.readLine();

            VaultMapper.LOGGER.info("Latest version: " + ver);

            return ver;
        } catch (Exception e) {
            VaultMapper.LOGGER.error("Error checking for updates: " + e.getMessage());
        }

        return "Unknown Latest Version";
    }

    private static boolean isLatestVersion() {
        VaultMapper.LOGGER.info("Checking whether " + CURRENT_VERSION + " is " + LATEST_VERSION);
        return CURRENT_VERSION.equals(LATEST_VERSION);
    }
}