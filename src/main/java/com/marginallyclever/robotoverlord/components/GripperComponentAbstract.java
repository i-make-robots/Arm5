package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.IntParameter;
import com.marginallyclever.robotoverlord.systems.render.mesh.Mesh;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A gripper is a component that can grab and hold things.
 * @author Dan Royer
 * @since 2.11.0
 */
public abstract class GripperComponentAbstract extends ShapeComponent {
    public static String [] names = new String[] {
            "Opening",
            "Open",
            "Closing",
            "Closed"
    };

    public static final int MODE_OPENING = 0;
    public static final int MODE_OPEN = 1;
    public static final int MODE_CLOSING = 2;
    public static final int MODE_CLOSED = 3;

    public final IntParameter mode = new IntParameter("Mode",MODE_OPEN);

    protected GripperComponentAbstract() {
        super();
        myMesh = new Mesh();
    }

    public int getMode() {
        return mode.get();
    }

    /**
     * @return the {@link ShapeComponent} of all {@link GripperComponentJaw} children.
     */
    public List<GripperComponentJaw> getJaws() {
        List<GripperComponentJaw> results = new ArrayList<>();
        List<Entity> children = getEntity().getChildren();
        for(Entity child : children) {
            GripperComponentJaw jaw = child.getComponent(GripperComponentJaw.class);
            if(jaw!=null) results.add(jaw);
        }
        return results;
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        jo.put("mode",mode.toJSON(context));
        return jo;
    }

    @Override
    public void parseJSON(JSONObject json, SerializationContext context) {
        mode.parseJSON(json.getJSONObject("mode"),context);
        super.parseJSON(json,context);
    }
}
