package com.marginallyclever.robotoverlord;

import com.marginallyclever.convenience.helpers.PathHelper;
import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.log.LogPanel3;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.preferences.GraphicsPreferences;
import com.marginallyclever.robotoverlord.preferences.InteractionPreferences;
import com.marginallyclever.robotoverlord.renderpanel.OpenGLRenderPanel;
import com.marginallyclever.robotoverlord.renderpanel.RenderPanel;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.actions.*;
import com.marginallyclever.robotoverlord.swing.componentmanagerpanel.ComponentManagerPanel;
import com.marginallyclever.robotoverlord.swing.entitytreepanel.EntityTreePanel;
import com.marginallyclever.robotoverlord.swing.translator.Translator;
import com.marginallyclever.robotoverlord.systems.SystemManager;
import com.marginallyclever.util.PropertiesFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.dnd.DropTarget;
import java.awt.event.*;
import java.io.IOException;
import java.util.prefs.Preferences;

/**
 * {@code RobotOverlord} is the top-level controller of an application to educate robots.
 * It is built around good design patterns.
 * See <a href="https://github.com/MarginallyClever/Robot-Overlord-App">https://github.com/MarginallyClever/Robot-Overlord-App</a>
 *
 * @author Dan Royer
 */
public class RobotOverlord {
	private static final Logger logger = LoggerFactory.getLogger(RobotOverlord.class);

	public static final String APP_TITLE = "Robot Overlord";
	public static final String APP_URL = "https://github.com/MarginallyClever/Robot-Overlord";
	private static final String KEY_LAST_DIRECTORY_IMPORT = "LastDirectoryImport";
	private static final String KEY_LAST_DIRECTORY_SAVE = "LastDirectorySave";
	private static final String KEY_LAST_DIRECTORY_LOAD = "LastDirectoryLoad";

	public static final FileNameExtensionFilter FILE_FILTER = new FileNameExtensionFilter("RO files", "RO");

	// used for checking the application version with the GitHub release, for "there is a new version available!" notification
	public static final String VERSION;

	static {
		try {
			VERSION = PropertiesFileHelper.getVersionPropertyValue();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// settings
	private final Preferences prefs = Preferences.userRoot().node("RobotOverlord");

	private final Project project = new Project();

	/**
	 * The main frame of the GUI
	 */
	private MainFrame mainFrame;

	/**
	 * The frame that contains the log panel.
	 */
	private static JFrame logFrame;

	/**
	 * The panel that contains the OpenGL canvas.
	 */
	private RenderPanel renderPanel;

	/**
	 * The menu bar of the main frame.
	 */
    private final MainMenu mainMenu = new MainMenu(this);

	/**
	 * The left contains the renderPanel.  The right contains the rightFrameSplitter.
	 */
	private final JSplitPane splitLeftRight = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

	/**
	 * The panel that contains the entity tree and the component panel.
	 */
	private final JSplitPane rightFrameSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

	/**
	 * Tree componentpanel of all Entities in the scene.
	 */
	private final EntityTreePanel entityTreePanel;

	/**
	 * Collated componentpanel of all components in all selected Entities.
	 */
	private final ComponentManagerPanel componentManagerPanel;

	private final SystemManager systemManager;

	public RobotOverlord() {
		super();

		if(GraphicsEnvironment.isHeadless()) {
			throw new RuntimeException("RobotOverlord cannot be run headless yet.");
		}

		Translator.start();
		UndoSystem.start();
		preferencesLoad();
		systemManager = new SystemManager(project.getEntityManager());

		buildMainFrame();
		entityTreePanel = new EntityTreePanel(project.getEntityManager());
		componentManagerPanel = new ComponentManagerPanel(project.getEntityManager(),systemManager);
		buildRenderPanel();
		setSplitterDefaults();
		layoutComponents();
		mainMenu.refresh();

		listenToClipboardChanges();

		ProjectClearAction action = new ProjectClearAction(project);
		action.doIt();

		updateActionEnableStatus();

		logger.info("** READY **");
    }

	private void setSplitterDefaults() {
		// make sure the master panel can't be squished.
		rightFrameSplitter.setMinimumSize(new Dimension(360,300));
		// if the window resizes, give top and bottom halves equal share of the real estate
		rightFrameSplitter.setResizeWeight(0.25);
		// if the window resizes, give left half as much real estate as it can get.
		splitLeftRight.setResizeWeight(1);
	}

	public void buildRenderPanel() {
		if(renderPanel!=null) {
			logger.debug("stopping old renderPanel");
			renderPanel.stopAnimationSystem();
			splitLeftRight.remove(renderPanel.getPanel());
		}

		//renderPanel = new OpenGLTestOrthographic(project.getEntityManager());
		//renderPanel = new OpenGLTestPerspective(project.getEntityManager());
		//renderPanel = new OpenGLTestStencil(project.getEntityManager());
		renderPanel = new OpenGLRenderPanel(project.getEntityManager());
		renderPanel.setUpdateCallback(systemManager::update);
	}

	private void listenToClipboardChanges() {
		Clipboard.addListener(this::updateActionEnableStatus);
	}

	/**
	 * Tell all Actions to check if they are active.
	 */
	private void updateActionEnableStatus() {
		entityTreePanel.setSelection(Clipboard.getSelectedEntities());
		if(renderPanel!=null) renderPanel.updateSubjects(Clipboard.getSelectedEntities());
		componentManagerPanel.refreshContentsFromClipboard();
		entityTreePanel.updateActionEnableStatus();
	}

	private void preferencesLoad() {
		ProjectImportAction.setLastDirectory(prefs.get(RobotOverlord.KEY_LAST_DIRECTORY_IMPORT, System.getProperty("user.dir")));
		ProjectLoadAction.setLastDirectory(prefs.get(RobotOverlord.KEY_LAST_DIRECTORY_LOAD, System.getProperty("user.dir")));
		ProjectSaveAction.setLastDirectory(prefs.get(RobotOverlord.KEY_LAST_DIRECTORY_SAVE, System.getProperty("user.dir")));
		GraphicsPreferences.load();
		InteractionPreferences.load();
	}

	private void preferencesSave() {
		InteractionPreferences.save();
		GraphicsPreferences.save();
		prefs.put(RobotOverlord.KEY_LAST_DIRECTORY_IMPORT, ProjectImportAction.getLastDirectory());
		prefs.put(RobotOverlord.KEY_LAST_DIRECTORY_LOAD, ProjectLoadAction.getLastDirectory());
		prefs.put(RobotOverlord.KEY_LAST_DIRECTORY_SAVE, ProjectSaveAction.getLastDirectory());
	}

	public void layoutComponents() {
		rightFrameSplitter.setTopComponent(entityTreePanel);
		rightFrameSplitter.setBottomComponent(componentManagerPanel);

        splitLeftRight.setLeftComponent(renderPanel.getPanel());
        splitLeftRight.setRightComponent(rightFrameSplitter);

        mainFrame.add(splitLeftRight);
	}

	private void buildMainFrame() {
		logger.info("buildMainFrame()");
		// start the main application frame - the largest visible rectangle on the screen with the minimize/maximize/close buttons.
        mainFrame = new MainFrame( APP_TITLE + " " + VERSION, prefs);
		mainFrame.setJMenuBar(mainMenu);
		mainFrame.setWindowSizeAndPosition();
		setupDropTarget();

        mainFrame.addWindowListener(new WindowAdapter() {
            // when someone tries to close the app, confirm it.
			@Override
			public void windowClosing(WindowEvent e) {
				confirmClose();
				super.windowClosing(e);
			}
		});

		if (Desktop.isDesktopSupported()) {
			Desktop desktop = Desktop.getDesktop();
			if (desktop.isSupported(Desktop.Action.APP_QUIT_HANDLER)) {
				desktop.setQuitHandler((evt, res) -> {
					if (confirmClose()) {
						res.performQuit();
					} else {
						res.cancelQuit();
					}
				});
			}
			if (desktop.isSupported(Desktop.Action.APP_ABOUT)) {
				desktop.setAboutHandler((e) ->{
					AboutAction a = new AboutAction(this.mainFrame);
					a.actionPerformed(null);
				});
			}
		}

		mainFrame.setVisible(true);
	}

	public boolean confirmClose() {
        int result = JOptionPane.showConfirmDialog(
				mainFrame,
				Translator.get("RobotOverlord.quitConfirm"),
				Translator.get("RobotOverlord.quitTitle"),
				JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
			mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			preferencesSave();

			// Run this on another thread than the AWT event queue to make sure the call to Animator.stop() completes before exiting
			new Thread(() -> {
				renderPanel.stopAnimationSystem();
				mainFrame.dispose();
				Log.end();
			}).start();
			return true;
		}
		return false;
	}

	private void setupDropTarget() {
		new DropTarget(mainFrame,new MainWindowDropTarget(mainFrame,project));
	}

	public Project getProject() {
		return project;
	}

	public void showLogDialog() {
		logFrame.setVisible(true);
	}

	public static void main(String[] argv) {
		Log.start();
		logFrame = LogPanel3.createFrame(Log.getLogLocation());
		PathHelper.start();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ignored) {}

		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(RobotOverlord::new);
	}
}