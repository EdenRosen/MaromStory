package ai.ui;

import base.Params;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import shared.MainRouter;
import shared.ui_ports.UiPort;

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
        JFrame frame = new JFrame("MaromQuest");
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
        JPanel panel = new JPanel();
        JButton resetButton = new JButton("Reset");
        resetButton.setFont(new Font("Arial", Font.PLAIN, 24));
        resetButton.setPreferredSize(new Dimension(220, 80));

        resetButton.addActionListener(e -> {
            mainRouter.route("/system/reset", Params.of());
            drawingPanel.requestFocusInWindow();
        });

        panel.add(resetButton);
        return panel;
    }
}
