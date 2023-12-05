package com.marginallyclever.robotoverlord;

import javax.swing.*;
import java.util.prefs.Preferences;

public class MainFrameTest {
    public static void main(String[] args) {
        MainFrame frame = new MainFrame("Test", Preferences.userRoot().node("com/marginallyclever/robotoverlord"));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setWindowSizeAndPosition();
        frame.setVisible(true);
    }
}
