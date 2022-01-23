package me.theguyhere.grinchsimulator.game.models.arenas;

public class ArenaRecord {
    private final int value; // Record value
    private final String player; // Name of player that reached this record
    private final ArenaRecordType type; // The type of the arena record

    public ArenaRecord(int value, String player, ArenaRecordType type) {
        this.value = value;
        this.player = player;
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public String getPlayer() {
        return player;
    }

    public ArenaRecordType getType() {
        return type;
    }
}
