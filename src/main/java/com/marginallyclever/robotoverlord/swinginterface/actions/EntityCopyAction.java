package com.marginallyclever.robotoverlord.swinginterface.actions;

import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.UnicodeIcon;
import com.marginallyclever.robotoverlord.swinginterface.EditorAction;
import com.marginallyclever.robotoverlord.swinginterface.translator.Translator;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

/**
 * Makes a deep copy of the selected {@link com.marginallyclever.robotoverlord.Entity}.
 */
public class EntityCopyAction extends AbstractAction implements EditorAction {
    protected final RobotOverlord ro;

    public EntityCopyAction(RobotOverlord ro) {
        super(Translator.get("EntityCopyAction.name"));
        this.ro=ro;
        putValue(Action.SMALL_ICON,new UnicodeIcon("📋"));
        putValue(Action.SHORT_DESCRIPTION, Translator.get("EntityCopyAction.shortDescription"));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK) );
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        List<Entity> list = ro.getSelectedEntities();
        Entity container = new Entity();
        for(Entity entity : list) {
            Entity e = new Entity();
            e.parseJSON(entity.toJSON());
            container.addEntity(e);
        }
        ro.setCopiedEntities(container);
    }

    private Entity makeDeepCopy(Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateEnableStatus() {
        setEnabled(!ro.getSelectedEntities().isEmpty());
    }
}
