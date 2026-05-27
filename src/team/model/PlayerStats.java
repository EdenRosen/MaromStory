package team.model;

/**
 * תכונות ה-RPG של השחקן.
 * מחולק בכוונה מ-MainPlayer כדי שכל מחלקה תטפל בנושא אחד:
 *   MainPlayer  = מיקום ופיזיקה
 *   PlayerStats = תכונות משחק
 */
public class PlayerStats {

    private double health;
    private double maxHealth;
    private double energy;
    private double maxEnergy;
    private double strength;   // כוח — משפיע על נזק
    private double agility;    // זריזות — משפיעה על מהירות וקפיצה

    public PlayerStats(double maxHealth, double maxEnergy, double strength, double agility) {
        this.maxHealth = maxHealth;
        this.health    = maxHealth;
        this.maxEnergy = maxEnergy;
        this.energy    = maxEnergy;
        this.strength  = strength;
        this.agility   = agility;
    }

    // --- חיים ---

    public double getHealth()    { return health; }
    public double getMaxHealth() { return maxHealth; }

    public void takeDamage(double amount) {
        health = Math.max(0, health - amount);
    }

    public void heal(double amount) {
        health = Math.min(maxHealth, health + amount);
    }

    public boolean isDead() { return health <= 0; }

    // --- אנרגיה ---

    public double getEnergy()    { return energy; }
    public double getMaxEnergy() { return maxEnergy; }

    public void useEnergy(double amount) {
        energy = Math.max(0, energy - amount);
    }

    public void restoreEnergy(double amount) {
        energy = Math.min(maxEnergy, energy + amount);
    }

    public boolean hasEnergy(double amount) { return energy >= amount; }

    // --- כוח וזריזות ---

    public double getStrength() { return strength; }
    public double getAgility()  { return agility; }

    public void setStrength(double strength) { this.strength = strength; }
    public void setAgility(double agility)   { this.agility  = agility; }

    // --- ציוד ---

    // מצייד חרב — מוסיפה את בונוס הכוח שלה לשחקן
    public void equipSword(Sword sword) {
        this.strength += sword.getStrengthBonus();
    }

    // מוריד ציוד חרב — מחזיר את הכוח למצב המקורי
    public void unequipSword(Sword sword) {
        this.strength -= sword.getStrengthBonus();
    }
}
