package com.nodiumhosting.vaultmapper.network.payloads;

public class Payload {
    public String type;
    public String data;

    public Payload(String type, String data) {
        this.type = type;
        this.data = data;
    }

    @Override
    public String toString() {
        return "Type: "+type+", Data: "+data;
    }
}
