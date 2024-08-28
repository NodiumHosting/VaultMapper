package com.nodiumhosting.vaultmapper.webmap;

import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.map.CellType;
import com.nodiumhosting.vaultmapper.map.TunnelType;
import com.nodiumhosting.vaultmapper.map.VaultCell;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;

public class SocketServer extends WebSocketServer {
    public SocketServer(InetSocketAddress address) {
        super(address);
        wslist = new ArrayList<>();
    }

    ArrayList<WebSocket> wslist;

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        wslist.add(conn);
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

    public void sendData(VaultCell cell, String color) {
        wslist.forEach((conn) -> {
            if (cell.type == CellType.ROOM) {
                conn.send("room:" + cell.x + ":" + cell.z + ":" + color);
            } else {
                if (cell.tType == TunnelType.X_FACING) {
                    conn.send("tunnelX:"+cell.x+":"+cell.z+":"+color);
                }
                else {
                    conn.send("tunnelZ:"+cell.x+":"+cell.z+":"+color);
                }
            }
        });
    }

    public void sendReset() {
        wslist.forEach((conn) -> {
            conn.send("reset");
        });
    }
}
