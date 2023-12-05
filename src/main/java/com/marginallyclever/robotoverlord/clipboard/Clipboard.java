package com.marginallyclever.robotoverlord.clipboard;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * A static class that stores {@link Entity} and {@link Component} data for copying and pasting.
 * @author Dan Royer
 * @since 2.4.0
 */
public class Clipboard {
    /**
     * The list of entities selected in the editor.  This list is used by Actions.
     */
    private static final List<Entity> selectedEntities = new ArrayList<>();

    /**
     * The list of entities copied to the clipboard.  This list is used by Actions.
     */
    private static Entity copiedEntities = new Entity();

    /**
     * The list of components copied to the clipboard.  This list is used by Actions.
     */
    private static Component copiedComponents = null;

    private static final List<ClipboardListener> listeners = new ArrayList<>();

    public static void setCopiedEntities(Entity container) {
        copiedEntities=container;
        fireClipboardChanged();
    }

    public static Entity getCopiedEntities() {
        return copiedEntities;
    }

    public static void setCopiedComponents(Component container) {
        copiedComponents=container;
        fireClipboardChanged();
    }

    public static Component getCopiedComponents() {
        return copiedComponents;
    }

    public static List<Entity> getSelectedEntities() {
        return new ArrayList<>(selectedEntities);
    }

    public static void setSelectedEntity(Entity entity) {
        selectedEntities.clear();
        selectedEntities.add(entity);
        fireClipboardChanged();
    }

    public static void setSelectedEntities(List<Entity> list) {
        selectedEntities.clear();
        selectedEntities.addAll(list);
        fireClipboardChanged();
    }

    public static void addListener(ClipboardListener l) {
        listeners.add(l);
    }

    public static void removeListener(ClipboardListener l) {
        listeners.remove(l);
    }

    public static void fireClipboardChanged() {
        for(ClipboardListener l : listeners) {
            l.onClipboardChanged();
        }
    }
}
