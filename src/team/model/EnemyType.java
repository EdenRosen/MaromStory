package team.model;

/**
 * סוגי האויבים במשחק. כל סוג נושא את כל הנתונים שלו — stats, מהירות, תגמולים ושם תצוגה —
 * במקום מחרוזות קסם מפוזרות בקוד. (אותו רעיון כמו HeroType לשחקן.)
 *
 *   SWIFT_HENRY   — מהיר אך שביר          (מפת Meadow)
 *   EVIL_HENRY    — מאוזן ומרושע          (מפת Meadow)
 *   GIANT_HENRY   — ענק חזק ואיטי         (מפת Meadow)
 *   INFERNO_HENRY — חזק מאוד, הרבה XP     (מפת Inferno)
 *   DOOM_HENRY    — בוס, XP אדיר          (מפת Inferno)
 */
public enum EnemyType {
    //              HP    MP   STR  AGI  speed  xp   coins  שם תצוגה
    SWIFT_HENRY(    60,  30,    6,   5,  4.5,   20,    8,  "Swift Henry"),
    EVIL_HENRY(     90,  30,    9,   3,  1.6,   30,   12,  "Evil Henry"),
    GIANT_HENRY(   150,  30,   14,   1,  1.0,   45,   20,  "Giant Henry"),
    INFERNO_HENRY( 220,  30,   20,   3,  2.2,   80,   35,  "Inferno Henry"),
    DOOM_HENRY(    350,  30,   28,   1,  1.4,  140,   60,  "Doom Henry");

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
