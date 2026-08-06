package com.csms;

import com.csms.view.login.LoginFrame;
import com.formdev.flatlaf.FlatLightLaf;
import com.csms.service.AutoBackupScheduler;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Font;

public final class App {

        private App() {
        }

        public static void main(String[] args) {
                initializeLookAndFeel();
                initializeUISettings();

                Runtime.getRuntime().addShutdownHook(
                                new Thread(() -> {
                                        AutoBackupScheduler.stop();
                                }));

                SwingUtilities.invokeLater(() -> {
                        LoginFrame loginFrame = new LoginFrame();
                        loginFrame.setVisible(true);
                });
        }

        private static void initializeLookAndFeel() {
                try {
                        FlatLightLaf.setup();

                } catch (Exception exception) {
                        System.err.println(
                                        "Không thể khởi tạo FlatLaf: "
                                                        + exception.getMessage());
                }
        }

        private static void initializeUISettings() {
                UIManager.put(
                                "defaultFont",
                                new Font(
                                                "Segoe UI",
                                                Font.PLAIN,
                                                14));

                UIManager.put("Button.arc", 16);
                UIManager.put("Component.arc", 14);
                UIManager.put("TextComponent.arc", 14);
                UIManager.put("Component.focusWidth", 1);
                UIManager.put("Component.innerFocusWidth", 0);

                UIManager.put(
                                "Button.focusedBorderColor",
                                new Color(0, 123, 210));

                UIManager.put(
                                "Button.hoverBackground",
                                new Color(229, 244, 255));

                UIManager.put(
                                "Button.pressedBackground",
                                new Color(210, 235, 252));

                UIManager.put("Table.rowHeight", 40);
                UIManager.put("Table.showHorizontalLines", true);
                UIManager.put("Table.showVerticalLines", false);

                UIManager.put(
                                "Table.selectionBackground",
                                new Color(225, 241, 253));

                UIManager.put(
                                "Table.selectionForeground",
                                new Color(31, 41, 55));
        }
}