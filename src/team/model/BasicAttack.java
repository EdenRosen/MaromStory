package team.model;

/**
 * Defines the basic melee attack available to every hero
 */
public class BasicAttack implements Attacks {

    private static final double BASE_DAMAGE = 10;
    private static final double COOLDOWN    = 0.5;
    private static final double MP_COST     = 2.0;

    @Override
    public void executeAttack(Character attacker, Character target) {
        double damage = BASE_DAMAGE + attacker.getStats().getStrength();
        target.takeDamage(damage);
    }

    @Override
    public String getAttackName() { return "Basic Attack"; }

    @Override
    public double getAttackRange() { return MainPlayer.ATTACK_RANGE; }

    @Override
    public double getCooldown() { return COOLDOWN; }

    @Override
    public double getMpCost() { return MP_COST; }


    @Override
    public boolean canExecute(Character attacker, Character target) {
        if (target == null || target.getStats().isDead()) return false;

        double dx = (target.getX() + Character.WIDTH  / 2.0) - (attacker.getX() + Character.WIDTH  / 2.0);
        double dy = Math.abs((target.getY() + Character.HEIGHT / 2.0) - (attacker.getY() + Character.HEIGHT / 2.0));
        boolean inFacingDirection = attacker.isFacingRight() ? dx >= 0 : dx <= 0;

        return inFacingDirection && Math.abs(dx) <= MainPlayer.ATTACK_RANGE && dy <= MainPlayer.ATTACK_HEIGHT;
    }

}
