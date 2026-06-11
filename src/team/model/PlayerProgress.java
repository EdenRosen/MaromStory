package team.model;

/**
 * התקדמות השחקן — מטבעות (כסף), נקודות ניסיון (XP) ורמה (Level).
 * מופרד מ-PlayerStats כדי שמחלקה אחת תטפל בנושא אחד:
 *   PlayerStats   = תכונות קרב (HP/MP/STR/AGI)
 *   PlayerProgress = כלכלה והתקדמות
 */
public class PlayerProgress {

    private int coins = 0;
    private int xp    = 0;
    private int level = 1;

    public int getCoins()    { return coins; }
    public int getXp()       { return xp; }
    public int getLevel()    { return level; }

    // כמות ה-XP הדרושה למעבר לרמה הבאה — גדלה עם הרמה
    public int getXpToNext() { return level * 100; }

    public void addCoins(int amount) {
        if (amount > 0) coins += amount;
    }

    /**
     * מוסיף XP ומקדם רמות לפי הצורך.
     * @return מספר הרמות שעלו (0 אם לא עלתה רמה).
     */
    public int addXp(int amount) {
        if (amount <= 0) return 0;
        xp += amount;
        int levelsGained = 0;
        while (xp >= getXpToNext()) {
            xp -= getXpToNext();
            level++;
            levelsGained++;
        }
        return levelsGained;
    }
}
