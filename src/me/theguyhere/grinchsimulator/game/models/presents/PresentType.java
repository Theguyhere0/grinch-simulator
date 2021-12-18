package me.theguyhere.grinchsimulator.game.models.presents;

public enum PresentType {
    WOOD("wood"),
    STONE("stone"),
    IRON("iron"),
    COPPER("copper"),
    GOLD("gold"),
    DIAMOND("diamond"),
    EMERALD("emerald"),
    NETHERITE("netherite"),
    BLACK("black"),
    BROWN("brown"),
    RED("red"),
    ORANGE("orange"),
    YELLOW("yellow"),
    GREEN("green"),
    CYAN("cyan"),
    BLUE("blue"),
    PURPLE("purple"),
    PINK("pink"),
    WHITE("white");

    public final String label;

    PresentType(String label) {
        this.label = label;
    }
}
