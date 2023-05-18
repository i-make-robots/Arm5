package com.marginallyclever.robotoverlord.parameters;

import com.marginallyclever.robotoverlord.SerializationContext;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Dan Royer
 * @since 1.6.0
 *
 */
public class StringParameter extends AbstractParameter<String> {
	public StringParameter(String name, String value) {
		super(name,value);
	}

	@Override
	public String toString() {
		return getName()+"="+t;
	}

	@Override
	public JSONObject toJSON(SerializationContext context) {
		JSONObject jo = super.toJSON(context);
		jo.put("value",get());
		return jo;
	}

	@Override
	public void parseJSON(JSONObject jo,SerializationContext context) throws JSONException {
		super.parseJSON(jo,context);
		// if value is null it will not appear in the JSON.
		if(jo.has("value")) {
			set(jo.getString("value"));
		}
	}
}
