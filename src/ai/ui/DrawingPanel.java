package ai.ui;

import base.Params;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.Map;
import javax.swing.*;
import my_base.App;
import shared.MainRouter;
import shared.ui_ports.UiPort;
import team.model.EnemyType;
import team.model.HeroType;
import team.model.MapRect;

public class DrawingPanel extends JPanel {
    private static final String BACKGROUND_IMAGE_ID = "99";
    private static final Image PLAYER_IMAGE = ImageElement.loadImage("resources/Player1.png");
    private static final Image ENEMY_HENRY1 = ImageElement.loadImage("resources/EnemyHenry1.png");
    private static final Image ENEMY_HENRY2 = ImageElement.loadImage("resources/EnemyHenry2.png");
    private static final Image ENEMY_HENRY3 = ImageElement.loadImage("resources/EnemyHenry3.png");
    private static final Image MAGE_IMAGE   = ImageElement.loadImage("resources/Mage.png");

    private Map<String, ImageElement> images;
    private MainRouter mainRouter;
    private boolean gameStarted = false;

    public void setGameStarted(boolean started) {
        this.gameStarted = started;
        repaint();
    }

    public DrawingPanel(Map<String, ImageElement> images, MainRouter mainRouter) {
        this.images      = images;
        this.mainRouter  = mainRouter;

        setFocusable(true);
        setFocusTraversalKeysEnabled(false); // כדי ש-TAB יגיע ל-KeyListener (בחירת דמות)
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                String key = getKeyName(e.getKeyCode());
                if (key != null) {
                    mainRouter.route("/system/key/down", Params.of(key));
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                String key = getKeyName(e.getKeyCode());
                if (key != null) {
                    mainRouter.route("/system/key/up", Params.of(key));
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    private String getKeyName(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_UP:    return "up";
            case KeyEvent.VK_DOWN:  return "down";
            case KeyEvent.VK_LEFT:  return "left";
            case KeyEvent.VK_RIGHT: return "right";
            case KeyEvent.VK_Z:     return "pickup";
            case KeyEvent.VK_X:     return "throw";
            case KeyEvent.VK_C:      return "attack";
            case KeyEvent.VK_1:      return "skill1";
            case KeyEvent.VK_2:      return "skill2";
            case KeyEvent.VK_TAB:    return "selectHero";
            case KeyEvent.VK_ENTER:  return "start";
            default: return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        try { UiPort.getInstance(); } catch (IllegalStateException e) { return; }

        if (!gameStarted) {
            renderStartScreen(g);
            return;
        }

        renderBackground(g);
        renderMap(g);
        renderImages(g);
        renderSword(g);
        renderEnemies(g);
        renderMainPlayer(g);
        renderEquippedSword(g);
        renderAttackAnimation(g);
        renderPlayerStats(g);
        renderActiveSkillHUD(g);

        // US-2 — מסך Game Over מעל העולם הקפוא
        if (App.content().backend().isGameOver()) {
            renderGameOver(g);
        }
    }

    private void renderGameOver(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // שכבת כהות חצי-שקופה מעל המשחק
        g2d.setColor(new Color(0, 0, 0, 175));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // כותרת GAME OVER
        g2d.setFont(new Font("Arial", Font.BOLD, 72));
        g2d.setColor(new Color(220, 50, 50));
        String title = "GAME OVER";
        FontMetrics fm = g2d.getFontMetrics();
        int tx = (getWidth() - fm.stringWidth(title)) / 2;
        g2d.drawString(title, tx, getHeight() / 2 - 30);

        // הוראת אתחול
        g2d.setFont(new Font("Arial", Font.PLAIN, 24));
        g2d.setColor(new Color(220, 220, 220));
        String prompt = "Press ENTER or Reset to play again";
        fm = g2d.getFontMetrics();
        int px = (getWidth() - fm.stringWidth(prompt)) / 2;
        g2d.drawString(prompt, px, getHeight() / 2 + 30);
    }

    // ---------- מסך פתיחה / בחירת דמות ----------

    private HeroType selectedHero() {
        return App.content().canvas().getSelectedHero();
    }

    private String heroName(HeroType h)  { return h == HeroType.MAGE ? "MAGE" : "WARRIOR"; }
    private String heroRole(HeroType h)  { return h == HeroType.MAGE ? "Ranged Spellcaster" : "Melee Fighter"; }
    private Image  heroImage(HeroType h) { return h == HeroType.MAGE ? MAGE_IMAGE : PLAYER_IMAGE; }
    private Color  heroAccent(HeroType h){ return h == HeroType.MAGE ? new Color(150, 120, 255) : new Color(255, 165, 60); }
    private String[] heroSkills(HeroType h) {
        return h == HeroType.MAGE
            ? new String[]{ "1  Basic Attack", "2  Fireball  (ranged magic)" }
            : new String[]{ "1  Basic Attack", "2  Slash  (needs a sword)" };
    }

    private void renderStartScreen(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth(), h = getHeight();

        // רקע — גרדיאנט אנכי + נצנוצי כוכבים
        g2d.setPaint(new GradientPaint(0, 0, new Color(18, 12, 34), 0, h, new Color(36, 24, 64)));
        g2d.fillRect(0, 0, w, h);
        g2d.setColor(new Color(255, 255, 255, 22));
        for (int i = 0; i < 70; i++) {
            int sx = (i * 97 + 13) % w;
            int sy = (i * 53 + 29) % (h / 2);
            int s = (i % 3 == 0) ? 2 : 1;
            g2d.fillOval(sx, sy, s, s);
        }

        // כותרת עם הילה
        g2d.setFont(new Font("Arial", Font.BOLD, 60));
        String title = "MaromQuest";
        FontMetrics fm = g2d.getFontMetrics();
        int tx = (w - fm.stringWidth(title)) / 2;
        int ty = 110;
        g2d.setColor(new Color(255, 180, 40, 60));
        g2d.drawString(title, tx + 2, ty + 2);
        g2d.setColor(new Color(255, 212, 64));
        g2d.drawString(title, tx, ty);

        g2d.setFont(new Font("Arial", Font.PLAIN, 22));
        g2d.setColor(new Color(200, 195, 220));
        String sub = "Choose your hero";
        g2d.drawString(sub, (w - g2d.getFontMetrics().stringWidth(sub)) / 2, ty + 40);

        // שני כרטיסים
        HeroType[] heroes = HeroType.values();
        int cardW = 230, cardH = 310, gap = 50;
        int totalW = heroes.length * cardW + (heroes.length - 1) * gap;
        int startX = (w - totalW) / 2;
        int cardY = ty + 75;

        for (int i = 0; i < heroes.length; i++) {
            int cx = startX + i * (cardW + gap);
            renderHeroCard(g2d, heroes[i], cx, cardY, cardW, cardH, heroes[i] == selectedHero());
        }

        // כיתובי הוראות
        int footY = cardY + cardH + 55;
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        String go = "ENTER  —  Start            TAB  —  Switch Hero";
        g2d.setColor(new Color(255, 224, 120));
        g2d.drawString(go, (w - g2d.getFontMetrics().stringWidth(go)) / 2, footY);

        g2d.setFont(new Font("Arial", Font.PLAIN, 15));
        g2d.setColor(new Color(150, 145, 170));
        String controls = "Arrows: Move    Up: Jump    Z: Pickup    X: Throw    C: Attack    1/2: Switch Skill";
        g2d.drawString(controls, (w - g2d.getFontMetrics().stringWidth(controls)) / 2, footY + 30);
    }

    private void renderHeroCard(Graphics2D g2d, HeroType hero, int x, int y, int cw, int ch, boolean selected) {
        Color accent = heroAccent(hero);

        // הילה לכרטיס הנבחר
        if (selected) {
            for (int r = 14; r > 0; r -= 2) {
                g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 10));
                g2d.fillRoundRect(x - r, y - r, cw + 2 * r, ch + 2 * r, 26 + r, 26 + r);
            }
        }

        // גוף הכרטיס
        g2d.setColor(selected ? new Color(44, 36, 74) : new Color(28, 22, 46));
        g2d.fillRoundRect(x, y, cw, ch, 22, 22);
        g2d.setStroke(new BasicStroke(selected ? 3.5f : 1.5f));
        g2d.setColor(selected ? accent : new Color(80, 72, 110));
        g2d.drawRoundRect(x, y, cw, ch, 22, 22);

        // שם
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        g2d.setColor(selected ? Color.WHITE : new Color(170, 162, 195));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(heroName(hero), x + (cw - fm.stringWidth(heroName(hero))) / 2, y + 38);

        // תפקיד
        g2d.setFont(new Font("Arial", Font.ITALIC, 15));
        g2d.setColor(accent);
        String role = heroRole(hero);
        g2d.drawString(role, x + (cw - g2d.getFontMetrics().stringWidth(role)) / 2, y + 60);

        // ספרייט
        Image sprite = heroImage(hero);
        if (isImageLoaded(sprite)) {
            drawImageFit(g2d, sprite, x + cw / 2 - 55, y + 72, 110, 130, false);
        }

        // קו מפריד
        g2d.setColor(new Color(255, 255, 255, 30));
        g2d.drawLine(x + 24, y + 212, x + cw - 24, y + 212);

        // סקילים
        g2d.setFont(new Font("Arial", Font.PLAIN, 15));
        String[] skills = heroSkills(hero);
        for (int i = 0; i < skills.length; i++) {
            g2d.setColor(selected ? new Color(225, 222, 235) : new Color(150, 145, 170));
            g2d.drawString(skills[i], x + 24, y + 240 + i * 24);
        }

        // תג "SELECTED"
        if (selected) {
            g2d.setFont(new Font("Arial", Font.BOLD, 13));
            String tag = "SELECTED";
            int tw = g2d.getFontMetrics().stringWidth(tag);
            int pillW = tw + 38, pillX = x + (cw - pillW) / 2, pillY = y + ch - 30;
            g2d.setColor(accent);
            g2d.fillRoundRect(pillX, pillY, pillW, 22, 11, 11);
            // משולש קטן במקום תו יוניקוד
            g2d.setColor(new Color(20, 16, 34));
            int ax = pillX + 12, ay = pillY + 11;
            g2d.fillPolygon(new int[]{ax, ax + 7, ax}, new int[]{ay - 5, ay, ay + 5}, 3);
            g2d.drawString(tag, pillX + 22, y + ch - 14);
        }
    }

    private void renderBackground(Graphics g) {
        ImageElement background = images.get(BACKGROUND_IMAGE_ID);
        if (background == null || !background.visible || !background.isLoaded()) return;

        Graphics2D g2d = (Graphics2D) g;
        RenderingHints oldHints = g2d.getRenderingHints();

        int sourceWidth = background.image.getWidth(this);
        int sourceHeight = background.image.getHeight(this);
        if (sourceWidth <= 0 || sourceHeight <= 0) return;

        double scale = Math.max(getWidth() / (double) sourceWidth, getHeight() / (double) sourceHeight);
        int drawWidth = (int) Math.round(sourceWidth * scale);
        int drawHeight = (int) Math.round(sourceHeight * scale);
        int drawX = (getWidth() - drawWidth) / 2;
        int drawY = (getHeight() - drawHeight) / 2;

        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(background.image, drawX, drawY, drawWidth, drawHeight, this);
        g2d.setRenderingHints(oldHints);
    }

    private void renderImages(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        for (Map.Entry<String, ImageElement> entry : images.entrySet()) {
            if (BACKGROUND_IMAGE_ID.equals(entry.getKey())) continue;

            ImageElement img = entry.getValue();
            if (img.isLoaded() && img.visible) {
                AffineTransform old = g2d.getTransform();
                g2d.translate(img.x + img.width / 2.0, img.y + img.height / 2.0);
                g2d.rotate(img.angle);
                g2d.drawImage(img.image, -img.width / 2, -img.height / 2, img.width, img.height, this);
                g2d.setTransform(old);
            }
        }
    }

    private void renderMap(Graphics g) {
        team.model.Map currentMap = UiPort.getInstance().getMap();
        if (currentMap == null || currentMap.getRectangles() == null) return;

        Graphics2D g2d = (Graphics2D) g;
        for (MapRect rect : currentMap.getRectangles()) {
            g2d.setColor(new Color(120, 220, 120));
            g2d.fillRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
        }
    }

    // חרב ביד השחקן — מוצגת כשהשחקן אוחז בה, מסתובבת עם הכיוון
    private void renderEquippedSword(Graphics g) {
        team.model.MainPlayer player = UiPort.getInstance().getMainPlayer();
        if (player == null || !player.hasSword()) return;

        Graphics2D g2d = (Graphics2D) g;
        int px = (int) player.getX();
        int py = (int) player.getY();

        boolean facingRight = player.isFacingRight();

        AffineTransform old = g2d.getTransform();

        if (facingRight) {
            // חרב לצד ימין, מסובבת -45 מעלות
            g2d.translate(px + 39, py + 30);
            g2d.rotate(Math.toRadians(-35));

            g2d.setColor(new Color(192, 192, 192));
            g2d.fillRect(0, -3, 28, 6);

            g2d.setColor(new Color(139, 90, 43));
            g2d.fillRect(-8, -5, 10, 10);

            g2d.setColor(new Color(220, 220, 255));
            int[] xp = { 28, 36, 28 };
            int[] yp = { -3,  0,  3 };
            g2d.fillPolygon(xp, yp, 3);
        } else {
            // חרב לצד שמאל, מסובבת -135 מעלות (הופכת כיוון)
            g2d.translate(px + 11, py + 30);
            g2d.rotate(Math.toRadians(-145));

            g2d.setColor(new Color(192, 192, 192));
            g2d.fillRect(0, -3, 28, 6);

            g2d.setColor(new Color(139, 90, 43));
            g2d.fillRect(-8, -5, 10, 10);

            g2d.setColor(new Color(220, 220, 255));
            int[] xp = { 28, 36, 28 };
            int[] yp = { -3,  0,  3 };
            g2d.fillPolygon(xp, yp, 3);
        }

        g2d.setTransform(old);
    }

    private void renderSword(Graphics g) {
        team.model.Sword sword = my_base.App.content().canvas().getSword();
        if (sword == null || !sword.isOnGround()) return;

        Graphics2D g2d = (Graphics2D) g;
        int sx = (int) sword.getX();
        int sy = (int) sword.getY();

        // גוף החרב
        g2d.setColor(new Color(192, 192, 192));
        g2d.fillRect(sx, sy + 8, 30, 6);

        // ידית
        g2d.setColor(new Color(139, 90, 43));
        g2d.fillRect(sx - 6, sy + 6, 10, 10);

        // טיפ
        g2d.setColor(new Color(220, 220, 255));
        int[] xp = { sx + 30, sx + 38, sx + 30 };
        int[] yp = { sy + 8,  sy + 11, sy + 14 };
        g2d.fillPolygon(xp, yp, 3);

        // תווית Z להרמה
        g2d.setColor(Color.YELLOW);
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.drawString("[Z]", sx + 5, sy - 4);
    }

    private Image getEnemyImage(EnemyType type) {
        switch (type) {
            case SWIFT_HENRY: return ENEMY_HENRY1;
            case EVIL_HENRY:  return ENEMY_HENRY2;
            case GIANT_HENRY: return ENEMY_HENRY3;
            default:          return ENEMY_HENRY1;
        }
    }

    private void renderEnemies(Graphics g) {
        java.util.List<team.model.Enemy> enemies = App.content().canvas().getEnemies();
        if (enemies == null || enemies.isEmpty()) return;

        Graphics2D g2d = (Graphics2D) g;

        for (team.model.Enemy enemy : enemies) {
            Image img = getEnemyImage(enemy.getType());
            
            if (img == null || !isImageLoaded(img)) {
                img = ENEMY_HENRY1;
            }

            int ex = (int) enemy.getX();
            int ey = (int) enemy.getY();
            int ew = 70, eh = 82, eyOff = 32;

            if (enemy.getType() == EnemyType.GIANT_HENRY) {
                ew = 110; eh = 130; eyOff = 80;
            }

            if (enemy.isDying()) {
                renderEnemyDeathAnimation(g2d, enemy, img, ex, ey - (eyOff - 12), ew, eh);
                continue;
            }

            drawImageFit(g2d, img, ex, ey - eyOff, ew, eh, enemy.isFacingRight());
            renderEnemyHealthBar(g2d, enemy, ex, ey - eyOff, ew);
        }
    }

    private void renderEnemyDeathAnimation(Graphics2D g2d, team.model.Enemy enemy, Image enemyImage, int x, int y, int w, int h) {
        Composite oldComposite = g2d.getComposite();
        Stroke oldStroke = g2d.getStroke();

        float progress = 1.0f - enemy.getDeathAnimationTicks() / (float) team.model.Enemy.DEATH_ANIMATION_TICKS;
        float fade = Math.max(0.15f, 1.0f - progress);
        int spread = (int) (progress * (w / 2.5));

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        g2d.drawImage(enemyImage, x, y + (int) (progress * 14), w, h - 12, null);

        g2d.setComposite(oldComposite);
        g2d.setColor(new Color(150, 0, 0, 210));
        g2d.fillOval(x + (w / 4) - spread / 2, y + h - 10, 36 + spread, 13);
        g2d.fillOval(x + (w / 8) - spread / 3, y + h - 4, 18 + spread / 2, 8);
        g2d.fillOval(x + (int) (w * 0.64), y + h - 5, 16 + spread / 3, 7);

        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(205, 20, 20, 190));
        g2d.drawLine(x + (w / 2), y + (int) (h * 0.65), x + (int) (w * 0.34) - spread / 3, y + h - 2);
        g2d.drawLine(x + (int) (w * 0.6), y + (int) (h * 0.68), x + (int) (w * 0.78) + spread / 3, y + h);
        g2d.drawLine(x + (int) (w * 0.43), y + (int) (h * 0.73), x + (int) (w * 0.45), y + h + 3);

        g2d.setStroke(oldStroke);
        g2d.setComposite(oldComposite);
    }

    private void renderEnemyHealthBar(Graphics2D g2d, team.model.Enemy enemy, int x, int y, int barWidth) {
        team.model.PlayerStats stats = enemy.getStats();
        int barHeight = 8;
        int barX = x;
        int barY = y - 14;
        double healthRatio = stats.getHealth() / stats.getMaxHealth();
        int fillWidth = (int) (barWidth * healthRatio);

        g2d.setColor(new Color(0, 0, 0, 170));
        g2d.fillRect(barX - 1, barY - 1, barWidth + 2, barHeight + 2);

        g2d.setColor(new Color(90, 20, 20));
        g2d.fillRect(barX, barY, barWidth, barHeight);

        g2d.setColor(new Color(220, 50, 50));
        g2d.fillRect(barX, barY, fillWidth, barHeight);

        g2d.setColor(Color.WHITE);
        g2d.drawRect(barX, barY, barWidth, barHeight);
    }

    private void renderAttackAnimation(Graphics g) {
        team.model.MainPlayer player = UiPort.getInstance().getMainPlayer();
        if (player == null || !player.isAttacking()) return;

        String anim = player.getCurrentAnimation();
        if ("Slash Attack".equals(anim)) {
            renderSlashAnimation(g, player);
        } else if ("Fireball".equals(anim)) {
            renderFireballAnimation(g, player);
        } else {
            renderBasicAttackAnimation(g, player);
        }
    }

    // אנימציית כדור-אש — כדור זוהר שטס קדימה עם זנב להבה
    private void renderFireballAnimation(Graphics g, team.model.MainPlayer player) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ticks    = player.getAttackAnimationTicks();
        int maxTicks = team.model.MainPlayer.ATTACK_ANIMATION_TICKS;
        float progress = 1.0f - (float) ticks / maxTicks;   // 0→1 ככל שהאנימציה מתקדמת

        boolean right = player.isFacingRight();
        int dir   = right ? 1 : -1;
        int startX = (int) player.getX() + (right ? 42 : 8);
        int y      = (int) player.getY() + 18;
        int travel = 160;
        int cx = startX + (int) (dir * progress * travel);

        // זנב להבה (כדורים דועכים לאחור)
        for (int i = 5; i >= 1; i--) {
            int tx = cx - dir * i * 11;
            int r  = Math.max(2, 15 - i * 2);
            int a  = Math.max(0, 150 - i * 26);
            g2d.setColor(new Color(255, 110 + i * 8, 28, a));
            g2d.fillOval(tx - r, y - r, r * 2, r * 2);
        }

        // הילה חיצונית
        g2d.setColor(new Color(255, 120, 30, 150));
        g2d.fillOval(cx - 17, y - 17, 34, 34);
        // טבעת אש
        g2d.setColor(new Color(255, 80, 20, 200));
        g2d.fillOval(cx - 13, y - 13, 26, 26);
        // ליבה לוהטת
        g2d.setColor(new Color(255, 232, 150));
        g2d.fillOval(cx - 7, y - 7, 14, 14);
        g2d.setColor(Color.WHITE);
        g2d.fillOval(cx - 3, y - 3, 6, 6);
    }

    private void renderBasicAttackAnimation(Graphics g, team.model.MainPlayer player) {
        Graphics2D g2d = (Graphics2D) g;
        Stroke oldStroke = g2d.getStroke();

        int px = (int) player.getX();
        int py = (int) player.getY();
        int arcX = player.isFacingRight() ? px + 34 : px - 42;
        int arcY = py + 6;
        int startAngle = player.isFacingRight() ? -45 : 135;
        int alpha = 80 + player.getAttackAnimationTicks() * 18;

        g2d.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(240, 245, 255, Math.min(220, alpha)));
        g2d.drawArc(arcX, arcY, 58, 48, startAngle, player.isFacingRight() ? 110 : -110);

        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(120, 190, 255, Math.min(180, alpha)));
        g2d.drawArc(arcX + 6, arcY + 6, 46, 36, startAngle, player.isFacingRight() ? 100 : -100);

        g2d.setStroke(oldStroke);
    }

    private void renderSlashAnimation(Graphics g, team.model.MainPlayer player) {
        Graphics2D g2d = (Graphics2D) g;
        Stroke oldStroke = g2d.getStroke();
        AffineTransform oldTransform = g2d.getTransform();

        int px = (int) player.getX() + 25;
        int py = (int) player.getY() + 25;

        int ticks     = player.getAttackAnimationTicks();
        int maxTicks  = team.model.MainPlayer.ATTACK_ANIMATION_TICKS;
        float progress = 1.0f - (float) ticks / maxTicks; // 0→1 כשהאנימציה מתקדמת

        // זווית סיבוב — החרב עוברת 180 מעלות
        double baseAngle = player.isFacingRight() ? -Math.PI / 2 : Math.PI / 2;
        double sweep     = player.isFacingRight() ? Math.PI : -Math.PI;
        double angle     = baseAngle + sweep * progress;

        int swordLen = 55;
        int alpha = Math.max(60, 255 - (int)(progress * 200));

        // קו החרב המסתובב
        g2d.translate(px, py);
        g2d.rotate(angle);

        g2d.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(200, 220, 255, alpha));
        g2d.drawLine(0, 0, swordLen, 0);

        // טיפ זוהר
        g2d.setColor(new Color(255, 255, 255, alpha));
        g2d.fillOval(swordLen - 5, -5, 10, 10);

        g2d.setTransform(oldTransform);

        // קשת זנב אחרי החרב
        int arcSize = swordLen * 2 + 10;
        int arcX = px - arcSize / 2;
        int arcY = py - arcSize / 2;
        int startDeg = (int) Math.toDegrees(baseAngle);
        int sweepDeg = (int) (Math.toDegrees(sweep) * progress);

        g2d.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2d.setColor(new Color(150, 200, 255, Math.min(160, alpha)));
        g2d.drawArc(arcX, arcY, arcSize, arcSize, startDeg, sweepDeg);

        g2d.setStroke(oldStroke);
    }

    private void renderActiveSkillHUD(Graphics g) {
        team.model.MainPlayer player = UiPort.getInstance().getMainPlayer();
        if (player == null) return;

        Graphics2D g2d = (Graphics2D) g;
        String skillName = player.getActiveAttackName();
        Color theme = skillColor(skillName);

        // מיקום: פינה ימנית למטה
        int boxW = 140, boxH = 50;
        int boxX = getWidth() - boxW - 15;
        int boxY = getHeight() - boxH - 15;

        // רקע
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRoundRect(boxX, boxY, boxW, boxH, 10, 10);

        // מסגרת צבעונית לפי skill
        g2d.setColor(theme);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(boxX, boxY, boxW, boxH, 10, 10);

        // כיתוב
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString("Active Skill [1/2]:", boxX + 8, boxY + 17);

        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(theme.brighter());
        g2d.drawString(skillName, boxX + 8, boxY + 38);
    }

    // צבע נושא לכל סקיל — Slash סגול, Fireball כתום, Basic כחול
    private Color skillColor(String skillName) {
        if ("Slash Attack".equals(skillName)) return new Color(180, 100, 255);
        if ("Fireball".equals(skillName))     return new Color(255, 140, 50);
        return new Color(80, 180, 255);
    }

    private void renderMainPlayer(Graphics g) {
        team.model.MainPlayer player = UiPort.getInstance().getMainPlayer();
        if (player == null) return;

        Graphics2D g2d = (Graphics2D) g;
        Image playerImage = heroImage(selectedHero());
        if (!isImageLoaded(playerImage)) playerImage = PLAYER_IMAGE;
        if (!isImageLoaded(playerImage)) return;

        int px = (int) player.getX();
        int py = (int) player.getY();

        if (player.isFacingRight()) {
            // הופך אופקית — התמונה המקורית פונה שמאלה
            drawImageFit(g2d, playerImage, px, py - 15, 50, 65, true);
        } else {
            drawImageFit(g2d, playerImage, px, py - 15, 50, 65, false);
        }

        renderMainPlayerHealthBar(g2d, player, px, py);
    }

    private boolean isImageLoaded(Image image) {
        return image != null && image.getWidth(this) > 0 && image.getHeight(this) > 0;
    }

    private void drawImageFit(Graphics2D g2d, Image image, int x, int y, int boxWidth, int boxHeight, boolean flipHorizontal) {
        int sourceWidth = image.getWidth(this);
        int sourceHeight = image.getHeight(this);
        if (sourceWidth <= 0 || sourceHeight <= 0) return;

        double scale = Math.min(boxWidth / (double) sourceWidth, boxHeight / (double) sourceHeight);
        int drawWidth = (int) Math.round(sourceWidth * scale);
        int drawHeight = (int) Math.round(sourceHeight * scale);
        int drawX = x + (boxWidth - drawWidth) / 2;
        int drawY = y + boxHeight - drawHeight;

        RenderingHints oldHints = g2d.getRenderingHints();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (flipHorizontal) {
            g2d.drawImage(image, drawX + drawWidth, drawY, -drawWidth, drawHeight, this);
        } else {
            g2d.drawImage(image, drawX, drawY, drawWidth, drawHeight, this);
        }

        g2d.setRenderingHints(oldHints);
    }

    private void renderMainPlayerHealthBar(Graphics2D g2d, team.model.MainPlayer player, int x, int y) {
        team.model.PlayerStats stats = player.getStats();
        int barWidth = 50;
        int barHeight = 7;
        int barX = x;
        int barY = y - 12;
        double healthRatio = stats.getHealth() / stats.getMaxHealth();
        int fillWidth = (int) (barWidth * healthRatio);

        g2d.setColor(new Color(0, 0, 0, 170));
        g2d.fillRect(barX - 1, barY - 1, barWidth + 2, barHeight + 2);

        g2d.setColor(new Color(90, 20, 20));
        g2d.fillRect(barX, barY, barWidth, barHeight);

        g2d.setColor(new Color(50, 220, 80));
        g2d.fillRect(barX, barY, fillWidth, barHeight);

        g2d.setColor(Color.WHITE);
        g2d.drawRect(barX, barY, barWidth, barHeight);

        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        g2d.drawString(String.valueOf((int) stats.getHealth()), barX + 18, barY - 2);
    }

    private void renderPlayerStats(Graphics g) {
        team.model.MainPlayer player = UiPort.getInstance().getMainPlayer();
        if (player == null || player.getStats() == null) return;

        team.model.PlayerStats stats = player.getStats();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int panelX = 10, panelY = 10, panelW = 190, panelH = 168;

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(panelX, panelY, panelW, panelH, 12, 12);

        g2d.setFont(new Font("Arial", Font.BOLD, 13));

        g2d.setColor(new Color(220, 60, 60));
        g2d.drawString("HP:  " + (int) stats.getHealth() + " / " + (int) stats.getMaxHealth(), panelX + 10, panelY + 24);

        g2d.setColor(new Color(60, 140, 220));
        g2d.drawString("MP:  " + (int) stats.getEnergy() + " / " + (int) stats.getMaxEnergy(), panelX + 10, panelY + 45);

        g2d.setColor(new Color(230, 140, 30));
        g2d.drawString("STR: " + (int) stats.getStrength(), panelX + 10, panelY + 66);

        g2d.setColor(new Color(60, 200, 80));
        g2d.drawString("AGI: " + (int) stats.getAgility(), panelX + 10, panelY + 87);

        // קו מפריד
        g2d.setColor(new Color(255, 255, 255, 40));
        g2d.drawLine(panelX + 10, panelY + 98, panelX + panelW - 10, panelY + 98);

        // רמה + מטבעות
        team.model.PlayerProgress prog = player.getProgress();

        g2d.setFont(new Font("Arial", Font.BOLD, 15));
        g2d.setColor(new Color(180, 150, 255));
        g2d.drawString("LV " + prog.getLevel(), panelX + 10, panelY + 122);

        // אייקון מטבע + ספירה (צד ימין)
        int coinX = panelX + panelW - 78, coinY = panelY + 110;
        g2d.setColor(new Color(170, 130, 20));
        g2d.fillOval(coinX, coinY, 16, 16);
        g2d.setColor(new Color(255, 215, 60));
        g2d.fillOval(coinX + 2, coinY + 2, 12, 12);
        g2d.setColor(new Color(150, 110, 10));
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.drawString("$", coinX + 5, coinY + 12);
        g2d.setColor(new Color(255, 220, 90));
        g2d.setFont(new Font("Arial", Font.BOLD, 15));
        g2d.drawString(String.valueOf(prog.getCoins()), coinX + 22, panelY + 123);

        // מד XP
        int barX = panelX + 10, barY = panelY + 134, barW = panelW - 20, barH = 14;
        g2d.setColor(new Color(38, 38, 50));
        g2d.fillRoundRect(barX, barY, barW, barH, 7, 7);
        int fill = (int) (barW * Math.min(1.0, prog.getXp() / (double) prog.getXpToNext()));
        g2d.setColor(new Color(150, 120, 255));
        g2d.fillRoundRect(barX, barY, fill, barH, 7, 7);
        g2d.setColor(new Color(255, 255, 255, 90));
        g2d.drawRoundRect(barX, barY, barW, barH, 7, 7);

        String xpText = "XP  " + prog.getXp() + " / " + prog.getXpToNext();
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.setColor(Color.WHITE);
        g2d.drawString(xpText, barX + (barW - fm.stringWidth(xpText)) / 2, barY + 11);
    }
}
