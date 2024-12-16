package com.nodiumhosting.vaultmapper.network.wssync;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.auth.Token;
import com.nodiumhosting.vaultmapper.map.VaultCell;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class WSClient extends WebSocketClient {
    //private final static String relayAddress = "wss://vmsync.boykiss.ing";
    private final static String relayAddress = "ws://127.0.0.1:25284";
    private final int timerPeriod = 10000;
    private final Timer keepConnectedTimer = new Timer();
    private final WSClient self;
    MovePacket old_data = new MovePacket("", "#000000", 0, 0, 0);
    private boolean keepMeOn = false;


    public WSClient(String playerUUID, String vaultID) {
        //super(URI.create(relayAddress + "/" + playerName + "/" + vaultID));
        super(URI.create(relayAddress + "/" + vaultID + "/?uuid=" + playerUUID + "&token=" + Token.getToken()));
//        Logger.getAnonymousLogger().info("(VMSYNC) Trying to connect to websocket " + URI.create(relayAddress + "/" + vaultID + "/?uuid=" + playerUUID + "&token=" + Token.getToken()).toString());

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
        VaultMapper.LOGGER.info(String.valueOf(x.type));
        //VaultMapper.LOGGER.info(x.data);
        if (x.type == PacketType.MOVE.ordinal()) {// player info

            MovePacket dat = new GsonBuilder().create().fromJson(x.data, MovePacket.class);
            VaultMapper.LOGGER.info(dat.x + " : " + dat.z + " : " + dat.yaw);

            VaultMap.updatePlayerMapData(dat.uuid, dat.color, dat.x, dat.z, dat.yaw);
        } else if (x.type == PacketType.CELL.ordinal()) {
            VaultCell cell = new GsonBuilder().create().fromJson(x.data, VaultCell.class);
            VaultMapper.LOGGER.info("NEW CELL: " + cell.toString());

            VaultMap.addOrReplaceCell(cell);
        } else if (x.type == PacketType.LEAVE.ordinal()) {
            LeavePacket d = new GsonBuilder().create().fromJson(x.data, LeavePacket.class);
            VaultMap.removePlayerMapData(d.uuid);
            VaultMapper.LOGGER.info("Disconnected: " + d.uuid);
        }


        /*var split = message.split(":");
        String arg1 = split[0]; //1-playername, 2-x, 3-y, 4-yaw
        if (arg1.equals("player")) {
            // update the players
            Logger.getAnonymousLogger().info("updated player data:" + split[1] + Integer.parseInt(split[2]) + Integer.parseInt(split[3]) + Float.parseFloat(split[4]));
            VaultMap.updatePlayerMapData(split[1], Integer.parseInt(split[2]), Integer.parseInt(split[3]), Float.parseFloat(split[4]));
        } else if (arg1.equals("cell")) {
            Logger.getAnonymousLogger().info("received new cell");
            int cell_x = Integer.parseInt(split[1]);
            int cell_y = Integer.parseInt(split[2]);
            CellType type = VaultMap.getCellType(cell_x, cell_y);
            if (!type.equals(CellType.NONE)) {
                VaultMap.addOrReplaceCell(new VaultCell(cell_x, cell_y, type, RoomType.BASIC));
            }
        }*/
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
            this.send(new GsonBuilder().create().toJson(new Capsule(PacketType.CELL.ordinal(), new GsonBuilder().create().toJson(cell))));
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

                this.send(new GsonBuilder().create().toJson(new Capsule(PacketType.MOVE.ordinal(), new GsonBuilder().create().toJson(data))));
            }

        }
    }

    enum PacketType {
        JOIN, //unused on client side for now
        LEAVE, //S2C for removing player arrows
        CELL,
        MOVE;
    }

    class Capsule {
        public int type;
        public String data;

        public Capsule(int type, String data) {
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
