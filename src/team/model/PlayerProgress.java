package team.model;







// Tracks level experience coins and level growth

public class PlayerProgress {

    private int coins = 0;
    private int xp    = 0;
    private int level = 1;

    public int getCoins()    { return coins; }
    public int getXp()       { return xp; }
    public int getLevel()    { return level; }

    public void setStartingLevel(int startingLevel) {
        level = Math.max(1, startingLevel);
        xp = 0;
    }


    public int getXpToNext() { return level * 100; }

    public void addCoins(int amount) {
        if (amount > 0) coins += amount;
    }

    public boolean spendCoins(int amount) {
        if (coins < amount) return false;
        coins -= amount;
        return true;
    }





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
