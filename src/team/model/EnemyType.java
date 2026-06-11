package team.model;

/**
 * סוגי האויבים במשחק. כל סוג נושא את כל הנתונים שלו — stats, מהירות ושם תצוגה —
 * במקום מחרוזות קסם מפוזרות בקוד. (אותו רעיון כמו HeroType לשחקן.)
 *
 *   SWIFT_HENRY  — מהיר אך שביר
 *   EVIL_HENRY   — מאוזן ומרושע
 *   GIANT_HENRY  — ענק חזק ואיטי
 */
public enum EnemyType {
    //            HP    MP  STR  AGI  speed  שם תצוגה
    SWIFT_HENRY(  60,  30,   6,   5,  4.5,  "Swift Henry"),
    EVIL_HENRY(   90,  30,   9,   3,  1.6,  "Evil Henry"),
    GIANT_HENRY( 150,  30,  14,   1,  1.0,  "Giant Henry");

    public final double maxHealth, maxEnergy, strength, agility, speed;
    public final String displayName;

    EnemyType(double maxHealth, double maxEnergy, double strength,
              double agility, double speed, String displayName) {
        this.maxHealth   = maxHealth;
        this.maxEnergy   = maxEnergy;
        this.strength    = strength;
        this.agility     = agility;
        this.speed       = speed;
        this.displayName = displayName;
    }
}
