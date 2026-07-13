package ai.ui;

import base.Params;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import my_base.App;
import shared.MainRouter;
import shared.ui_ports.UiPort;
import team.control.GameState;

/**
 * Creates the game window and connects the drawing panel to the router
 */
public class Ui {
    private MainRouter mainRouter;
    private Map<String, ImageElement> images = new HashMap<>();
    private DrawingPanel drawingPanel;
    private UiPortImpl uiInstance;

    public void setUiPorts() { }

    public void start(MainRouter mainRouter) {
        this.mainRouter = mainRouter;
        createAndShowWindow();
        mainRouter.route("/system/init", Params.of());
    }

    private void createAndShowWindow() {
        JFrame frame = new JFrame("Marom Story");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);

        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        frame.setLayout(new BorderLayout());

        drawingPanel = new DrawingPanel(images, mainRouter);
        frame.add(drawingPanel, BorderLayout.CENTER);

        JPanel controlPanel = createControlPanel();
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        uiInstance = new UiPortImpl(images, null, drawingPanel);
        UiPort.setInstance((UiPort) uiInstance);
    }

    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 8));
        panel.setPreferredSize(new Dimension(0, 56)); // thinner bottom bar

        JButton homeButton = new JButton("Home");
        homeButton.setFont(new Font("Arial", Font.PLAIN, 18));
        homeButton.setPreferredSize(new Dimension(120, 40));
        homeButton.addActionListener(e -> {
            mainRouter.route("/system/home", Params.of());
            drawingPanel.requestFocusInWindow();
        });

        JButton restartButton = new JButton("Restart");
        restartButton.setFont(new Font("Arial", Font.PLAIN, 18));
        restartButton.setPreferredSize(new Dimension(120, 40));
        restartButton.setVisible(false);
        restartButton.addActionListener(e -> {
            // Restart the current game without going to Home
            mainRouter.route("/system/reset", Params.of());
            drawingPanel.requestFocusInWindow();
        });

        panel.add(homeButton);
        panel.add(restartButton);

        // Toggle restart button visibility based on backend state
        Timer t = new Timer(120, ev -> {
            try {
                boolean playing = App.content().backend().getState() == GameState.PLAYING;
                if (restartButton.isVisible() != playing) {
                    restartButton.setVisible(playing);
                    panel.revalidate();
                    panel.repaint();
                }
            } catch (Exception ignored) { }
        });
        t.setRepeats(true);
        t.start();
        return panel;
    }
}
