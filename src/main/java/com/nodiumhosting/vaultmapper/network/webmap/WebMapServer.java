package com.nodiumhosting.vaultmapper.network.webmap;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.config.ClientConfig;
import com.nodiumhosting.vaultmapper.map.VaultCell;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class WebMapServer extends WebSocketServer {
    int WEBMAP_VERSION = 3;
    ArrayList<WebSocket> wslist;

    public WebMapServer(InetSocketAddress address) {
        super(address);
        wslist = new ArrayList<>();
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        wslist.add(conn);

        conn.send("version|" + WEBMAP_VERSION);
        sendConfig();

        if (!VaultMap.enabled) {
            sendReset();
            return;
        }

        //send all cells
        List<VaultCell> cells = VaultMap.getCells();
        for (VaultCell cell : cells) {
            sendCell(cell);
        }

        VaultCell current = VaultMap.getCurrentCell();
        Player player = Minecraft.getInstance().player;
        if (current != null) {
            sendArrow(current.x, current.z, player.getYHeadRot(), player.getName().getString(), ClientConfig.POINTER_COLOR.get());
        }
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
        wslist.remove(conn);
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
            gzos.write(json.getBytes(StandardCharsets.UTF_8));
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

    public void sendCell(VaultCell cell) {
        try {
            // Gson serialize -> byte array -> gzip -> base64 encode
            Gson gson = new Gson();
            String json = gson.toJson(cell);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzos = new GZIPOutputStream(baos);
            gzos.write(json.getBytes(StandardCharsets.UTF_8));
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

    public void sendArrow(int x, int z, float yaw, String uuid, String color) {
        Arrow arrow = new Arrow(x, z, yaw, uuid, color);

        try {
            // Gson serialize -> byte array -> gzip -> base64 encode
            Gson gson = new Gson();
            String json = gson.toJson(arrow);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzos = new GZIPOutputStream(baos);
            gzos.write(json.getBytes(StandardCharsets.UTF_8));
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

    public void removeArrow(String uuid) {
        wslist.forEach((conn) -> {
            conn.send("removearrow|" + uuid);
        });
    }

    public void sendReset() {
        wslist.forEach((conn) -> {
            conn.send("reset");
        });
    }

    static class Arrow {
        int x;
        int z;
        @SerializedName("y")
        float yaw;
        @SerializedName("u")
        String uuid;
        @SerializedName("c")
        String color;

        public Arrow(int x, int z, float yaw, String uuid, String color) {
            this.x = x;
            this.z = z;
            this.yaw = yaw;
            this.uuid = uuid;
            this.color = color;
        }
    }

    class ClientConfigObject { // I'm sure this could be done better but I have no more fucks left to give
        public String ROOM_COLOR;
        public String START_ROOM_COLOR;
        public String MARKED_ROOM_COLOR;
        public String INSCRIPTION_ROOM_COLOR;
        public String OMEGA_ROOM_COLOR;
        public String CHALLENGE_ROOM_COLOR;
        public String ORE_ROOM_COLOR;
        public boolean SHOW_INSCRIPTIONS;
        public boolean SHOW_ROOM_ICONS;

        public ClientConfigObject() {
            this.ROOM_COLOR = ClientConfig.ROOM_COLOR.get();
            this.START_ROOM_COLOR = ClientConfig.START_ROOM_COLOR.get();
            this.MARKED_ROOM_COLOR = ClientConfig.MARKED_ROOM_COLOR.get();
            this.INSCRIPTION_ROOM_COLOR = ClientConfig.INSCRIPTION_ROOM_COLOR.get();
            this.OMEGA_ROOM_COLOR = ClientConfig.OMEGA_ROOM_COLOR.get();
            this.CHALLENGE_ROOM_COLOR = ClientConfig.CHALLENGE_ROOM_COLOR.get();
            this.ORE_ROOM_COLOR = ClientConfig.ORE_ROOM_COLOR.get();
            this.SHOW_INSCRIPTIONS = ClientConfig.SHOW_INSCRIPTIONS.get();
            this.SHOW_ROOM_ICONS = ClientConfig.SHOW_ROOM_ICONS.get();
        }
    }
}
