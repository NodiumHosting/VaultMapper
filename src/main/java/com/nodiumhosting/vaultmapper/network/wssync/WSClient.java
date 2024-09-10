package com.nodiumhosting.vaultmapper.network.wssync;


import com.nodiumhosting.vaultmapper.VaultMapper;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

// wss://MCUSERNAME@vmsync.boykiss.ing:25284/VAULTDIMENSIONID
public class WSClient extends WebSocketClient {

    private static final String wsEndpoint = "vmsync.boykiss.ing:25284";

    public WSClient(String playerName, String dimensionID) {
        super(URI.create("wss://" + playerName + "@" + wsEndpoint + "/" + dimensionID));
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        send("test");
    }

    @Override
    public void onMessage(String message) {
        VaultMapper.LOGGER.info(message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        VaultMapper.LOGGER.info("Sync WS closed with code: " + code + " reason: " + reason);
    }

    @Override
    public void onError(Exception ex) {
        VaultMapper.LOGGER.error("Something went horribly wrong with the sync websocket");
        VaultMapper.LOGGER.error(ex.toString());
    }
}
