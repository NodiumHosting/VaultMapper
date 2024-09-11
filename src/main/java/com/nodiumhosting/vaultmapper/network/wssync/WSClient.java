package com.nodiumhosting.vaultmapper.network.wssync;

import com.nodiumhosting.vaultmapper.map.CellType;
import com.nodiumhosting.vaultmapper.map.VaultCell;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

public class WSClient extends WebSocketClient {
    private final int timerPeriod = 10000;
    private final Timer keepConnectedTimer = new Timer();
    private final WSClient self;
    private boolean keepMeOn = false;

    //private final String relayAddress = "wss://vmsync.boykiss.ing";

    public WSClient(String playerName, String vaultID) {
        super(URI.create("wss://vmsync.boykiss.ing/" + playerName + "/" + vaultID));

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
        if (this.isClosing() || this.isClosing()) {
            Logger.getAnonymousLogger().info("Can't send keep-alive, socket is closed.");
        }
        //this.send("keep_me_alive");
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
        var split = message.split(":");
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
            if (type.equals(CellType.ROOM)) {
                VaultMap.addCell(new VaultCell(type, cell_x, cell_y));
            } else if (type.equals(CellType.TUNNEL)) {
                VaultMap.addCell(new VaultCell(type, VaultMap.getTunnelType(cell_x, cell_y), cell_x, cell_y));
            }
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
     * @param x
     * @param y
     */
    public void sendCellData(int x, int y) {
        if (this.isOpen()) this.send("cell:" + x + ":" + y);
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
        if (this.isOpen()) this.send("player:" + name + ":" + cellX + ":" + cellY + ":" + rotation);
    }

}
