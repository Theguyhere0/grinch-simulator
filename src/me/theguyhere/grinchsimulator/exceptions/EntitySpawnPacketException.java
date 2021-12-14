package me.theguyhere.grinchsimulator.exceptions;

public class EntitySpawnPacketException extends Exception {
    public EntitySpawnPacketException(String msg) {
        super(msg);
    }

    public EntitySpawnPacketException() {
        super();
    }
}
