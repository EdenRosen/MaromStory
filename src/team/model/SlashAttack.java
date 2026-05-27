package team.model;

public class SlashAttack implements Attacks{
    private static final double BASE_DAMAGE = 20;
    private static final double COOLDOWN    = 1.0;
    private static final double MP_COST     = 15.0;

    @Override
    public void executeAttack(Character attacker, Character target) {
        double damage = BASE_DAMAGE + attacker.getStats().getStrength() * 1.5;
        target.takeDamage(damage);
    }

    @Override
    public String getAttackName() { return "Slash Attack"; }

    @Override
    public double getAttackRange() { return MainPlayer.ATTACK_RANGE * 1.5; }

    @Override
    public double getCooldown() { return COOLDOWN; }

    @Override
    public double getMpCost() { return MP_COST; }

    @Override
    public boolean canExecute(Character attacker, Character target) {
        if (!attacker.hasSword()) return false;
        if (target == null || target.getStats().isDead()) return false;

        double dx = (target.getX() + Character.WIDTH  / 2.0) - (attacker.getX() + Character.WIDTH  / 2.0);
        double dy = Math.abs((target.getY() + Character.HEIGHT / 2.0) - (attacker.getY() + Character.HEIGHT / 2.0));
        boolean inFacingDirection = attacker.isFacingRight() ? dx >= 0 : dx <= 0;

        return inFacingDirection && Math.abs(dx) <= getAttackRange() && dy <= MainPlayer.ATTACK_HEIGHT * 1.5;
    }
}
