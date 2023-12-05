package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.SerializationContext;
import com.marginallyclever.robotoverlord.parameters.DoubleParameter;
import com.marginallyclever.robotoverlord.parameters.ListParameter;
import com.marginallyclever.robotoverlord.parameters.ReferenceParameter;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RobotArmComponent extends Component {
    public final ListParameter<ReferenceParameter> joints = new ListParameter<>("Joints",new ReferenceParameter());
    public final ReferenceParameter endEffectorTarget = new ReferenceParameter("End Effector Target",null);
    public DoubleParameter desiredLinearVelocity = new DoubleParameter("Desired Linear Velocity (cm/s)",1);

    public RobotArmComponent() {
        super();
    }

    @Override
    public JSONObject toJSON(SerializationContext context) {
        JSONObject json= super.toJSON(context);
        json.put("joints",joints.toJSON(context));
        return json;
    }

    @Override
    public void parseJSON(JSONObject jo, SerializationContext context) throws JSONException {
        super.parseJSON(jo, context);
        joints.parseJSON(jo.getJSONObject("joints"),context);
    }
}
