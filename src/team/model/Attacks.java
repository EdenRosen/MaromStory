package team.model;

// Defines the common contract for every player attack

public interface Attacks {
    void executeAttack(Character attacker, Character target);
    String getAttackName();
    double getAttackRange();
    double getCooldown();
    boolean canExecute(Character attacker, Character target);
    double getMpCost();
}

