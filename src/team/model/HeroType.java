package team.model;

/**
 * סוגי הדמויות שהשחקן יכול לבחור מהן במסך הפתיחה.
 * כל סוג מקבל סט סקילים אחר ב-Canvas.initCanvas().
 *   WARRIOR — לוחם מגע: Basic + Slash (דורש חרב)
 *   MAGE    — קוסם טווח: Basic + Fireball (קסם, ללא חרב)
 */
public enum HeroType {
    WARRIOR,
    MAGE
}
