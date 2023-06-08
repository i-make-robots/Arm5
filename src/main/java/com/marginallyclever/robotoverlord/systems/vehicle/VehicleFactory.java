package com.marginallyclever.robotoverlord.systems.vehicle;

import com.marginallyclever.robotoverlord.components.MaterialComponent;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.motors.MotorComponent;
import com.marginallyclever.robotoverlord.components.shapes.Box;
import com.marginallyclever.robotoverlord.components.shapes.Cylinder;
import com.marginallyclever.robotoverlord.components.vehicle.CarComponent;
import com.marginallyclever.robotoverlord.components.vehicle.WheelComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.ReferenceParameter;
import com.marginallyclever.robotoverlord.systems.motor.MotorFactory;

import javax.swing.*;
import javax.vecmath.Vector3d;

public class VehicleFactory {
    public static final int FWD4 = 0;
    public static final int RWD4 = 1;
    public static final int MOTORCYCLE = 2;
    public static final int MECANUM = 3;
    public static final int OMNI = 4;
    public static final int TANK = 5;
    public static final int CHASSIS4 = 6;

    private static final String[] names = {
            "FWD 4",
            "RWD 4",
            "Motorcycle",
            "Mecanum",
            "Omni",
            "Tank",
            "4 Wheel Chassis",
    };

    public static Entity createByID(int type, EntityManager entityManager) {
        return switch (type) {
            case VehicleFactory.CHASSIS4 -> VehicleFactory.build4WheelCarWithNoMotor(entityManager);
            case VehicleFactory.MECANUM -> VehicleFactory.buildMecanum(entityManager);
            case VehicleFactory.OMNI -> VehicleFactory.buildOmni(entityManager);
            case VehicleFactory.TANK -> VehicleFactory.buildTank(entityManager);
            case VehicleFactory.FWD4 -> VehicleFactory.buildFWD(entityManager);
            case VehicleFactory.RWD4 -> VehicleFactory.buildRWD(entityManager);
            case VehicleFactory.MOTORCYCLE -> VehicleFactory.buildMotorcycle(entityManager);
            default -> throw new RuntimeException("Unknown vehicle type: " + type);
        };
    }

    public static String [] getNames() {
        return names;
    }

    /**
     * Build a car with 4 wheels and front wheel steering.
     * Immediately add it to the entityManager root.
     * Assume X+ is the forward direction of travel.
     * The order of the wheels is front left, front right, rear left, rear right.
     * @return the car entity
     */
    private static Entity build4WheelCarWithNoMotor(EntityManager entityManager) {
        Entity carEntity = new Entity("CarWithNoMotor");
        CarComponent car = new CarComponent();
        carEntity.addComponent(car);
        entityManager.addEntityToParent(carEntity, entityManager.getRoot());

        Entity mesh = new Entity("Mesh");
        mesh.addComponent(new Box(20,2,18));
        mesh.getComponent(PoseComponent.class).setPosition(new Vector3d(0,0,1.5));
        entityManager.addEntityToParent(mesh, carEntity);

        // add 4 wheels
        Entity[] wheelEntity = new Entity[4];
        for (int i = 0; i < wheelEntity.length; ++i) {
            // add suspension to the body
            Entity suspension = new Entity("Suspension" + i);
            entityManager.addEntityToParent(suspension, carEntity);
            // add wheel to suspension
            wheelEntity[i] = new Entity("Wheel" + i);
            entityManager.addEntityToParent(wheelEntity[i], suspension);
            Entity wheelMesh = new Entity("Mesh");
            wheelMesh.addComponent(new Cylinder(0.5,2,2));
            wheelMesh.getComponent(PoseComponent.class).setRotation(new Vector3d(0,90,90));
            entityManager.addEntityToParent(wheelMesh, wheelEntity[i]);

            WheelComponent wheel = new WheelComponent();
            wheelEntity[i].addComponent(wheel);
            wheel.diameter.set(4.0);
            wheel.width.set(0.5);
            car.addWheel(wheelEntity[i]);
        }

        // place wheels at the corners of the car
        wheelEntity[0].getComponent(PoseComponent.class).setPosition(new Vector3d( 10, -10, 1));
        wheelEntity[1].getComponent(PoseComponent.class).setPosition(new Vector3d( 10,  10, 1));
        wheelEntity[2].getComponent(PoseComponent.class).setPosition(new Vector3d(-10, -10, 1));
        wheelEntity[3].getComponent(PoseComponent.class).setPosition(new Vector3d(-10,  10, 1));

        return carEntity;
    }

    /**
     * Build a car with 3 wheels.  Steering is omni-style (wheels turn outwards).
     */
    public static Entity buildOmni(EntityManager entityManager) {
        Entity carEntity = new Entity("Omni");
        CarComponent car = new CarComponent();
        carEntity.addComponent(car);

        car.wheelType.set(CarComponent.WHEEL_OMNI);

        Entity mesh = new Entity("Mesh");
        mesh.addComponent(new Cylinder(2,8,8));
        mesh.getComponent(PoseComponent.class).setPosition(new Vector3d(0,0,1.5));
        entityManager.addEntityToParent(mesh, carEntity);

        for (int i = 0; i < 3; ++i) {
            Entity wheelEntity = new Entity("Wheel" + i);
            entityManager.addEntityToParent(wheelEntity, carEntity);
            car.addWheel(wheelEntity);

            Entity wheelMesh = new Entity("Mesh");
            wheelMesh.addComponent(new Cylinder(0.5,2,2));
            wheelMesh.getComponent(PoseComponent.class).setRotation(new Vector3d(0,90,0));
            entityManager.addEntityToParent(wheelMesh, wheelEntity);

            WheelComponent wc = new WheelComponent();
            wheelEntity.addComponent(wc);
            wc.diameter.set(4.0);
            wc.width.set(0.5);

            // add motors to all wheels
            MotorComponent motor = MotorFactory.createDefaultMotor();
            wheelEntity.addComponent(motor);
            wc.drive.set(motor.getEntity());
            motor.addConnection(wheelMesh);

            // rotate wheels so they point outwards
            wheelEntity.getComponent(PoseComponent.class).setRotation(new Vector3d(0, 0, 120*i));
            // place wheels at the corners of the car
            wheelEntity.getComponent(PoseComponent.class).setPosition(new Vector3d(
                    10*Math.cos(Math.toRadians(120*i)),
                    10*Math.sin(Math.toRadians(120*i)),
                    1));
        }

        return carEntity;
    }

    /**
     * Build a tank with 2 wheels.  Steering is differential drive.
     */
    public static Entity buildTank(EntityManager entityManager) {
        Entity carEntity = new Entity("Tank");
        CarComponent car = new CarComponent();
        carEntity.addComponent(car);
        car.wheelType.set(CarComponent.WHEEL_DIFFERENTIAL);

        Entity mesh = new Entity("Mesh");
        mesh.addComponent(new Box(20,2,18));
        mesh.getComponent(PoseComponent.class).setPosition(new Vector3d(0,0,1.5));
        entityManager.addEntityToParent(mesh, carEntity);

        Entity [] wheelEntity = new Entity[2];
        for (int i = 0; i < wheelEntity.length; ++i) {
            wheelEntity[i] = new Entity("Wheel" + i);
            entityManager.addEntityToParent(wheelEntity[i], carEntity);
            car.addWheel(wheelEntity[i]);

            Entity wheelMesh = new Entity("Mesh");
            wheelMesh.addComponent(new Cylinder(0.5,2,2));
            wheelMesh.getComponent(PoseComponent.class).setRotation(new Vector3d(0,90,90));
            entityManager.addEntityToParent(wheelMesh, wheelEntity[i]);

            WheelComponent wc = new WheelComponent();
            wheelEntity[i].addComponent(wc);
            wc.diameter.set(4.0);
            wc.width.set(0.5);

            MotorComponent motor = MotorFactory.createDefaultMotor();
            wheelEntity[i].addComponent(motor);
            wc.drive.set(motor.getEntity());
            motor.addConnection(wheelMesh);
        }

        // place wheels at either side of the car
        wheelEntity[0].getComponent(PoseComponent.class).setPosition(new Vector3d(0, -10, 1));
        wheelEntity[1].getComponent(PoseComponent.class).setPosition(new Vector3d(0,  10, 1));

        return carEntity;
    }

    public static Entity buildMecanum(EntityManager entityManager) {
        Entity carEntity = build4WheelCarWithNoMotor(entityManager);
        carEntity.setName("Mecanum");
        CarComponent car = carEntity.getComponent(CarComponent.class);

        // add motors to all wheels
        for (int i = 0; i < 4; ++i) {
            Entity wheelEntity = entityManager.findEntityByUniqueID(car.getWheel(i));
            MotorComponent mc = MotorFactory.createDefaultMotor();
            wheelEntity.addComponent(mc);
            WheelComponent wc = wheelEntity.getComponent(WheelComponent.class);
            wc.drive.set(wheelEntity);
            mc.addConnection(wheelEntity.getChildren().get(0));
        }

        // change wheel type to mecanum
        car.wheelType.set(CarComponent.WHEEL_MECANUM);
        return carEntity;
    }

    /**
     * make car with real-wheel drive
     */
    public static Entity buildRWD(EntityManager entityManager) {
        Entity carEntity = build4WheelCarWithNoMotor(entityManager);
        carEntity.setName("RWD");
        CarComponent car = carEntity.getComponent(CarComponent.class);

        // add one motor
        Entity motor = new Entity("Motor");
        MotorComponent mc = MotorFactory.createDefaultMotor();
        motor.addComponent(mc);
        entityManager.addEntityToParent(motor,carEntity);

        // add front wheel steering
        for (int i = 0; i < 2; ++i) {
            Entity wheelEntity = entityManager.findEntityByUniqueID(car.getWheel(i));
            Entity suspension = wheelEntity.getParent();
            MotorComponent steerMotor = MotorFactory.createDefaultServo();
            suspension.addComponent(steerMotor);
            steerMotor.addConnection(wheelEntity);
            WheelComponent wc = wheelEntity.getComponent(WheelComponent.class);
            wc.steer.set(suspension);
        }

        // connect the motor to the back wheels
        for (int i = 2; i < 4; ++i) {
            Entity wheelEntity = entityManager.findEntityByUniqueID(car.getWheel(i));
            WheelComponent wc = wheelEntity.getComponent(WheelComponent.class);
            wc.drive.set(motor);
            mc.addConnection(wheelEntity.getChildren().get(0));
        }

        return carEntity;
    }

    public static Entity buildFWD(EntityManager entityManager) {
        Entity carEntity = build4WheelCarWithNoMotor(entityManager);
        carEntity.setName("FWD");
        CarComponent car = carEntity.getComponent(CarComponent.class);

        // add one motor
        Entity motor = new Entity("Motor");
        MotorComponent mc = MotorFactory.createDefaultMotor();
        motor.addComponent(mc);
        entityManager.addEntityToParent(motor,carEntity);

        // add front wheel steering
        for (int i = 0; i < 2; ++i) {
            // add servo in the suspension
            Entity wheelEntity = entityManager.findEntityByUniqueID(car.getWheel(i));
            Entity suspension = wheelEntity.getParent();
            MotorComponent steerMotor = MotorFactory.createDefaultServo();
            suspension.addComponent(steerMotor);
            steerMotor.addConnection(wheelEntity);
            // tell wheel for reference later
            WheelComponent wc = wheelEntity.getComponent(WheelComponent.class);
            wc.steer.set(suspension);
        }

        // connect the motor to the front wheels
        for (int i = 0; i < 2; ++i) {
            Entity wheelEntity = entityManager.findEntityByUniqueID(car.getWheel(i));
            WheelComponent wc = wheelEntity.getComponent(WheelComponent.class);
            wc.drive.set(motor);
            mc.addConnection(wheelEntity.getChildren().get(0));
        }

        return carEntity;
    }

    private static Entity buildMotorcycle(EntityManager entityManager) {
        Entity carEntity = new Entity("Motorcycle");
        CarComponent car = new CarComponent();
        carEntity.addComponent(car);
        entityManager.addEntityToParent(carEntity, entityManager.getRoot());

        Entity mesh = new Entity("Mesh");
        mesh.addComponent(new Box(5,2,3));
        mesh.getComponent(PoseComponent.class).setPosition(new Vector3d(0,0,1.5));
        entityManager.addEntityToParent(mesh, carEntity);

        // add wheels
        Entity[] wheelEntity = new Entity[2];
        for (int i = 0; i < wheelEntity.length; ++i) {
            // add suspension to the body
            Entity suspension = new Entity("Suspension" + i);
            entityManager.addEntityToParent(suspension, carEntity);
            // add wheel to suspension
            wheelEntity[i] = new Entity("Wheel" + i);
            entityManager.addEntityToParent(wheelEntity[i], suspension);
            Entity wheelMesh = new Entity("Mesh");
            wheelMesh.addComponent(new Cylinder(0.5,2,2));
            wheelMesh.getComponent(PoseComponent.class).setRotation(new Vector3d(0,90,90));
            entityManager.addEntityToParent(wheelMesh, wheelEntity[i]);

            WheelComponent wc = new WheelComponent();
            wheelEntity[i].addComponent(wc);
            wc.diameter.set(4.0);
            wc.width.set(0.5);
            car.addWheel(wheelEntity[i]);
        }

        // place wheels
        wheelEntity[0].getComponent(PoseComponent.class).setPosition(new Vector3d( 3, 0, 1));
        wheelEntity[1].getComponent(PoseComponent.class).setPosition(new Vector3d(-3, 0, 1));

        // add one motor
        Entity motor = new Entity("Motor");
        MotorComponent mc = MotorFactory.createDefaultMotor();
        motor.addComponent(mc);
        entityManager.addEntityToParent(motor,carEntity);

        // add front wheel steering
        // add servo in the suspension
        Entity frontWheel = entityManager.findEntityByUniqueID(car.getWheel(0));
        Entity suspension = frontWheel.getParent();
        MotorComponent steerMotor = MotorFactory.createDefaultServo();
        suspension.addComponent(steerMotor);
        // tell wheel for reference later
        WheelComponent wc = frontWheel.getComponent(WheelComponent.class);
        wc.steer.set(suspension);
        steerMotor.addConnection(frontWheel);

        // connect the motor to the back wheel
        Entity backWheel = entityManager.findEntityByUniqueID(car.getWheel(1));
        wc = backWheel.getComponent(WheelComponent.class);
        wc.drive.set(motor);
        mc.addConnection(backWheel.getChildren().get(0));

        return carEntity;
    }
}
