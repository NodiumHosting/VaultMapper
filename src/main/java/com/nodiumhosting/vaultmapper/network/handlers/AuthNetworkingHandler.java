package com.nodiumhosting.vaultmapper.network.handlers;

import com.google.gson.Gson;
import com.nodiumhosting.vaultmapper.VaultMapper;
import com.nodiumhosting.vaultmapper.network.packets.AuthPacket;
import com.nodiumhosting.vaultmapper.network.payloads.Payload;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class AuthNetworkingHandler {

    private static final String PROTOCOL_VERSION = "1";
    private static final Gson gson = new Gson();

    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder.named(
            new ResourceLocation(VaultMapper.MODID, "auth"))
            .serverAcceptedVersions((version)->true)
            .clientAcceptedVersions((version)->true)
            .networkProtocolVersion(()->PROTOCOL_VERSION)
            .simpleChannel();

    public static void register() {
        int id = 0;

        CHANNEL.messageBuilder(AuthPacket.class, id++)
                .encoder(AuthPacket::encode)
                .decoder(AuthPacket::decode)
                .consumer(AuthPacket::handle)
                .add();

        VaultMapper.LOGGER.info("Registered auth packet");
    }

    public static void sendModInfoResponse() {
        Payload payload = new Payload("mod_info", "vaultmapper");
        CHANNEL.sendToServer(new AuthPacket(gson.toJson(payload)));
    }

    public static void sendTokenResponse(String token) {
        Payload payload = new Payload("token_ack", token);
        CHANNEL.sendToServer(new AuthPacket(gson.toJson(payload)));
    }
}
