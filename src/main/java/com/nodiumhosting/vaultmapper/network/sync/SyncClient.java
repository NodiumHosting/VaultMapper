package com.nodiumhosting.vaultmapper.network.sync;

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

public class SyncClient extends WebSocketClient {
    private final Timer keepConnectedTimer = new Timer();
    private final SyncClient self;
    MovePacket old_data = new MovePacket("", "", 0, 0, 0);
    private boolean keepMeOn = false;

    public SyncClient(String playerUUID, String vaultID) {
        super(URI.create(ClientConfig.SYNC_SERVER.get() + "/" + vaultID + "/" + playerUUID)); //TODO: add check whether server is even online

        self = this;

        int timerPeriod = 10000;
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
//            VaultMapper.LOGGER.info("Can't send keep-alive, socket is closed.");
        }
        //this.send("[\"keep_me_alive\"]");
        this.sendPing();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
//        VaultMapper.LOGGER.info("Sync WS Connected");
        keepMeOn = true;
    }

    @Override
    public void onMessage(String message) {
//        VaultMapper.LOGGER.info(message); // log the json
        try {
            var dataCapsule = new GsonBuilder().create().fromJson(message, Capsule.class);
            if (dataCapsule.type.equals(String.valueOf(PacketType.MOVE.getValue()))) {
                MovePacket movePacket = new GsonBuilder().create().fromJson(dataCapsule.data, MovePacket.class);

                VaultMap.updatePlayerMapData(movePacket.uuid, movePacket.color, movePacket.x, movePacket.z, movePacket.yaw);
                VaultMapper.webMapServer.sendArrow(movePacket.x, movePacket.z, movePacket.yaw, movePacket.uuid, movePacket.color);
            } else if (dataCapsule.type.equals(String.valueOf(PacketType.CELL.getValue()))) {
                VaultCell cellPacket = new GsonBuilder().create().fromJson(dataCapsule.data, VaultCell.class); //have to change maybe

                VaultMap.addOrReplaceCell(cellPacket);
                VaultMapper.webMapServer.sendCell(cellPacket);
            } else if (dataCapsule.type.equals(String.valueOf(PacketType.LEAVE.getValue()))) {
                LeavePacket leavePacket = new GsonBuilder().create().fromJson(dataCapsule.data, LeavePacket.class);

                VaultMap.removePlayerMapData(leavePacket.uuid);
                VaultMapper.webMapServer.removeArrow(leavePacket.uuid);
            }
        } catch (Exception e) {
            VaultMapper.LOGGER.error("Sync WS Error: " + e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
//        Logger.getAnonymousLogger().info("closed");
//        Logger.getAnonymousLogger().info(String.valueOf(code));
//        Logger.getAnonymousLogger().info(reason);
//        Logger.getAnonymousLogger().info(String.valueOf(remote));
    }

    @Override
    public void onError(Exception ex) {
        VaultMapper.LOGGER.error("Sync WS Error: " + ex.toString());
    }

    public void closeGracefully() {
        keepMeOn = false;
        keepConnectedTimer.cancel();
        this.close();
    }

    public void sendCellPacket(VaultCell cell) {
        if (this.isOpen()) {
            this.send(new GsonBuilder().create().toJson(new Capsule(PacketType.CELL.getValue(), new GsonBuilder().create().toJson(cell))));
        }
    }

    public void sendMovePacket(String name, int cellX, int cellZ, float rotation) {
        if (this.isOpen()) {
            MovePacket data = new MovePacket(name, "", cellX, cellZ, rotation);
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

    class LeavePacket {
        public String uuid;
        public String color;
    }

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
