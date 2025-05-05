package com.nodiumhosting.vaultmapper.network.sync;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.config.ClientConfig;
import com.nodiumhosting.vaultmapper.gui.ToastMessageManager;
import com.nodiumhosting.vaultmapper.map.VaultCell;
import com.nodiumhosting.vaultmapper.map.VaultMap;
import com.nodiumhosting.vaultmapper.map.VaultMapOverlayRenderer;
import com.nodiumhosting.vaultmapper.proto.Color;
import com.nodiumhosting.vaultmapper.proto.Message;
import com.nodiumhosting.vaultmapper.proto.MessageType;
import com.nodiumhosting.vaultmapper.proto.VaultPlayer;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

public class SyncClient extends WebSocketClient {
    private final Timer keepConnectedTimer = new Timer();
    private final SyncClient self;
    MovePacket old_data = new MovePacket("", "", 0, 0, 0);
    private boolean keepMeOn = false;

    public SyncClient(String playerUUID, String vaultID) {
        super(URI.create(ClientConfig.VMSYNC_SERVER.get() + "/?vaultID=" + vaultID + "&uuid=" + playerUUID)); //TODO: add check whether server is even online

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
        this.send("keep_me_alive");
        //this.sendPing();
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
//        VaultMapper.LOGGER.info("Sync WS Connected");
        keepMeOn = true;
        VaultMapOverlayRenderer.syncErrorState = false;
    }

    @Override
    public void onMessage(ByteBuffer buf) {
        try {
            var msg = Message.parseFrom(buf);
            switch (msg.getType()) {
                case VAULT -> {
                    var data = msg.getVault();
                    for (var cell : data.getCellsList()) {
                        VaultCell vaultCell = CellFromPacket(cell);

                        VaultMap.addOrReplaceCell(vaultCell);
                    }
                }
                case VAULT_PLAYER -> {
                    var data = msg.getVaultPlayer();
                    var uuid = data.getUuid();
                    var color = data.getColor();
                    var x = data.getX();
                    var z = data.getZ();
                    var yaw = data.getYaw();

                    String red = Integer.toHexString(color.getR());
                    String green = Integer.toHexString(color.getG());
                    String blue = Integer.toHexString(color.getB());
                    String paddedRed = red.length() == 1 ? "0" + red : red;
                    String paddedGreen = green.length() == 1 ? "0" + green : green;
                    String paddedBlue = blue.length() == 1 ? "0" + blue : blue;
                    String hex = "#" + paddedRed + paddedGreen + paddedBlue;

                    VaultMap.updatePlayerMapData(uuid, hex, x, z, yaw);
                }
                case VAULT_CELL -> {
                    var data = msg.getVaultCell();
                    VaultCell cell = CellFromPacket(data);

                    VaultMap.addOrReplaceCell(cell);
                }
                case PLAYER_DISCONNECT -> {
                    var data = msg.getPlayerDisconnect();

                    VaultMap.removePlayerMapData(data.getUuid());
                }
                case TOAST -> {
                    var data = msg.getToast();
                    ToastMessageManager.displayToast(data.getMessage());
                }
                case VIEWER_CODE -> {
                    var data = msg.getViewerCode();
                    VaultMap.viewerCode = data.getCode();
                }
                default -> {
                    VaultMapper.LOGGER.info("Something weird with onMessage");
                }
            }
        } catch (Exception e) {
            VaultMapper.LOGGER.error("Sync WS Error: " + e);
        }
    }

    private VaultCell CellFromPacket(com.nodiumhosting.vaultmapper.proto.VaultCell data) {
        var x = data.getX();
        var z = data.getZ();
        var cellType = data.getCellType();
        var roomType = data.getRoomType();

        var cell = new VaultCell(x, z, cellType, roomType);

        cell.roomName = data.getRoomName();
        cell.explored = data.getExplored();
        cell.inscripted = data.getInscribed();
        cell.marked = data.getMarked();

        return cell;
    }

    private com.nodiumhosting.vaultmapper.proto.VaultCell PCellFromCell(VaultCell cell) {
        return com.nodiumhosting.vaultmapper.proto.VaultCell.newBuilder()
                .setX(cell.x)
                .setZ(cell.z)
                .setCellType(cell.cellType)
                .setRoomType(cell.roomType)
                .setRoomName(cell.roomName)
                .setExplored(cell.explored)
                .setInscribed(cell.inscripted)
                .setMarked(cell.marked)
                .build();
    }


    @Override
    public void onMessage(String message) {
//        VaultMapper.LOGGER.info(message); // log the json
        try {
            var dataCapsule = new GsonBuilder().create().fromJson(message, Capsule.class);
            if (dataCapsule.type.equals(String.valueOf(PacketType.MOVE.getValue()))) {
                MovePacket movePacket = new GsonBuilder().create().fromJson(dataCapsule.data, MovePacket.class);

                VaultMap.updatePlayerMapData(movePacket.uuid, movePacket.color, movePacket.x, movePacket.z, movePacket.yaw);
            } else if (dataCapsule.type.equals(String.valueOf(PacketType.CELL.getValue()))) {
                VaultCell cellPacket = new GsonBuilder().create().fromJson(dataCapsule.data, VaultCell.class); //have to change maybe

                VaultMap.addOrReplaceCell(cellPacket);
            } else if (dataCapsule.type.equals(String.valueOf(PacketType.LEAVE.getValue()))) {
                LeavePacket leavePacket = new GsonBuilder().create().fromJson(dataCapsule.data, LeavePacket.class);

                VaultMap.removePlayerMapData(leavePacket.uuid);
            }
        } catch (Exception e) {
            VaultMapper.LOGGER.error("Sync WS Error: " + e);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        VaultMapOverlayRenderer.syncErrorState = true;
//        Logger.getAnonymousLogger().info("closed");
//        Logger.getAnonymousLogger().info(String.valueOf(code));
//        Logger.getAnonymousLogger().info(reason);
//        Logger.getAnonymousLogger().info(String.valueOf(remote));
    }

    @Override
    public void onError(Exception ex) {
        VaultMapOverlayRenderer.syncErrorState = true;
        VaultMapper.LOGGER.error("Sync WS Error: " + ex.toString());
    }

    public void closeGracefully() {
        keepMeOn = false;
        keepConnectedTimer.cancel();
        this.close();
    }

    public void sendCellPacket(VaultCell cell) {
        if (this.isOpen()) {
            this.send(Message.newBuilder()
                    .setType(MessageType.VAULT_CELL)
                    .setVaultCell(PCellFromCell(cell))
                    .build()
                    .toByteArray());
        }
    }

    private Color getSyncColor() {
        String col = ClientConfig.SYNC_COLOR.get();
        int R = Integer.parseInt(col.substring(1, 3), 16);
        int G = Integer.parseInt(col.substring(3, 5), 16);
        int B = Integer.parseInt(col.substring(5, 7), 16);

        return Color.newBuilder().setR(R).setG(G).setB(B).build();
    }

    public void sendMovePacket(String name, int cellX, int cellZ, float rotation) {
        if (this.isOpen()) {
            MovePacket data = new MovePacket(name, "", cellX, cellZ, rotation); // legacy, remove and reimplement optimalization
            if (!old_data.equals(data)) {
                old_data = data;

                this.send(Message.newBuilder()
                        .setType(MessageType.VAULT_PLAYER)
                        .setVaultPlayer(VaultPlayer.newBuilder()
                                .setUuid(name)
                                .setX(cellX)
                                .setZ(cellZ)
                                .setYaw(rotation)
                                .setColor(getSyncColor())
                                .build())
                        .build()
                        .toByteArray()
                );
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
