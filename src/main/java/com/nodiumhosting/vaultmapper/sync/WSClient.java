package com.nodiumhosting.vaultmapper.sync;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.config.ClientConfig;
import com.nodiumhosting.vaultmapper.map.VaultCell;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class WSClient extends WebSocketClient {
    private final static String relayAddress = ClientConfig.SYNC_SERVER.get();
    private final int timerPeriod = 10000;
    private final Timer keepConnectedTimer = new Timer();
    private final WSClient self;
    MovePacket old_data = new MovePacket("", "", 0, 0, 0);
    private boolean keepMeOn = false;


    public WSClient(String playerUUID, String vaultID) {
        super(URI.create(relayAddress + "/" + vaultID + "/" + playerUUID));

        self = this;

        keepConnectedTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (keepMeOn) { // if connected
                    if (self.isOpen()) { // if socket is open
                        sendKeepalive();
                    } else {
                        self.reconnect(); // if socket closed, try to reconnect non-blocking
                    }
                }
            }
        }, timerPeriod, timerPeriod);
    }

    public void sendKeepalive() {
        if (this.isClosing() || this.isClosed()) {
            Logger.getAnonymousLogger().info("Can't send keep-alive, socket is closed.");
        }
        //this.send("[\"keep_me_alive\"]");
        this.sendPing();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        Logger.getAnonymousLogger().info("CONNECTED!");
        keepMeOn = true;
    }

    @Override
    public void onMessage(String message) {
        Logger.getAnonymousLogger().info(message);
        var x = new GsonBuilder().create().fromJson(message, Capsule.class);
        if (x.type.equals(String.valueOf(PacketType.MOVE.getValue()))) {
            MovePacket movePacket = new GsonBuilder().create().fromJson(x.data, MovePacket.class);
            VaultMapper.LOGGER.info("MOVE " + movePacket.uuid + " " + movePacket.x + " " + movePacket.z + " " + movePacket.yaw);

            VaultMap.updatePlayerMapData(movePacket.uuid, movePacket.color, movePacket.x, movePacket.z, movePacket.yaw);
        } else if (x.type.equals(String.valueOf(PacketType.CELL.getValue()))) {
            VaultCell cellPacket = new GsonBuilder().create().fromJson(x.data, VaultCell.class); //have to change maybe
            VaultMapper.LOGGER.info("CELL " + cellPacket.x + " " + cellPacket.z);

            VaultMap.addOrReplaceCell(cellPacket);
        } else if (x.type.equals(String.valueOf(PacketType.LEAVE.getValue()))) {
            LeavePacket leavePacket = new GsonBuilder().create().fromJson(x.data, LeavePacket.class);
            VaultMapper.LOGGER.info("LEAVE " + leavePacket.uuid);

            VaultMap.removePlayerMapData(leavePacket.uuid);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Logger.getAnonymousLogger().info("closed");
        Logger.getAnonymousLogger().info(String.valueOf(code));
        Logger.getAnonymousLogger().info(reason);
        Logger.getAnonymousLogger().info(String.valueOf(remote));

    }

    @Override
    public void onError(Exception ex) {
        Logger.getAnonymousLogger().info(ex.toString());
    }

    public void closeGracefully() {
        keepMeOn = false;
        keepConnectedTimer.cancel();
        this.close();
    }

    /**
     * Sends new(hopefully) cell data to the proxy server
     *
     * @param cell
     */
    public void sendCellData(VaultCell cell) {
        if (this.isOpen()) {
            this.send(new GsonBuilder().create().toJson(new Capsule(PacketType.CELL.getValue(), new GsonBuilder().create().toJson(cell))));
        }
    }

    public void sendMapPing() {

    }

    /**
     * Sends the player arrow data to the proxy server
     *
     * @param name     Player name
     * @param cellX
     * @param cellZ
     * @param rotation
     */
    public void sendPlayerData(String name, int cellX, int cellZ, float rotation) {
        if (this.isOpen()) {
            MovePacket data = new MovePacket(name, "#000000", cellX, cellZ, rotation);
            if (!old_data.equals(data)) {
                old_data = data;

                this.send(new GsonBuilder().create().toJson(new Capsule(PacketType.MOVE.getValue(), new GsonBuilder().create().toJson(data))));
            }

        }
    }

    enum PacketType {
        @SerializedName("0") JOIN("0"), //unused on client side for now
        @SerializedName("1") LEAVE("1"), //S2C for removing player arrows
        @SerializedName("2") CELL("2"),
        @SerializedName("3") MOVE("3");

        private final String value;

        PacketType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    class Capsule {
        public String type;
        public String data;

        public Capsule(String type, String data) {
            this.type = type;
            this.data = data;
        }
    }

    /**
     * Helper class for JSON deserialization of disconnect packets
     */
    class LeavePacket {
        public String uuid;
        public String color;
    }

    /**
     * class for easy JSON serialization when sending player data
     */
    class MovePacket {
        public String uuid;
        public String color;
        public int x;
        public int z;
        public float yaw;

        public MovePacket(String uuid, String color, int x, int z, float yaw) {
            this.uuid = uuid;
            this.color = color;
            this.x = x;
            this.z = z;
            this.yaw = yaw;
        }

        public boolean equals(MovePacket data) {
            return data.uuid.equals(this.uuid) && data.color.equals(this.color) && data.x == this.x && data.z == this.z && data.yaw == this.yaw;
        }
    }
}
