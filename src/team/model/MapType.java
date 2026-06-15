package team.model;

/**
 * המפות (שלבים) במשחק. כל מפה יודעת את שם התצוגה שלה ואת מאגר סוגי האויבים
 * שמופיעים בה (משמש גם ל-spawn ההתחלתי וגם לריספאון).
 *
 *   MEADOW  — מפת הפתיחה, אויבים רגילים
 *   INFERNO — מפה קשה: אויבים חזקים מאוד שנותנים הרבה יותר XP, וחרב חזקה
 */
public enum MapType {
    MEADOW ("Meadow",  EnemyType.SWIFT_HENRY, EnemyType.EVIL_HENRY, EnemyType.GIANT_HENRY),
    INFERNO("Inferno", EnemyType.INFERNO_HENRY, EnemyType.DOOM_HENRY);

    public final String      displayName;
    public final EnemyType[] enemyTypes;

    MapType(String displayName, EnemyType... enemyTypes) {
        this.displayName = displayName;
        this.enemyTypes  = enemyTypes;
    }
}
