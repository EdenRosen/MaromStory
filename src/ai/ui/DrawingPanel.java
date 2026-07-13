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

/**
 * Renders menus world objects players enemies and combat effects
 */
public class DrawingPanel extends JPanel {
    private static final String BACKGROUND_IMAGE_ID = "99";
    private static final Image PLAYER_IMAGE = ImageElement.loadImage("resources/Player1.png");
    private static final Image ENEMY_HENRY1 = ImageElement.loadImage("resources/EnemyHenry1.png");
    private static final Image ENEMY_HENRY2 = ImageElement.loadImage("resources/EnemyHenry2.png");
    private static final Image ENEMY_HENRY3 = ImageElement.loadImage("resources/EnemyHenry3.png");
    private static final Image MAGE_IMAGE   = ImageElement.loadImage("resources/Mage.png");
    private static final Image DRAGON_IMAGE = ImageElement.loadImage("resources/Dragon.png");

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
        setFocusTraversalKeysEnabled(false);
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

    // Maps keyboard input to router command names
    private String getKeyName(int keyCode) {
        switch (keyCode) {

            case KeyEvent.VK_UP:    return "up";
            case KeyEvent.VK_DOWN:  return "down";
            case KeyEvent.VK_LEFT:  return "left";
            case KeyEvent.VK_RIGHT: return "right";


            case KeyEvent.VK_W:     return "up_p2";
            case KeyEvent.VK_A:     return "left_p2";
            case KeyEvent.VK_S:     return "down_p2";
            case KeyEvent.VK_D:     return "right_p2";


            case KeyEvent.VK_SPACE: return "attack";
            case KeyEvent.VK_N:     return "pickup";
            case KeyEvent.VK_M:     return "nextAttack";
            case KeyEvent.VK_T:     return "throw";


            case KeyEvent.VK_R:     return "attack_p2";
            case KeyEvent.VK_Q:     return "pickup_p2";
            case KeyEvent.VK_E:     return "nextAttack_p2";
            case KeyEvent.VK_F:     return "throw_p2";


            case KeyEvent.VK_C:     return "upgradePanel";
            case KeyEvent.VK_1:     return "skill1";
            case KeyEvent.VK_2:     return "skill2";
            case KeyEvent.VK_3:     return "skill3";
            case KeyEvent.VK_4:     return "skill4";
            case KeyEvent.VK_5:     return "skill5";
            case KeyEvent.VK_9:     return "skill1_p2";
            case KeyEvent.VK_8:     return "skill2_p2";
            case KeyEvent.VK_7:     return "skill3_p2";
            case KeyEvent.VK_6:     return "skill4_p2";
            case KeyEvent.VK_NUMPAD1: return "skill1_p2";
            case KeyEvent.VK_NUMPAD2: return "skill2_p2";
            case KeyEvent.VK_NUMPAD3: return "skill3_p2";
            case KeyEvent.VK_NUMPAD4: return "skill4_p2";
            case KeyEvent.VK_NUMPAD5: return "skill5_p2";
            case KeyEvent.VK_B:     return "mapSelect";
            case KeyEvent.VK_SLASH: return "shop";


            case KeyEvent.VK_TAB:   return getTabKeyName();
            case KeyEvent.VK_ENTER: return "start";

            default: return null;
        }
    }

    // Chooses the tab action based on the active screen
    private String getTabKeyName() {

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

    // Draws either setup screens or the active game frame
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


    // Draws the current map name at the top of the screen
    private void renderMapLabel(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        MapType m = App.content().canvas().getCurrentMap();
        boolean isPvpMode = App.content().canvas().getSelectedGameMode() == GameMode.PVP;
        String label = isPvpMode ? "PvP Arena: " + m.displayName : "Map: " + m.displayName + "    [B] maps";
        graphics2D.setFont(new Font("Arial", Font.BOLD, 15));
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        int labelWidth = fontMetrics.stringWidth(label);
        int x = (getWidth() - labelWidth) / 2, y = 26;
        graphics2D.setColor(new Color(0, 0, 0, 150));
        graphics2D.fillRoundRect(x - 12, y - 18, labelWidth + 24, 26, 12, 12);
        graphics2D.setColor(m == MapType.INFERNO ? new Color(255, 120, 90) : new Color(150, 230, 150));
        graphics2D.drawString(label, x, y);
    }


    // Draws the upgrade menu and available stat choices
    private void renderUpgradePanel(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight(), cx = w / 2;
        graphics2D.setColor(new Color(10, 8, 24, 210));
        graphics2D.fillRect(0, 0, w, h);

        graphics2D.setColor(new Color(150, 210, 255));
        graphics2D.setFont(new Font("Arial", Font.BOLD, 50));
        String title = "UPGRADE STATS";
        graphics2D.drawString(title, cx - graphics2D.getFontMetrics().stringWidth(title) / 2, h / 2 - 140);

        int pendingUpgradePoints = App.content().backend().getPendingUpgrades();
        graphics2D.setFont(new Font("Arial", Font.PLAIN, 22));
        graphics2D.setColor(pendingUpgradePoints > 0 ? new Color(255, 220, 120) : new Color(170, 165, 190));
        String subtitle = pendingUpgradePoints > 0 ? ("Points available: " + pendingUpgradePoints)
                                 : "No points — defeat enemies to earn XP";
        graphics2D.drawString(subtitle, cx - graphics2D.getFontMetrics().stringWidth(subtitle) / 2, h / 2 - 105);

        String[] upgradeKeys  = { "1", "2", "3", "4" };
        String[] upgradeDescriptions = { "+20 Max HP", "+10 Max MP", "+3 STR", "+3 AGI" };
        Color[]  upgradeAccentColors   = { new Color(60, 200, 80), new Color(80, 160, 240), new Color(240, 150, 50), new Color(60, 200, 200) };

        int cardWidth = 170, cardHeight = 130, gap = 24;
        int totalCardsWidth = upgradeKeys.length * cardWidth + (upgradeKeys.length - 1) * gap;
        int firstCardX = cx - totalCardsWidth / 2, cardTopY = h / 2 - 60;

        for (int i = 0; i < upgradeKeys.length; i++) {
            int x = firstCardX + i * (cardWidth + gap);
            graphics2D.setColor(new Color(30, 26, 52));
            graphics2D.fillRoundRect(x, cardTopY, cardWidth, cardHeight, 18, 18);
            graphics2D.setStroke(new BasicStroke(2.5f));
            graphics2D.setColor(upgradeAccentColors[i]);
            graphics2D.drawRoundRect(x, cardTopY, cardWidth, cardHeight, 18, 18);


            graphics2D.fillRoundRect(x + cardWidth / 2 - 22, cardTopY + 18, 44, 40, 10, 10);
            graphics2D.setColor(new Color(20, 16, 34));
            graphics2D.setFont(new Font("Arial", Font.BOLD, 26));
            graphics2D.drawString(upgradeKeys[i], x + cardWidth / 2 - graphics2D.getFontMetrics().stringWidth(upgradeKeys[i]) / 2, cardTopY + 47);


            graphics2D.setColor(Color.WHITE);
            graphics2D.setFont(new Font("Arial", Font.BOLD, 20));
            graphics2D.drawString(upgradeDescriptions[i], x + cardWidth / 2 - graphics2D.getFontMetrics().stringWidth(upgradeDescriptions[i]) / 2, cardTopY + 95);
        }

        graphics2D.setColor(new Color(160, 155, 180));
        graphics2D.setFont(new Font("Arial", Font.PLAIN, 16));
        String footerHint = pendingUpgradePoints > 0 ? "Press 1 / 2 / 3 / 4 to spend a point        C to close"
                                  : "Press C to close";
        graphics2D.drawString(footerHint, cx - graphics2D.getFontMetrics().stringWidth(footerHint) / 2, cardTopY + cardHeight + 42);
    }


    // Draws map selection cards for adventure and duel setup
    private void renderMapSelect(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight(), cx = w / 2;
        boolean isPvpSetupScreen = App.content().backend().getState() == GameState.PVP_MAP_SELECT;

        graphics2D.setColor(new Color(8, 8, 20, 216));
        graphics2D.fillRect(0, 0, w, h);

        graphics2D.setColor(new Color(255, 220, 120));
        graphics2D.setFont(new Font("Arial", Font.BOLD, 50));
        String title = isPvpSetupScreen ? "CHOOSE PVP ARENA" : "SELECT MAP";
        graphics2D.drawString(title, cx - graphics2D.getFontMetrics().stringWidth(title) / 2, 130);

        graphics2D.setColor(new Color(190, 185, 210));
        graphics2D.setFont(new Font("Arial", Font.PLAIN, 18));
        String subtitle = isPvpSetupScreen
                ? "Press 1–5 or click a map to start the duel"
                : "Press the number to travel  —  your progress carries over";
        graphics2D.drawString(subtitle, cx - graphics2D.getFontMetrics().stringWidth(subtitle) / 2, 162);

        MapType[] maps = MapType.values();
        MapType currentMap = App.content().canvas().getCurrentMap();
        int n = maps.length, gap = 18;
        int cardWidth = Math.min(230, (w - 80 - gap * (n - 1)) / n);
        int cardHeight = 160;
        int totalCardsWidth = n * cardWidth + (n - 1) * gap;
        int firstCardX = cx - totalCardsWidth / 2, cardTopY = h / 2 - 70;

        for (int i = 0; i < maps.length; i++) {
            MapType m = maps[i];
            int x = firstCardX + i * (cardWidth + gap);
            boolean isUnlocked = isPvpSetupScreen || App.content().backend().isMapUnlocked(i);
            Color accentColor = isUnlocked ? mapAccent(m) : new Color(100, 96, 115);
            boolean isCurrentMap = !isPvpSetupScreen && (m == currentMap);

            graphics2D.setColor(new Color(26, 24, 44));
            graphics2D.fillRoundRect(x, cardTopY, cardWidth, cardHeight, 20, 20);
            graphics2D.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 55));
            graphics2D.fillRoundRect(x, cardTopY, cardWidth, 48, 20, 20);
            graphics2D.setStroke(new BasicStroke(isCurrentMap ? 4f : 2f));
            graphics2D.setColor(accentColor);
            graphics2D.drawRoundRect(x, cardTopY, cardWidth, cardHeight, 20, 20);


            graphics2D.setColor(accentColor);
            graphics2D.fillOval(x + 14, cardTopY + 11, 30, 30);
            graphics2D.setColor(new Color(18, 14, 30));
            graphics2D.setFont(new Font("Arial", Font.BOLD, 18));
            String numberLabel = String.valueOf(i + 1);
            graphics2D.drawString(numberLabel, x + 29 - graphics2D.getFontMetrics().stringWidth(numberLabel) / 2, cardTopY + 32);


            graphics2D.setColor(isUnlocked ? Color.WHITE : new Color(155, 150, 165));
            graphics2D.setFont(new Font("Arial", Font.BOLD, 24));
            graphics2D.drawString(m.displayName, x + 54, cardTopY + 34);
            graphics2D.setColor(new Color(205, 200, 224));
            graphics2D.setFont(new Font("Arial", Font.ITALIC, 16));
            String mapConceptLabel = isUnlocked ? m.concept : "Clear previous stage";
            graphics2D.drawString(mapConceptLabel, x + 20, cardTopY + 78);


            graphics2D.setColor(new Color(200, 196, 220));
            graphics2D.setFont(new Font("Arial", Font.PLAIN, 14));
            graphics2D.drawString("Power", x + 20, cardTopY + 110);
            graphics2D.setColor(accentColor);
            for (int d = 0; d <= i; d++) graphics2D.fillOval(x + 78 + d * 16, cardTopY + 100, 11, 11);

            if (isCurrentMap) {
                graphics2D.setColor(accentColor);
                graphics2D.fillRoundRect(x + 20, cardTopY + cardHeight - 36, 128, 24, 12, 12);
                graphics2D.setColor(new Color(18, 14, 30));
                graphics2D.setFont(new Font("Arial", Font.BOLD, 13));
                graphics2D.drawString("YOU ARE HERE", x + 28, cardTopY + cardHeight - 19);
            } else if (!isUnlocked) {
                graphics2D.setColor(new Color(75, 72, 88));
                graphics2D.fillRoundRect(x + 20, cardTopY + cardHeight - 36, 92, 24, 12, 12);
                graphics2D.setColor(new Color(205, 200, 215));
                graphics2D.setFont(new Font("Arial", Font.BOLD, 13));
                graphics2D.drawString("LOCKED", x + 39, cardTopY + cardHeight - 19);
            }
        }

        graphics2D.setColor(new Color(160, 155, 180));
        graphics2D.setFont(new Font("Arial", Font.PLAIN, 16));
        String footerHint = isPvpSetupScreen ? "All arenas are available in PvP" : "B to close";
        graphics2D.drawString(footerHint, cx - graphics2D.getFontMetrics().stringWidth(footerHint) / 2, cardTopY + cardHeight + 52);
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

    // Draws the shop overlay with weapon and armor tabs
    private void renderShop(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth(), h = getHeight(), cx = w / 2;
        int selectedShopPage = App.content().backend().getShopPage();

        graphics2D.setColor(new Color(10, 8, 20, 220));
        graphics2D.fillRect(0, 0, w, h);
        graphics2D.setPaint(new GradientPaint(0, 0, new Color(60, 45, 0, 35), 0, h, new Color(0, 0, 0, 0)));
        graphics2D.fillRect(0, 0, w, h);


        graphics2D.setFont(new Font("Arial", Font.BOLD, 48));
        String title = "SHOP";
        graphics2D.setColor(new Color(200, 160, 10, 50));
        graphics2D.drawString(title, cx - graphics2D.getFontMetrics().stringWidth(title)/2 + 2, 97);
        graphics2D.setColor(new Color(255, 210, 50));
        graphics2D.drawString(title, cx - graphics2D.getFontMetrics().stringWidth(title)/2, 95);


        team.model.MainPlayer player = UiPort.getInstance().getMainPlayer();
        int playerCoins = (player != null) ? player.getProgress().getCoins() : 0;
        drawCoinLabel(graphics2D, "Balance:", playerCoins, cx, 130);


        Color[] tabAccents = { new Color(200, 165, 30), new Color(70, 160, 255) };
        String[] tabLabels = { "WEAPONS", "ARMOR" };
        int tabWidth = 155, tabHeight = 34, tabGapWidth = 12;
        int tabsStartX = cx - (tabWidth * 2 + tabGapWidth) / 2;
        for (int t = 0; t < 2; t++) {
            int tx = tabsStartX + t * (tabWidth + tabGapWidth);
            boolean active = (t == selectedShopPage);
            graphics2D.setColor(active ? new Color(32, 26, 52) : new Color(18, 14, 30));
            graphics2D.fillRoundRect(tx, 150, tabWidth, tabHeight, 10, 10);
            graphics2D.setStroke(new BasicStroke(active ? 2.5f : 1f));
            graphics2D.setColor(active ? tabAccents[t] : new Color(70, 65, 90));
            graphics2D.drawRoundRect(tx, 150, tabWidth, tabHeight, 10, 10);
            graphics2D.setFont(new Font("Arial", Font.BOLD, 14));
            graphics2D.setColor(active ? tabAccents[t] : new Color(120, 115, 140));
            graphics2D.drawString(tabLabels[t], tx + tabWidth/2 - graphics2D.getFontMetrics().stringWidth(tabLabels[t])/2, 173);
        }

        int cardTopY = 200, cardHeight = 215;

        if (selectedShopPage == 0) {

            team.model.ShopItem[] items = team.model.ShopItem.values();
            int n = items.length, gap = 14;
            int cardWidth = Math.min(190, (w - 80 - gap * (n - 1)) / n);
            int totalCardsWidth = n * cardWidth + (n - 1) * gap;
            int firstCardX = cx - totalCardsWidth / 2;

            for (int i = 0; i < n; i++) {
                team.model.ShopItem item = items[i];
                int x = firstCardX + i * (cardWidth + gap);
                boolean playerCanAfford = playerCoins >= item.price;
                Color accentColor = playerCanAfford ? new Color(200, 165, 30) : new Color(65, 60, 85);

                graphics2D.setColor(new Color(28, 22, 44));
                graphics2D.fillRoundRect(x, cardTopY, cardWidth, cardHeight, 16, 16);
                graphics2D.setStroke(new BasicStroke(playerCanAfford ? 2f : 1.2f));
                graphics2D.setColor(accentColor);
                graphics2D.drawRoundRect(x, cardTopY, cardWidth, cardHeight, 16, 16);

                graphics2D.setColor(playerCanAfford ? new Color(255, 210, 50) : new Color(85, 80, 105));
                graphics2D.fillRoundRect(x + cardWidth/2 - 20, cardTopY + 12, 40, 32, 8, 8);
                graphics2D.setColor(new Color(20, 16, 34));
                graphics2D.setFont(new Font("Arial", Font.BOLD, 20));
                String numberLabel = String.valueOf(i + 1);
                graphics2D.drawString(numberLabel, x + cardWidth/2 - graphics2D.getFontMetrics().stringWidth(numberLabel)/2, cardTopY + 34);

                drawMiniSword(graphics2D, item.name, x + cardWidth/2, cardTopY + 74, playerCanAfford);

                graphics2D.setColor(playerCanAfford ? Color.WHITE : new Color(125, 120, 145));
                graphics2D.setFont(new Font("Arial", Font.BOLD, 14));
                drawCenteredWrapped(graphics2D, item.name, x + cardWidth/2, cardTopY + 106, cardWidth - 12, graphics2D.getFontMetrics());

                graphics2D.setFont(new Font("Arial", Font.BOLD, 15));
                graphics2D.setColor(playerCanAfford ? new Color(110, 220, 110) : new Color(80, 110, 80));
                String strengthLabel = "STR +" + item.strBonus;
                graphics2D.drawString(strengthLabel, x + cardWidth/2 - graphics2D.getFontMetrics().stringWidth(strengthLabel)/2, cardTopY + 145);

                drawPriceTag(graphics2D, item.price, playerCoins, x + cardWidth/2, cardTopY + cardHeight - 16);
            }

            graphics2D.setFont(new Font("Arial", Font.PLAIN, 14));
            graphics2D.setColor(new Color(160, 155, 180));
            String footerHint = "Press 1–" + n + " to buy    Sword drops at your feet (N to pick up)    TAB: Armor    /: Close";
            graphics2D.drawString(footerHint, cx - graphics2D.getFontMetrics().stringWidth(footerHint)/2, cardTopY + cardHeight + 34);

        } else {

            team.model.ArmorSet[] sets = team.model.ArmorSet.values();
            int n = sets.length, gap = 14;
            int cardWidth = Math.min(190, (w - 80 - gap * (n - 1)) / n);
            int totalCardsWidth = n * cardWidth + (n - 1) * gap;
            int firstCardX = cx - totalCardsWidth / 2;

            for (int i = 0; i < n; i++) {
                team.model.ArmorSet armor = sets[i];
                int x = firstCardX + i * (cardWidth + gap);
                boolean playerCanAfford = playerCoins >= armor.price;
                Color accentColor = playerCanAfford ? new Color(70, 160, 255) : new Color(65, 60, 85);

                graphics2D.setColor(new Color(22, 26, 44));
                graphics2D.fillRoundRect(x, cardTopY, cardWidth, cardHeight, 16, 16);
                graphics2D.setStroke(new BasicStroke(playerCanAfford ? 2f : 1.2f));
                graphics2D.setColor(accentColor);
                graphics2D.drawRoundRect(x, cardTopY, cardWidth, cardHeight, 16, 16);

                graphics2D.setColor(playerCanAfford ? new Color(70, 160, 255) : new Color(85, 80, 105));
                graphics2D.fillRoundRect(x + cardWidth/2 - 20, cardTopY + 12, 40, 32, 8, 8);
                graphics2D.setColor(new Color(20, 16, 34));
                graphics2D.setFont(new Font("Arial", Font.BOLD, 20));
                String numberLabel = String.valueOf(i + 1);
                graphics2D.drawString(numberLabel, x + cardWidth/2 - graphics2D.getFontMetrics().stringWidth(numberLabel)/2, cardTopY + 34);

                drawMiniShield(graphics2D, armor.name, x + cardWidth/2, cardTopY + 74, playerCanAfford);

                graphics2D.setColor(playerCanAfford ? Color.WHITE : new Color(125, 120, 145));
                graphics2D.setFont(new Font("Arial", Font.BOLD, 13));
                drawCenteredWrapped(graphics2D, armor.name, x + cardWidth/2, cardTopY + 104, cardWidth - 12, graphics2D.getFontMetrics());

                int bx = x + 8, by = cardTopY + 124;
                int enabledColorValue = playerCanAfford ? 220 : 120;
                graphics2D.setFont(new Font("Arial", Font.BOLD, 11));
                if (armor.hpBonus  > 0) { graphics2D.setColor(new Color(enabledColorValue,55,55));    graphics2D.drawString("+"+armor.hpBonus +" HP",  bx, by);           }
                if (armor.mpBonus  > 0) { graphics2D.setColor(new Color(55,110,enabledColorValue));   graphics2D.drawString("+"+armor.mpBonus +" MP",  x+cardWidth/2+4, by);   }
                by += 15;
                if (armor.strBonus > 0) { graphics2D.setColor(new Color(enabledColorValue,135,45));   graphics2D.drawString("+"+armor.strBonus+" STR", bx, by);            }
                if (armor.defBonus > 0) { graphics2D.setColor(new Color(55,enabledColorValue,enabledColorValue));   graphics2D.drawString("+"+armor.defBonus+" DEF", x+cardWidth/2+4, by);   }

                drawPriceTag(graphics2D, armor.price, playerCoins, x + cardWidth/2, cardTopY + cardHeight - 16);
            }

            graphics2D.setFont(new Font("Arial", Font.PLAIN, 14));
            graphics2D.setColor(new Color(160, 155, 180));
            String footerHint = "Press 1–" + n + " to equip (applied instantly)    TAB: Weapons    /: Close";
            graphics2D.drawString(footerHint, cx - graphics2D.getFontMetrics().stringWidth(footerHint)/2, cardTopY + cardHeight + 34);
        }
    }



    private void drawCoinLabel(Graphics2D g, String label, int playerCoins, int cx, int y) {
        g.setFont(new Font("Arial", Font.BOLD, 19));
        g.setColor(new Color(200, 195, 220));
        int labelTextWidth = g.getFontMetrics().stringWidth(label);
        g.drawString(label, cx - labelTextWidth/2 - 46, y);
        int coinIconX = cx - labelTextWidth/2 + 12;
        g.setColor(new Color(170, 130, 20));  g.fillOval(coinIconX, y-16, 18, 18);
        g.setColor(new Color(255, 215, 60));  g.fillOval(coinIconX+2, y-14, 14, 14);
        g.setColor(new Color(140, 100, 10));  g.setFont(new Font("Arial", Font.BOLD, 10)); g.drawString("$", coinIconX+5, y-3);
        g.setFont(new Font("Arial", Font.BOLD, 19));
        g.setColor(new Color(255, 220, 80));  g.drawString(String.valueOf(playerCoins), coinIconX+22, y);
    }

    private void drawPriceTag(Graphics2D g, int price, int playerCoins, int cx, int y) {
        if (playerCoins >= price) {
            int coinIconX = cx - 34;
            g.setColor(new Color(170, 130, 20)); g.fillOval(coinIconX, y-14, 14, 14);
            g.setColor(new Color(255, 215, 60)); g.fillOval(coinIconX+2, y-12, 10, 10);
            g.setColor(new Color(140, 100, 10)); g.setFont(new Font("Arial", Font.BOLD, 9)); g.drawString("$", coinIconX+3, y-4);
            g.setFont(new Font("Arial", Font.BOLD, 15)); g.setColor(new Color(255, 215, 60));
            g.drawString(String.valueOf(price), coinIconX+18, y);
        } else {
            g.setFont(new Font("Arial", Font.BOLD, 12)); g.setColor(new Color(180, 55, 55));
            String s = "Need " + price;
            g.drawString(s, cx - g.getFontMetrics().stringWidth(s)/2, y);
        }
    }

    private void drawCenteredWrapped(Graphics2D g, String text, int cx, int y, int maxTextWidth, FontMetrics fontMetrics) {
        if (fontMetrics.stringWidth(text) <= maxTextWidth) {
            g.drawString(text, cx - fontMetrics.stringWidth(text)/2, y);
        } else {
            String[] wrappedParts = text.split(" ", 2);
            g.drawString(wrappedParts[0], cx - fontMetrics.stringWidth(wrappedParts[0])/2, y - 8);
            if (wrappedParts.length > 1) g.drawString(wrappedParts[1], cx - fontMetrics.stringWidth(wrappedParts[1])/2, y + 10);
        }
    }


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
            default:
                g.setColor(new Color(48, 18, 75, a));   g.fillRect(cx-24,cy-2,10,5);
                g.setColor(new Color(125, 58, 195, a)); g.fillRect(cx-14,cy-7,5,14);
                g.setColor(new Color(33, 13, 58, a));   g.fillRect(cx-9,cy-2,33,5);
                g.setColor(new Color(188, 118, 252, a));g.fillRect(cx-8,cy-1,31,2);
                g.setColor(new Color(198, 138, 252, a));g.fillPolygon(new int[]{cx+24,cx+32,cx+24},new int[]{cy-2,cy,cy+3},3);
                if (bright) { g.setColor(new Color(160,80,255,48)); g.fillRoundRect(cx-10,cy-8,44,15,5,5); }
        }
    }


    private void drawMiniShield(Graphics2D g, String name, int cx, int cy, boolean bright) {
        int a = bright ? 255 : 105;
        Color fillColor, borderColor, shineColor;
        switch (name) {
            case "Leather Set":   fillColor=new Color(135,85,48,a);  borderColor=new Color(95,58,28,a);  shineColor=new Color(178,118,68,a);  break;
            case "Chain Mail":    fillColor=new Color(115,120,132,a);borderColor=new Color(75,82,92,a);  shineColor=new Color(178,183,198,a); break;
            case "Battle Plate":  fillColor=new Color(75,88,108,a);  borderColor=new Color(48,58,78,a);  shineColor=new Color(148,162,198,a); break;
            case "Mage Robe":     fillColor=new Color(88,48,138,a);  borderColor=new Color(58,28,98,a);  shineColor=new Color(178,128,252,a); break;
            default:              fillColor=new Color(38,18,68,a);   borderColor=new Color(98,48,178,a); shineColor=new Color(198,148,252,a); break;
        }
        int shieldWidth=26, shieldHeight=30;
        g.setColor(fillColor);
        g.fillRoundRect(cx-shieldWidth/2, cy-shieldHeight/2, shieldWidth, shieldHeight-6, 7, 7);
        g.fillPolygon(new int[]{cx-shieldWidth/2,cx+shieldWidth/2,cx}, new int[]{cy+shieldHeight/2-12,cy+shieldHeight/2-12,cy+shieldHeight/2}, 3);
        g.setStroke(new BasicStroke(1.8f));
        g.setColor(borderColor);
        g.drawRoundRect(cx-shieldWidth/2, cy-shieldHeight/2, shieldWidth, shieldHeight-6, 7, 7);
        g.drawLine(cx-shieldWidth/2, cy+shieldHeight/2-12, cx, cy+shieldHeight/2);
        g.drawLine(cx+shieldWidth/2, cy+shieldHeight/2-12, cx, cy+shieldHeight/2);
        g.setColor(shineColor);
        g.fillOval(cx-4, cy-8, 8, 8);
        g.drawLine(cx, cy, cx, cy+shieldHeight/2-14);
    }

    // Draws the game over overlay and restart prompt
    private void renderGameOver(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        graphics2D.setColor(new Color(0, 0, 0, 175));
        graphics2D.fillRect(0, 0, getWidth(), getHeight());


        graphics2D.setFont(new Font("Arial", Font.BOLD, 72));
        int pvpWinner = App.content().backend().getPvpWinner();
        boolean isPvpMode = App.content().canvas().getSelectedGameMode() == GameMode.PVP;
        graphics2D.setColor(isPvpMode ? new Color(255, 210, 70) : new Color(220, 50, 50));
        String title = isPvpMode
                ? (pvpWinner == 0 ? "DRAW" : "PLAYER " + pvpWinner + " WINS")
                : "GAME OVER";
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        int tx = (getWidth() - fontMetrics.stringWidth(title)) / 2;
        graphics2D.drawString(title, tx, getHeight() / 2 - 30);


        graphics2D.setFont(new Font("Arial", Font.PLAIN, 24));
        graphics2D.setColor(new Color(220, 220, 220));
        String prompt = "Press ENTER or Reset to play again";
        fontMetrics = graphics2D.getFontMetrics();
        int px = (getWidth() - fontMetrics.stringWidth(prompt)) / 2;
        graphics2D.drawString(prompt, px, getHeight() / 2 + 30);
    }



    // Returns the display name used on hero cards and stat panels
    private String heroName(HeroType h) {
        switch (h) {
            case MAGE: return "MAGE";
            case DRAGON: return "DRAGON";
            default: return "WARRIOR";
        }
    }

    // Returns the role text used on hero cards
    private String heroRole(HeroType h) {
        switch (h) {
            case MAGE: return "Ranged Spellcaster";
            case DRAGON: return "Slow Tough Dragon";
            default: return "Melee Fighter";
        }
    }

    // Returns the sprite image for the selected hero type
    private Image heroImage(HeroType h) {
        switch (h) {
            case MAGE: return MAGE_IMAGE;
            case DRAGON: return DRAGON_IMAGE;
            default: return PLAYER_IMAGE;
        }
    }

    // Returns the accent color for hero selection cards
    private Color heroAccent(HeroType h) {
        switch (h) {
            case MAGE: return new Color(150, 120, 255);
            case DRAGON: return new Color(85, 210, 115);
            default: return new Color(255, 165, 60);
        }
    }

    // Returns the visible skill list for each hero card
    private String[] heroSkills(HeroType h) {
        switch (h) {
            case MAGE:
                return new String[]{ "1  Basic Attack  (2 MP)", "2  Fireball  (ranged magic, 3 MP)", "3  AquaBeam  (high dmg, 70 MP)" };
            case DRAGON:
                return new String[]{ "1  Basic Attack  (2 MP)", "2  Fireball  (small dragon, 2 MP)" };
            default:
                return new String[]{ "1  Basic Attack  (2 MP)", "2  Slash  (needs a sword)" };
        }
    }

    // Draws the hero selection screen
    private void renderStartScreen(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth(), h = getHeight();
        GameState selectionState = App.content().backend().getState();
        boolean selectingPlayer2 = selectionState == GameState.HERO_PLAYER2_SELECT;
        HeroType currentSelection = selectingPlayer2
                ? App.content().canvas().getSelectedHero2()
                : App.content().canvas().getSelectedHero();


        graphics2D.setPaint(new GradientPaint(0, 0, new Color(18, 12, 34), 0, h, new Color(36, 24, 64)));
        graphics2D.fillRect(0, 0, w, h);
        graphics2D.setColor(new Color(255, 255, 255, 22));
        for (int i = 0; i < 70; i++) {
            int sx = (i * 97 + 13) % w;
            int sy = (i * 53 + 29) % (h / 2);
            int s = (i % 3 == 0) ? 2 : 1;
            graphics2D.fillOval(sx, sy, s, s);
        }


        graphics2D.setFont(new Font("Arial", Font.BOLD, 60));
        String title = "Marom Story";
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        int tx = (w - fontMetrics.stringWidth(title)) / 2;
        int ty = 110;
        graphics2D.setColor(new Color(255, 180, 40, 60));
        graphics2D.drawString(title, tx + 2, ty + 2);
        graphics2D.setColor(new Color(255, 212, 64));
        graphics2D.drawString(title, tx, ty);

        graphics2D.setFont(new Font("Arial", Font.PLAIN, 22));
        graphics2D.setColor(new Color(200, 195, 220));
        String subtitle = selectingPlayer2
                ? "Player 2: Choose your hero"
                : "Player 1: Choose your hero";
        graphics2D.drawString(subtitle, (w - graphics2D.getFontMetrics().stringWidth(subtitle)) / 2, ty + 40);


        HeroType[] heroes = HeroType.values();
        int cardWidth = 230, cardHeight = 330, gap = 50;
        int totalCardsWidth = heroes.length * cardWidth + (heroes.length - 1) * gap;
        int firstCardX = (w - totalCardsWidth) / 2;
        int cardTopY = ty + 75;

        for (int i = 0; i < heroes.length; i++) {
            int cx = firstCardX + i * (cardWidth + gap);
            renderHeroCard(graphics2D, heroes[i], cx, cardTopY, cardWidth, cardHeight, heroes[i] == currentSelection);
        }


        int footY = cardTopY + cardHeight + 55;
        graphics2D.setFont(new Font("Arial", Font.BOLD, 22));
        String go = "ENTER or CLICK  —  Confirm            TAB  —  Switch Hero";
        graphics2D.setColor(new Color(255, 224, 120));
        graphics2D.drawString(go, (w - graphics2D.getFontMetrics().stringWidth(go)) / 2, footY);

        graphics2D.setFont(new Font("Arial", Font.PLAIN, 15));
        graphics2D.setColor(new Color(150, 145, 170));
        String controls = App.content().canvas().getSelectedGameMode() == GameMode.SOLO
                ? "Player 1: Arrows + Space attack + M skill + T throw"
                : "P1: Arrows + Space attack + M skill + T throw     •     P2: WASD + R attack + E skill + F throw";
        graphics2D.drawString(controls, (w - graphics2D.getFontMetrics().stringWidth(controls)) / 2, footY + 30);
    }

    // Draws the game mode selection screen
    private void renderGameModeSelect(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int w = getWidth(), h = getHeight();


        graphics2D.setPaint(new GradientPaint(0, 0, new Color(18, 12, 34), 0, h, new Color(36, 24, 64)));
        graphics2D.fillRect(0, 0, w, h);
        graphics2D.setColor(new Color(255, 255, 255, 22));
        for (int i = 0; i < 70; i++) {
            int sx = (i * 97 + 13) % w;
            int sy = (i * 53 + 29) % (h / 2);
            int s = (i % 3 == 0) ? 2 : 1;
            graphics2D.fillOval(sx, sy, s, s);
        }


        graphics2D.setFont(new Font("Arial", Font.BOLD, 60));
        String title = "Marom Story";
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        int tx = (w - fontMetrics.stringWidth(title)) / 2;
        int ty = 110;
        graphics2D.setColor(new Color(255, 180, 40, 60));
        graphics2D.drawString(title, tx + 2, ty + 2);
        graphics2D.setColor(new Color(255, 212, 64));
        graphics2D.drawString(title, tx, ty);

        graphics2D.setFont(new Font("Arial", Font.PLAIN, 22));
        graphics2D.setColor(new Color(200, 195, 220));
        String subtitle = "Choose game mode";
        graphics2D.drawString(subtitle, (w - graphics2D.getFontMetrics().stringWidth(subtitle)) / 2, ty + 40);


        GameMode selectedMode = App.content().canvas().getSelectedGameMode();
        int cardWidth = 230, cardHeight = 280, gap = 30;
        int totalCardsWidth = 3 * cardWidth + 2 * gap;
        int firstCardX = (w - totalCardsWidth) / 2;
        int cardTopY = ty + 75;

        renderModeCard(graphics2D, "SOLO", "Single Player", firstCardX, cardTopY, cardWidth, cardHeight, selectedMode == GameMode.SOLO);
        renderModeCard(graphics2D, "MULTIPLAYER", "2 Players", firstCardX + cardWidth + gap, cardTopY, cardWidth, cardHeight, selectedMode == GameMode.MULTIPLAYER);
        renderModeCard(graphics2D, "PVP", "Player vs Player", firstCardX + 2 * (cardWidth + gap), cardTopY, cardWidth, cardHeight, selectedMode == GameMode.PVP);


        int footY = cardTopY + cardHeight + 55;
        graphics2D.setFont(new Font("Arial", Font.BOLD, 22));
        String go = "ENTER or CLICK  —  Start            TAB  —  Switch Mode";
        graphics2D.setColor(new Color(255, 224, 120));
        graphics2D.drawString(go, (w - graphics2D.getFontMetrics().stringWidth(go)) / 2, footY);

        graphics2D.setFont(new Font("Arial", Font.PLAIN, 14));
        graphics2D.setColor(new Color(150, 145, 170));
        String info = "Solo: 1 player • Multiplayer: 2-player co-op • PvP: duel with no enemies";
        graphics2D.drawString(info, (w - graphics2D.getFontMetrics().stringWidth(info)) / 2, footY + 35);
    }

    // Draws one selectable game mode card
    private void renderModeCard(Graphics2D graphics2D, String title, String description, int x, int y, int cw, int ch, boolean selected) {
        Color accentColor = new Color(150, 145, 170);


        graphics2D.setColor(new Color(28, 22, 46));
        graphics2D.fillRoundRect(x, y, cw, ch, 22, 22);
        graphics2D.setStroke(new BasicStroke(1.5f));
        graphics2D.setColor(new Color(80, 72, 110));
        graphics2D.drawRoundRect(x, y, cw, ch, 22, 22);


        graphics2D.setFont(new Font("Arial", Font.BOLD, 32));
        graphics2D.setColor(new Color(200, 194, 220));
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        graphics2D.drawString(title, x + (cw - fontMetrics.stringWidth(title)) / 2, y + 60);


        graphics2D.setFont(new Font("Arial", Font.ITALIC, 16));
        graphics2D.setColor(accentColor);
        graphics2D.drawString(description, x + (cw - graphics2D.getFontMetrics().stringWidth(description)) / 2, y + 95);


        graphics2D.setFont(new Font("Arial", Font.PLAIN, 13));
        graphics2D.setColor(new Color(160, 154, 180));
        String info = title.equals("SOLO") ? "Play alone"
                : title.equals("PVP") ? "Fight each other"
                : "Play with a friend";
        graphics2D.drawString(info, x + (cw - graphics2D.getFontMetrics().stringWidth(info)) / 2, y + 140);

    }

    // Draws one selectable hero card
    private void renderHeroCard(Graphics2D graphics2D, HeroType hero, int x, int y, int cw, int ch, boolean selected) {
        Color accentColor = heroAccent(hero);


        graphics2D.setColor(new Color(28, 22, 46));
        graphics2D.fillRoundRect(x, y, cw, ch, 22, 22);
        graphics2D.setStroke(new BasicStroke(1.5f));
        graphics2D.setColor(new Color(80, 72, 110));
        graphics2D.drawRoundRect(x, y, cw, ch, 22, 22);


        graphics2D.setFont(new Font("Arial", Font.BOLD, 28));
        graphics2D.setColor(new Color(200, 194, 220));
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        graphics2D.drawString(heroName(hero), x + (cw - fontMetrics.stringWidth(heroName(hero))) / 2, y + 38);


        graphics2D.setFont(new Font("Arial", Font.ITALIC, 15));
        graphics2D.setColor(accentColor);
        String role = heroRole(hero);
        graphics2D.drawString(role, x + (cw - graphics2D.getFontMetrics().stringWidth(role)) / 2, y + 60);


        Image sprite = heroImage(hero);
        if (isImageLoaded(sprite)) {
            drawImageFit(graphics2D, sprite, x + cw / 2 - 55, y + 72, 110, 130, false);
        }


        graphics2D.setColor(new Color(255, 255, 255, 30));
        graphics2D.drawLine(x + 24, y + 212, x + cw - 24, y + 212);


        graphics2D.setFont(new Font("Arial", Font.PLAIN, 15));
        String[] skills = heroSkills(hero);
        for (int i = 0; i < skills.length; i++) {
            graphics2D.setColor(new Color(175, 170, 195));
            graphics2D.drawString(skills[i], x + 24, y + 240 + i * 24);
        }
    }

    // Draws the active map background
    private void renderBackground(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        MapType map = App.content().canvas().getCurrentMap();
        if (map == MapType.MEADOW) {
            drawPhotoBackground(graphics2D);
        } else {
            drawThemedBackground(graphics2D, map);
        }
    }

    private void drawPhotoBackground(Graphics2D graphics2D) {
        ImageElement background = images.get(BACKGROUND_IMAGE_ID);
        if (background == null || !background.visible || !background.isLoaded()) return;

        RenderingHints previousRenderingHints = graphics2D.getRenderingHints();
        int imageSourceWidth = background.image.getWidth(this);
        int imageSourceHeight = background.image.getHeight(this);
        if (imageSourceWidth <= 0 || imageSourceHeight <= 0) return;

        double scale = Math.max(getWidth() / (double) imageSourceWidth, getHeight() / (double) imageSourceHeight);
        int renderedImageWidth = (int) Math.round(imageSourceWidth * scale);
        int renderedImageHeight = (int) Math.round(imageSourceHeight * scale);
        int renderedImageX = (getWidth() - renderedImageWidth) / 2;
        int renderedImageY = (getHeight() - renderedImageHeight) / 2;

        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(background.image, renderedImageX, renderedImageY, renderedImageWidth, renderedImageHeight, this);
        graphics2D.setRenderingHints(previousRenderingHints);
    }


    private void drawThemedBackground(Graphics2D graphics2D, MapType map) {
        int w = getWidth(), h = getHeight();
        Color top, bottom;
        switch (map) {
            case INFERNO: top = new Color(40, 6, 4);     bottom = new Color(150, 40, 10);  break;
            case FROST:   top = new Color(150, 195, 230); bottom = new Color(228, 242, 255); break;
            case VOID:    top = new Color(8, 4, 22);     bottom = new Color(42, 16, 72);   break;
            default:      top = new Color(30, 30, 50);    bottom = new Color(60, 60, 90);   break;
        }
        graphics2D.setPaint(new GradientPaint(0, 0, top, 0, h, bottom));
        graphics2D.fillRect(0, 0, w, h);

        switch (map) {
            case INFERNO:
                for (int i = 0; i < 60; i++) {
                    int ex = (i * 89 + 30) % w;
                    int ey = h - ((i * 53 + 17) % h);
                    int s = (i % 3) + 2;
                    graphics2D.setColor(new Color(255, 130 + (i % 80), 30, 150));
                    graphics2D.fillOval(ex, ey, s, s);
                }
                graphics2D.setPaint(new GradientPaint(0, h - 130, new Color(255, 80, 0, 0), 0, h, new Color(255, 90, 10, 150)));
                graphics2D.fillRect(0, h - 130, w, 130);
                break;
            case FROST:
                graphics2D.setColor(new Color(210, 228, 245, 130));
                int base = (int) (h * 0.6);
                graphics2D.fillPolygon(new int[]{ -20, w / 4, w / 2 + 40 }, new int[]{ base, (int) (h * 0.32), base }, 3);
                graphics2D.fillPolygon(new int[]{ w / 2 - 40, 3 * w / 4, w + 20 }, new int[]{ base, (int) (h * 0.38), base }, 3);
                graphics2D.setColor(new Color(255, 255, 255, 210));
                for (int i = 0; i < 90; i++) {
                    int sx = (i * 97 + 13) % w;
                    int sy = (i * 61 + 29) % h;
                    int s = (i % 3) + 2;
                    graphics2D.fillOval(sx, sy, s, s);
                }
                break;
            case VOID:
                for (int i = 0; i < 120; i++) {
                    int sx = (i * 113 + 7) % w;
                    int sy = (i * 71 + 19) % h;
                    int s = (i % 4 == 0) ? 3 : 1;
                    graphics2D.setColor(new Color(230, 230, 255, 120 + (i % 120)));
                    graphics2D.fillOval(sx, sy, s, s);
                }
                for (int r = 6; r >= 1; r--) {
                    int rad = r * 60;
                    graphics2D.setColor(new Color(150, 60, 220, 14));
                    graphics2D.fillOval(w / 2 - rad, (int) (h * 0.4) - rad, rad * 2, rad * 2);
                }
                break;
            default:
                break;
        }
    }

    // Draws all registered image elements except the background
    private void renderImages(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        for (Map.Entry<String, ImageElement> entry : images.entrySet()) {
            if (BACKGROUND_IMAGE_ID.equals(entry.getKey())) continue;

            ImageElement img = entry.getValue();
            if (img.isLoaded() && img.visible) {
                AffineTransform old = graphics2D.getTransform();
                graphics2D.translate(img.x + img.width / 2.0, img.y + img.height / 2.0);
                graphics2D.rotate(img.angle);
                graphics2D.drawImage(img.image, -img.width / 2, -img.height / 2, img.width, img.height, this);
                graphics2D.setTransform(old);
            }
        }
    }

    // Draws collision platforms for the active map
    private void renderMap(Graphics g) {
        team.model.Map currentMap = UiPort.getInstance().getMap();
        if (currentMap == null || currentMap.getRectangles() == null) return;

        Graphics2D graphics2D = (Graphics2D) g;
        Color platformColor;
        switch (App.content().canvas().getCurrentMap()) {
            case INFERNO: platformColor = new Color(95, 45, 45);   break;
            case FROST:   platformColor = new Color(170, 215, 235); break;
            case VOID:    platformColor = new Color(95, 60, 140);  break;
            default:      platformColor = new Color(120, 220, 120); break;
        }
        for (MapRect rect : currentMap.getRectangles()) {
            graphics2D.setColor(platformColor);
            graphics2D.fillRect((int) rect.getX(), (int) rect.getY(), (int) rect.getWidth(), (int) rect.getHeight());
        }
    }


    // Draws swords held by player characters
    private void renderEquippedSword(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        drawEquippedSwordFor(graphics2D, UiPort.getInstance().getMainPlayer());
        drawEquippedSwordFor(graphics2D, UiPort.getInstance().getMainPlayer2());
    }

    private void drawEquippedSwordFor(Graphics2D graphics2D, team.model.MainPlayer player) {
        if (player == null || !player.hasSword()) return;

        String swordName = player.getEquippedSword().getName();
        boolean facingRight = player.isFacingRight();
        AffineTransform old = graphics2D.getTransform();

        int px = (int) player.getX(), py = (int) player.getY();
        if (facingRight) {
            graphics2D.translate(px + 39, py + 30);
            graphics2D.rotate(Math.toRadians(-35));
        } else {
            graphics2D.translate(px + 11, py + 30);
            graphics2D.rotate(Math.toRadians(-145));
        }

        drawEquippedSwordShape(graphics2D, swordName);
        graphics2D.setTransform(old);
    }


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

    // Draws available swords on the ground
    private void renderSword(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        team.model.Sword sword = my_base.App.content().canvas().getSword();
        if (sword != null && sword.isOnGround()) {
            int sx = (int) sword.getX(), sy = (int) sword.getY() + 8;
            drawSwordOnGround(graphics2D, sword.getName(), sx, sy);
            graphics2D.setColor(Color.YELLOW);
            graphics2D.setFont(new Font("Arial", Font.BOLD, 11));
            graphics2D.drawString("[N]", sx + 5, (int) sword.getY() - 4);
        }


        for (team.model.Sword s : my_base.App.content().canvas().getExtraSwords()) {
            if (!s.isOnGround()) continue;
            int sx = (int) s.getX(), sy = (int) s.getY() + 8;
            drawSwordOnGround(graphics2D, s.getName(), sx, sy);
            graphics2D.setColor(Color.YELLOW);
            graphics2D.setFont(new Font("Arial", Font.BOLD, 11));
            graphics2D.drawString("[N]", sx + 5, (int) s.getY() - 4);
        }
    }


    private void drawSwordOnGround(Graphics2D g, String name, int x, int y) {
        Stroke old = g.getStroke();
        switch (name) {
            case "Worn Dagger": {

                g.setColor(new Color(130, 95, 60));
                g.fillRect(x, y + 2, 7, 5);
                g.setColor(new Color(155, 120, 80));
                g.fillRect(x + 7, y + 1, 3, 7);
                g.setColor(new Color(160, 140, 110));
                g.fillRect(x + 10, y + 3, 16, 3);
                g.setColor(new Color(140, 115, 90));
                int[] tx = {x+26, x+30, x+26}; int[] ty = {y+3, y+4, y+6};
                g.fillPolygon(tx, ty, 3);
                break;
            }
            case "Iron Sword": {

                g.setColor(new Color(100, 70, 40));
                g.fillRect(x, y + 2, 10, 6);
                g.setColor(new Color(80, 80, 90));
                g.fillRect(x+10, y, 4, 9);
                g.setColor(new Color(185, 185, 195));
                g.fillRect(x+14, y+2, 24, 5);
                g.setColor(new Color(215, 215, 230));
                int[] tx = {x+38, x+44, x+38}; int[] ty = {y+2, y+4, y+7};
                g.fillPolygon(tx, ty, 3);
                break;
            }
            case "Silver Blade": {

                g.setColor(new Color(110, 80, 50));
                g.fillRect(x, y+2, 10, 6);
                g.setColor(new Color(100, 130, 170));
                g.fillRect(x+10, y, 4, 9);
                g.setColor(new Color(210, 215, 235));
                g.fillRect(x+14, y+2, 26, 5);

                g.setColor(new Color(180, 210, 255));
                g.fillRect(x+16, y+3, 22, 2);
                g.setColor(new Color(230, 240, 255));
                int[] tx = {x+40, x+47, x+40}; int[] ty = {y+2, y+4, y+7};
                g.fillPolygon(tx, ty, 3);
                break;
            }
            case "Demon Blade": {

                g.setColor(new Color(80, 30, 30));
                g.fillRect(x, y+2, 10, 6);
                g.setColor(new Color(120, 20, 20));
                g.fillRect(x+10, y-1, 5, 11);
                g.setColor(new Color(80, 20, 20));
                g.fillRect(x+15, y+2, 28, 5);

                g.setColor(new Color(180, 30, 20));
                g.fillRect(x+15, y+1, 28, 2);
                for (int i = 0; i < 3; i++) {
                    g.fillRect(x+17+i*8, y-1, 4, 4);
                }

                g.setColor(new Color(220, 50, 30));
                int[] tx = {x+43, x+50, x+43}; int[] ty = {y+2, y+4, y+7};
                g.fillPolygon(tx, ty, 3);

                g.setColor(new Color(255, 30, 10, 50));
                g.fillRect(x+14, y-2, 38, 13);
                break;
            }
            case "Void Reaver": {

                g.setColor(new Color(50, 20, 80));
                g.fillRect(x, y+2, 10, 6);
                g.setColor(new Color(130, 60, 200));
                g.fillRect(x+10, y-2, 5, 13);
                g.setColor(new Color(35, 15, 60));
                g.fillRect(x+15, y+2, 32, 5);

                g.setColor(new Color(190, 120, 255));
                g.fillRect(x+15, y+3, 32, 2);

                g.setColor(new Color(200, 140, 255));
                int[] tx = {x+47, x+55, x+47}; int[] ty = {y+2, y+4, y+7};
                g.fillPolygon(tx, ty, 3);

                g.setColor(new Color(160, 80, 255, 55));
                g.fillRoundRect(x+13, y-3, 44, 15, 6, 6);
                break;
            }
            case "Soul Sever": {

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
        if (base == null || base.getWidth(this) <= 0) return base;
        Image tinted = tintImage(base, tint, 0.5f);
        enemyImgCache.put(type, tinted);
        return tinted;
    }


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

    // Draws enemies and their health bars
    private void renderEnemies(Graphics g) {
        java.util.List<team.model.Enemy> enemies = App.content().canvas().getEnemies();
        if (enemies == null || enemies.isEmpty()) return;

        Graphics2D graphics2D = (Graphics2D) g;

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
                renderEnemyDeathAnimation(graphics2D, enemy, img, ex, ey - (eyOff - 12), ew, eh);
                continue;
            }


            Color aura = enemyAura(type);
            if (aura != null) {
                int cxp = ex + ew / 2, cyp = ey - eyOff + eh / 2;
                for (int r = 3; r >= 1; r--) {
                    int rad = (ew / 2) + r * 8;
                    graphics2D.setColor(new Color(aura.getRed(), aura.getGreen(), aura.getBlue(), 45));
                    graphics2D.fillOval(cxp - rad, cyp - rad, rad * 2, rad * 2);
                }
            }

            drawImageFit(graphics2D, img, ex, ey - eyOff, ew, eh, enemy.isFacingRight());
            renderEnemyHealthBar(graphics2D, enemy, ex, ey - eyOff, ew);
        }
    }

    private void renderEnemyDeathAnimation(Graphics2D graphics2D, team.model.Enemy enemy, Image enemyImage, int x, int y, int w, int h) {
        Composite previousComposite = graphics2D.getComposite();
        Stroke previousStroke = graphics2D.getStroke();

        float progress = 1.0f - enemy.getDeathAnimationTicks() / (float) team.model.Enemy.DEATH_ANIMATION_TICKS;
        float fade = Math.max(0.15f, 1.0f - progress);
        int spread = (int) (progress * (w / 2.5));

        graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));
        graphics2D.drawImage(enemyImage, x, y + (int) (progress * 14), w, h - 12, null);

        graphics2D.setComposite(previousComposite);
        graphics2D.setColor(new Color(150, 0, 0, 210));
        graphics2D.fillOval(x + (w / 4) - spread / 2, y + h - 10, 36 + spread, 13);
        graphics2D.fillOval(x + (w / 8) - spread / 3, y + h - 4, 18 + spread / 2, 8);
        graphics2D.fillOval(x + (int) (w * 0.64), y + h - 5, 16 + spread / 3, 7);

        graphics2D.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics2D.setColor(new Color(205, 20, 20, 190));
        graphics2D.drawLine(x + (w / 2), y + (int) (h * 0.65), x + (int) (w * 0.34) - spread / 3, y + h - 2);
        graphics2D.drawLine(x + (int) (w * 0.6), y + (int) (h * 0.68), x + (int) (w * 0.78) + spread / 3, y + h);
        graphics2D.drawLine(x + (int) (w * 0.43), y + (int) (h * 0.73), x + (int) (w * 0.45), y + h + 3);

        graphics2D.setStroke(previousStroke);
        graphics2D.setComposite(previousComposite);
    }

    private void renderEnemyHealthBar(Graphics2D graphics2D, team.model.Enemy enemy, int x, int y, int barWidth) {
        team.model.PlayerStats stats = enemy.getStats();
        int barHeight = 8;
        int barX = x;
        int barY = y - 14;
        double healthRatio = stats.getHealth() / stats.getMaxHealth();
        int filledBarWidth = (int) (barWidth * healthRatio);

        graphics2D.setColor(new Color(0, 0, 0, 170));
        graphics2D.fillRect(barX - 1, barY - 1, barWidth + 2, barHeight + 2);

        graphics2D.setColor(new Color(90, 20, 20));
        graphics2D.fillRect(barX, barY, barWidth, barHeight);

        graphics2D.setColor(new Color(220, 50, 50));
        graphics2D.fillRect(barX, barY, filledBarWidth, barHeight);

        graphics2D.setColor(Color.WHITE);
        graphics2D.drawRect(barX, barY, barWidth, barHeight);
    }

    // Draws the active attack animation for each player
    private void renderAttackAnimation(Graphics g) {

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


    // Draws a moving fireball animation
    private void renderFireballAnimation(Graphics g, team.model.MainPlayer player) {
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int ticks    = player.getAttackAnimationTicks();
        int maxTicks = team.model.MainPlayer.ATTACK_ANIMATION_TICKS;
        float progress = 1.0f - (float) ticks / maxTicks;

        boolean facingRight = player.isFacingRight();
        int facingDirection   = facingRight ? 1 : -1;
        int firstCardX = (int) player.getX() + (facingRight ? 42 : 8);
        int y      = (int) player.getY() + 18;
        int travelDistance = 160;
        int cx = firstCardX + (int) (facingDirection * progress * travelDistance);


        for (int i = 5; i >= 1; i--) {
            int tx = cx - facingDirection * i * 11;
            int r  = Math.max(2, 15 - i * 2);
            int a  = Math.max(0, 150 - i * 26);
            graphics2D.setColor(new Color(255, 110 + i * 8, 28, a));
            graphics2D.fillOval(tx - r, y - r, r * 2, r * 2);
        }


        graphics2D.setColor(new Color(255, 120, 30, 150));
        graphics2D.fillOval(cx - 17, y - 17, 34, 34);

        graphics2D.setColor(new Color(255, 80, 20, 200));
        graphics2D.fillOval(cx - 13, y - 13, 26, 26);

        graphics2D.setColor(new Color(255, 232, 150));
        graphics2D.fillOval(cx - 7, y - 7, 14, 14);
        graphics2D.setColor(Color.WHITE);
        graphics2D.fillOval(cx - 3, y - 3, 6, 6);
    }


    // Draws a blue beam attack animation
    private void renderAquaBeamAnimation(Graphics g, team.model.MainPlayer player) {
        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Stroke previousStroke = graphics2D.getStroke();

        int ticks    = player.getAttackAnimationTicks();
        int maxTicks = team.model.MainPlayer.ATTACK_ANIMATION_TICKS;
        float progress = 1.0f - (float) ticks / maxTicks;

        boolean facingRight = player.isFacingRight();
        int facingDirection    = facingRight ? 1 : -1;
        int originX = (int) player.getX() + (facingRight ? 46 : 4);
        int originY = (int) player.getY() + 22;


        int beamLength = (int) (320 * progress);
        int endX = originX + facingDirection * beamLength;


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
            graphics2D.setStroke(new BasicStroke(widths[i], BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            graphics2D.setColor(new Color(cols[i].getRed(), cols[i].getGreen(), cols[i].getBlue(), alphas[i]));
            graphics2D.drawLine(originX, originY, endX, originY);
        }


        for (int i = 1; i <= 7; i++) {
            float sparkleProgress = (float) i / 8;
            if (sparkleProgress > progress) break;
            int sx = originX + (int) (facingDirection * beamLength * sparkleProgress);
            int sparkleSize = (i % 2 == 0) ? 8 : 5;
            int sparkleAlpha = 160 + (i % 3) * 30;
            graphics2D.setColor(new Color(200, 235, 255, Math.min(255, sparkleAlpha)));
            graphics2D.fillOval(sx - sparkleSize / 2, originY - sparkleSize / 2, sparkleSize, sparkleSize);
        }


        graphics2D.setColor(new Color(60, 160, 255, 200));
        graphics2D.fillOval(originX - 9, originY - 9, 18, 18);
        graphics2D.setColor(new Color(200, 240, 255, 230));
        graphics2D.fillOval(originX - 5, originY - 5, 10, 10);


        if (progress > 0.85f) {
            int impactAlpha = (int) ((progress - 0.85f) / 0.15f * 255);
            graphics2D.setColor(new Color(140, 210, 255, Math.min(255, impactAlpha)));
            graphics2D.fillOval(endX - 14, originY - 14, 28, 28);
            graphics2D.setColor(new Color(255, 255, 255, Math.min(200, impactAlpha)));
            graphics2D.fillOval(endX - 7, originY - 7, 14, 14);
        }

        graphics2D.setStroke(previousStroke);
    }

    // Draws the short basic attack arc
    private void renderBasicAttackAnimation(Graphics g, team.model.MainPlayer player) {
        Graphics2D graphics2D = (Graphics2D) g;
        Stroke previousStroke = graphics2D.getStroke();

        int px = (int) player.getX();
        int py = (int) player.getY();
        int arcX = player.isFacingRight() ? px + 34 : px - 42;
        int arcY = py + 6;
        int startAngle = player.isFacingRight() ? -45 : 135;
        int alpha = 80 + player.getAttackAnimationTicks() * 18;

        graphics2D.setStroke(new BasicStroke(5, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics2D.setColor(new Color(240, 245, 255, Math.min(220, alpha)));
        graphics2D.drawArc(arcX, arcY, 58, 48, startAngle, player.isFacingRight() ? 110 : -110);

        graphics2D.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics2D.setColor(new Color(120, 190, 255, Math.min(180, alpha)));
        graphics2D.drawArc(arcX + 6, arcY + 6, 46, 36, startAngle, player.isFacingRight() ? 100 : -100);

        graphics2D.setStroke(previousStroke);
    }

    // Draws the sword slash sweep
    private void renderSlashAnimation(Graphics g, team.model.MainPlayer player) {
        Graphics2D graphics2D = (Graphics2D) g;
        Stroke previousStroke = graphics2D.getStroke();
        AffineTransform previousTransform = graphics2D.getTransform();

        int px = (int) player.getX() + 25;
        int py = (int) player.getY() + 25;

        int ticks     = player.getAttackAnimationTicks();
        int maxTicks  = team.model.MainPlayer.ATTACK_ANIMATION_TICKS;
        float progress = 1.0f - (float) ticks / maxTicks;


        double baseAngle = player.isFacingRight() ? -Math.PI / 2 : Math.PI / 2;
        double sweep     = player.isFacingRight() ? Math.PI : -Math.PI;
        double angle     = baseAngle + sweep * progress;

        int swordLength = 55;
        int alpha = Math.max(60, 255 - (int)(progress * 200));


        graphics2D.translate(px, py);
        graphics2D.rotate(angle);

        graphics2D.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics2D.setColor(new Color(200, 220, 255, alpha));
        graphics2D.drawLine(0, 0, swordLength, 0);


        graphics2D.setColor(new Color(255, 255, 255, alpha));
        graphics2D.fillOval(swordLength - 5, -5, 10, 10);

        graphics2D.setTransform(previousTransform);


        int arcSize = swordLength * 2 + 10;
        int arcX = px - arcSize / 2;
        int arcY = py - arcSize / 2;
        int startDegrees = (int) Math.toDegrees(baseAngle);
        int sweepDegrees = (int) (Math.toDegrees(sweep) * progress);

        graphics2D.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        graphics2D.setColor(new Color(150, 200, 255, Math.min(160, alpha)));
        graphics2D.drawArc(arcX, arcY, arcSize, arcSize, startDegrees, sweepDegrees);

        graphics2D.setStroke(previousStroke);
    }

    // Draws the active skill boxes near the bottom of the screen
    private void renderActiveSkillHUD(Graphics g) {
        Graphics2D graphics2D = (Graphics2D) g;
        team.model.MainPlayer player1 = UiPort.getInstance().getMainPlayer();
        team.model.MainPlayer player2 = UiPort.getInstance().getMainPlayer2();
        if (player1 == null) return;

        int boxWidth = 170, boxHeight = 50, boxTopY = getHeight() - boxHeight - 15;
        String p1Keys = player1.getHeroType() == HeroType.MAGE ? "[M / 1-3]" : "[M / 1-2]";
        String p2Keys = player2 != null && player2.getHeroType() == HeroType.MAGE
                ? "[E / 9-7]"
                : "[E / 9-8]";
        if (player2 == null) {
            renderSkillBox(graphics2D, player1, "P1", p1Keys, getWidth() - boxWidth - 15, boxTopY, boxWidth, boxHeight);
        } else {
            renderSkillBox(graphics2D, player1, "P1", p1Keys, 15, boxTopY, boxWidth, boxHeight);
            renderSkillBox(graphics2D, player2, "P2", p2Keys, getWidth() - boxWidth - 15, boxTopY, boxWidth, boxHeight);
        }
    }

    private void renderSkillBox(Graphics2D graphics2D, team.model.MainPlayer player, String playerLabel,
                                String keyHint, int boxLeftX, int boxTopY, int boxWidth, int boxHeight) {
        String skillName = player.getActiveAttackName();
        Color theme = skillColor(skillName);
        graphics2D.setColor(new Color(0, 0, 0, 160));
        graphics2D.fillRoundRect(boxLeftX, boxTopY, boxWidth, boxHeight, 10, 10);

        graphics2D.setColor(theme);
        graphics2D.setStroke(new BasicStroke(2));
        graphics2D.drawRoundRect(boxLeftX, boxTopY, boxWidth, boxHeight, 10, 10);

        graphics2D.setFont(new Font("Arial", Font.BOLD, 11));
        graphics2D.setColor(Color.LIGHT_GRAY);
        graphics2D.drawString(playerLabel + " Active Skill " + keyHint + ":", boxLeftX + 8, boxTopY + 17);

        graphics2D.setFont(new Font("Arial", Font.BOLD, 14));
        graphics2D.setColor(theme.brighter());
        graphics2D.drawString(skillName, boxLeftX + 8, boxTopY + 38);
    }


    private Color skillColor(String skillName) {
        if ("Slash Attack".equals(skillName)) return new Color(180, 100, 255);
        if ("Fireball".equals(skillName))     return new Color(255, 140, 50);
        if ("AquaBeam".equals(skillName))     return new Color(60, 190, 255);
        return new Color(80, 180, 255);
    }

    // Draws both player sprites and health bars
    private void renderMainPlayer(Graphics g) {
        team.model.MainPlayer player1 = UiPort.getInstance().getMainPlayer();
        if (player1 == null) return;

        Graphics2D graphics2D = (Graphics2D) g;
        Image player1Image = heroImage(player1.getHeroType());
        if (!isImageLoaded(player1Image)) player1Image = PLAYER_IMAGE;
        if (!isImageLoaded(player1Image)) return;


        int px1 = (int) player1.getX();
        int py1 = (int) player1.getY();

        if (player1.isFacingRight()) {
            drawImageFit(graphics2D, player1Image, px1, py1 - 15, 50, 65, true);
        } else {
            drawImageFit(graphics2D, player1Image, px1, py1 - 15, 50, 65, false);
        }

        renderMainPlayerHealthBar(graphics2D, player1, px1, py1);


        team.model.MainPlayer player2 = UiPort.getInstance().getMainPlayer2();
        if (player2 != null) {
            Image player2Image = heroImage(player2.getHeroType());
            if (!isImageLoaded(player2Image)) player2Image = PLAYER_IMAGE;
            int px2 = (int) player2.getX();
            int py2 = (int) player2.getY();

            if (player2.isFacingRight()) {
                drawImageFit(graphics2D, player2Image, px2, py2 - 15, 50, 65, true);
            } else {
                drawImageFit(graphics2D, player2Image, px2, py2 - 15, 50, 65, false);
            }

            renderMainPlayerHealthBar(graphics2D, player2, px2, py2);
        }
    }

    private boolean isImageLoaded(Image image) {
        return image != null && image.getWidth(this) > 0 && image.getHeight(this) > 0;
    }

    private void drawImageFit(Graphics2D graphics2D, Image image, int x, int y, int boxWidth, int boxHeight, boolean flipHorizontal) {
        int imageSourceWidth = image.getWidth(this);
        int imageSourceHeight = image.getHeight(this);
        if (imageSourceWidth <= 0 || imageSourceHeight <= 0) return;

        double scale = Math.min(boxWidth / (double) imageSourceWidth, boxHeight / (double) imageSourceHeight);
        int renderedImageWidth = (int) Math.round(imageSourceWidth * scale);
        int renderedImageHeight = (int) Math.round(imageSourceHeight * scale);
        int renderedImageX = x + (boxWidth - renderedImageWidth) / 2;
        int renderedImageY = y + boxHeight - renderedImageHeight;

        RenderingHints previousRenderingHints = graphics2D.getRenderingHints();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (flipHorizontal) {
            graphics2D.drawImage(image, renderedImageX + renderedImageWidth, renderedImageY, -renderedImageWidth, renderedImageHeight, this);
        } else {
            graphics2D.drawImage(image, renderedImageX, renderedImageY, renderedImageWidth, renderedImageHeight, this);
        }

        graphics2D.setRenderingHints(previousRenderingHints);
    }

    private void renderMainPlayerHealthBar(Graphics2D graphics2D, team.model.MainPlayer player, int x, int y) {
        team.model.PlayerStats stats = player.getStats();
        int barWidth = 50;
        int barHeight = 7;
        int barX = x;
        int barY = y - 12;
        double healthRatio = stats.getHealth() / stats.getMaxHealth();
        int filledBarWidth = (int) (barWidth * healthRatio);

        graphics2D.setColor(new Color(0, 0, 0, 170));
        graphics2D.fillRect(barX - 1, barY - 1, barWidth + 2, barHeight + 2);

        graphics2D.setColor(new Color(90, 20, 20));
        graphics2D.fillRect(barX, barY, barWidth, barHeight);

        graphics2D.setColor(new Color(50, 220, 80));
        graphics2D.fillRect(barX, barY, filledBarWidth, barHeight);

        graphics2D.setColor(Color.WHITE);
        graphics2D.drawRect(barX, barY, barWidth, barHeight);

        graphics2D.setFont(new Font("Arial", Font.BOLD, 10));
        graphics2D.drawString(String.valueOf((int) stats.getHealth()), barX + 18, barY - 2);
    }

    // Draws the player stat panels and progress bars
    private void renderPlayerStats(Graphics g) {
        team.model.MainPlayer player1 = UiPort.getInstance().getMainPlayer();
        if (player1 == null || player1.getStats() == null) return;

        Graphics2D graphics2D = (Graphics2D) g;
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int pendingUpgradePoints = App.content().backend().getPendingUpgrades();
        int panelX = 10, panelY = 10, panelWidth = 190, panelHeight = (pendingUpgradePoints > 0) ? 198 : 168;


        renderPlayerStatsPanel(graphics2D, player1, panelX, panelY, panelWidth, panelHeight,
                "P1 " + heroName(player1.getHeroType()), pendingUpgradePoints);


        team.model.MainPlayer player2 = UiPort.getInstance().getMainPlayer2();
        if (player2 != null && player2.getStats() != null) {
            int player2PanelX = getWidth() - panelWidth - 10;
            renderPlayerStatsPanel(graphics2D, player2, player2PanelX, panelY, panelWidth, panelHeight,
                    "P2 " + heroName(player2.getHeroType()), 0);
        }
    }

    private void renderPlayerStatsPanel(Graphics2D graphics2D, team.model.MainPlayer player, int panelX, int panelY, int panelWidth, int panelHeight, String label, int pendingUpgradePoints) {
        team.model.PlayerStats stats = player.getStats();

        graphics2D.setColor(new Color(0, 0, 0, 150));
        graphics2D.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 12, 12);


        graphics2D.setFont(new Font("Arial", Font.BOLD, 12));
        graphics2D.setColor(new Color(200, 190, 220));
        graphics2D.drawString(label, panelX + 10, panelY + 12);

        graphics2D.setFont(new Font("Arial", Font.BOLD, 13));

        graphics2D.setColor(new Color(220, 60, 60));
        graphics2D.drawString("HP:  " + (int) stats.getHealth() + " / " + (int) stats.getMaxHealth(), panelX + 10, panelY + 33);

        graphics2D.setColor(new Color(60, 140, 220));
        graphics2D.drawString("MP:  " + (int) stats.getEnergy() + " / " + (int) stats.getMaxEnergy(), panelX + 10, panelY + 54);

        graphics2D.setColor(new Color(230, 140, 30));
        graphics2D.drawString("STR: " + (int) stats.getStrength(), panelX + 10, panelY + 75);

        graphics2D.setColor(new Color(60, 200, 80));
        graphics2D.drawString("AGI: " + (int) stats.getAgility(), panelX + 10, panelY + 96);

        if (stats.getDefense() > 0) {
            graphics2D.setColor(new Color(60, 210, 220));
            graphics2D.drawString("DEF: " + (int) stats.getDefense(), panelX + 10, panelY + 117);
        }


        graphics2D.setColor(new Color(255, 255, 255, 40));
        graphics2D.drawLine(panelX + 10, panelY + 108, panelX + panelWidth - 10, panelY + 108);


        team.model.PlayerProgress playerProgress = player.getProgress();

        graphics2D.setFont(new Font("Arial", Font.BOLD, 15));
        graphics2D.setColor(new Color(180, 150, 255));
        graphics2D.drawString("LV " + playerProgress.getLevel(), panelX + 10, panelY + 131);


        int coinX = panelX + panelWidth - 78, coinY = panelY + 110;
        graphics2D.setColor(new Color(170, 130, 20));
        graphics2D.fillOval(coinX, coinY, 16, 16);
        graphics2D.setColor(new Color(255, 215, 60));
        graphics2D.fillOval(coinX + 2, coinY + 2, 12, 12);
        graphics2D.setColor(new Color(150, 110, 10));
        graphics2D.setFont(new Font("Arial", Font.BOLD, 11));
        graphics2D.drawString("$", coinX + 5, coinY + 12);
        graphics2D.setColor(new Color(255, 220, 90));
        graphics2D.setFont(new Font("Arial", Font.BOLD, 15));
        graphics2D.drawString(String.valueOf(playerProgress.getCoins()), coinX + 22, panelY + 131);


        int barX = panelX + 10, barY = panelY + 143, barWidth = panelWidth - 20, barHeight = 14;
        graphics2D.setColor(new Color(38, 38, 50));
        graphics2D.fillRoundRect(barX, barY, barWidth, barHeight, 7, 7);
        int filledXpWidth = (int) (barWidth * Math.min(1.0, playerProgress.getXp() / (double) playerProgress.getXpToNext()));
        graphics2D.setColor(new Color(150, 120, 255));
        graphics2D.fillRoundRect(barX, barY, filledXpWidth, barHeight, 7, 7);
        graphics2D.setColor(new Color(255, 255, 255, 90));
        graphics2D.drawRoundRect(barX, barY, barWidth, barHeight, 7, 7);

        String xpText = "XP  " + playerProgress.getXp() + " / " + playerProgress.getXpToNext();
        graphics2D.setFont(new Font("Arial", Font.BOLD, 10));
        FontMetrics fontMetrics = graphics2D.getFontMetrics();
        graphics2D.setColor(Color.WHITE);
        graphics2D.drawString(xpText, barX + (barWidth - fontMetrics.stringWidth(xpText)) / 2, barY + 11);


        if (pendingUpgradePoints > 0) {
            graphics2D.setColor(new Color(150, 210, 255));
            graphics2D.setFont(new Font("Arial", Font.BOLD, 13));
            graphics2D.drawString("Upgrade pts: " + pendingUpgradePoints + "  (press C)", panelX + 10, panelY + 201);
        }
    }


    // Handles mouse selection on hero cards
    private void handleHeroCardClick(int mouseX, int mouseY) {
        int w = getWidth(), h = getHeight();
        HeroType[] heroes = HeroType.values();


        int cardWidth = 230, cardHeight = 330, gap = 50;
        int totalCardsWidth = heroes.length * cardWidth + (heroes.length - 1) * gap;
        int firstCardX = (w - totalCardsWidth) / 2;
        int cardTopY = 185;

        for (int i = 0; i < heroes.length; i++) {
            int cx = firstCardX + i * (cardWidth + gap);
            int cy = cardTopY;

            if (mouseX >= cx && mouseX <= cx + cardWidth && mouseY >= cy && mouseY <= cy + cardHeight) {
                HeroType clickedHero = heroes[i];
                HeroType currentHero = App.content().backend().getState() == GameState.HERO_PLAYER2_SELECT
                        ? App.content().canvas().getSelectedHero2()
                        : App.content().canvas().getSelectedHero();

                if (clickedHero != currentHero) {

                    int currentHeroIndex = currentHero.ordinal();
                    int targetHeroIndex = clickedHero.ordinal();
                    int heroCyclesToTarget = (targetHeroIndex - currentHeroIndex + heroes.length) % heroes.length;
                    for (int j = 0; j < heroCyclesToTarget; j++) {
                        mainRouter.route("/system/key/down", Params.of("selectHero"));
                    }
                    mainRouter.route("/system/key/down", Params.of("start"));
                } else {

                    mainRouter.route("/system/key/down", Params.of("start"));
                }
                return;
            }
        }
    }


    // Handles mouse selection on game mode cards
    private void handleModeCardClick(int mouseX, int mouseY) {
        int w = getWidth();
        int cardWidth = 230, cardHeight = 280, gap = 30;
        int totalCardsWidth = 3 * cardWidth + 2 * gap;
        int firstCardX = (w - totalCardsWidth) / 2;
        int cardTopY = 185;

        GameMode selectedMode = App.content().canvas().getSelectedGameMode();

        GameMode[] modes = GameMode.values();
        for (int i = 0; i < modes.length; i++) {
            int x = firstCardX + i * (cardWidth + gap);
            if (mouseX >= x && mouseX <= x + cardWidth
                    && mouseY >= cardTopY && mouseY <= cardTopY + cardHeight) {
                int modeCyclesToTarget = (modes[i].ordinal() - selectedMode.ordinal() + modes.length) % modes.length;
                for (int j = 0; j < modeCyclesToTarget; j++) {
                    mainRouter.route("/system/key/down", Params.of("selectGameMode"));
                }
                mainRouter.route("/system/key/down", Params.of("start"));
                return;
            }
        }
    }

    // Handles mouse selection on duel arena cards
    private void handlePvpMapClick(int mouseX, int mouseY) {
        MapType[] maps = MapType.values();
        int w = getWidth(), h = getHeight(), gap = 18;
        int cardWidth = Math.min(230, (w - 80 - gap * (maps.length - 1)) / maps.length);
        int cardHeight = 160;
        int totalCardsWidth = maps.length * cardWidth + (maps.length - 1) * gap;
        int firstCardX = w / 2 - totalCardsWidth / 2;
        int cardTopY = h / 2 - 70;

        for (int i = 0; i < maps.length; i++) {
            int x = firstCardX + i * (cardWidth + gap);
            if (mouseX >= x && mouseX <= x + cardWidth
                    && mouseY >= cardTopY && mouseY <= cardTopY + cardHeight) {
                mainRouter.route("/system/key/down", Params.of("skill" + (i + 1)));
                return;
            }
        }
    }
}
