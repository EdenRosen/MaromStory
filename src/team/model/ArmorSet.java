package team.model;

/**
 * Lists armor items sold in the shop
 */
public enum ArmorSet {
    LEATHER_SET ("Leather Set",   20,  0,  2,  5, 120, "Basic protection"),
    CHAIN_MAIL  ("Chain Mail",    45,  0,  4, 14, 400, "Solid iron links"),
    BATTLE_PLATE("Battle Plate",  90,  0,  8, 28, 950, "Heavy warrior armor"),
    MAGE_ROBE   ("Mage Robe",     35, 35, 10, 10, 750, "Enhances arcane power"),
    VOID_ARMOR  ("Void Armor",   130, 25, 14, 45,2500, "Forged in the cosmos");

    public final String name;
    public final int hpBonus, mpBonus, strBonus, defBonus, price;
    public final String desc;

    ArmorSet(String name, int hpBonus, int mpBonus, int strBonus, int defBonus, int price, String desc) {
        this.name     = name;
        this.hpBonus  = hpBonus;
        this.mpBonus  = mpBonus;
        this.strBonus = strBonus;
        this.defBonus = defBonus;
        this.price    = price;
        this.desc     = desc;
    }
}
