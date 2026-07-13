package team.model;

/**
 * Tracks health energy strength agility defense and equipment bonuses
 */
public class PlayerStats {

    private double health;
    private double maxHealth;
    private double energy;
    private double maxEnergy;
    private double strength;
    private double agility;
    private double defense = 0;

    public PlayerStats(double maxHealth, double maxEnergy, double strength, double agility) {
        this.maxHealth = maxHealth;
        this.health    = maxHealth;
        this.maxEnergy = maxEnergy;
        this.energy    = maxEnergy;
        this.strength  = strength;
        this.agility   = agility;
    }


    // Provides health and defense access for combat systems
    public double getHealth()    { return health; }
    public double getMaxHealth() { return maxHealth; }

    public double getDefense()              { return defense; }
    public void   increaseDefense(double d) { defense += d; }

    public void takeDamage(double amount) {
        health = Math.max(0, health - Math.max(1, amount - defense));
    }

    public void heal(double amount) {
        health = Math.min(maxHealth, health + amount);
    }

    public boolean isDead() { return health <= 0; }


    // Provides energy access for attacks and regeneration
    public double getEnergy()    { return energy; }
    public double getMaxEnergy() { return maxEnergy; }

    public void useEnergy(double amount) {
        energy = Math.max(0, energy - amount);
    }

    public void restoreEnergy(double amount) {
        energy = Math.min(maxEnergy, energy + amount);
    }


    // Raises maximum resources while keeping current values aligned
    public void increaseMaxHealth(double amount) {
        maxHealth += amount;
        health    += amount;
    }

    public void increaseMaxEnergy(double amount) {
        maxEnergy += amount;
        energy    += amount;
    }

    public boolean hasEnergy(double amount) { return energy >= amount; }


    // Provides offensive and mobility attributes
    public double getStrength() { return strength; }
    public double getAgility()  { return agility; }

    public void setStrength(double strength) { this.strength = strength; }
    public void setAgility(double agility)   { this.agility  = agility; }

    // Applies the strength bonus from an equipped sword
    public void equipSword(Sword sword) {
        this.strength += sword.getStrengthBonus();
    }

    // Removes the strength bonus from an unequipped sword
    public void unequipSword(Sword sword) {
        this.strength -= sword.getStrengthBonus();
    }
}
