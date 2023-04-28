package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.swinginterface.robotlibrarypanel.GithubFetcher;
import com.marginallyclever.robotoverlord.swinginterface.robotlibrarypanel.RobotLibraryPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ShowRobotLibraryPanel extends AbstractAction {
    RobotOverlord robotOverlord;

    public ShowRobotLibraryPanel(RobotOverlord robotOverlord) {
        super("Get more robots...");
        this.robotOverlord = robotOverlord;
    }
    /**
     * Invoked when an action occurs.
     *
     * @param e the event to be processed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Component source = (Component) e.getSource();
        JFrame parentFrame = (JFrame)SwingUtilities.getWindowAncestor(source);

        RobotLibraryPanel panel = new RobotLibraryPanel();
        panel.addRobotLibraryListener(robotOverlord);
        JFrame frame = new JFrame("Robot Library");
        frame.setContentPane(panel);
        frame.setPreferredSize(new Dimension(450,600));
        frame.setSize(450,600);
        frame.pack();
        frame.setLocationRelativeTo(parentFrame);
        frame.setVisible(true);
    }
}
