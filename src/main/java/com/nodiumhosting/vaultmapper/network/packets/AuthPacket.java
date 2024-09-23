package com.nodiumhosting.vaultmapper.network.packets;

import com.google.gson.Gson;
import com.nodiumhosting.vaultmapper.network.handlers.AuthNetworkingHandler;
import com.nodiumhosting.vaultmapper.network.payloads.Payload;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraftforge.network.NetworkEvent;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Supplier;

public class AuthPacket {
    private final String data;

    public AuthPacket(String data) {
        this.data = data;
    }

    // Encode the packet (write data to buffer)
    public static void encode(AuthPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUtf(packet.data);
    }

    // Decode the packet (read data from buffer)
    public static AuthPacket decode(FriendlyByteBuf buffer) {
        // can't just readUtf because first byte is discriminator
        return new AuthPacket(buffer.toString(1, buffer.capacity()-1, StandardCharsets.UTF_8));  // Read data from the buffer
    }

    // Handle the packet
    public static void handle(AuthPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            System.out.println("Received auth packet: " + packet.data);
            Gson gson = new Gson();
            Payload payload = gson.fromJson(packet.data, Payload.class);

            if (payload.type.equals("mod_info_request")) {
                AuthNetworkingHandler.sendModInfoResponse();
            }
            else if (payload.type.equals("token")) {
                String token = payload.data;
                System.out.println("TOKEN IS: "+token);

                // TODO: implement actual token logic

                AuthNetworkingHandler.sendTokenResponse(token);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
