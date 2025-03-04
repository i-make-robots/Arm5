package com.marginallyclever.ro3.apps.shared;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.prefs.Preferences;

/**
 * <p>{@link PersistentJFileChooser} is a {@link JFileChooser} that uses {@link Preferences} to remember the last
 * directory used.  All instances share the same memory.</p>
 */
public class PersistentJFileChooser extends JFileChooser {
    private static final String LAST_USED_DIR = "lastUsedDirectory";
    private final Preferences prefs;

    public PersistentJFileChooser() {
        super();
        prefs = Preferences.userNodeForPackage(PersistentJFileChooser.class);
    }

    @Override
    public int showOpenDialog(java.awt.Component parent) throws HeadlessException {
        retrieveLastUsedDirectory();
        int result = super.showOpenDialog(parent);
        if(result==JFileChooser.APPROVE_OPTION) {
            updateLastUsedDirectory();
        }
        return result;
    }

    @Override
    public int showSaveDialog(java.awt.Component parent) throws HeadlessException {
        retrieveLastUsedDirectory();
        int result = super.showSaveDialog(parent);
        if(result==JFileChooser.APPROVE_OPTION) {
            updateLastUsedDirectory();
        }
        return result;
    }

    private void retrieveLastUsedDirectory() {
        String lastDirPath = prefs.get(LAST_USED_DIR, null);
        if (lastDirPath != null) {
            setCurrentDirectory(new File(lastDirPath));
        }
    }

    private void updateLastUsedDirectory() {
        File currentDir = getCurrentDirectory();
        if (currentDir != null) {
            prefs.put(LAST_USED_DIR, currentDir.getAbsolutePath());
        }
    }
}