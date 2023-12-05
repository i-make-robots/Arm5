package com.marginallyclever.robotoverlord.components;

import com.marginallyclever.robotoverlord.components.demo.CrabRobotComponent;
import com.marginallyclever.robotoverlord.components.demo.DogRobotComponent;
import com.marginallyclever.robotoverlord.components.motors.DCMotorComponent;
import com.marginallyclever.robotoverlord.components.motors.ServoComponent;
import com.marginallyclever.robotoverlord.components.motors.StepperMotorComponent;
import com.marginallyclever.robotoverlord.components.program.ProgramComponent;
import com.marginallyclever.robotoverlord.components.program.ProgramPathComponent;
import com.marginallyclever.robotoverlord.components.shapes.*;
import com.marginallyclever.robotoverlord.components.vehicle.VehicleComponent;
import com.marginallyclever.robotoverlord.components.vehicle.WheelComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/**
 * A factory to list and create {@link Component}s.
 *
 * @author Dan Royer
 * @since 2.5.0
 */
public abstract class ComponentFactory {
	private static final Logger logger = LoggerFactory.getLogger(ComponentFactory.class);
	private static final Class<?> [] available = {
			PoseComponent.class,

			LightComponent.class,
			CameraComponent.class,
			MaterialComponent.class,

			MeshFromFile.class,
			Box.class,
			Cylinder.class,
			Decal.class,
			Grid.class,
			Sphere.class,

			Background.class,

			OriginAdjustComponent.class,

			DHComponent.class,
			RobotComponent.class,
			//RobotArmComponent.class,
			ArmEndEffectorComponent.class,

			CrabRobotComponent.class,
			DogRobotComponent.class,

			GCodePathComponent.class,
			ProgramPathComponent.class,
			ProgramComponent.class,
			ProjectorComponent.class,

			GripperComponentLinear.class,
			GripperComponentRotary.class,
			GripperComponentJaw.class,

			DCMotorComponent.class,
			ServoComponent.class,
			StepperMotorComponent.class,

			VehicleComponent.class,
			WheelComponent.class,
	};
	
	public static ArrayList<String> getAllComponentNames() {
		ArrayList<String> names = new ArrayList<>();
		for( Class<?> c : available ) {
			names.add( c.getName() );
		}
		return names;
	}

	public static Class<?> getClassFromName(String name) {
		for( Class<?> c : available ) {
			if( c.getName().equals(name) ) return c;
			//if( c.getSimpleName().equals(name) ) return c;
		}
		return null;
	}

	public static Component load(String name) throws IllegalArgumentException {
		Class<?> c = getClassFromName(name);
		if(c==null) {
			throw new IllegalArgumentException("ComponentFactory does not recognize '"+name+"'.");
		}
		return createInstance(c);
	}

	public static Component createInstance(Class<?> c) {
		try {
			for (Constructor<?> constructor : c.getDeclaredConstructors()) {
				if (constructor.getParameterCount() == 0) {
					return (Component) constructor.newInstance();
				}
			}
		} catch (IllegalAccessException | InvocationTargetException | InstantiationException var5) {
			var5.printStackTrace();
		}
		return null;
	}
}
