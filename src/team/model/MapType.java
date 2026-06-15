package team.model;

/**
 * המפות (שלבים) במשחק. לכל מפה קונספט עיצובי שונה, מאגר אויבים תואם,
 * ושם + תיאור קצר להצגה בתפריט בחירת המפות.
 */
public enum MapType {
    MEADOW ("Meadow",  "Grassy plains",  EnemyType.SWIFT_HENRY, EnemyType.EVIL_HENRY, EnemyType.GIANT_HENRY),
    INFERNO("Inferno", "Hellfire wastes", EnemyType.INFERNO_HENRY, EnemyType.DOOM_HENRY),
    FROST  ("Frost",   "Frozen tundra",  EnemyType.FROST_HENRY, EnemyType.YETI_HENRY),
    VOID   ("Void",    "The cosmic void", EnemyType.VOID_HENRY, EnemyType.COSMIC_HENRY);

    public final String      displayName;
    public final String      concept;
    public final EnemyType[] enemyTypes;

    MapType(String displayName, String concept, EnemyType... enemyTypes) {
        this.displayName = displayName;
        this.concept     = concept;
        this.enemyTypes  = enemyTypes;
    }
}
