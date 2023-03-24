package com.marginallyclever.robotoverlord.components.demo;

import com.jogamp.opengl.GL2;
import com.marginallyclever.convenience.MathHelper;
import com.marginallyclever.convenience.MatrixHelper;
import com.marginallyclever.convenience.PrimitiveSolids;
import com.marginallyclever.robotoverlord.Entity;
import com.marginallyclever.robotoverlord.components.*;
import com.marginallyclever.robotoverlord.components.shapes.MeshFromFile;
import com.marginallyclever.robotoverlord.parameters.DoubleEntity;
import com.marginallyclever.robotoverlord.parameters.IntEntity;
import com.marginallyclever.robotoverlord.robots.Robot;
import com.marginallyclever.robotoverlord.swinginterface.view.ViewPanel;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

public class CrabRobotComponent extends RenderComponent {
    private static final int NUM_LEGS = 6;
    static final String HIP = "Hip";
    static final String THIGH = "Thigh";
    static final String CALF = "Calf";
    static final String FOOT = "Foot";
    private final String[] modeNames = {
            "Calibrate",
            "Sit down",
            "Stand up",
            "Only body ",
            "Ripple",
            "Wave",
            "Tripod",
    };
    private final IntEntity modeSelector = new IntEntity("Mode", 0);
    private final DoubleEntity standingRadius = new DoubleEntity("Standing radius", 21);
    private final DoubleEntity standingHeight = new DoubleEntity("Standing height", 5.5);
    private final DoubleEntity turningStrideLength = new DoubleEntity("Turning stride length", 150);
    private final DoubleEntity strideLength = new DoubleEntity("Stride length", 15);
    private final DoubleEntity strideHeight = new DoubleEntity("Stride height", 5);
    private final DoubleEntity speedScale = new DoubleEntity("Speed scale", 1);
    private final Entity [] legs = new Entity[NUM_LEGS];
    private final Vector3d [] lastPOC = new Vector3d[NUM_LEGS];
    private final Vector3d [] nextPOC = new Vector3d[NUM_LEGS];

    double gaitCycleTime = 0;

    public CrabRobotComponent() {
        super();
    }

    @Override
    public void render(GL2 gl2) {
        gl2.glColor3d(1,0,1);
        PrimitiveSolids.drawCircleXY(gl2,standingRadius.get(),32);

        gl2.glPushMatrix();
        //MatrixHelper.setMatrix(gl2,MatrixHelper.createIdentityMatrix4());
        for(int i=0;i<NUM_LEGS;++i) {
            drawVectorAsStar(gl2,lastPOC[i],0);
            drawVectorAsStar(gl2,nextPOC[i],1);
        }
        gl2.glPopMatrix();
    }

    private void drawVectorAsStar(GL2 gl2,Vector3d v,int color) {
        if(color==0) PrimitiveSolids.drawStar(gl2,v,5);
        else PrimitiveSolids.drawSphere(gl2,v,1);
    }

    @Override
    public void setEntity(Entity entity) {
        super.setEntity(entity);
        if(entity==null) return;

        getEntity().addComponent(new PoseComponent());

        createMesh(getEntity(),"/Spidee/body.stl");

        // 0   5
        // 1 x 4
        // 2   3
        legs[0] = createLimb("LF",0,false,  135);
        legs[1] = createLimb("LM",1,false,  180);
        legs[2] = createLimb("LB",2,false, -135);
        legs[3] = createLimb("RB",3,true,   -45);
        legs[4] = createLimb("RM",4,true,     0);
        legs[5] = createLimb("RF",5,true,    45);

        for(Entity leg : legs) {
            getEntity().addEntity(leg);
        }
    }

    @Override
    public void getView(ViewPanel view) {
        super.getView(view);
        view.pushStack("Config",false);
        view.add(standingRadius);
        view.add(standingHeight);
        view.add(turningStrideLength);
        view.add(strideLength);
        view.add(strideHeight);
        view.popStack();
        view.pushStack("Gait",true);
        view.addComboBox(modeSelector, modeNames);
        view.add(speedScale);
        view.popStack();
    }

    @Override
    public void update(double dt) {
        gaitCycleTime += dt;

        updateBasedOnMode(dt);
    }

    /**
     * Update the gait for one leg.
     * @param index the leg to update
     * @param step 0 to 1, 0 is start of step, 1 is end of step
     */
    private void updateGaitForOneLeg(int index, double step) {
        Entity leg = legs[index];
        RobotComponent robotLeg = leg.findFirstComponent(RobotComponent.class);
        if(robotLeg==null) return;

        // horizontal distance from foot to next point of contact.
        Vector3d start = lastPOC[index];
        Vector3d end = nextPOC[index];
        Vector3d mid = MathHelper.interpolate(start, end, step);

        // add in the height of the step.
        double stepAdj = (step <= 0.5f) ? step : 1 - step;
        stepAdj = Math.sin(stepAdj * Math.PI);
        mid.z = stepAdj * strideHeight.get();

        // tell the leg where to go.
        robotLeg.set(RobotComponent.END_EFFECTOR_TARGET_POSITION,mid);
    }

    private void setPointOfContact(Entity poc,Vector3d point) {
        PoseComponent pose = poc.findFirstComponent(PoseComponent.class);
        Matrix4d m = pose.getLocal();
        m.setTranslation(point);
        pose.setLocalMatrix4(m);
    }

    private void updateBasedOnMode(double dt) {
        switch (modeSelector.get()) {
            case 0 -> updateCalibrate(dt);
            case 1 -> updateSitDown(dt);
            case 2 -> updateStandUp(dt);
            case 3 -> updateOnlyBody(dt);
            case 4 -> updateRipple(dt);
            case 5 -> updateWave(dt);
            case 6 -> updateTripod(dt);
        }
    }

    private void updateCalibrate(double dt) {
        for(int i=0;i<NUM_LEGS;++i) {
            updateGaitForOneLeg(i, 0);
        }
    }

    private void updateSitDown(double dt) {
    }

    private void updateStandUp(double dt) {
        for (int i = 0; i < NUM_LEGS; ++i) {
            putFootDown(i);
        }
    }

    private void updateOnlyBody(double dt) {
    }

    private void updateRipple(double dt) {
        double step = (gaitCycleTime - Math.floor(gaitCycleTime));
        int legToMove = ((int) Math.floor(gaitCycleTime) % NUM_LEGS);

        //updateGaitTarget(dt, 1d/6d);

        for(int i = 0; i < NUM_LEGS; ++i) {
            if (i != legToMove) {
                putFootDown(i);
            } else {
                updateGaitForOneLeg(i, step);
            }
        }
    }

    private void updateWave(double dt) {
        gaitCycleTime += dt;

        //updateGaitTarget(dt, 2d/6d);

        double gc1 = gaitCycleTime + 0.5f;
        double gc2 = gaitCycleTime;

        double x1 = gc1 - Math.floor(gc1);
        double x2 = gc2 - Math.floor(gc2);
        double step1 = Math.max(0, x1);
        double step2 = Math.max(0, x2);
        int leg1 = (int) Math.floor(gc1) % 3;
        int leg2 = (int) Math.floor(gc2) % 3;

        // 0   5
        // 1 x 4
        // 2   3
        // order should be 0,3,1,5,2,4
        int o1, o2;
        o1 = switch (leg1) {
            case 0 -> 0;
            case 1 -> 1;
            case 2 -> 2;
            default -> 0;
        };
        o2 = switch (leg2) {
            case 0 -> 3;
            case 1 -> 5;
            case 2 -> 4;
            default -> 0;
        };

        updateGaitForOneLeg(o1, step1);
        updateGaitForOneLeg(o2, step2);

        // Put all feet down except the active leg(s).
        int i;
        for (i = 0; i < NUM_LEGS; ++i) {
            if (i != o1 && i != o2) {
                putFootDown(i);
            }
        }
    }

    private void updateTripod(double dt) {
        gaitCycleTime += dt;

        //updateGaitTarget(dt, 0.5f);

        double step = (gaitCycleTime - Math.floor(gaitCycleTime));
        int legToMove = ((int) Math.floor(gaitCycleTime) % 2);

        // put all feet down except the active leg(s).
        for (int i = 0; i < NUM_LEGS; ++i) {
            Entity leg = legs[i];
            if ((i % 2) != legToMove) {
                putFootDown(i);
            } else {
                updateGaitForOneLeg(i, step);
            }
        }
    }

    private void putFootDown(int index) {
        Vector3d toe = lastPOC[index];
        toe.z=0;
        setLegTargetPosition(index,toe);
    }

    private void setLegTargetPosition(int index,Vector3d point) {
        Entity leg = legs[index];
        RobotComponent robotLeg = leg.findFirstComponent(RobotComponent.class);
        if(robotLeg==null) return;
        PoseComponent bodyPose = leg.findFirstComponent(PoseComponent.class);
        if(bodyPose==null) return;
        Matrix4d bodyMatrix = bodyPose.getWorld();
        //bodyMatrix.invert();
        Point3d p1 = new Point3d(point);
        Point3d p2 = new Point3d();

        bodyMatrix.transform(p1,p2);
        Vector3d point2 = new Vector3d(p2);
        robotLeg.set(RobotComponent.END_EFFECTOR_TARGET_POSITION,point2);
    }


    private Entity createLimb(String name,int index,boolean isRight, float degrees) {
        DHComponent[] dh = new DHComponent[3];
        for(int i=0;i<dh.length;++i) {
            dh[i] = new DHComponent();
            dh[i].setVisible(true);
        }
        Entity limb = createPoseEntity(name);

        Entity hip = createPoseEntity(HIP);
        limb.addEntity(hip);
        Entity thigh = createPoseEntity(THIGH);
        hip.addEntity(thigh);
        Entity calf = createPoseEntity(CALF);
        thigh.addEntity(calf);
        Entity foot = createPoseEntity(FOOT);
        calf.addEntity(foot);

        hip.addComponent(dh[0]);
        dh[0].set(0,2.2,90,0,60,-60);
        if(isRight) createMesh(hip,"/Spidee/shoulder_right.obj");
        else        createMesh(hip,"/Spidee/shoulder_left.obj");

        thigh.addComponent(dh[1]);
        dh[1].set( 0,8.5,0,0,106,-72);
        createMesh(thigh,"/Spidee/thigh.obj");

        calf.addComponent(dh[2]);
        dh[2].set(0,10.5,0,0,15,-160);
        if(isRight) createMesh(calf,"/Spidee/calf_right.obj");
        else		createMesh(calf,"/Spidee/calf_left.obj");

        foot.addComponent(new ArmEndEffectorComponent());

        // position limb
        PoseComponent pose = limb.findFirstComponent(PoseComponent.class);
        double r = Math.toRadians(degrees);
        pose.setPosition(new Vector3d(Math.cos(r)*10,Math.sin(r)*10,2.6));
        pose.setRotation(new Vector3d(0,0,degrees));

        // Done at the end so RobotComponent can find all bones DHComponents.
        limb.addComponent(new RobotComponent());

        setInitialPointOfContact(limb,index);

        return limb;
    }

    private void setInitialPointOfContact(Entity limb,int index) {
        Entity foot = limb.findByPath(HIP+"/"+THIGH+"/"+CALF+"/"+FOOT);
        PoseComponent footPose = foot.findFirstComponent(PoseComponent.class);
        Vector3d toe = new Vector3d();
        footPose.getWorld().get(toe);

        PoseComponent bodyPose = getEntity().findFirstComponent(PoseComponent.class);
        Vector3d body = new Vector3d();
        bodyPose.getWorld().get(body);

        toe.sub(body);
        toe.normalize();
        toe.scaleAdd(standingRadius.get(),body);
        toe.z=0;
        nextPOC[index] = new Vector3d(toe);
        lastPOC[index] = new Vector3d(toe);
    }

    private Entity createPoseEntity(String name) {
        Entity result = new Entity(name);
        result.addComponent(new PoseComponent());
        return result;
    }

    private void createMesh(Entity parent,String filename) {
        Entity mesh = createPoseEntity("Mesh");
        parent.addEntity(mesh);

        mesh.addComponent(new MaterialComponent());

        MeshFromFile mff = new MeshFromFile();
        mff.setFilename(filename);
        mesh.addComponent(mff);

        OriginAdjustComponent oac = new OriginAdjustComponent();
        mesh.addComponent(oac);
        oac.adjust();
        mesh.removeComponent(oac);
    }
}
