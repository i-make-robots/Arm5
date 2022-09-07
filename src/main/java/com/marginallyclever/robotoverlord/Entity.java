package com.marginallyclever.robotoverlord;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

/**
 * Entities are nodes in a tree of data that can find each other and observe/be
 * observed
 * 
 * @author Dan Royer
 *
 */
public class Entity implements PropertyChangeListener, Cloneable, Serializable {
	/**
	 * 
	 */
	@Serial
	private static final long serialVersionUID = -470516237871859765L;

	private String name;

	// my children
	protected transient ArrayList<Entity> children = new ArrayList<>();
	
	// my parent
	protected transient Entity parent;

	// unique ids for all objects in the world.
	// zero is reserved to indicate no object.
	static private int pickNameCounter=1;

	// my unique id
	private final transient int pickName = pickNameCounter++;

	// who is listening to me?
	protected ArrayList<PropertyChangeListener> propertyChangeListeners = new ArrayList<>();

	private final List<Component> components = new ArrayList<>();
	
	public Entity() {
		super();
		this.name = this.getClass().getSimpleName();
	}

	public Entity(String name) {
		super();
		this.name = name;
	}

	public void set(Entity b) {
		setName(b.getName());
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		// if(hasChanged()) return;
		// setChanged();
		this.name = name;
		// notifyObservers(name);
	}

	/**
	 * @param dt seconds since last update.
	 */
	public void update(double dt) {
		for(Component c : components) {
			if(c.getEnabled()) c.update(dt);
		}

		for(Entity e : children) {
			e.update(dt);
		}
	}

	/**
	 * Render this Entity to the display
	 * @param gl2 the render context
	 */
	public void render(GL2 gl2) {}
	
	public boolean hasChild(Entity o) {
		return children.contains(o);
	}

	public String getUniqueChildName(Entity e) {
		String rootName = e.getName(); 
		// strip digits from end of name.
		rootName = rootName.replaceAll("\\d*$", "");
		String name = rootName;
		
		int count=1;
		boolean foundMatch;
		
		do {
			foundMatch=false;
			for( Entity c : children ) {
				if( c.getName().equals(name) ) {
					// matches an existing name.  increment by one and check everybody again.
					name = rootName+Integer.toString(count++);
					foundMatch=true;
				}
			}
		} while(foundMatch);
		// unique name found.
		return name;
	}
	
	public void addChild(int index,Entity e) {
		// check if any child has a matching name
		e.setName(getUniqueChildName(e));
		children.add(index,e);
		e.setParent(this);
	}
	
	public void addChild(Entity e) {
		addChild(children.size(),e);
	}

	public void removeChild(Entity e) {
		if (children.contains(e)) {
			children.remove(e);
			e.setParent(null);
		}
	}

	public ArrayList<Entity> getChildren() {
		return children;
	}

	public void removeParent() {
		parent = null;
	}

	public Entity getParent() {
		return parent;
	}

	public void setParent(Entity e) {
		parent = e;
	}

	// Find the root node.
	public Entity getRoot() {
		Entity e = this;
		while (e.getParent() != null)
			e = e.getParent();
		return e;
	}

	/**
	 * Search the entity tree based on an absolute or relative Unix-style path.
	 * 
	 * @param path the search query
	 * @return the requested entity or null.
	 */
	public Entity findByPath(String path) {
		String[] pathComponents = path.split("/");

		// if absolute path, start with root node.
		int i = 0;
		Entity e;
		if (path.startsWith("/")) {
			e = getRoot();
			i = 2;
		} else {
			e = this;
		}

		while (i < pathComponents.length) {
			String name = pathComponents[i++];

			if (e == null)
				break;
			if (name.contentEquals("..")) {
				// ".." = my parent
				e = e.getParent();
			} else if (name.contentEquals(".")) {
				// "." is me!
				continue;
			} else {
				boolean found = false;
				for (Entity c : e.getChildren()) {
					if (name.contentEquals(c.getName())) {
						e = c;
						found = true;
						break;
					}
				}
				if (found == false)
					return null; // does not exist
			}
		}

		return e;
	}

	/**
	 * @return This entity's full pathname in the entity tree.
	 */
	public String getFullPath() {
		String sum = "";
		Entity e = this;

		do {
			sum = "/" + e.getName() + sum;
			e = e.getParent();
		} while (e != null);

		return sum;
	}

	/**
	 * Explains to View in abstract terms the control interface for this entity.
	 * Derivatives of View implement concrete versions of that view.
	 * 
	 * @param view
	 */
	public void getView(ViewPanel view) {
		for(Component c : components) {
			view.pushStack(c.getName(),c.getName());
			c.getView(view);
			view.popStack();
		}
	}

	protected void getViewOfChildren(ViewPanel view) {
		for (Entity child : children) {
			if (child.getChildren().isEmpty()) {
				// only leaves
				child.getView(view);
			}
		}
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Entity e = (Entity)super.clone();
		
		e.children = new ArrayList<Entity>();
		for(int i=0;i<children.size();++i) {
			e.children.add((Entity)children.get(i).clone());
		}
		
		e.propertyChangeListeners = new ArrayList<PropertyChangeListener>();
		
		return e;
	}
	
	// Serialization
	private void writeObject(ObjectOutputStream stream) throws IOException {
		stream.writeObject(name);
		stream.writeInt(children.size());
		for( Entity c : children ) {
			stream.writeObject(c);
		}
	}

	// Serialization
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
		name = (String)stream.readObject();

		// load and associate children
		children = new ArrayList<Entity>();
		int count = stream.readInt();
		for(int i=0;i<count;++i) {
			Entity child = (Entity)stream.readObject();
			addChild(child);
		}
	}

	/**
	 * Something this Entity is observing has changed. Deal with it!
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {}
	
	public void addPropertyChangeListener(PropertyChangeListener p) {
		propertyChangeListeners.add(p);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener p) {
		propertyChangeListeners.remove(p);
	}
	
	protected void notifyPropertyChangeListeners(PropertyChangeEvent evt) {
		for( PropertyChangeListener p : propertyChangeListeners ) {
			p.propertyChange(evt);
		}
	}

	public void removeAllChildren() {
		while(!children.isEmpty()) removeChild(children.get(0));
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append("name="+name+", ");
		s.append("children="+Arrays.toString(children.toArray())+", ");
		s.append("components="+Arrays.toString(components.toArray()));
		return s.toString();
	}

	public int getComponentCount() {
		return components.size();
	}

	public void addComponent(Component c) {
		if(containsAnInstanceOfTheSameClass(c)) return;
		components.add(c);
		c.setEntity(this);
	}

	public boolean containsAnInstanceOfTheSameClass(Component c0) {
		Class clazz = c0.getClass();
		for(Component c : components) {
			if(clazz == c.getClass()) return true;
		}
		return false;
	}

	public Component getComponent(int i) {
		return components.get(i);
	}

	public Component findComponentByName(String name) {
		for(Component c : components) {
			if(name.equals(c.getClass().getSimpleName())) {
				return c;
			}
		}
		return null;
	}

	public Iterator<Component> getComponentIterator() {
		return components.iterator();
	}

	public void removeComponent(Component c) {
		components.remove(c);
	}

	/**
	 * Returns any instance of class T found attached to this Entity.
	 * @return the
	 * @param <T> the type to find and return.  Must be derived from Component
	 */
	public <T extends Component> T getComponent(Class<T> clazz) {
		for(Component c : components) {
			if(clazz.isInstance(c)) {
				return (T)c;
			}
		}
		return null;
	}

	/**
	 * Search this Entity and then all child Entities until a {@link Component} match is found.
	 */
	public <T extends Component> T findFirstComponent(Class<T> clazz) {
		T found = getComponent(clazz);
		if(found!=null) return found;

		for(Entity e : children) {
			found = e.findFirstComponent(clazz);
			if(found!=null) return found;
		}

		return null;
	}

	/**
	 * Search the parent of this entity for a component and then that parent and so on.
	 * @param clazz the class of the Component to find
	 * @return the instance found or null
	 * @param <T> the class of the Component to find
	 */
	public <T extends Component> T findFirstComponentInParents(Class<T> clazz) {
		Entity p = parent;
		while(p!=null) {
			T found = p.getComponent(clazz);
			if (found != null) return found;
			p = p.getParent();
		}
		return null;
	}

	protected int getPickName() {
		return pickName;
	}

    public void save(BufferedWriter writer) throws IOException {
		saveName(writer);
		saveChildren(writer);
		saveComponents(writer);
	}

	public void load(BufferedReader reader) throws Exception {
		readName(reader);
		readChildren(reader);
		readComponents(reader);
	}

	private void saveChildren(BufferedWriter writer) throws IOException {
		writer.write("Children=[\n");
		String add="";
		for(Entity c : children) {
			writer.write(c.getClass().getName()+"={\n");
			c.save(writer);
			writer.write("}"+add+"\n");
			add=",";
		}
		writer.write("]\n");
	}

	private void readChildren(BufferedReader reader) throws Exception {
		String str = reader.readLine();
		if(!str.startsWith("Children=[")) throw new IOException("Expected 'Children=[{]' but found "+str);
		while(!(str = reader.readLine()).trim().startsWith("]")) {
			if(!str.endsWith("={")) throw new IOException("Expected '={' and end but found "+str);
			Entity entity = EntityFactory.load(str.substring(0, str.length() - 2));
			this.addChild(entity);
			entity.load(reader);
			str = reader.readLine();  // } or }, at end
			if(!str.startsWith("}")) throw new IOException("Expected '}' and end but found "+str);
		}
	}

	private void saveComponents(BufferedWriter writer) throws IOException {
		writer.write("Components=[\n");
		String add="";
		for(Component c : components) {
			writer.write(c.getClass().getName()+"={\n");
			c.save(writer);
			writer.write("}"+add+"\n");
			add=",";
		}
		writer.write("]\n");
    }

	private void readComponents(BufferedReader reader) throws Exception {
		String str = reader.readLine();
		if(!str.startsWith("Components=[")) throw new IOException("Expected 'Components=[' but found "+str);
		while(!(str = reader.readLine()).trim().startsWith("]")) {
			if (str.endsWith("={")) {
				Component component = ComponentFactory.load(str.substring(0,str.length()-2));
				this.addComponent(component);
				component.load(reader);
				str = reader.readLine();  // } or }, at end
				if(!str.startsWith("}")) throw new IOException("Expected '}' and end but found "+str);
			}
		}
	}

	private void saveName(BufferedWriter writer) throws IOException {
		writer.write("name="+this.name+",\n");
	}

	private void readName(BufferedReader reader) throws IOException {
		String str = reader.readLine();
		if(!str.startsWith("name=")) throw new IOException("Expected 'name=' but found "+str.substring(0,10));
		if(str.endsWith(",")) str = str.substring(0,str.length()-1);
		this.name = str.substring(5);
	}
}
