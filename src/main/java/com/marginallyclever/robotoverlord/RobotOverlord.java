package com.marginallyclever.robotoverlord;

import com.marginallyclever.convenience.log.Log;
import com.marginallyclever.convenience.log.LogPanel3;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.components.CameraComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.ShapeComponent;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.components.shapes.mesh.load.MeshFactory;
import com.marginallyclever.robotoverlord.demos.DemoDog;
import com.marginallyclever.robotoverlord.demos.DemoSpidee;
import com.marginallyclever.robotoverlord.swinginterface.componentpanel.ComponentPanel;
import com.marginallyclever.robotoverlord.swinginterface.EditorAction;
import com.marginallyclever.robotoverlord.swinginterface.SoundSystem;
import com.marginallyclever.robotoverlord.swinginterface.UndoSystem;
import com.marginallyclever.robotoverlord.swinginterface.actions.*;
import com.marginallyclever.robotoverlord.swinginterface.edits.EntityAddEdit;
import com.marginallyclever.robotoverlord.swinginterface.entitytreepanel.EntityTreePanel;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;
import com.marginallyclever.util.PropertiesFileHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * {@code RobotOverlord} is the top-level controller of an application to educate robots.
 * It is built around good design patterns.
 * See <a href="https://github.com/MarginallyClever/Robot-Overlord-App">https://github.com/MarginallyClever/Robot-Overlord-App</a>
 *
 * @author Dan Royer
 */
public class RobotOverlord extends Entity {
	private static final Logger logger = LoggerFactory.getLogger(RobotOverlord.class);

	public static final String APP_TITLE = "Robot Overlord";
	public static final String APP_URL = "https://github.com/MarginallyClever/Robot-Overlord";

	private static final String KEY_WINDOW_WIDTH = "windowWidth";
	private static final String KEY_WINDOW_HEIGHT = "windowHeight";
	private static final String KEY_WINDOW_X = "windowX";
	private static final String KEY_WINDOW_Y = "windowY";
	private static final String KEY_IS_FULLSCREEN = "isFullscreen";
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
	private final Preferences prefs = Preferences.userRoot().node("Evil Overlord");
    //private RecentFiles recentFiles = new RecentFiles();

	/**
	 * The scene being edited and all the entities therein.
	 */
	private final Scene scene = new Scene(System.getProperty("user.dir"));

	/**
	 * The list of actions registered in the editor.  This list is used for calls to
	 * {@link #updateActionEnableStatus()}.
	 */
	private final ArrayList<AbstractAction> actions = new ArrayList<>();

	/**
	 * The main frame of the GUI
	 */
	private JFrame mainFrame;

	/**
	 * The frame that contains the log panel.
	 */
	private static JFrame logFrame;

	/**
	 * The panel that contains the OpenGL canvas.
	 */
	private OpenGLRenderPanel renderPanel;

	/**
	 * The menu bar of the main frame.
	 */
    private JMenuBar mainMenu;

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
	private final EntityTreePanel entityTreePanel = new EntityTreePanel();

	/**
	 * Collated componentpanel of all components in all selected Entities.
	 */
	private final ComponentPanel componentPanel = new ComponentPanel(this);


	public static void main(String[] argv) {
		Log.start();
		//logFrame = LogPanel.createFrame();
		logFrame = LogPanel3.createFrame(Log.getLogLocation());
		PathUtils.goToAppWorkingDirectory();

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception ignored) {}

		//Schedule a job for the event-dispatching thread:
		//creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(RobotOverlord::new);
	}


	private RobotOverlord() {
		super("");

		this.addComponent(new PoseComponent());

		if(GraphicsEnvironment.isHeadless()) {
			throw new RuntimeException("RobotOverlord cannot be run headless yet.");
		}

		Translator.start();
		SoundSystem.start();
		UndoSystem.start();

		preferencesLoad();

		buildMainFrame();
		buildMainMenu();
		createSimulationPanel();
		layoutComponents();
		renderPanel.startAnimationSystem();

		entityTreePanel.addEntity(scene);
		scene.addSceneChangeListener(entityTreePanel);

		addEntity(scene);

		SceneClearAction action = new SceneClearAction(this);
		action.clearScene();
		action.addDefaultEntities();

		listenToClipboardChanges();

		logger.info("** READY **");
    }

	private void listenToClipboardChanges() {
		Clipboard.addListener(this::updateActionEnableStatus);
	}

	private void preferencesLoad() {
		SceneImportAction.setLastDirectory(prefs.get(RobotOverlord.KEY_LAST_DIRECTORY_IMPORT, System.getProperty("user.dir")));
		SceneLoadAction.setLastDirectory(prefs.get(RobotOverlord.KEY_LAST_DIRECTORY_LOAD, System.getProperty("user.dir")));
		SceneSaveAction.setLastDirectory(prefs.get(RobotOverlord.KEY_LAST_DIRECTORY_SAVE, System.getProperty("user.dir")));
	}

	private void preferencesSave() {
		prefs.put(RobotOverlord.KEY_LAST_DIRECTORY_IMPORT, SceneImportAction.getLastDirectory());
		prefs.put(RobotOverlord.KEY_LAST_DIRECTORY_LOAD, SceneLoadAction.getLastDirectory());
		prefs.put(RobotOverlord.KEY_LAST_DIRECTORY_SAVE, SceneSaveAction.getLastDirectory());
	}

	private void createSimulationPanel() {
		renderPanel = new OpenGLRenderPanel(this,scene);
	}

	private JComponent buildEntityManagerPanel() {
        logger.info("buildEntityManagerPanel()");

		return entityTreePanel;
	}

	private void layoutComponents() {
        logger.info("build main splitter");

		// the right hand stuff
		rightFrameSplitter.add(buildEntityManagerPanel());
		rightFrameSplitter.add(componentPanel);
		// make sure the master panel can't be squished.
        Dimension minimumSize = new Dimension(360,300);
        rightFrameSplitter.setMinimumSize(minimumSize);
        // if the window resizes, give top and bottom halves equal share of the real estate
		rightFrameSplitter.setResizeWeight(0.25);

        logger.info("Build right side splitter");
        splitLeftRight.add(renderPanel);
        splitLeftRight.add(rightFrameSplitter);
        // if the window resizes, give left half as much real estate as it can get.
        splitLeftRight.setResizeWeight(1);

        mainFrame.add(splitLeftRight);
        
        mainFrame.setJMenuBar(mainMenu);
	}

	private void buildMainFrame() {
		logger.info("buildMainFrame()");
		// start the main application frame - the largest visible rectangle on the screen with the minimize/maximize/close buttons.
        mainFrame = new JFrame( APP_TITLE + " " + VERSION );
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        mainFrame.setLayout(new java.awt.BorderLayout());
        mainFrame.setExtendedState(mainFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        mainFrame.setVisible(true);
		setWindowSizeAndPosition();
		setupDropTarget();
        mainFrame.addWindowListener(new WindowAdapter() {
            // when someone tries to close the app, confirm it.
			@Override
			public void windowClosing(WindowEvent e) {
				confirmClose();
				super.windowClosing(e);
			}

			// switch back to this window
			@Override
            public void windowActivated(WindowEvent e) {
				super.windowActivated(e);
			}

			// switch away to another window
			@Override
            public void windowDeactivated(WindowEvent e) {
				super.windowDeactivated(e);
			}
		});

		mainFrame.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				saveWindowSizeAndPosition();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
				saveWindowSizeAndPosition();
			}
		});
	}

	private void setWindowSizeAndPosition() {
		logger.info("Set window size and position");

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		int windowW = prefs.getInt(KEY_WINDOW_WIDTH, dim.width);
		int windowH = prefs.getInt(KEY_WINDOW_HEIGHT, dim.height);
		int windowX = prefs.getInt(KEY_WINDOW_X, (dim.width - windowW)/2);
		int windowY = prefs.getInt(KEY_WINDOW_Y, (dim.height - windowH)/2);
		mainFrame.setBounds(windowX, windowY,windowW, windowH);
		boolean isFullscreen = prefs.getBoolean("isFullscreen",false);
		if(isFullscreen) {
			mainFrame.setExtendedState(mainFrame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		}
	}

	// remember window location for next time.
	private void saveWindowSizeAndPosition() {
		int state = mainFrame.getExtendedState();
		boolean isFullscreen = ((state & JFrame.MAXIMIZED_BOTH)!=0);
		prefs.putBoolean(KEY_IS_FULLSCREEN, isFullscreen);
		if(!isFullscreen) {
			Dimension frameSize = mainFrame.getSize();
			prefs.putInt(KEY_WINDOW_WIDTH, frameSize.width);
			prefs.putInt(KEY_WINDOW_HEIGHT, frameSize.height);
			Point p = mainFrame.getLocation();
			prefs.putInt(KEY_WINDOW_X, p.x);
			prefs.putInt(KEY_WINDOW_Y, p.y);
		}
	}

	public JFrame getMainFrame() {
		return mainFrame;
	}

	public Scene getScene() {
		return scene;
	}

	private void buildMainMenu() {
		logger.info("buildMainMenu()");

		mainMenu = new JMenuBar();
		mainMenu.removeAll();
		mainMenu.add(createFileMenu());
		mainMenu.add(createEditMenu());
		mainMenu.add(createDemoMenu());
		mainMenu.add(createHelpMenu());
        mainMenu.updateUI();
	}

	private JComponent createFileMenu() {
		JMenu menu = new JMenu(APP_TITLE);

		menu.add(new SceneClearAction(this));
		menu.add(new SceneLoadAction(this));
		menu.add(new SceneImportAction(this));
		menu.add(new SceneSaveAction(this));
		menu.add(new JSeparator());
		menu.add(new QuitAction(this));

		return menu;
	}

	private JComponent createEditMenu() {
		JMenu menu = new JMenu("Edit");
		menu.add(new JMenuItem(UndoSystem.getCommandUndo()));
		menu.add(new JMenuItem(UndoSystem.getCommandRedo()));
		return menu;
	}

	private JComponent createDemoMenu() {
		JMenu menu = new JMenu("Demos");
		menu.add(new JMenuItem(new DemoAction(this,new DemoSpidee())));
		menu.add(new JMenuItem(new DemoAction(this,new DemoDog())));
		//menu.add(new JMenuItem(new DemoAction(this,new ODEPhysicsDemo())));
		menu.addSeparator();
		menu.add(new JMenuItem(new ShowRobotLibraryPanel(this)));
		buildAvailableScenesTree(menu);
		return menu;
	}

	/**
	 * Searches for all files matching <code>scenes/[owner]/[repo]/[tag]/something.ro</code>
	 * builds <code>[owner]/[repo]/[tag]</code> to the JMenu tree AND adds a
	 * new SceneImportAction(this, something.ro)) to the leaf of the tree.
	 * @param menu the JMenu that is the root of the new menu tree.
	 */
	private void buildAvailableScenesTree(JMenu menu) {
		// scan 'plugins' folder for sub-folders.  make them submenus.
		File rootDirectory = new File(PathUtils.APP_PLUGINS);

		if (!rootDirectory.isDirectory()) {
			return;
		}

		boolean first=true;

		File[] level1Dirs = rootDirectory.listFiles(File::isDirectory);
		if (level1Dirs == null) return;

		for (File level1Dir : level1Dirs) {
			JMenu level1Menu = new JMenu(level1Dir.getName());

			File[] level2Dirs = level1Dir.listFiles(File::isDirectory);
			if (level2Dirs == null) continue;

			for (File level2Dir : level2Dirs) {
				JMenu level2Menu = new JMenu(level2Dir.getName());

				File[] level3Dirs = level2Dir.listFiles(File::isDirectory);
				if (level3Dirs == null) continue;

				for (File level3Dir : level3Dirs) {
					File[] roFiles = level3Dir.listFiles((dir, name) -> name.toLowerCase().endsWith(".ro"));
					if (roFiles == null || roFiles.length == 0) continue;

					JMenu level3Menu = new JMenu(level3Dir.getName());

					for (File roFile : roFiles) {
						level3Menu.add(new JMenuItem(new SceneImportAction(this, roFile)));
					}

					// we found something, add the parent menu.
					if(level3Menu.getItemCount()!=0) {
						level2Menu.add(level3Menu);
					}
				}

				// we found something, add the parent menu.
				if(level2Menu.getItemCount()!=0) {
					level1Menu.add(level2Menu);
				}
			}

			// we found something, add the parent menu.
			if(level1Menu.getItemCount()!=0) {
				// first time through, add a separator.
				if(first) {
					first = false;
					menu.add(new JSeparator());
				}
				menu.add(level1Menu);
			}
		}
	}

	private JComponent createHelpMenu() {
		JMenu menu = new JMenu("Help");
		JMenuItem buttonViewLog = new JMenuItem("Show Log");
		buttonViewLog.addActionListener((e) -> showLogDialog() );
		menu.add(buttonViewLog);
		menu.add(new JMenuItem(new AboutControlsAction()));
		menu.add(new JMenuItem(new ForumsAction()));
		menu.add(new JMenuItem(new CheckForUpdateAction()));
		menu.add(new JMenuItem(new AboutAction()));
		return menu;
	}

	private void showLogDialog() {
		logFrame.setVisible(true);
	}

    private void updateSelectEntities() {
		entityTreePanel.setSelection(Clipboard.getSelectedEntities());
		renderPanel.updateSubjects(Clipboard.getSelectedEntities());
		updateComponentPanel();
	}

	public void updateComponentPanel() {
		componentPanel.refreshContentsFromClipboard();
	}

	public void confirmClose() {
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
        }
	}

	/**
	 * Deep search for a child with this name.
	 * @param name the name to match
	 * @return the entity.  null if nothing found.
	 */
	@Deprecated
	public Entity findEntityWithName(String name) {
		ArrayList<Entity> list = new ArrayList<>();
		list.add(scene);
		while( !list.isEmpty() ) {
			Entity obj = list.remove(0);
			String objectName = obj.getName();
			if(name.equals(objectName)) return obj;
			list.addAll(obj.getChildren());
		}
		return null;
	}

	public CameraComponent getCamera() {
		return findFirstComponentRecursive(CameraComponent.class);
	}

	/**
	 * Tell all Actions to check if they are active.
	 */
	private void updateActionEnableStatus() {
		updateSelectEntities();

		for(AbstractAction a : actions) {
			if(a instanceof EditorAction) {
				((EditorAction)a).updateEnableStatus();
			}
		}

		entityTreePanel.updateActionEnableStatus();
	}

	private void setupDropTarget() {
		logger.debug("adding drag + drop support...");
		new DropTarget(mainFrame,new DropTargetAdapter() {
			@Override
			public void drop(DropTargetDropEvent event) {
				try {
					Transferable tr = event.getTransferable();
					DataFlavor[] flavors = tr.getTransferDataFlavors();
					for (DataFlavor flavor : flavors) {
						logger.debug("Possible flavor: {}", flavor.getMimeType());
						if (flavor.isFlavorJavaFileListType()) {
							event.acceptDrop(DnDConstants.ACTION_COPY);
							Object object = tr.getTransferData(flavor);
							if (object instanceof List<?>) {
								List<?> list = (List<?>) object;
								if (list.size() > 0) {
									object = list.get(0);
									if (object instanceof File) {
										File file = (File) object;
										if(importMesh(file.getAbsolutePath())) {
											event.dropComplete(true);
											return;
										}
										if(importScene(file)) {
											event.dropComplete(true);
											return;
										}
									}
								}
							}
						}
					}
					logger.debug("Drop failed: {}", event);
					event.rejectDrop();
				} catch (Exception e) {
					logger.error("Drop error", e);
					event.rejectDrop();
				}
			}
		});
	}

	private boolean importScene(File file) {
		SceneImportAction action = new SceneImportAction(this);
		return action.loadFile(file);
	}

	private boolean importMesh(String absolutePath) {
		if(!MeshFactory.canLoad(absolutePath)) return false;

		logger.debug("importing mesh "+absolutePath);
        try {
			// create entity.
			Entity entity = new Entity();
			entity.setName(getFilenameWithoutExtensionFromPath(absolutePath));
			// add shape, which will add pose and material.
			ShapeComponent shape = new MeshFromFile(absolutePath);
			entity.addComponent(shape);
			// move entity to camera orbit point so that it is visible.
			PoseComponent pose = entity.findFirstComponent(PoseComponent.class);
			pose.setPosition(getCamera().getOrbitPoint());

			// add entity to scene.
			UndoSystem.addEvent(this,new EntityAddEdit(getScene(),entity));
			//robotoverlord.setSelectedEntity(entity);
		} catch(Exception e) {
			logger.error("Error opening file",e);
			return false;
		}
		return true;
	}

	private String getFilenameWithoutExtensionFromPath(String absolutePath) {
		File f = new File(absolutePath);
		String fullName = f.getName();
		return fullName.substring(0,fullName.lastIndexOf('.'));
	}
}
