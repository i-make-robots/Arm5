package com.marginallyclever.robotoverlord.swing.entitytreepanel;

import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.entity.EntityManagerEvent;
import com.marginallyclever.robotoverlord.clipboard.Clipboard;
import com.marginallyclever.robotoverlord.swing.EditorAction;
import com.marginallyclever.robotoverlord.swing.UndoSystem;
import com.marginallyclever.robotoverlord.swing.actions.*;
import com.marginallyclever.robotoverlord.swing.edits.SelectEdit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * {@link EntityTreePanel} provides a UI to view/edit the contents of an {@link EntityManager}.
 * @author Dan Royer
 */
public class EntityTreePanel extends JPanel {
	private static final Logger logger = LoggerFactory.getLogger(EntityTreePanel.class);
	private final JTree tree = new JTree();
	private final DefaultTreeModel treeModel = new EntityTreeModel(null);
	private final List<AbstractAction> actions = new ArrayList<>();
	private final EntityManager entityManager;

	public EntityTreePanel(EntityManager entityManager) {
		super(new BorderLayout());
		this.setName("EntityTreePanel");
		this.entityManager = entityManager;

		tree.setShowsRootHandles(true);
		DefaultTreeCellRenderer treeCellRenderer = new FullNameTreeCellRenderer();
		tree.setCellRenderer(treeCellRenderer);
		tree.setCellEditor(new EntityTreeCellEditor(tree, treeCellRenderer));
		tree.setEditable(true);
		tree.setModel(treeModel);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		tree.setDragEnabled(true);
		tree.setDropMode(DropMode.ON_OR_INSERT);
		tree.setTransferHandler(new EntityTreeTransferHandler(entityManager));
		JScrollPane scroll = new JScrollPane();
		scroll.setViewportView(tree);
		this.add(scroll, BorderLayout.CENTER);
		this.add(createMenu(), BorderLayout.NORTH);

		addTreeSelectionListener();
		addEntityManagerListener();

		populateTree();
	}

	private void addEntityManagerListener() {
		entityManager.addListener((event)-> {
			if(event.type == EntityManagerEvent.ENTITY_ADDED) {
				addEntityToParent(event.child,event.parent);
			} else if(event.type == EntityManagerEvent.ENTITY_REMOVED) {
				removeEntityFromParent(event.child,event.parent);
			} else if(event.type == EntityManagerEvent.ENTITY_RENAMED) {
				EntityTreeNode node = findTreeNode(event.child);
				treeModel.nodeChanged(node);
			}
			repaint();
		});
	}

	private JComponent createMenu() {
		JToolBar menu = new JToolBar();

		EntityAddChildAction entityAddAction = new EntityAddChildAction(entityManager);
		EntityCopyAction entityCopyAction = new EntityCopyAction(entityManager);
		EntityPasteAction entityPasteAction = new EntityPasteAction(entityManager);
		EntityDeleteAction entityDeleteAction = new EntityDeleteAction(entityManager);
		EntityCutAction entityCutAction = new EntityCutAction(entityDeleteAction, entityCopyAction);
		EntityRenameAction entityRenameAction = new EntityRenameAction(entityManager);

		menu.add(entityAddAction);
		menu.add(entityDeleteAction);
		menu.add(entityCopyAction);
		menu.add(entityCutAction);
		menu.add(entityPasteAction);
		menu.add(entityRenameAction);

		actions.add(entityCopyAction);
		actions.add(entityPasteAction);
		actions.add(entityCutAction);
		actions.add(entityDeleteAction);
		actions.add(entityRenameAction);

		return menu;
	}

	/**
	 * Set the selection list to one entity.
	 * @param one the entity to select
	 */
	public void setSelection(Entity one) {
		List<Entity> newSelectionList = new ArrayList<>();
		if(one!=null) newSelectionList.add(one);
		setSelection(newSelectionList);
	}

	/**
	 * Set the selection to the given list of entities.
	 * @param newSelectionList the list of entities to select
	 */
	public void setSelection(List<Entity> newSelectionList) {
		ArrayList<TreePath> pathList = new ArrayList<>();
		for(Entity e : newSelectionList) {
			EntityTreeNode node = findTreeNode(e);
			if(node!=null) {
				pathList.add(new TreePath(node.getPath()));
			}
		}

		if(pathList.isEmpty()) {
			tree.clearSelection();
		} else {
			TreePath[] paths = new TreePath[pathList.size()];
			pathList.toArray(paths);

			tree.setSelectionPaths(paths);
		}
	}

	private EntityTreeNode findTreeNode(Entity e) {
		EntityTreeNode root = ((EntityTreeNode)treeModel.getRoot());
		if(root==null) return null;

		List<TreeNode> list = new ArrayList<>();
		list.add(root);
		while(!list.isEmpty()) {
			TreeNode treeNode = list.remove(0);
			if(treeNode instanceof EntityTreeNode) {
				EntityTreeNode node = (EntityTreeNode) treeNode;
				if (e == node.getUserObject()) {
					return node;
				}
			} else {
				System.err.println("findTreeNode problem @ "+treeNode);
			}
			list.addAll(Collections.list(treeNode.children()));
		}
		return null;
	}

	public void populateTree() {
		//logger.debug("populateTree");
        Entity root = entityManager.getRoot();

		treeModel.setRoot(new EntityTreeNode(root));
		List<Entity> list = new ArrayList<>(root.getChildren());
		while(!list.isEmpty()) {
			Entity child = list.remove(0);
			addEntityToParent(child,child.getParent());
			list.addAll(child.getChildren());
		}
	}

	private void addTreeSelectionListener() {
		tree.addTreeSelectionListener((arg0) -> updateTreeSelection());
		updateTreeSelection();
	}

	private void updateTreeSelection() {
		List<Entity> selected = new ArrayList<>();
		TreePath[] selectedPaths = tree.getSelectionPaths();
		if(selectedPaths!=null) {
			for (TreePath selectedPath : selectedPaths) {
				EntityTreeNode selectedNode = (EntityTreeNode) selectedPath.getLastPathComponent();
				Entity entity = (Entity)selectedNode.getUserObject();
				selected.add(entity);
			}
		}
		UndoSystem.addEvent(new SelectEdit(Clipboard.getSelectedEntities(), selected));
	}


	private void recursivelyAddChildren(EntityTreeNode parentNode, Entity child) {
		//logger.debug("recursivelyAddChildren "+child.getName()+" to "+parentNode.getEntity().getName());
		EntityTreeNode newNode = new EntityTreeNode(child);
		parentNode.add(newNode);
		int [] index = new int[]{parentNode.getIndex(findTreeNode(child))};
		treeModel.nodesWereInserted(parentNode, index);
		for(Entity child2 : child.getChildren()) {
			recursivelyAddChildren(newNode,child2);
		}
	}

	/**
	 * Add a child to a parent.
	 * @param child the child to add
	 * @param parent the parent to add the child to
	 */
	private void addEntityToParent(Entity child, Entity parent) {
		EntityTreeNode parentNode = findTreeNode(parent);
		if(parentNode!=null) {
			recursivelyAddChildren(parentNode,child);
		}
	}

	private void removeEntityFromParent(Entity child, Entity parent) {
		EntityTreeNode parentNode = findTreeNode(parent);
		EntityTreeNode childNode = findTreeNode(child);
		if(parentNode!=null && childNode!=null) {
			int [] list = new int[]{parentNode.getIndex(childNode)};
			parentNode.remove(childNode);
			treeModel.nodesWereRemoved(parentNode, list, new Object[]{childNode});
		}
	}

	/**
	 * Tell all {@link EditorAction}s of this panel to check if they are active.
	 */
	public void updateActionEnableStatus() {
		for(AbstractAction a : actions) {
			if(a instanceof EditorAction) {
				((EditorAction)a).updateEnableStatus();
			}
		}
	}
}
