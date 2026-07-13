package team.model;

// Defines the dragon fireball with a reduced energy cost

public class DragonFireballAttack extends FireballAttack {

    private static final double MP_COST = 2.0;

    @Override
    public double getMpCost() { return MP_COST; }
}
