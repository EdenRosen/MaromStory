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
import team.control.GameState;
import team.model.EnemyType;
import team.model.GameMode;
import team.model.HeroType;
import team.model.MapRect;
import team.model.MapType;

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
                    repaint();
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
                if (!SwingUtilities.isLeftMouseButton(e)) return;
                requestFocusInWindow();
                
                GameState state = App.content().backend().getState();
                if (state == GameState.HERO_PLAYER1_SELECT
                        || state == GameState.HERO_PLAYER2_SELECT) {
                    handleHeroCardClick(e.getX(), e.getY());
                } else if (state == GameState.GAME_MODE_SELECT) {
                    handleModeCardClick(e.getX(), e.getY());
                } else if (state == GameState.PVP_MAP_SELECT) {
                    handlePvpMapClick(e.getX(), e.getY());
                }
                repaint();
            }
        });
    }

    private String getKeyName(int keyCode) {
        switch (keyCode) {
            // Player 1 (Arrow Keys)
            case KeyEvent.VK_UP:    return "up";
            case KeyEvent.VK_DOWN:  return "down";
            case KeyEvent.VK_LEFT:  return "left";
            case KeyEvent.VK_RIGHT: return "right";
            
            // Player 2 (WASD)
            case KeyEvent.VK_W:     return "up_p2";
            case KeyEvent.VK_A:     return "left_p2";
            case KeyEvent.VK_S:     return "down_p2";
            case KeyEvent.VK_D:     return "right_p2";
            
            // Player 1 actions
            case KeyEvent.VK_SPACE: return "attack";       // P1 attack
            case KeyEvent.VK_N:     return "pickup";       // P1 pickup
            case KeyEvent.VK_M:     return "nextAttack";   // P1 change attack style
            case KeyEvent.VK_T:     return "throw";        // P1 throw sword
            
            // Player 2 actions
            case KeyEvent.VK_R:     return "attack_p2";    // P2 attack
            case KeyEvent.VK_Q:     return "pickup_p2";    // P2 pickup
            case KeyEvent.VK_E:     return "nextAttack_p2";// P2 change attack style
            case KeyEvent.VK_F:     return "throw_p2";     // P2 throw sword
            
            // Shared menu controls
            case KeyEvent.VK_C:     return "upgradePanel";
            case KeyEvent.VK_1:     return "skill1";
            case KeyEvent.VK_2:     return "skill2";
            case KeyEvent.VK_3:     return "skill3";
            case KeyEvent.VK_4:     return "skill4";
            case KeyEvent.VK_5:     return "skill5";
            case KeyEvent.VK_B:     return "mapSelect";
            case KeyEvent.VK_SLASH: return "shop";
            
            // Selection / Navigation
            case KeyEvent.VK_TAB:   return getTabKeyName();
            case KeyEvent.VK_ENTER: return "start";
            
            default: return null;
        }
    }
    
    private String getTabKeyName() {
        // TAB behavior depends on state
        switch (App.content().backend().getState()) {
            case HERO_PLAYER1_SELECT:
            case HERO_PLAYER2_SELECT:
                return "selectHero";
            case GAME_MODE_SELECT:
                return "selectGameMode";
            case SHOP:
                return "shopTab";
            default:
                return null;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        try { UiPort.getInstance(); } catch (IllegalStateException e) { return; }

        GameState state = App.content().backend().getState();
        boolean setupScreen = state == GameState.GAME_MODE_SELECT
                || state == GameState.HERO_PLAYER1_SELECT
                || state == GameState.HERO_PLAYER2_SELECT
                || state == GameState.PVP_MAP_SELECT;
        if (!gameStarted || setupScreen) {
            if (state == GameState.GAME_MODE_SELECT) {
                renderGameModeSelect(g);
            } else if (state == GameState.PVP_MAP_SELECT) {
                renderMapSelect(g);
            } else {
                renderStartScreen(g);
            }
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
        renderMapLabel(g);

        if (App.content().backend().isGameOver()) {
            renderGameOver(g);
        } else if (App.content().backend().isUpgrade()) {
            renderUpgradePanel(g);
        } else if (App.content().backend().isMapSelect()) {
            renderMapSelect(g);
        } else if (App.content().backend().isShop()) {
            renderShop(g);
        }
    }

    // תווית המפה הנוכחית + רמז למקש M
    private void renderMapLabel(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        MapType m = App.content().canvas().getCurrentMap();
        boolean pvp = App.content().canvas().getSelectedGameMode() == GameMode.PVP;
        String label = pvp ? "PvP Arena: " + m.displayName : "Map: " + m.displayName + "    [B] maps";
        g2d.setFont(new Font("Arial", Font.BOLD, 15));
        FontMetrics fm = g2d.getFontMetrics();
        int tw = fm.stringWidth(label);
        int x = (getWidth() - tw) / 2, y = 26;
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(x - 12, y - 18, tw + 24, 26, 12, 12);
        g2d.setColor(m == MapType.INFERNO ? new Color(255, 120, 90) : new Color(150, 230, 150));
        g2d.drawString(label, x, y);
    }

    // פאנל שדרוג נקודות — נפתח ב-C, מקפיא את המשחק ומאפשר להוציא נקודות שדרוג
    private void renderUpgradePanel(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight(), cx = w / 2;
        g2d.setColor(new Color(10, 8, 24, 210));
        g2d.fillRect(0, 0, w, h);

        g2d.setColor(new Color(150, 210, 255));
        g2d.setFont(new Font("Arial", Font.BOLD, 50));
        String title = "UPGRADE STATS";
        g2d.drawString(title, cx - g2d.getFontMetrics().stringWidth(title) / 2, h / 2 - 140);

        int pending = App.content().backend().getPendingUpgrades();
        g2d.setFont(new Font("Arial", Font.PLAIN, 22));
        g2d.setColor(pending > 0 ? new Color(255, 220, 120) : new Color(170, 165, 190));
        String sub = pending > 0 ? ("Points available: " + pending)
                                 : "No points — defeat enemies to earn XP";
        g2d.drawString(sub, cx - g2d.getFontMetrics().stringWidth(sub) / 2, h / 2 - 105);

        String[] keys  = { "1", "2", "3", "4" };
        String[] descs = { "+20 Max HP", "+10 Max MP", "+3 STR", "+3 AGI" };
        Color[]  acc   = { new Color(60, 200, 80), new Color(80, 160, 240), new Color(240, 150, 50), new Color(60, 200, 200) };

        int cardW = 170, cardH = 130, gap = 24;
        int totalW = keys.length * cardW + (keys.length - 1) * gap;
        int startX = cx - totalW / 2, cardY = h / 2 - 60;

        for (int i = 0; i < keys.length; i++) {
            int x = startX + i * (cardW + gap);
            g2d.setColor(new Color(30, 26, 52));
            g2d.fillRoundRect(x, cardY, cardW, cardH, 18, 18);
            g2d.setStroke(new BasicStroke(2.5f));
            g2d.setColor(acc[i]);
            g2d.drawRoundRect(x, cardY, cardW, cardH, 18, 18);

            // תג מקש
            g2d.fillRoundRect(x + cardW / 2 - 22, cardY + 18, 44, 40, 10, 10);
            g2d.setColor(new Color(20, 16, 34));
            g2d.setFont(new Font("Arial", Font.BOLD, 26));
            g2d.drawString(keys[i], x + cardW / 2 - g2d.getFontMetrics().stringWidth(keys[i]) / 2, cardY + 47);

            // תיאור השדרוג
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString(descs[i], x + cardW / 2 - g2d.getFontMetrics().stringWidth(descs[i]) / 2, cardY + 95);
        }

        g2d.setColor(new Color(160, 155, 180));
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        String hint = pending > 0 ? "Press 1 / 2 / 3 / 4 to spend a point        C to close"
                                  : "Press C to close";
        g2d.drawString(hint, cx - g2d.getFontMetrics().stringWidth(hint) / 2, cardY + cardH + 42);
    }

    // תפריט בחירת מפה — נפתח ב-M, מציג את כל המפות לבחירה במספרים
    private void renderMapSelect(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight(), cx = w / 2;
        boolean pvpSetup = App.content().backend().getState() == GameState.PVP_MAP_SELECT;

        g2d.setColor(new Color(8, 8, 20, 216));
        g2d.fillRect(0, 0, w, h);

        g2d.setColor(new Color(255, 220, 120));
        g2d.setFont(new Font("Arial", Font.BOLD, 50));
        String title = pvpSetup ? "CHOOSE PVP ARENA" : "SELECT MAP";
        g2d.drawString(title, cx - g2d.getFontMetrics().stringWidth(title) / 2, 130);

        g2d.setColor(new Color(190, 185, 210));
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        String sub = pvpSetup
                ? "Press 1–5 or click a map to start the duel"
                : "Press the number to travel  —  your progress carries over";
        g2d.drawString(sub, cx - g2d.getFontMetrics().stringWidth(sub) / 2, 162);

        MapType[] maps = MapType.values();
        MapType current = App.content().canvas().getCurrentMap();
        int n = maps.length, gap = 18;
        int cardW = Math.min(230, (w - 80 - gap * (n - 1)) / n);  // מתאים את עצמו לרוחב החלון
        int cardH = 160;
        int totalW = n * cardW + (n - 1) * gap;
        int startX = cx - totalW / 2, cardY = h / 2 - 70;

        for (int i = 0; i < maps.length; i++) {
            MapType m = maps[i];
            int x = startX + i * (cardW + gap);
            boolean unlocked = pvpSetup || App.content().backend().isMapUnlocked(i);
            Color accent = unlocked ? mapAccent(m) : new Color(100, 96, 115);
            boolean isCurrent = !pvpSetup && (m == current);

            g2d.setColor(new Color(26, 24, 44));
            g2d.fillRoundRect(x, cardY, cardW, cardH, 20, 20);
            g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 55));
            g2d.fillRoundRect(x, cardY, cardW, 48, 20, 20);
            g2d.setStroke(new BasicStroke(isCurrent ? 4f : 2f));
            g2d.setColor(accent);
            g2d.drawRoundRect(x, cardY, cardW, cardH, 20, 20);

            // תג מספר
            g2d.setColor(accent);
            g2d.fillOval(x + 14, cardY + 11, 30, 30);
            g2d.setColor(new Color(18, 14, 30));
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            String num = String.valueOf(i + 1);
            g2d.drawString(num, x + 29 - g2d.getFontMetrics().stringWidth(num) / 2, cardY + 32);

            // שם וקונספט
            g2d.setColor(unlocked ? Color.WHITE : new Color(155, 150, 165));
            g2d.setFont(new Font("Arial", Font.BOLD, 24));
            g2d.drawString(m.displayName, x + 54, cardY + 34);
            g2d.setColor(new Color(205, 200, 224));
            g2d.setFont(new Font("Arial", Font.ITALIC, 16));
            String concept = unlocked ? m.concept : "Clear previous stage";
            g2d.drawString(concept, x + 20, cardY + 78);

            // עוצמה — נקודות
            g2d.setColor(new Color(200, 196, 220));
            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
            g2d.drawString("Power", x + 20, cardY + 110);
            g2d.setColor(accent);
            for (int d = 0; d <= i; d++) g2d.fillOval(x + 78 + d * 16, cardY + 100, 11, 11);

            if (isCurrent) {
                g2d.setColor(accent);
                g2d.fillRoundRect(x + 20, cardY + cardH - 36, 128, 24, 12, 12);
                g2d.setColor(new Color(18, 14, 30));
                g2d.setFont(new Font("Arial", Font.BOLD, 13));
                g2d.drawString("YOU ARE HERE", x + 28, cardY + cardH - 19);
            } else if (!unlocked) {
                g2d.setColor(new Color(75, 72, 88));
                g2d.fillRoundRect(x + 20, cardY + cardH - 36, 92, 24, 12, 12);
                g2d.setColor(new Color(205, 200, 215));
                g2d.setFont(new Font("Arial", Font.BOLD, 13));
                g2d.drawString("LOCKED", x + 39, cardY + cardH - 19);
            }
        }

        g2d.setColor(new Color(160, 155, 180));
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        String hint = pvpSetup ? "All arenas are available in PvP" : "B to close";
        g2d.drawString(hint, cx - g2d.getFontMetrics().stringWidth(hint) / 2, cardY + cardH + 52);
    }

    private Color mapAccent(MapType m) {
        switch (m) {
            case MEADOW:      return new Color(90, 200, 110);
            case INFERNO:     return new Color(255, 110, 50);
            case FROST:       return new Color(120, 200, 255);
            case VOID:        return new Color(175, 110, 255);
            case BOSS_ARENA:  return new Color(255, 200, 30);
            default:          return new Color(200, 200, 200);
        }
    }

    private void renderShop(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight(), cx = w / 2;
        int shopPage = App.content().backend().getShopPage();

        g2d.setColor(new Color(10, 8, 20, 220));
        g2d.fillRect(0, 0, w, h);
        g2d.setPaint(new GradientPaint(0, 0, new Color(60, 45, 0, 35), 0, h, new Color(0, 0, 0, 0)));
        g2d.fillRect(0, 0, w, h);

        // כותרת
        g2d.setFont(new Font("Arial", Font.BOLD, 48));
        String title = "SHOP";
        g2d.setColor(new Color(200, 160, 10, 50));
        g2d.drawString(title, cx - g2d.getFontMetrics().stringWidth(title)/2 + 2, 97);
        g2d.setColor(new Color(255, 210, 50));
        g2d.drawString(title, cx - g2d.getFontMetrics().stringWidth(title)/2, 95);

        // יתרת מטבעות
        team.model.MainPlayer player = UiPort.getInstance().getMainPlayer();
        int coins = (player != null) ? player.getProgress().getCoins() : 0;
        drawCoinLabel(g2d, "Balance:", coins, cx, 130);

        // טאבים
        Color[] tabAccents = { new Color(200, 165, 30), new Color(70, 160, 255) };
        String[] tabLabels = { "WEAPONS", "ARMOR" };
        int tabW = 155, tabH = 34, tabGap = 12;
        int tabsX = cx - (tabW * 2 + tabGap) / 2;
        for (int t = 0; t < 2; t++) {
            int tx = tabsX + t * (tabW + tabGap);
            boolean active = (t == shopPage);
            g2d.setColor(active ? new Color(32, 26, 52) : new Color(18, 14, 30));
            g2d.fillRoundRect(tx, 150, tabW, tabH, 10, 10);
            g2d.setStroke(new BasicStroke(active ? 2.5f : 1f));
            g2d.setColor(active ? tabAccents[t] : new Color(70, 65, 90));
            g2d.drawRoundRect(tx, 150, tabW, tabH, 10, 10);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            g2d.setColor(active ? tabAccents[t] : new Color(120, 115, 140));
            g2d.drawString(tabLabels[t], tx + tabW/2 - g2d.getFontMetrics().stringWidth(tabLabels[t])/2, 173);
        }

        int cardY = 200, cardH = 215;

        if (shopPage == 0) {
            // --- טאב נשק ---
            team.model.ShopItem[] items = team.model.ShopItem.values();
            int n = items.length, gap = 14;
            int cardW = Math.min(190, (w - 80 - gap * (n - 1)) / n);
            int totalW = n * cardW + (n - 1) * gap;
            int startX = cx - totalW / 2;

            for (int i = 0; i < n; i++) {
                team.model.ShopItem item = items[i];
                int x = startX + i * (cardW + gap);
                boolean canAfford = coins >= item.price;
                Color accent = canAfford ? new Color(200, 165, 30) : new Color(65, 60, 85);

                g2d.setColor(new Color(28, 22, 44));
                g2d.fillRoundRect(x, cardY, cardW, cardH, 16, 16);
                g2d.setStroke(new BasicStroke(canAfford ? 2f : 1.2f));
                g2d.setColor(accent);
                g2d.drawRoundRect(x, cardY, cardW, cardH, 16, 16);

                g2d.setColor(canAfford ? new Color(255, 210, 50) : new Color(85, 80, 105));
                g2d.fillRoundRect(x + cardW/2 - 20, cardY + 12, 40, 32, 8, 8);
                g2d.setColor(new Color(20, 16, 34));
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                String num = String.valueOf(i + 1);
                g2d.drawString(num, x + cardW/2 - g2d.getFontMetrics().stringWidth(num)/2, cardY + 34);

                drawMiniSword(g2d, item.name, x + cardW/2, cardY + 74, canAfford);

                g2d.setColor(canAfford ? Color.WHITE : new Color(125, 120, 145));
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                drawCenteredWrapped(g2d, item.name, x + cardW/2, cardY + 106, cardW - 12, g2d.getFontMetrics());

                g2d.setFont(new Font("Arial", Font.BOLD, 15));
                g2d.setColor(canAfford ? new Color(110, 220, 110) : new Color(80, 110, 80));
                String str = "STR +" + item.strBonus;
                g2d.drawString(str, x + cardW/2 - g2d.getFontMetrics().stringWidth(str)/2, cardY + 145);

                drawPriceTag(g2d, item.price, coins, x + cardW/2, cardY + cardH - 16);
            }

            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
            g2d.setColor(new Color(160, 155, 180));
            String hint = "Press 1–" + n + " to buy    Sword drops at your feet (N to pick up)    TAB: Armor    /: Close";
            g2d.drawString(hint, cx - g2d.getFontMetrics().stringWidth(hint)/2, cardY + cardH + 34);

        } else {
            // --- טאב שריון ---
            team.model.ArmorSet[] sets = team.model.ArmorSet.values();
            int n = sets.length, gap = 14;
            int cardW = Math.min(190, (w - 80 - gap * (n - 1)) / n);
            int totalW = n * cardW + (n - 1) * gap;
            int startX = cx - totalW / 2;

            for (int i = 0; i < n; i++) {
                team.model.ArmorSet armor = sets[i];
                int x = startX + i * (cardW + gap);
                boolean canAfford = coins >= armor.price;
                Color accent = canAfford ? new Color(70, 160, 255) : new Color(65, 60, 85);

                g2d.setColor(new Color(22, 26, 44));
                g2d.fillRoundRect(x, cardY, cardW, cardH, 16, 16);
                g2d.setStroke(new BasicStroke(canAfford ? 2f : 1.2f));
                g2d.setColor(accent);
                g2d.drawRoundRect(x, cardY, cardW, cardH, 16, 16);

                g2d.setColor(canAfford ? new Color(70, 160, 255) : new Color(85, 80, 105));
                g2d.fillRoundRect(x + cardW/2 - 20, cardY + 12, 40, 32, 8, 8);
                g2d.setColor(new Color(20, 16, 34));
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                String num = String.valueOf(i + 1);
                g2d.drawString(num, x + cardW/2 - g2d.getFontMetrics().stringWidth(num)/2, cardY + 34);

                drawMiniShield(g2d, armor.name, x + cardW/2, cardY + 74, canAfford);

                g2d.setColor(canAfford ? Color.WHITE : new Color(125, 120, 145));
                g2d.setFont(new Font("Arial", Font.BOLD, 13));
                drawCenteredWrapped(g2d, armor.name, x + cardW/2, cardY + 104, cardW - 12, g2d.getFontMetrics());

                int bx = x + 8, by = cardY + 124;
                int col = canAfford ? 220 : 120;
                g2d.setFont(new Font("Arial", Font.BOLD, 11));
                if (armor.hpBonus  > 0) { g2d.setColor(new Color(col,55,55));    g2d.drawString("+"+armor.hpBonus +" HP",  bx, by);           }
                if (armor.mpBonus  > 0) { g2d.setColor(new Color(55,110,col));   g2d.drawString("+"+armor.mpBonus +" MP",  x+cardW/2+4, by);   }
                by += 15;
                if (armor.strBonus > 0) { g2d.setColor(new Color(col,135,45));   g2d.drawString("+"+armor.strBonus+" STR", bx, by);            }
                if (armor.defBonus > 0) { g2d.setColor(new Color(55,col,col));   g2d.drawString("+"+armor.defBonus+" DEF", x+cardW/2+4, by);   }

                drawPriceTag(g2d, armor.price, coins, x + cardW/2, cardY + cardH - 16);
            }

            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
            g2d.setColor(new Color(160, 155, 180));
            String hint = "Press 1–" + n + " to equip (applied instantly)    TAB: Weapons    /: Close";
            g2d.drawString(hint, cx - g2d.getFontMetrics().stringWidth(hint)/2, cardY + cardH + 34);
        }
    }

    // --- עזרי חנות ---

    private void drawCoinLabel(Graphics2D g, String label, int coins, int cx, int y) {
        g.setFont(new Font("Arial", Font.BOLD, 19));
        g.setColor(new Color(200, 195, 220));
        int lw = g.getFontMetrics().stringWidth(label);
        g.drawString(label, cx - lw/2 - 46, y);
        int cx2 = cx - lw/2 + 12;
        g.setColor(new Color(170, 130, 20));  g.fillOval(cx2, y-16, 18, 18);
        g.setColor(new Color(255, 215, 60));  g.fillOval(cx2+2, y-14, 14, 14);
        g.setColor(new Color(140, 100, 10));  g.setFont(new Font("Arial", Font.BOLD, 10)); g.drawString("$", cx2+5, y-3);
        g.setFont(new Font("Arial", Font.BOLD, 19));
        g.setColor(new Color(255, 220, 80));  g.drawString(String.valueOf(coins), cx2+22, y);
    }

    private void drawPriceTag(Graphics2D g, int price, int coins, int cx, int y) {
        if (coins >= price) {
            int cix = cx - 34;
            g.setColor(new Color(170, 130, 20)); g.fillOval(cix, y-14, 14, 14);
            g.setColor(new Color(255, 215, 60)); g.fillOval(cix+2, y-12, 10, 10);
            g.setColor(new Color(140, 100, 10)); g.setFont(new Font("Arial", Font.BOLD, 9)); g.drawString("$", cix+3, y-4);
            g.setFont(new Font("Arial", Font.BOLD, 15)); g.setColor(new Color(255, 215, 60));
            g.drawString(String.valueOf(price), cix+18, y);
        } else {
            g.setFont(new Font("Arial", Font.BOLD, 12)); g.setColor(new Color(180, 55, 55));
            String s = "Need " + price;
            g.drawString(s, cx - g.getFontMetrics().stringWidth(s)/2, y);
        }
    }

    private void drawCenteredWrapped(Graphics2D g, String text, int cx, int y, int maxW, FontMetrics fm) {
        if (fm.stringWidth(text) <= maxW) {
            g.drawString(text, cx - fm.stringWidth(text)/2, y);
        } else {
            String[] parts = text.split(" ", 2);
            g.drawString(parts[0], cx - fm.stringWidth(parts[0])/2, y - 8);
            if (parts.length > 1) g.drawString(parts[1], cx - fm.stringWidth(parts[1])/2, y + 10);
        }
    }

    // מיני-חרב בכרטיס חנות — עיצוב ייחודי לפי שם
    private void drawMiniSword(Graphics2D g, String name, int cx, int cy, boolean bright) {
        int a = bright ? 255 : 110;
        switch (name) {
            case "Worn Dagger":
                g.setColor(new Color(120, 80, 45, a));  g.fillRect(cx-15,cy-2,10,5);
                g.setColor(new Color(155, 120, 80, a)); g.fillRect(cx-5,cy-4,3,8);
                g.setColor(new Color(155, 130, 100, a));g.fillRect(cx-2,cy-2,18,4);
                g.setColor(new Color(135, 110, 85, a)); g.fillPolygon(new int[]{cx+16,cx+21,cx+16},new int[]{cy-2,cy,cy+2},3);
                break;
            case "Iron Sword":
                g.setColor(new Color(95, 65, 38, a));   g.fillRect(cx-18,cy-2,10,5);
                g.setColor(new Color(75, 75, 88, a));   g.fillRect(cx-8,cy-5,4,10);
                g.setColor(new Color(185, 185, 195, a));g.fillRect(cx-4,cy-2,24,4);
                g.setColor(new Color(215, 215, 230, a));g.fillPolygon(new int[]{cx+20,cx+26,cx+20},new int[]{cy-2,cy,cy+2},3);
                break;
            case "Silver Blade":
                g.setColor(new Color(105, 75, 48, a));  g.fillRect(cx-20,cy-2,10,5);
                g.setColor(new Color(95, 125, 165, a)); g.fillRect(cx-10,cy-5,4,10);
                g.setColor(new Color(210, 215, 235, a));g.fillRect(cx-6,cy-2,27,4);
                g.setColor(new Color(175, 210, 255, a));g.fillRect(cx-5,cy-1,25,2);
                g.setColor(new Color(230, 242, 255, a));g.fillPolygon(new int[]{cx+21,cx+28,cx+21},new int[]{cy-2,cy,cy+2},3);
                break;
            case "Demon Blade":
                g.setColor(new Color(75, 28, 28, a));   g.fillRect(cx-22,cy-2,10,5);
                g.setColor(new Color(115, 18, 18, a));  g.fillRect(cx-12,cy-6,5,12);
                g.setColor(new Color(78, 18, 18, a));   g.fillRect(cx-7,cy-2,30,5);
                g.setColor(new Color(178, 28, 18, a));  g.fillRect(cx-7,cy-4,28,2);
                for (int i=0;i<3;i++) { g.setColor(new Color(178,28,18,a)); g.fillRect(cx-4+i*8,cy-6,3,3); }
                g.setColor(new Color(218, 48, 28, a));  g.fillPolygon(new int[]{cx+23,cx+30,cx+23},new int[]{cy-2,cy,cy+3},3);
                if (bright) { g.setColor(new Color(255,30,10,40)); g.fillRoundRect(cx-8,cy-7,40,13,4,4); }
                break;
            default: // Void Reaver
                g.setColor(new Color(48, 18, 75, a));   g.fillRect(cx-24,cy-2,10,5);
                g.setColor(new Color(125, 58, 195, a)); g.fillRect(cx-14,cy-7,5,14);
                g.setColor(new Color(33, 13, 58, a));   g.fillRect(cx-9,cy-2,33,5);
                g.setColor(new Color(188, 118, 252, a));g.fillRect(cx-8,cy-1,31,2);
                g.setColor(new Color(198, 138, 252, a));g.fillPolygon(new int[]{cx+24,cx+32,cx+24},new int[]{cy-2,cy,cy+3},3);
                if (bright) { g.setColor(new Color(160,80,255,48)); g.fillRoundRect(cx-10,cy-8,44,15,5,5); }
        }
    }

    // מיני-מגן בכרטיס שריון
    private void drawMiniShield(Graphics2D g, String name, int cx, int cy, boolean bright) {
        int a = bright ? 255 : 105;
        Color fill, border, shine;
        switch (name) {
            case "Leather Set":   fill=new Color(135,85,48,a);  border=new Color(95,58,28,a);  shine=new Color(178,118,68,a);  break;
            case "Chain Mail":    fill=new Color(115,120,132,a);border=new Color(75,82,92,a);  shine=new Color(178,183,198,a); break;
            case "Battle Plate":  fill=new Color(75,88,108,a);  border=new Color(48,58,78,a);  shine=new Color(148,162,198,a); break;
            case "Mage Robe":     fill=new Color(88,48,138,a);  border=new Color(58,28,98,a);  shine=new Color(178,128,252,a); break;
            default:              fill=new Color(38,18,68,a);   border=new Color(98,48,178,a); shine=new Color(198,148,252,a); break;
        }
        int sw=26, sh=30;
        g.setColor(fill);
        g.fillRoundRect(cx-sw/2, cy-sh/2, sw, sh-6, 7, 7);
        g.fillPolygon(new int[]{cx-sw/2,cx+sw/2,cx}, new int[]{cy+sh/2-12,cy+sh/2-12,cy+sh/2}, 3);
        g.setStroke(new BasicStroke(1.8f));
        g.setColor(border);
        g.drawRoundRect(cx-sw/2, cy-sh/2, sw, sh-6, 7, 7);
        g.drawLine(cx-sw/2, cy+sh/2-12, cx, cy+sh/2);
        g.drawLine(cx+sw/2, cy+sh/2-12, cx, cy+sh/2);
        g.setColor(shine);
        g.fillOval(cx-4, cy-8, 8, 8);
        g.drawLine(cx, cy, cx, cy+sh/2-14);
    }

    private void renderGameOver(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // שכבת כהות חצי-שקופה מעל המשחק
        g2d.setColor(new Color(0, 0, 0, 175));
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // כותרת GAME OVER
        g2d.setFont(new Font("Arial", Font.BOLD, 72));
        int pvpWinner = App.content().backend().getPvpWinner();
        boolean pvp = App.content().canvas().getSelectedGameMode() == GameMode.PVP;
        g2d.setColor(pvp ? new Color(255, 210, 70) : new Color(220, 50, 50));
        String title = pvp
                ? (pvpWinner == 0 ? "DRAW" : "PLAYER " + pvpWinner + " WINS")
                : "GAME OVER";
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

    private String heroName(HeroType h)  { return h == HeroType.MAGE ? "MAGE" : "WARRIOR"; }
    private String heroRole(HeroType h)  { return h == HeroType.MAGE ? "Ranged Spellcaster" : "Melee Fighter"; }
    private Image  heroImage(HeroType h) { return h == HeroType.MAGE ? MAGE_IMAGE : PLAYER_IMAGE; }
    private Color  heroAccent(HeroType h){ return h == HeroType.MAGE ? new Color(150, 120, 255) : new Color(255, 165, 60); }
    private String[] heroSkills(HeroType h) {
        return h == HeroType.MAGE
            ? new String[]{ "1  Basic Attack", "2  Fireball  (ranged magic, 3 MP)", "3  AquaBeam  (high dmg, 70 MP)" }
            : new String[]{ "1  Basic Attack", "2  Slash  (needs a sword)" };
    }

    private void renderStartScreen(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth(), h = getHeight();
        GameState selectionState = App.content().backend().getState();
        boolean selectingPlayer2 = selectionState == GameState.HERO_PLAYER2_SELECT;
        HeroType currentSelection = selectingPlayer2
                ? App.content().canvas().getSelectedHero2()
                : App.content().canvas().getSelectedHero();

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
        String sub = selectingPlayer2
                ? "Player 2: Choose your hero"
                : "Player 1: Choose your hero";
        g2d.drawString(sub, (w - g2d.getFontMetrics().stringWidth(sub)) / 2, ty + 40);

        // שני כרטיסים — הקוסם גבוה יותר (3 סקילים)
        HeroType[] heroes = HeroType.values();
        int cardW = 230, cardH = 330, gap = 50;
        int totalW = heroes.length * cardW + (heroes.length - 1) * gap;
        int startX = (w - totalW) / 2;
        int cardY = ty + 75;

        for (int i = 0; i < heroes.length; i++) {
            int cx = startX + i * (cardW + gap);
            renderHeroCard(g2d, heroes[i], cx, cardY, cardW, cardH, heroes[i] == currentSelection);
        }

        // כיתובי הוראות
        int footY = cardY + cardH + 55;
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        String go = "ENTER or CLICK  —  Confirm            TAB  —  Switch Hero";
        g2d.setColor(new Color(255, 224, 120));
        g2d.drawString(go, (w - g2d.getFontMetrics().stringWidth(go)) / 2, footY);

        g2d.setFont(new Font("Arial", Font.PLAIN, 15));
        g2d.setColor(new Color(150, 145, 170));
        String controls = App.content().canvas().getSelectedGameMode() == GameMode.SOLO
                ? "Player 1: Arrows + Space attack + M skill + T throw"
                : "P1: Arrows + Space attack + M skill + T throw     •     P2: WASD + R attack + E skill + F throw";
        g2d.drawString(controls, (w - g2d.getFontMetrics().stringWidth(controls)) / 2, footY + 30);
    }

    private void renderGameModeSelect(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth(), h = getHeight();

        // Background — vertical gradient with stars
        g2d.setPaint(new GradientPaint(0, 0, new Color(18, 12, 34), 0, h, new Color(36, 24, 64)));
        g2d.fillRect(0, 0, w, h);
        g2d.setColor(new Color(255, 255, 255, 22));
        for (int i = 0; i < 70; i++) {
            int sx = (i * 97 + 13) % w;
            int sy = (i * 53 + 29) % (h / 2);
            int s = (i % 3 == 0) ? 2 : 1;
            g2d.fillOval(sx, sy, s, s);
        }

        // Title
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
        String sub = "Choose game mode";
        g2d.drawString(sub, (w - g2d.getFontMetrics().stringWidth(sub)) / 2, ty + 40);

        // Game mode cards
        GameMode selectedMode = App.content().canvas().getSelectedGameMode();
        int cardW = 230, cardH = 280, gap = 30;
        int totalW = 3 * cardW + 2 * gap;
        int startX = (w - totalW) / 2;
        int cardY = ty + 75;

        renderModeCard(g2d, "SOLO", "Single Player", startX, cardY, cardW, cardH, selectedMode == GameMode.SOLO);
        renderModeCard(g2d, "MULTIPLAYER", "2 Players", startX + cardW + gap, cardY, cardW, cardH, selectedMode == GameMode.MULTIPLAYER);
        renderModeCard(g2d, "PVP", "Player vs Player", startX + 2 * (cardW + gap), cardY, cardW, cardH, selectedMode == GameMode.PVP);

        // Instructions
        int footY = cardY + cardH + 55;
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        String go = "ENTER or CLICK  —  Start            TAB  —  Switch Mode";
        g2d.setColor(new Color(255, 224, 120));
        g2d.drawString(go, (w - g2d.getFontMetrics().stringWidth(go)) / 2, footY);

        g2d.setFont(new Font("Arial", Font.PLAIN, 14));
        g2d.setColor(new Color(150, 145, 170));
        String info = "Solo: 1 player • Multiplayer: 2-player co-op • PvP: duel with no enemies";
        g2d.drawString(info, (w - g2d.getFontMetrics().stringWidth(info)) / 2, footY + 35);
    }

    private void renderModeCard(Graphics2D g2d, String title, String description, int x, int y, int cw, int ch, boolean selected) {
        Color accent = selected ? new Color(255, 180, 90) : new Color(150, 145, 170);

        // Glow for selected
        if (selected) {
            for (int r = 14; r > 0; r -= 2) {
                g2d.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 10));
                g2d.fillRoundRect(x - r, y - r, cw + 2 * r, ch + 2 * r, 26 + r, 26 + r);
            }
        }

        // Card body
        g2d.setColor(selected ? new Color(44, 36, 74) : new Color(28, 22, 46));
        g2d.fillRoundRect(x, y, cw, ch, 22, 22);
        g2d.setStroke(new BasicStroke(selected ? 3.5f : 1.5f));
        g2d.setColor(selected ? accent : new Color(80, 72, 110));
        g2d.drawRoundRect(x, y, cw, ch, 22, 22);

        // Title
        g2d.setFont(new Font("Arial", Font.BOLD, 32));
        g2d.setColor(selected ? Color.WHITE : new Color(170, 162, 195));
        FontMetrics fm = g2d.getFontMetrics();
        g2d.drawString(title, x + (cw - fm.stringWidth(title)) / 2, y + 60);

        // Description
        g2d.setFont(new Font("Arial", Font.ITALIC, 16));
        g2d.setColor(accent);
        g2d.drawString(description, x + (cw - g2d.getFontMetrics().stringWidth(description)) / 2, y + 95);

        // Info text
        g2d.setFont(new Font("Arial", Font.PLAIN, 13));
        g2d.setColor(selected ? new Color(220, 210, 240) : new Color(140, 135, 165));
        String info = title.equals("SOLO") ? "Play alone"
                : title.equals("PVP") ? "Fight each other"
                : "Play with a friend";
        g2d.drawString(info, x + (cw - g2d.getFontMetrics().stringWidth(info)) / 2, y + 140);

        // Selected tag
        if (selected) {
            g2d.setFont(new Font("Arial", Font.BOLD, 13));
            String tag = "SELECTED";
            int tw = g2d.getFontMetrics().stringWidth(tag);
            int pillW = tw + 38, pillX = x + (cw - pillW) / 2, pillY = y + ch - 30;
            g2d.setColor(accent);
            g2d.fillRoundRect(pillX, pillY, pillW, 22, 11, 11);
            g2d.setColor(new Color(20, 16, 34));
            int ax = pillX + 12, ay = pillY + 11;
            g2d.fillPolygon(new int[]{ax, ax + 7, ax}, new int[]{ay - 5, ay, ay + 5}, 3);
            g2d.drawString(tag, pillX + 22, y + ch - 14);
        }
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
        Graphics2D g2d = (Graphics2D) g;
        MapType map = App.content().canvas().getCurrentMap();
        if (map == MapType.MEADOW) {
            drawPhotoBackground(g2d);
        } else {
            drawThemedBackground(g2d, map);
        }
    }

    private void drawPhotoBackground(Graphics2D g2d) {
        ImageElement background = images.get(BACKGROUND_IMAGE_ID);
        if (background == null || !background.visible || !background.isLoaded()) return;

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

    // רקע פרוצדורלי לפי קונספט המפה — לכל מפה אווירה אחרת לגמרי
    private void drawThemedBackground(Graphics2D g2d, MapType map) {
        int w = getWidth(), h = getHeight();
        Color top, bottom;
        switch (map) {
            case INFERNO: top = new Color(40, 6, 4);     bottom = new Color(150, 40, 10);  break;
            case FROST:   top = new Color(150, 195, 230); bottom = new Color(228, 242, 255); break;
            case VOID:    top = new Color(8, 4, 22);     bottom = new Color(42, 16, 72);   break;
            default:      top = new Color(30, 30, 50);    bottom = new Color(60, 60, 90);   break;
        }
        g2d.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
        g2d.fillRect(0, 0, w, h);

        switch (map) {
            case INFERNO:
                for (int i = 0; i < 60; i++) {
                    int ex = (i * 89 + 30) % w;
                    int ey = h - ((i * 53 + 17) % h);
                    int s = (i % 3) + 2;
                    g2d.setColor(new Color(255, 130 + (i % 80), 30, 150));
                    g2d.fillOval(ex, ey, s, s);
                }
                g2d.setPaint(new GradientPaint(0, h - 130, new Color(255, 80, 0, 0), 0, h, new Color(255, 90, 10, 150)));
                g2d.fillRect(0, h - 130, w, 130);
                break;
            case FROST:
                g2d.setColor(new Color(210, 228, 245, 130));
                int base = (int) (h * 0.6);
                g2d.fillPolygon(new int[]{ -20, w / 4, w / 2 + 40 }, new int[]{ base, (int) (h * 0.32), base }, 3);
                g2d.fillPolygon(new int[]{ w / 2 - 40, 3 * w / 4, w + 20 }, new int[]{ base, (int) (h * 0.38), base }, 3);
                g2d.setColor(new Color(255, 255, 255, 210));
                for (int i = 0; i < 90; i++) {
                    int sx = (i * 97 + 13) % w;
                    int sy = (i * 61 + 29) % h;
                    int s = (i % 3) + 2;
                    g2d.fillOval(sx, sy, s, s);
                }
                break;
            case VOID:
                for (int i = 0; i < 120; i++) {
                    int sx = (i * 113 + 7) % w;
                    int sy = (i * 71 + 19) % h;
                    int s = (i % 4 == 0) ? 3 : 1;
                    g2d.setColor(new Color(230, 230, 255, 120 + (i % 120)));
                    g2d.fillOval(sx, sy, s, s);
                }
                for (int r = 6; r >= 1; r--) {
                    int rad = r * 60;
                    g2d.setColor(new Color(150, 60, 220, 14));
                    g2d.fillOval(w / 2 - rad, (int) (h * 0.4) - rad, rad * 2, rad * 2);
                }
                break;
            default:
                break;
        }
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
        Color platformColor;
        switch (App.content().canvas().getCurrentMap()) {
            case INFERNO: platformColor = new Color(95, 45, 45);   break;
            case FROST:   platformColor = new Color(170, 215, 235); break;
            case VOID:    platformColor = new Color(95, 60, 140);  break;
            default:      platformColor = new Color(120, 220, 120); break;
        }
        for (MapRect rect : currentMap.getRectangles()) {
            g2d.setColor(platformColor);
            g2d.fillRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
        }
    }

    // חרב ביד השחקן — מסתובבת עם הכיוון, עיצוב ייחודי לפי שם החרב. מצויר לשני השחקנים
    private void renderEquippedSword(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawEquippedSwordFor(g2d, UiPort.getInstance().getMainPlayer());
        drawEquippedSwordFor(g2d, UiPort.getInstance().getMainPlayer2());
    }

    private void drawEquippedSwordFor(Graphics2D g2d, team.model.MainPlayer player) {
        if (player == null || !player.hasSword()) return;

        String swordName = player.getEquippedSword().getName();
        boolean right = player.isFacingRight();
        AffineTransform old = g2d.getTransform();

        int px = (int) player.getX(), py = (int) player.getY();
        if (right) {
            g2d.translate(px + 39, py + 30);
            g2d.rotate(Math.toRadians(-35));
        } else {
            g2d.translate(px + 11, py + 30);
            g2d.rotate(Math.toRadians(-145));
        }

        drawEquippedSwordShape(g2d, swordName);
        g2d.setTransform(old);
    }

    // מציירת את צורת החרב כשהיא ביד — ממורכזת ב-(0,0), פונה ימינה
    private void drawEquippedSwordShape(Graphics2D g, String name) {
        switch (name) {
            case "Worn Dagger": {
                g.setColor(new Color(130, 95, 60));
                g.fillRect(-8, -3, 8, 6);
                g.setColor(new Color(155, 120, 80));
                g.fillRect(0, -4, 3, 8);
                g.setColor(new Color(160, 140, 110));
                g.fillRect(3, -2, 18, 4);
                g.setColor(new Color(140, 115, 90));
                g.fillPolygon(new int[]{21, 25, 21}, new int[]{-2, 0, 2}, 3);
                break;
            }
            case "Iron Sword": {
                g.setColor(new Color(100, 70, 40));
                g.fillRect(-10, -3, 10, 6);
                g.setColor(new Color(80, 80, 90));
                g.fillRect(0, -5, 4, 10);
                g.setColor(new Color(185, 185, 195));
                g.fillRect(4, -3, 26, 5);
                g.setColor(new Color(220, 220, 230));
                g.fillPolygon(new int[]{30, 36, 30}, new int[]{-3, 0, 3}, 3);
                break;
            }
            case "Silver Blade": {
                g.setColor(new Color(110, 80, 50));
                g.fillRect(-10, -3, 10, 6);
                g.setColor(new Color(100, 130, 170));
                g.fillRect(0, -5, 4, 10);
                g.setColor(new Color(210, 215, 235));
                g.fillRect(4, -3, 28, 5);
                g.setColor(new Color(180, 210, 255));
                g.fillRect(5, -1, 26, 2);
                g.setColor(new Color(235, 245, 255));
                g.fillPolygon(new int[]{32, 39, 32}, new int[]{-3, 0, 3}, 3);
                break;
            }
            case "Demon Blade": {
                g.setColor(new Color(80, 30, 30));
                g.fillRect(-10, -3, 10, 6);
                g.setColor(new Color(120, 20, 20));
                g.fillRect(0, -6, 5, 12);
                g.setColor(new Color(80, 20, 20));
                g.fillRect(5, -3, 32, 5);
                g.setColor(new Color(180, 30, 20));
                g.fillRect(5, -4, 30, 2);
                for (int i = 0; i < 3; i++) g.fillRect(7+i*8, -6, 4, 4);
                g.setColor(new Color(220, 50, 30));
                g.fillPolygon(new int[]{37, 44, 37}, new int[]{-3, 0, 3}, 3);
                g.setColor(new Color(255, 30, 10, 60));
                g.fillRoundRect(4, -7, 42, 13, 4, 4);
                break;
            }
            case "Void Reaver": {
                g.setColor(new Color(50, 20, 80));
                g.fillRect(-10, -3, 10, 6);
                g.setColor(new Color(130, 60, 200));
                g.fillRect(0, -7, 5, 14);
                g.setColor(new Color(35, 15, 60));
                g.fillRect(5, -3, 36, 5);
                g.setColor(new Color(190, 120, 255));
                g.fillRect(5, -1, 35, 2);
                g.setColor(new Color(200, 140, 255));
                g.fillPolygon(new int[]{41, 50, 41}, new int[]{-3, 0, 3}, 3);
                g.setColor(new Color(160, 80, 255, 65));
                g.fillRoundRect(4, -8, 48, 16, 5, 5);
                break;
            }
            case "Soul Sever": {
                g.setColor(new Color(60, 30, 10));
                g.fillRect(-10, -3, 10, 6);
                g.setColor(new Color(255, 180, 30));
                g.fillRect(0, -7, 5, 14);
                g.setColor(new Color(190, 145, 15));
                g.fillRect(5, -3, 40, 6);
                g.setColor(new Color(255, 220, 80));
                g.fillRect(5, -1, 40, 2);
                g.fillPolygon(new int[]{45, 55, 45}, new int[]{-3, 0, 3}, 3);
                g.setColor(new Color(255, 200, 50, 80));
                g.fillRoundRect(4, -8, 52, 16, 6, 6);
                break;
            }
            case "Chaos Blade": {
                g.setColor(new Color(40, 10, 10));
                g.fillRect(-10, -3, 10, 6);
                g.setColor(new Color(200, 80, 20));
                g.fillRect(0, -7, 6, 14);
                g.setColor(new Color(90, 18, 10));
                g.fillRect(6, -3, 38, 6);
                g.setColor(new Color(255, 100, 30));
                g.fillRect(6, -4, 36, 2);
                for (int i = 0; i < 4; i++) g.fillRect(8+i*8, -6, 4, 4);
                g.setColor(new Color(220, 60, 25));
                g.fillPolygon(new int[]{44, 53, 44}, new int[]{-3, 0, 3}, 3);
                g.setColor(new Color(255, 60, 0, 60));
                g.fillRoundRect(5, -8, 50, 14, 4, 4);
                break;
            }
            case "Wraith Edge": {
                g.setColor(new Color(20, 40, 50));
                g.fillRect(-10, -3, 10, 6);
                g.setColor(new Color(100, 210, 220));
                g.fillRect(0, -6, 5, 12);
                g.setColor(new Color(30, 70, 80));
                g.fillRect(5, -3, 36, 6);
                g.setColor(new Color(150, 230, 240));
                g.fillRect(5, -1, 36, 2);
                g.fillPolygon(new int[]{41, 49, 41}, new int[]{-3, 0, 3}, 3);
                g.setColor(new Color(100, 200, 220, 65));
                g.fillRoundRect(4, -7, 47, 14, 5, 5);
                break;
            }
            case "Bone Crusher": {
                g.setColor(new Color(50, 35, 20));
                g.fillRect(-10, -3, 12, 6);
                g.setColor(new Color(200, 185, 150));
                g.fillRect(2, -7, 7, 14);
                g.setColor(new Color(150, 135, 100));
                g.fillRect(9, -4, 32, 8);
                g.setColor(new Color(220, 205, 170));
                g.fillRect(9, -2, 32, 3);
                g.fillPolygon(new int[]{41, 50, 41}, new int[]{-4, 0, 4}, 3);
                break;
            }
            case "Eternal Lance": {
                g.setColor(new Color(20, 50, 20));
                g.fillRect(-10, -2, 10, 5);
                g.setColor(new Color(50, 180, 80));
                g.fillRect(0, -5, 4, 10);
                g.setColor(new Color(20, 100, 30));
                g.fillRect(4, -3, 46, 5);
                g.setColor(new Color(80, 220, 100));
                g.fillRect(4, -1, 46, 2);
                g.fillPolygon(new int[]{50, 62, 50}, new int[]{-3, 0, 3}, 3);
                g.setColor(new Color(50, 200, 70, 55));
                g.fillRoundRect(3, -6, 60, 12, 4, 4);
                break;
            }
            default: {
                g.setColor(new Color(139, 90, 43));
                g.fillRect(-8, -5, 10, 10);
                g.setColor(new Color(192, 192, 192));
                g.fillRect(0, -3, 28, 6);
                g.setColor(new Color(220, 220, 255));
                g.fillPolygon(new int[]{28, 36, 28}, new int[]{-3, 0, 3}, 3);
            }
        }
    }

    private void renderSword(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        team.model.Sword sword = my_base.App.content().canvas().getSword();
        if (sword != null && sword.isOnGround()) {
            int sx = (int) sword.getX(), sy = (int) sword.getY() + 8;
            drawSwordOnGround(g2d, sword.getName(), sx, sy);
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            g2d.drawString("[N]", sx + 5, (int) sword.getY() - 4);
        }

        // חפצי Boss ופריטים נוספים על הקרקע
        for (team.model.Sword s : my_base.App.content().canvas().getExtraSwords()) {
            if (!s.isOnGround()) continue;
            int sx = (int) s.getX(), sy = (int) s.getY() + 8;
            drawSwordOnGround(g2d, s.getName(), sx, sy);
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            g2d.drawString("[N]", sx + 5, (int) s.getY() - 4);
        }
    }

    // מציירת חרב על הקרקע לפי שמה — כל חרב עם צורה וצבע ייחודיים
    private void drawSwordOnGround(Graphics2D g, String name, int x, int y) {
        Stroke old = g.getStroke();
        switch (name) {
            case "Worn Dagger": {
                // פגיון קצר וחלוד
                g.setColor(new Color(130, 95, 60));
                g.fillRect(x, y + 2, 7, 5);            // ידית עץ
                g.setColor(new Color(155, 120, 80));
                g.fillRect(x + 7, y + 1, 3, 7);        // גארד
                g.setColor(new Color(160, 140, 110));
                g.fillRect(x + 10, y + 3, 16, 3);      // להב חלוד
                g.setColor(new Color(140, 115, 90));
                int[] tx = {x+26, x+30, x+26}; int[] ty = {y+3, y+4, y+6};
                g.fillPolygon(tx, ty, 3);
                break;
            }
            case "Iron Sword": {
                // חרב ברזל קלאסית
                g.setColor(new Color(100, 70, 40));
                g.fillRect(x, y + 2, 10, 6);
                g.setColor(new Color(80, 80, 90));
                g.fillRect(x+10, y, 4, 9);             // גארד
                g.setColor(new Color(185, 185, 195));
                g.fillRect(x+14, y+2, 24, 5);
                g.setColor(new Color(215, 215, 230));
                int[] tx = {x+38, x+44, x+38}; int[] ty = {y+2, y+4, y+7};
                g.fillPolygon(tx, ty, 3);
                break;
            }
            case "Silver Blade": {
                // להב כסף מבריק עם גוון כחלחל
                g.setColor(new Color(110, 80, 50));
                g.fillRect(x, y+2, 10, 6);
                g.setColor(new Color(100, 130, 170));   // גארד כחול
                g.fillRect(x+10, y, 4, 9);
                g.setColor(new Color(210, 215, 235));
                g.fillRect(x+14, y+2, 26, 5);
                // שפיץ קדמי ועורק אמצע
                g.setColor(new Color(180, 210, 255));
                g.fillRect(x+16, y+3, 22, 2);          // קו אמצע כחלחל
                g.setColor(new Color(230, 240, 255));
                int[] tx = {x+40, x+47, x+40}; int[] ty = {y+2, y+4, y+7};
                g.fillPolygon(tx, ty, 3);
                break;
            }
            case "Demon Blade": {
                // להב אדום-שחור מחורץ
                g.setColor(new Color(80, 30, 30));
                g.fillRect(x, y+2, 10, 6);
                g.setColor(new Color(120, 20, 20));
                g.fillRect(x+10, y-1, 5, 11);          // גארד רחב
                g.setColor(new Color(80, 20, 20));
                g.fillRect(x+15, y+2, 28, 5);
                // שיניים
                g.setColor(new Color(180, 30, 20));
                g.fillRect(x+15, y+1, 28, 2);
                for (int i = 0; i < 3; i++) {
                    g.fillRect(x+17+i*8, y-1, 4, 4);   // שיניים על הגב
                }
                // עצה
                g.setColor(new Color(220, 50, 30));
                int[] tx = {x+43, x+50, x+43}; int[] ty = {y+2, y+4, y+7};
                g.fillPolygon(tx, ty, 3);
                // זוהר אדום
                g.setColor(new Color(255, 30, 10, 50));
                g.fillRect(x+14, y-2, 38, 13);
                break;
            }
            case "Void Reaver": {
                // להב קוסמי בסגול-שחור
                g.setColor(new Color(50, 20, 80));
                g.fillRect(x, y+2, 10, 6);
                g.setColor(new Color(130, 60, 200));    // גארד סגול זוהר
                g.fillRect(x+10, y-2, 5, 13);
                g.setColor(new Color(35, 15, 60));
                g.fillRect(x+15, y+2, 32, 5);
                // קו סגול זוהר
                g.setColor(new Color(190, 120, 255));
                g.fillRect(x+15, y+3, 32, 2);
                // עצה
                g.setColor(new Color(200, 140, 255));
                int[] tx = {x+47, x+55, x+47}; int[] ty = {y+2, y+4, y+7};
                g.fillPolygon(tx, ty, 3);
                // הילה
                g.setColor(new Color(160, 80, 255, 55));
                g.fillRoundRect(x+13, y-3, 44, 15, 6, 6);
                break;
            }
            case "Soul Sever": {
                // להב זהב-נשמות עם הילה בוהקת
                g.setColor(new Color(60, 30, 10));
                g.fillRect(x, y+2, 10, 6);
                g.setColor(new Color(255, 180, 30));
                g.fillRect(x+10, y-2, 5, 13);
                g.setColor(new Color(200, 155, 20));
                g.fillRect(x+15, y+1, 36, 7);
                g.setColor(new Color(255, 220, 80));
                g.fillRect(x+15, y+3, 36, 2);
                int[] tx2 = {x+51, x+59, x+51}; int[] ty2 = {y+1, y+4, y+8};
                g.setColor(new Color(255, 220, 80));
                g.fillPolygon(tx2, ty2, 3);
                g.setColor(new Color(255, 200, 50, 75));
                g.fillRoundRect(x+13, y-3, 48, 16, 7, 7);
                break;
            }
            case "Chaos Blade": {
                // להב אש-כאוס אדום-כתום עם שיניים
                g.setColor(new Color(40, 10, 10));
                g.fillRect(x, y+2, 10, 6);
                g.setColor(new Color(200, 80, 20));
                g.fillRect(x+10, y-2, 6, 13);
                g.setColor(new Color(100, 20, 10));
                g.fillRect(x+16, y+1, 34, 7);
                g.setColor(new Color(255, 100, 30));
                g.fillRect(x+16, y+1, 34, 3);
                for (int i = 0; i < 4; i++) { g.setColor(new Color(255, 80, 20)); g.fillRect(x+18+i*7, y-1, 3, 4); }
                int[] tx3 = {x+50, x+58, x+50}; int[] ty3 = {y+1, y+4, y+8};
                g.setColor(new Color(255, 80, 20));
                g.fillPolygon(tx3, ty3, 3);
                g.setColor(new Color(255, 60, 0, 60));
                g.fillRoundRect(x+14, y-3, 46, 14, 5, 5);
                break;
            }
            case "Wraith Edge": {
                // להב רוח ציאן-לבן שקוף
                g.setColor(new Color(20, 40, 50));
                g.fillRect(x, y+2, 10, 6);
                g.setColor(new Color(100, 210, 220));
                g.fillRect(x+10, y-2, 5, 13);
                g.setColor(new Color(30, 70, 80));
                g.fillRect(x+15, y+1, 32, 7);
                g.setColor(new Color(150, 230, 240));
                g.fillRect(x+15, y+3, 32, 2);
                int[] tx4 = {x+47, x+54, x+47}; int[] ty4 = {y+1, y+4, y+8};
                g.setColor(new Color(180, 240, 255));
                g.fillPolygon(tx4, ty4, 3);
                g.setColor(new Color(100, 200, 220, 65));
                g.fillRoundRect(x+13, y-3, 43, 13, 6, 6);
                break;
            }
            case "Bone Crusher": {
                // להב עצמות כבד וגס
                g.setColor(new Color(50, 35, 20));
                g.fillRect(x, y+2, 12, 6);
                g.setColor(new Color(200, 185, 150));
                g.fillRect(x+12, y-2, 7, 13);
                g.setColor(new Color(155, 140, 105));
                g.fillRect(x+19, y, 30, 9);
                g.setColor(new Color(220, 205, 170));
                g.fillRect(x+19, y+1, 30, 3);
                int[] tx5 = {x+49, x+57, x+49}; int[] ty5 = {y, y+4, y+9};
                g.setColor(new Color(235, 220, 190));
                g.fillPolygon(tx5, ty5, 3);
                break;
            }
            case "Eternal Lance": {
                // להב ירוק-אמרלד ארוך ויפה
                g.setColor(new Color(20, 50, 20));
                g.fillRect(x, y+3, 10, 4);
                g.setColor(new Color(50, 180, 80));
                g.fillRect(x+10, y-1, 4, 11);
                g.setColor(new Color(20, 100, 30));
                g.fillRect(x+14, y+2, 40, 5);
                g.setColor(new Color(80, 220, 100));
                g.fillRect(x+14, y+3, 40, 2);
                int[] tx6 = {x+54, x+64, x+54}; int[] ty6 = {y+2, y+4, y+7};
                g.setColor(new Color(100, 255, 120));
                g.fillPolygon(tx6, ty6, 3);
                g.setColor(new Color(50, 200, 70, 55));
                g.fillRoundRect(x+12, y-2, 54, 13, 5, 5);
                break;
            }
            default: {
                g.setColor(new Color(192, 192, 192));
                g.fillRect(x, y, 30, 6);
                g.setColor(new Color(139, 90, 43));
                g.fillRect(x-6, y-2, 10, 10);
            }
        }
        g.setStroke(old);
    }

    private final java.util.Map<EnemyType, Image> enemyImgCache = new java.util.HashMap<>();

    private Image getEnemyImage(EnemyType type) {
        Image cached = enemyImgCache.get(type);
        if (cached != null) return cached;

        Image base; Color tint;
        switch (type) {
            case SWIFT_HENRY:   base = ENEMY_HENRY1; tint = null; break;
            case EVIL_HENRY:    base = ENEMY_HENRY2; tint = null; break;
            case GIANT_HENRY:   base = ENEMY_HENRY3; tint = null; break;
            case INFERNO_HENRY: base = ENEMY_HENRY2; tint = new Color(255, 70, 25);  break;
            case DOOM_HENRY:    base = ENEMY_HENRY3; tint = new Color(225, 25, 25);  break;
            case FROST_HENRY:   base = ENEMY_HENRY1; tint = new Color(120, 205, 255); break;
            case YETI_HENRY:    base = ENEMY_HENRY3; tint = new Color(185, 230, 255); break;
            case VOID_HENRY:    base = ENEMY_HENRY2; tint = new Color(175, 95, 255);  break;
            case COSMIC_HENRY:  base = ENEMY_HENRY3; tint = new Color(140, 70, 255);  break;
            case FINAL_BOSS:    base = ENEMY_HENRY3; tint = new Color(255, 180, 0);   break;
            default:            base = ENEMY_HENRY1; tint = null; break;
        }
        if (tint == null) { enemyImgCache.put(type, base); return base; }
        if (base == null || base.getWidth(this) <= 0) return base;   // הספרייט עוד לא נטען — ננסה שוב
        Image tinted = tintImage(base, tint, 0.5f);
        enemyImgCache.put(type, tinted);
        return tinted;
    }

    // יוצר עותק צבוע של ספרייט (להבחנה ויזואלית בין אויבי מפות שונות)
    private Image tintImage(Image src, Color tint, float alpha) {
        int w = src.getWidth(this), h = src.getHeight(this);
        if (w <= 0 || h <= 0) return src;
        java.awt.image.BufferedImage out =
                new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, 0, this);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));
        g.setColor(tint);
        g.fillRect(0, 0, w, h);
        g.dispose();
        return out;
    }

    private boolean isBigEnemy(EnemyType t) {
        return t == EnemyType.GIANT_HENRY || t == EnemyType.DOOM_HENRY
            || t == EnemyType.YETI_HENRY  || t == EnemyType.COSMIC_HENRY
            || t == EnemyType.FINAL_BOSS;
    }

    private Color enemyAura(EnemyType t) {
        switch (t) {
            case INFERNO_HENRY: case DOOM_HENRY:   return new Color(255, 60, 20);
            case FROST_HENRY:   case YETI_HENRY:   return new Color(120, 200, 255);
            case VOID_HENRY:    case COSMIC_HENRY: return new Color(175, 95, 255);
            case FINAL_BOSS:                       return new Color(255, 200, 30);
            default: return null;
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

            EnemyType type = enemy.getType();
            if (isBigEnemy(type)) {
                ew = 110; eh = 130; eyOff = 80;
            }

            if (enemy.isDying()) {
                renderEnemyDeathAnimation(g2d, enemy, img, ex, ey - (eyOff - 12), ew, eh);
                continue;
            }

            // הילה צבעונית לפי קונספט המפה (לאויבים החזקים)
            Color aura = enemyAura(type);
            if (aura != null) {
                int cxp = ex + ew / 2, cyp = ey - eyOff + eh / 2;
                for (int r = 3; r >= 1; r--) {
                    int rad = (ew / 2) + r * 8;
                    g2d.setColor(new Color(aura.getRed(), aura.getGreen(), aura.getBlue(), 45));
                    g2d.fillOval(cxp - rad, cyp - rad, rad * 2, rad * 2);
                }
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
        // Render Player 1 attack
        team.model.MainPlayer player1 = UiPort.getInstance().getMainPlayer();
        if (player1 != null && player1.isAttacking()) {
            String anim = player1.getCurrentAnimation();
            if ("Slash Attack".equals(anim)) {
                renderSlashAnimation(g, player1);
            } else if ("Fireball".equals(anim)) {
                renderFireballAnimation(g, player1);
            } else if ("AquaBeam".equals(anim)) {
                renderAquaBeamAnimation(g, player1);
            } else {
                renderBasicAttackAnimation(g, player1);
            }
        }
        
        // Render Player 2 attack (if in multiplayer mode)
        team.model.MainPlayer player2 = UiPort.getInstance().getMainPlayer2();
        if (player2 != null && player2.isAttacking()) {
            String anim = player2.getCurrentAnimation();
            if ("Slash Attack".equals(anim)) {
                renderSlashAnimation(g, player2);
            } else if ("Fireball".equals(anim)) {
                renderFireballAnimation(g, player2);
            } else if ("AquaBeam".equals(anim)) {
                renderAquaBeamAnimation(g, player2);
            } else {
                renderBasicAttackAnimation(g, player2);
            }
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

    // אנימציית AquaBeam — קרן לייזר כחולה רחבה עם הילה ופולסינג
    private void renderAquaBeamAnimation(Graphics g, team.model.MainPlayer player) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Stroke oldStroke = g2d.getStroke();

        int ticks    = player.getAttackAnimationTicks();
        int maxTicks = team.model.MainPlayer.ATTACK_ANIMATION_TICKS;
        float progress = 1.0f - (float) ticks / maxTicks;   // 0→1 ככל שהאנימציה מתקדמת

        boolean right = player.isFacingRight();
        int dir    = right ? 1 : -1;
        int originX = (int) player.getX() + (right ? 46 : 4);
        int originY = (int) player.getY() + 22;

        // הקרן מתארכת בהדרגה לאורך של 320px
        int beamLength = (int) (320 * progress);
        int endX = originX + dir * beamLength;

        // שכבות הילה חיצוניות (כחול רחב + שקוף)
        int[] widths = { 28, 18, 10, 5, 2 };
        int[] alphas = { 30,  60, 110, 180, 255 };
        Color[] cols = {
            new Color(40, 140, 255),
            new Color(80, 175, 255),
            new Color(140, 210, 255),
            new Color(210, 235, 255),
            Color.WHITE
        };
        for (int i = 0; i < widths.length; i++) {
            g2d.setStroke(new BasicStroke(widths[i], BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2d.setColor(new Color(cols[i].getRed(), cols[i].getGreen(), cols[i].getBlue(), alphas[i]));
            g2d.drawLine(originX, originY, endX, originY);
        }

        // נצנוצים לאורך הקרן
        for (int i = 1; i <= 7; i++) {
            float frac = (float) i / 8;
            if (frac > progress) break;
            int sx = originX + (int) (dir * beamLength * frac);
            int sparkleSz = (i % 2 == 0) ? 8 : 5;
            int sparAlpha = 160 + (i % 3) * 30;
            g2d.setColor(new Color(200, 235, 255, Math.min(255, sparAlpha)));
            g2d.fillOval(sx - sparkleSz / 2, originY - sparkleSz / 2, sparkleSz, sparkleSz);
        }

        // נקודת מקור — עיגול אנרגיה ביד הקוסם
        g2d.setColor(new Color(60, 160, 255, 200));
        g2d.fillOval(originX - 9, originY - 9, 18, 18);
        g2d.setColor(new Color(200, 240, 255, 230));
        g2d.fillOval(originX - 5, originY - 5, 10, 10);

        // פיצוץ בקצה (רק כשהקרן הגיעה לאורך מלא)
        if (progress > 0.85f) {
            int impactAlpha = (int) ((progress - 0.85f) / 0.15f * 255);
            g2d.setColor(new Color(140, 210, 255, Math.min(255, impactAlpha)));
            g2d.fillOval(endX - 14, originY - 14, 28, 28);
            g2d.setColor(new Color(255, 255, 255, Math.min(200, impactAlpha)));
            g2d.fillOval(endX - 7, originY - 7, 14, 14);
        }

        g2d.setStroke(oldStroke);
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
        Graphics2D g2d = (Graphics2D) g;
        team.model.MainPlayer player1 = UiPort.getInstance().getMainPlayer();
        team.model.MainPlayer player2 = UiPort.getInstance().getMainPlayer2();
        if (player1 == null) return;

        int boxW = 170, boxH = 50, boxY = getHeight() - boxH - 15;
        String p1Keys = player1.getHeroType() == HeroType.MAGE ? "[M / 1-3]" : "[M / 1-2]";
        if (player2 == null) {
            renderSkillBox(g2d, player1, "P1", p1Keys, getWidth() - boxW - 15, boxY, boxW, boxH);
        } else {
            renderSkillBox(g2d, player1, "P1", p1Keys, 15, boxY, boxW, boxH);
            renderSkillBox(g2d, player2, "P2", "[E]", getWidth() - boxW - 15, boxY, boxW, boxH);
        }
    }

    private void renderSkillBox(Graphics2D g2d, team.model.MainPlayer player, String playerLabel,
                                String keys, int boxX, int boxY, int boxW, int boxH) {
        String skillName = player.getActiveAttackName();
        Color theme = skillColor(skillName);
        g2d.setColor(new Color(0, 0, 0, 160));
        g2d.fillRoundRect(boxX, boxY, boxW, boxH, 10, 10);

        g2d.setColor(theme);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(boxX, boxY, boxW, boxH, 10, 10);

        g2d.setFont(new Font("Arial", Font.BOLD, 11));
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.drawString(playerLabel + " Active Skill " + keys + ":", boxX + 8, boxY + 17);

        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.setColor(theme.brighter());
        g2d.drawString(skillName, boxX + 8, boxY + 38);
    }

    // צבע נושא לכל סקיל
    private Color skillColor(String skillName) {
        if ("Slash Attack".equals(skillName)) return new Color(180, 100, 255);
        if ("Fireball".equals(skillName))     return new Color(255, 140, 50);
        if ("AquaBeam".equals(skillName))     return new Color(60, 190, 255);
        return new Color(80, 180, 255);
    }

    private void renderMainPlayer(Graphics g) {
        team.model.MainPlayer player1 = UiPort.getInstance().getMainPlayer();
        if (player1 == null) return;

        Graphics2D g2d = (Graphics2D) g;
        Image player1Image = heroImage(player1.getHeroType());
        if (!isImageLoaded(player1Image)) player1Image = PLAYER_IMAGE;
        if (!isImageLoaded(player1Image)) return;

        // Render Player 1
        int px1 = (int) player1.getX();
        int py1 = (int) player1.getY();

        if (player1.isFacingRight()) {
            drawImageFit(g2d, player1Image, px1, py1 - 15, 50, 65, true);
        } else {
            drawImageFit(g2d, player1Image, px1, py1 - 15, 50, 65, false);
        }

        renderMainPlayerHealthBar(g2d, player1, px1, py1);

        // Render Player 2 (if in multiplayer mode)
        team.model.MainPlayer player2 = UiPort.getInstance().getMainPlayer2();
        if (player2 != null) {
            Image player2Image = heroImage(player2.getHeroType());
            if (!isImageLoaded(player2Image)) player2Image = PLAYER_IMAGE;
            int px2 = (int) player2.getX();
            int py2 = (int) player2.getY();

            if (player2.isFacingRight()) {
                drawImageFit(g2d, player2Image, px2, py2 - 15, 50, 65, true);
            } else {
                drawImageFit(g2d, player2Image, px2, py2 - 15, 50, 65, false);
            }

            renderMainPlayerHealthBar(g2d, player2, px2, py2);
        }
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
        team.model.MainPlayer player1 = UiPort.getInstance().getMainPlayer();
        if (player1 == null || player1.getStats() == null) return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int pending = App.content().backend().getPendingUpgrades();
        int panelX = 10, panelY = 10, panelW = 190, panelH = (pending > 0) ? 198 : 168;

        // Render Player 1 stats (left side)
        renderPlayerStatsPanel(g2d, player1, panelX, panelY, panelW, panelH,
                "P1 " + heroName(player1.getHeroType()), pending);

        // Render Player 2 stats (right side) if in multiplayer mode
        team.model.MainPlayer player2 = UiPort.getInstance().getMainPlayer2();
        if (player2 != null && player2.getStats() != null) {
            int panelX2 = getWidth() - panelW - 10;
            renderPlayerStatsPanel(g2d, player2, panelX2, panelY, panelW, panelH,
                    "P2 " + heroName(player2.getHeroType()), 0);
        }
    }

    private void renderPlayerStatsPanel(Graphics2D g2d, team.model.MainPlayer player, int panelX, int panelY, int panelW, int panelH, String label, int pending) {
        team.model.PlayerStats stats = player.getStats();

        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRoundRect(panelX, panelY, panelW, panelH, 12, 12);

        // Label (P1 or P2)
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.setColor(new Color(200, 190, 220));
        g2d.drawString(label, panelX + 10, panelY + 12);

        g2d.setFont(new Font("Arial", Font.BOLD, 13));

        g2d.setColor(new Color(220, 60, 60));
        g2d.drawString("HP:  " + (int) stats.getHealth() + " / " + (int) stats.getMaxHealth(), panelX + 10, panelY + 33);

        g2d.setColor(new Color(60, 140, 220));
        g2d.drawString("MP:  " + (int) stats.getEnergy() + " / " + (int) stats.getMaxEnergy(), panelX + 10, panelY + 54);

        g2d.setColor(new Color(230, 140, 30));
        g2d.drawString("STR: " + (int) stats.getStrength(), panelX + 10, panelY + 75);

        g2d.setColor(new Color(60, 200, 80));
        g2d.drawString("AGI: " + (int) stats.getAgility(), panelX + 10, panelY + 96);

        if (stats.getDefense() > 0) {
            g2d.setColor(new Color(60, 210, 220));
            g2d.drawString("DEF: " + (int) stats.getDefense(), panelX + 10, panelY + 117);
        }

        // Separator line
        g2d.setColor(new Color(255, 255, 255, 40));
        g2d.drawLine(panelX + 10, panelY + 108, panelX + panelW - 10, panelY + 108);

        // Level + Coins
        team.model.PlayerProgress prog = player.getProgress();

        g2d.setFont(new Font("Arial", Font.BOLD, 15));
        g2d.setColor(new Color(180, 150, 255));
        g2d.drawString("LV " + prog.getLevel(), panelX + 10, panelY + 131);

        // Coin icon + count (right side)
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
        g2d.drawString(String.valueOf(prog.getCoins()), coinX + 22, panelY + 131);

        // XP bar
        int barX = panelX + 10, barY = panelY + 143, barW = panelW - 20, barH = 14;
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

        // Upgrade points available — hint for opening panel in C
        if (pending > 0) {
            g2d.setColor(new Color(150, 210, 255));
            g2d.setFont(new Font("Arial", Font.BOLD, 13));
            g2d.drawString("Upgrade pts: " + pending + "  (press C)", panelX + 10, panelY + 201);
        }
    }

    // Handle mouse clicks on hero cards
    private void handleHeroCardClick(int mouseX, int mouseY) {
        int w = getWidth(), h = getHeight();
        HeroType[] heroes = HeroType.values();
        
        // Calculate card positions (same as renderStartScreen)
        int cardW = 230, cardH = 330, gap = 50;
        int totalW = heroes.length * cardW + (heroes.length - 1) * gap;
        int startX = (w - totalW) / 2;
        int cardY = 185;  // From renderStartScreen calculation
        
        for (int i = 0; i < heroes.length; i++) {
            int cx = startX + i * (cardW + gap);
            int cy = cardY;
            
            if (mouseX >= cx && mouseX <= cx + cardW && mouseY >= cy && mouseY <= cy + cardH) {
                HeroType clickedHero = heroes[i];
                HeroType currentHero = App.content().backend().getState() == GameState.HERO_PLAYER2_SELECT
                        ? App.content().canvas().getSelectedHero2()
                        : App.content().canvas().getSelectedHero();
                
                if (clickedHero != currentHero) {
                    // Cycle until we reach the clicked hero
                    int currentIndex = currentHero.ordinal();
                    int targetIndex = clickedHero.ordinal();
                    int cyclesToTarget = (targetIndex - currentIndex + heroes.length) % heroes.length;
                    for (int j = 0; j < cyclesToTarget; j++) {
                        mainRouter.route("/system/key/down", Params.of("selectHero"));
                    }
                    mainRouter.route("/system/key/down", Params.of("start"));
                } else {
                    // Clicked on same hero — transition to game mode select
                    mainRouter.route("/system/key/down", Params.of("start"));
                }
                return;
            }
        }
    }

    // Handle mouse clicks on mode cards
    private void handleModeCardClick(int mouseX, int mouseY) {
        int w = getWidth();
        int cardW = 230, cardH = 280, gap = 30;
        int totalW = 3 * cardW + 2 * gap;
        int startX = (w - totalW) / 2;
        int cardY = 185;

        GameMode selectedMode = App.content().canvas().getSelectedGameMode();

        GameMode[] modes = GameMode.values();
        for (int i = 0; i < modes.length; i++) {
            int x = startX + i * (cardW + gap);
            if (mouseX >= x && mouseX <= x + cardW
                    && mouseY >= cardY && mouseY <= cardY + cardH) {
                int cycles = (modes[i].ordinal() - selectedMode.ordinal() + modes.length) % modes.length;
                for (int j = 0; j < cycles; j++) {
                    mainRouter.route("/system/key/down", Params.of("selectGameMode"));
                }
                mainRouter.route("/system/key/down", Params.of("start"));
                return;
            }
        }
    }

    private void handlePvpMapClick(int mouseX, int mouseY) {
        MapType[] maps = MapType.values();
        int w = getWidth(), h = getHeight(), gap = 18;
        int cardW = Math.min(230, (w - 80 - gap * (maps.length - 1)) / maps.length);
        int cardH = 160;
        int totalW = maps.length * cardW + (maps.length - 1) * gap;
        int startX = w / 2 - totalW / 2;
        int cardY = h / 2 - 70;

        for (int i = 0; i < maps.length; i++) {
            int x = startX + i * (cardW + gap);
            if (mouseX >= x && mouseX <= x + cardW
                    && mouseY >= cardY && mouseY <= cardY + cardH) {
                mainRouter.route("/system/key/down", Params.of("skill" + (i + 1)));
                return;
            }
        }
    }
}
