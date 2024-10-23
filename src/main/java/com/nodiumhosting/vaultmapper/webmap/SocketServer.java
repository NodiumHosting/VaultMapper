package com.nodiumhosting.vaultmapper.webmap;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.config.ClientConfig;
import com.nodiumhosting.vaultmapper.map.VaultCell;
import net.minecraft.world.entity.projectile.Arrow;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Base64;
import java.util.zip.GZIPOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SocketServer extends WebSocketServer {
    public SocketServer(InetSocketAddress address) {
        super(address);
        wslist = new ArrayList<>();
    }

    int WEBMAP_VERSION = 2;
    ArrayList<WebSocket> wslist;

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        wslist.add(conn);

        conn.send("version:"+WEBMAP_VERSION);
        sendConfig();
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        wslist.remove(conn);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {

    }

    @Override
    public void onError(WebSocket conn, Exception ex) {

    }

    @Override
    public void onStart() {
        VaultMapper.LOGGER.info("Started Socket Server");
    }

    public void sendConfig() {
        try {
//                 Gson serialize -> byte array -> gzip -> base64 encode
            Gson gson = new Gson();
            String json = gson.toJson(new ClientConfigObject());

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzos = new GZIPOutputStream(baos);
            gzos.write(json.getBytes("UTF-8"));
            gzos.close();
            byte[] compressed = baos.toByteArray();
            String base64 = Base64.getEncoder().encodeToString(compressed);

            wslist.forEach((conn) -> {
                conn.send("config|" + base64);
            });
        } catch (IOException e) {
            VaultMapper.LOGGER.error("Failed to send config data to webmap");
        }
    }

    class ClientConfigObject { // I'm sure this could be done better but I have no more fucks left to give
        public String POINTER_COLOR;
        public String ROOM_COLOR;
        public String START_ROOM_COLOR;
        public String MARKED_ROOM_COLOR;
        public String INSCRIPTION_ROOM_COLOR;
        public String OMEGA_ROOM_COLOR;
        public String CHALLENGE_ROOM_COLOR;
        public boolean SHOW_INSCRIPTIONS;
        public boolean SHOW_ROOM_ICONS;

        public ClientConfigObject() {
            this.POINTER_COLOR = ClientConfig.POINTER_COLOR.get();
            this.ROOM_COLOR = ClientConfig.ROOM_COLOR.get();
            this.START_ROOM_COLOR = ClientConfig.START_ROOM_COLOR.get();
            this.MARKED_ROOM_COLOR = ClientConfig.MARKED_ROOM_COLOR.get();
            this.INSCRIPTION_ROOM_COLOR = ClientConfig.INSCRIPTION_ROOM_COLOR.get();
            this.OMEGA_ROOM_COLOR = ClientConfig.OMEGA_ROOM_COLOR.get();
            this.CHALLENGE_ROOM_COLOR = ClientConfig.CHALLENGE_ROOM_COLOR.get();
            this.SHOW_INSCRIPTIONS = ClientConfig.SHOW_INSCRIPTIONS.get();
            this.SHOW_ROOM_ICONS = ClientConfig.SHOW_ROOM_ICONS.get();
        }
    }

    public void sendCell(VaultCell cell) {
        try {
            // Gson serialize -> byte array -> gzip -> base64 encode
            Gson gson = new Gson();
            String json = gson.toJson(cell);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzos = new GZIPOutputStream(baos);
            gzos.write(json.getBytes("UTF-8"));
            gzos.close();
            byte[] compressed = baos.toByteArray();
            String base64 = Base64.getEncoder().encodeToString(compressed);

            wslist.forEach((conn) -> {
                conn.send("cell|" + base64);
            });
        } catch (IOException e) {
            VaultMapper.LOGGER.error("Failed to send cell data to webmap");
        }
    }

    public void sendArrow(int x, int z, float yaw, String username, String color) {
        Arrow arrow = new Arrow(x, z, yaw, username, color);

        try {
            // Gson serialize -> byte array -> gzip -> base64 encode
            Gson gson = new Gson();
            String json = gson.toJson(arrow);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzos = new GZIPOutputStream(baos);
            gzos.write(json.getBytes("UTF-8"));
            gzos.close();
            byte[] compressed = baos.toByteArray();
            String base64 = Base64.getEncoder().encodeToString(compressed);

            wslist.forEach((conn) -> {
                conn.send("arrow|" + base64);
            });
        } catch (IOException e) {
            VaultMapper.LOGGER.error("Failed to send cell data to webmap");
        }
    }

    static class Arrow {
        int x;
        int z;
        @SerializedName("y")
        float yaw;
        @SerializedName("u")
        String username;
        @SerializedName("c")
        String color;

        public Arrow(int x, int z, float yaw, String username, String color) {
            this.x = x;
            this.z = z;
            this.yaw = yaw;
            this.username = username;
            this.color = color;
        }
    }

    public void sendReset() {
        wslist.forEach((conn) -> {
            conn.send("reset");
        });
    }
}
