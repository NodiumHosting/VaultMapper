package com.nodiumhosting.vaultmapper.network.wssync;

import com.google.gson.GsonBuilder;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.auth.Token;
import com.nodiumhosting.vaultmapper.map.VaultCell;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class WSClient extends WebSocketClient {
    //private final static String relayAddress = "wss://vmsync.boykiss.ing";
    private final static String relayAddress = "ws://localhost:25284";
    private final int timerPeriod = 10000;
    private final Timer keepConnectedTimer = new Timer();
    private final WSClient self;
    PlayerData old_data = new PlayerData("", 0, 0, 0);
    private boolean keepMeOn = false;


    public WSClient(String playerUUID, String vaultID) {
        //super(URI.create(relayAddress + "/" + playerName + "/" + vaultID));
        super(URI.create(relayAddress + "/" + vaultID + "/?uuid=" + playerUUID + "&token=" + Token.getToken()));

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
        if (x.type == 0) {// player info

            PlayerData dat = new GsonBuilder().create().fromJson(x.data, PlayerData.class);
            VaultMapper.LOGGER.info(dat.cellX + " : " + dat.cellY + " : " + dat.rot);
        } else if (x.type == 1) {
            VaultCell cell = new GsonBuilder().create().fromJson(x.data, VaultCell.class);
            VaultMapper.LOGGER.info("NEW CELL: " + cell.toString());
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
            this.send(new GsonBuilder().create().toJson(new Capsule(1, new GsonBuilder().create().toJson(cell))));
        }
    }

    public void sendMapPing() {

    }

    /**
     * Sends the player arrow data to the proxy server
     *
     * @param name     Player name
     * @param cellX
     * @param cellY
     * @param rotation
     */
    public void sendPlayerData(String name, int cellX, int cellY, float rotation) {
        if (this.isOpen()) {
            PlayerData data = new PlayerData(name, cellX, cellY, rotation);
            if (!old_data.equals(data)) {
                old_data = data;

                this.send(new GsonBuilder().create().toJson(new Capsule(0, new GsonBuilder().create().toJson(data))));
            }

        }
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
     * class for easy JSON serialization when sending player data
     */
    class PlayerData {
        public String uuid;
        public int cellX;
        public int cellY;
        public float rot;

        public PlayerData(String uuid, int cellX, int cellY, float rot) {
            this.uuid = uuid;
            this.cellX = cellX;
            this.cellY = cellY;
            this.rot = rot;
        }

        public boolean equals(PlayerData data) {
            return data.uuid.equals(this.uuid) && data.cellX == this.cellX && data.cellY == this.cellY && data.rot == this.rot;
        }
    }
}
