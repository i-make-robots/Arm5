package com.marginallyclever.robotoverlord.components;

import com.jogamp.opengl.GL3;
import com.marginallyclever.convenience.helpers.MatrixHelper;
import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import org.json.JSONObject;

import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import java.util.List;

/**
 * Identifier so that systems can find each child jaw of a gripper.
 */
public class GripperComponentJaw extends Component {
    public DoubleParameter openDistance = new DoubleParameter("Open Distance",5.0);
    public DoubleParameter closeDistance = new DoubleParameter("Close Distance",1.0);

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject jo = super.toJSON(context);
        jo.put("openDistance",openDistance.toJSON(context));
        jo.put("closeDistance",closeDistance.toJSON(context));
        return jo;
    }

    @Override
    public void parseJSON(JSONObject json, SerializationContext context) {
        openDistance.parseJSON(json.getJSONObject("openDistance"),context);
        if(json.has("closeDistance")) closeDistance.parseJSON(json.getJSONObject("closeDistance"),context);
        super.parseJSON(json,context);
    }
}
