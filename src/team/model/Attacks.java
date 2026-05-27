package team.model;

public interface Attacks {
    void executeAttack(Character attacker, Character target);
    String getAttackName();
    double getAttackRange();
    double getCooldown();
    boolean canExecute(Character attacker, Character target);
    double getMpCost();
}

