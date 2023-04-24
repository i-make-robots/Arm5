package com.marginallyclever.robotoverlord.swinginterface;

import com.marginallyclever.robotoverlord.Component;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.RobotOverlord;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swinginterface.actions.ComponentAddAction;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Collate all the {@link java.awt.Component}s for selected {@link Entity}s.
 */
public class ComponentPanel extends JPanel {
	private final RobotOverlord robotOverlord;
	private final JPanel centerPanel = new JPanel(new BorderLayout());

	public ComponentPanel(RobotOverlord robotOverlord) {
		super(new BorderLayout());
		this.robotOverlord = robotOverlord;

		add(createToolBar(),BorderLayout.NORTH);
		add(new JScrollPane(centerPanel),BorderLayout.CENTER);
		refreshContentsFromClipboard();
	}

	private JComponent createToolBar() {
		JToolBar bar = new JToolBar();
		ComponentAddAction add = new ComponentAddAction(this);
		bar.add(add);
		return bar;
	}

	/**
	 * Collate all the {@link java.awt.Component}s for selected {@link Entity}s.
	 * @param entityList the list of entities to collate
	 */
	public void refreshContentsFromClipboard() {
		List<Entity> entityList = Clipboard.getSelectedEntities();
		centerPanel.removeAll();

		int size = entityList.size();
		if(size>0) {
			ViewPanel panel = new ViewPanel(robotOverlord);

			if (size == 1) {
				buildSingleEntityPanel(panel, entityList.get(0));
			} else if (size > 0) {
				// TODO finish and re-enable this.
				//buildMultipleEntityPanel(panel,entityList);
			}

			centerPanel.add(panel.getFinalView(), BorderLayout.PAGE_START);
		}
		centerPanel.repaint();
		centerPanel.revalidate();

		if( centerPanel.getParent() instanceof JScrollPane ) {
			JScrollPane scroller = (JScrollPane)getParent();
			scroller.getVerticalScrollBar().setValue(0);
		}
	}

	private void buildSingleEntityPanel(ViewPanel panel, Entity entity) {
		if(entity != null) entity.getView(panel);
	}

	private void buildMultipleEntityPanel(ViewPanel panel, List<Entity> entityList) {
		// compare Entities and keep matching Components.
		Entity first = entityList.get(0);
		for(int i=0;i<first.getComponentCount();++i) {
			Component component = first.getComponent(i);
			if(sameComponentInAllEntities(component,entityList)) {
				// TODO display values shared across all remaining Components.  At present this shows only the first entity's component.
				panel.startNewSubPanel(component);
				component.getView(panel);
			}
		}
	}

	private boolean sameComponentInAllEntities(Component component, List<Entity> entityList) {
		for(Entity e : entityList) {
			if(!e.containsAnInstanceOfTheSameClass(component)) {
				return false;
			}
		}
		return true;
	}
}
