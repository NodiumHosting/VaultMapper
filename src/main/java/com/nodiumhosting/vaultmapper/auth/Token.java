package com.nodiumhosting.vaultmapper.auth;

import org.apache.commons.io.IOUtils;

import java.io.*;

public class Token {
    private static final String tokenPath = "vaultmapper/";
    private static final String tokenName = "tok.ken";
    private static String tkn = "";

    static {
        readTokenFromFile();
    }

    public static boolean hasToken() {
        return tkn != null && !tkn.isEmpty();
    }

    public static String getToken() {
        return tkn;
    }

    public static void setToken(String token) {
        tkn = token;

        writeTokenToFile();
    }

    private static void readTokenFromFile() {
        prepareFiles();

        try {
            FileInputStream fis = new FileInputStream(tokenPath + tokenName);
            InputStreamReader reader = new InputStreamReader(fis);

            tkn = IOUtils.toString(reader);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeTokenToFile() {
        prepareFiles();

        try {
            FileOutputStream fos = new FileOutputStream(tokenPath + tokenName);
            OutputStreamWriter writer = new OutputStreamWriter(fos);

            writer.write(tkn);

            writer.close();
        } catch (Exception e) {
            System.out.println("couldn't write token");
        }
    }

    /**
     * Makes sure all files required for filesystem handling are ready to use
     */
    private static void prepareFiles() {
        try {
            File tokPath = new File(tokenPath);
            File tokFile = new File(tokenPath + tokenName);
            if (!tokPath.exists()) {
                tokPath.mkdirs();
            }

            if (!tokFile.exists()) {
                tokFile.createNewFile();
            }
        } catch (Exception e) {
            System.out.println("couldn't prepare token files");
        }
    }
}
