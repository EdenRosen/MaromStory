package team.model;





// Lists enemy stat profiles rewards speed and display names
public enum EnemyType {


    SWIFT_HENRY(     40,  30,    4,   5,  4.5,   20,    8,  "Swift Henry"),
    EVIL_HENRY(      55,  30,    6,   3,  1.6,   30,   12,  "Evil Henry"),
    GIANT_HENRY(     90,  30,    8,   1,  1.0,   45,   20,  "Giant Henry"),

    INFERNO_HENRY(  220,  30,   20,   3,  2.2,   80,   35,  "Inferno Henry"),
    DOOM_HENRY(     350,  30,   28,   1,  1.4,  140,   60,  "Doom Henry"),

    FROST_HENRY(    300,  30,   24,   2,  1.8,  180,   80,  "Frost Henry"),
    YETI_HENRY(     500,  30,   34,   1,  1.2,  300,  130,  "Yeti Henry"),

    VOID_HENRY(     650,  30,   42,   3,  2.4,  500,  220,  "Void Henry"),
    COSMIC_HENRY(   950,  30,   55,   2,  1.6,  800,  400,  "Cosmic Henry"),

    FINAL_BOSS(    6000, 100,  120,   2,  2.2, 5000, 2000,  "FINAL BOSS");

    public final double maxHealth, maxEnergy, strength, agility, speed;
    public final int    xpReward, coinReward;
    public final String displayName;

    EnemyType(double maxHealth, double maxEnergy, double strength, double agility,
              double speed, int xpReward, int coinReward, String displayName) {
        this.maxHealth   = maxHealth;
        this.maxEnergy   = maxEnergy;
        this.strength    = strength;
        this.agility     = agility;
        this.speed       = speed;
        this.xpReward    = xpReward;
        this.coinReward  = coinReward;
        this.displayName = displayName;
    }
}
