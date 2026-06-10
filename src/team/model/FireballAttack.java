package team.model;

/**
 * סקיל הקוסם — כדור אש מטווח.
 * שלא כמו Slash, אינו דורש חרב (זהו קסם), והטווח שלו ארוך בהרבה.
 */
public class FireballAttack implements Attacks {

    private static final double BASE_DAMAGE = 25;
    private static final double COOLDOWN    = 1.0;
    private static final double MP_COST     = 12.0;

    @Override
    public void executeAttack(Character attacker, Character target) {
        double damage = BASE_DAMAGE + attacker.getStats().getStrength();
        target.takeDamage(damage);
    }

    @Override
    public String getAttackName() { return "Fireball"; }

    @Override
    public double getAttackRange() { return MainPlayer.ATTACK_RANGE * 2.5; }

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

        return inFacingDirection && Math.abs(dx) <= getAttackRange() && dy <= MainPlayer.ATTACK_HEIGHT * 2.0;
    }
}
