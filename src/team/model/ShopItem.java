package team.model;

public enum ShopItem {
    WORN_DAGGER    ("Worn Dagger",      5,   60, "A basic starter blade"),
    IRON_SWORD     ("Iron Sword",      15,  220, "Solid forged iron"),
    SILVER_BLADE   ("Silver Blade",    30,  550, "Sharp enchanted edge"),
    DEMON_BLADE    ("Demon Blade",     55, 1200, "Hellfire forged steel"),
    VOID_REAVER    ("Void Reaver",     90, 2800, "Blade of the cosmos");

    public final String name;
    public final int    strBonus;
    public final int    price;
    public final String desc;

    ShopItem(String name, int strBonus, int price, String desc) {
        this.name     = name;
        this.strBonus = strBonus;
        this.price    = price;
        this.desc     = desc;
    }
}
