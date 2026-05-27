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
import team.model.MapRect;

public class DrawingPanel extends JPanel {
    private static final String BACKGROUND_IMAGE_ID = "99";
    private static final Image PLAYER_IMAGE = ImageElement.loadImage("resources/Player1.png");
    private static final Image ENEMY_HENRY1 = ImageElement.loadImage("resources/EnemyHenry1.png");
    private static final Image ENEMY_HENRY2 = ImageElement.loadImage("resources/EnemyHenry2.png");
    private static final Image ENEMY_HENRY3 = ImageElement.loadImage("resources/EnemyHenry3.png");

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
    }

    private void renderStartScreen(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // רקע כהה
        g2d.setColor(new Color(15, 10, 30));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // כותרת
        g2d.setFont(new Font("Arial", Font.BOLD, 64));
        g2d.setColor(new Color(255, 210, 50));
        String title = "MaromQuest";
        FontMetrics fm = g2d.getFontMetrics();
        int tx = (getWidth() - fm.stringWidth(title)) / 2;
        g2d.drawString(title, tx, getHeight() / 2 - 60);

        // כיתוב Press Enter
        g2d.setFont(new Font("Arial", Font.PLAIN, 26));
        g2d.setColor(new Color(200, 200, 200));
        String prompt = "Press ENTER to Start";
        fm = g2d.getFontMetrics();
        int px = (getWidth() - fm.stringWidth(prompt)) / 2;
        g2d.drawString(prompt, px, getHeight() / 2 + 20);

        // הוראות
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.setColor(new Color(140, 140, 160));
        String controls = "Arrow Keys: Move   |   Up: Jump   |   Z: Pickup   |   C: Attack   |   1/2: Switch Skill";
        fm = g2d.getFontMetrics();
        int cx = (getWidth() - fm.stringWidth(controls)) / 2;
        g2d.drawString(controls, cx, getHeight() / 2 + 70);
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

    private Image getEnemyImage(String type) {
        if ("Henry1".equals(type)) return ENEMY_HENRY1;
        if ("Henry2".equals(type)) return ENEMY_HENRY2;
        if ("Henry3".equals(type)) return ENEMY_HENRY3;
        return ENEMY_HENRY1;
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

            if ("Henry3".equals(enemy.getType())) {
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
        } else {
            renderBasicAttackAnimation(g, player);
        }
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
        boolean isSlash  = "Slash Attack".equals(skillName);

        // מיקום: פינה ימנית למטה
        int boxW = 130, boxH = 50;
        int boxX = getWidth() - boxW - 15;
        int boxY = getHeight() - boxH - 15;

        // רקע
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRoundRect(boxX, boxY, boxW, boxH, 10, 10);

        // מסגרת צבעונית לפי skill
        g2d.setColor(isSlash ? new Color(180, 100, 255) : new Color(80, 180, 255));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(boxX, boxY, boxW, boxH, 10, 10);

        // כיתוב
        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString("Active Skill [1/2]:", boxX + 8, boxY + 17);

        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(isSlash ? new Color(210, 150, 255) : new Color(130, 210, 255));
        g2d.drawString(skillName, boxX + 8, boxY + 38);
    }

    private void renderMainPlayer(Graphics g) {
        team.model.MainPlayer player = UiPort.getInstance().getMainPlayer();
        if (player == null) return;

        Graphics2D g2d = (Graphics2D) g;
        if (!isImageLoaded(PLAYER_IMAGE)) return;
        Image playerImage = PLAYER_IMAGE;

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

        int panelX = 10, panelY = 10, panelW = 160, panelH = 110;

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(panelX, panelY, panelW, panelH, 12, 12);

        g2d.setFont(new Font("Arial", Font.BOLD, 13));

        g2d.setColor(new Color(220, 60, 60));
        g2d.drawString("HP:  " + (int) stats.getHealth() + " / " + (int) stats.getMaxHealth(), panelX + 10, panelY + 25);

        g2d.setColor(new Color(60, 140, 220));
        g2d.drawString("MP:  " + (int) stats.getEnergy() + " / " + (int) stats.getMaxEnergy(), panelX + 10, panelY + 48);

        g2d.setColor(new Color(230, 140, 30));
        g2d.drawString("STR: " + (int) stats.getStrength(), panelX + 10, panelY + 71);

        g2d.setColor(new Color(60, 200, 80));
        g2d.drawString("AGI: " + (int) stats.getAgility(), panelX + 10, panelY + 94);
    }
}
