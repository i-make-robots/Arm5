package com.marginallyclever.robotoverlord.systems.robot.robotarm;

import com.marginallyclever.robotoverlord.components.Component;
import com.marginallyclever.robotoverlord.components.PoseComponent;
import com.marginallyclever.robotoverlord.components.RobotComponent;
import com.marginallyclever.robotoverlord.components.GripperComponentLinear;
import com.marginallyclever.robotoverlord.components.program.ProgramComponent;
import com.marginallyclever.robotoverlord.components.program.ProgramEventComponent;
import com.marginallyclever.robotoverlord.components.program.ProgramPathComponent;
import com.marginallyclever.robotoverlord.components.program.ProgramStepComponent;
import com.marginallyclever.robotoverlord.entity.Entity;
import com.marginallyclever.robotoverlord.entity.EntityManager;
import com.marginallyclever.robotoverlord.parameters.swing.ViewElementButton;
import com.marginallyclever.robotoverlord.parameters.swing.ComponentSwingViewFactory;
import com.marginallyclever.robotoverlord.robots.Robot;
import com.marginallyclever.robotoverlord.systems.EntitySystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Matrix4d;
import java.util.LinkedList;
import java.util.List;

public class ProgramExecutorSystem  implements EntitySystem {
    private static final Logger logger = LoggerFactory.getLogger(ProgramExecutorSystem.class);
    private final EntityManager entityManager;

    public ProgramExecutorSystem(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void decorate(ComponentSwingViewFactory view, Component component) {
        if( component instanceof ProgramComponent) decorateProgram(view,component);
    }

    private void decorateProgram(ComponentSwingViewFactory view, Component component) {
        final ProgramComponent program = (ProgramComponent)component;
        view.add(program.programEntity).addPropertyChangeListener((evt) -> {
            program.setRunning(false);
            program.stepEntity.set((String)null);
        });
        view.add(program.stepEntity).setReadOnly(true);

        ViewElementButton bRun = view.addButton(program.getRunning()?"Pause":"Play");
        program.addRunningPropertyChangeListener( (evt) -> bRun.setText(program.getRunning()?"Pause":"Play") );
        bRun.addActionEventListener( (evt) -> program.setRunning(!program.getRunning()) );
        view.addComboBox(program.mode, ProgramComponent.MODE_NAMES);
    }

    /**
     * Update the system over time.
     *
     * @param dt the time step in seconds.
     */
    @Override
    public void update(double dt) {
        List<Entity> list = new LinkedList<>(entityManager.getEntities());
        while(!list.isEmpty()) {
            Entity e = list.remove(0);
            list.addAll(e.getChildren());
            ProgramComponent found = e.getComponent(ProgramComponent.class);
            if( found!=null ) updateProgram(found,dt);
        }
    }

    private void updateProgram(ProgramComponent program, double dt) {
        if( !program.getRunning() ) return;

        // ProgramComponent must be adjacent to a RobotComponent.
        RobotComponent robot = program.getEntity().getComponent(RobotComponent.class);
        if( robot == null ) return;

        // must have program to run.
        Entity programRoot = entityManager.findEntityByUniqueID(program.programEntity.get());
        if(programRoot==null) return;

        Entity programStep = getCurrentProgramStep(program);
        if(programStep == null) {
            programStep = getFirstProgramStep(programRoot);
            if(programStep != null) {
                pushStack(robot, program, programStep);
            }
        }
        if(programStep == null) {
            // program is empty.
            program.setRunning(false);
            return;
        }
        // TODO check if programStep is a child of programRoot?
        int mode = program.mode.get();

        boolean done = executeStep(robot,program,programStep,dt);
        if(done) {
            Entity nextStep = getNextStep(robot,program,programStep,programRoot);
            if(nextStep==null) {
                // no more steps to run.
                if (mode == ProgramComponent.RUN_LOOP) {
                    // go back to start
                    nextStep = programRoot.getChildren().get(0);
                } else if(mode == ProgramComponent.RUN_TO_END) {
                    program.setRunning(false);
                }
            }

            program.stepEntity.set(nextStep==null ? null : nextStep.getUniqueID());
        }

        if( mode == ProgramComponent.RUN_STEP ) {
            // step mode always stops after one step.
            program.setRunning(false);
        }
    }

    /**
     * Get the current step in the program.
     * @param program the program to run.
     * @return the current step in the program or null.
     */
    private Entity getCurrentProgramStep(ProgramComponent program) {
        // find step to run.  if no step, assume program start.
        return entityManager.findEntityByUniqueID(program.stepEntity.get());
    }

    private Entity getFirstProgramStep(Entity programRoot) {
        List<Entity> children = programRoot.getChildren();
        if(children.isEmpty()) return null;
        return programRoot.getChildren().get(0);
    }

    /**
     * a program is a tree of entities.  walk the tree one step.
     * @param programStep the current step in the program tree.
     * @param programRoot the root of the program tree
     * @return the next step in the program or null.
     */
    public Entity getNextStep(RobotComponent robot,ProgramComponent program,Entity programStep, Entity programRoot) {
        Entity nextStep=null;
        // we've just executed programStep, now what?
        if(!programStep.getChildren().isEmpty()) {
            // go to first child
            nextStep = programStep.getChildren().get(0);
        }
        if(nextStep==null) {
            // no children, go to next sibling
            nextStep = programStep.getNextSibling();
        }
        if (nextStep != null) {
            pushStack(robot,program,nextStep);
            return nextStep;
        }

        // no next sibling, pop the stack
        while(true) {
            popStack(program,programStep);
            Entity parent = programStep.getParent();

            if( parent==null ) return null;
            if( parent == programRoot ) return null;

            // go to parent's next sibling
            nextStep = parent.getNextSibling();
            if (nextStep != null) {
                return nextStep;
            }

            // parent has no next sibling, keep poppin'
            programStep = parent;
        }
    }

    private void pushStack(RobotComponent robot, ProgramComponent program, Entity programStep) {
        ProgramStepComponent step = programStep.getComponent(ProgramStepComponent.class);
        if(step!=null) program.pushStack(step);
    }

    private void popStack(ProgramComponent program, Entity programStep) {
        ProgramStepComponent step = programStep.getComponent(ProgramStepComponent.class);
        if(step!=null) program.popStack();
    }

    /**
     * Execute the current step in the program.
     * @param robot the robot to move.
     * @param program the program that owns the step.
     * @param programStep the step to execute.
     * @param dt the time step in seconds.
     * @return true if the step is finished.
     */
    private boolean executeStep(RobotComponent robot, ProgramComponent program, Entity programStep, double dt) {
        ProgramStepComponent step = programStep.getComponent(ProgramStepComponent.class);
        Object stackTop = program.peekStack();

        if(stackTop instanceof ProgramEventComponent) {
            ProgramEventComponent event = (ProgramEventComponent) stackTop;
            return executeEvent(robot, program, event, programStep, dt);
        }
        if(stackTop instanceof ProgramPathComponent) {
            ProgramPathComponent path = (ProgramPathComponent) stackTop;
            return executePath(robot, program, path, programStep, dt);
        }
        // TODO handle unrecognized component type.
        return true;
    }

    private boolean executeEvent(RobotComponent robot, ProgramComponent program, ProgramEventComponent event, Entity programStep, double dt) {
        GripperComponentLinear gripper = robot.getEntity().findFirstComponentRecursive(GripperComponentLinear.class);
        if(gripper==null) return true;  // no gripper, no action.

        switch (event.type.get()) {
            case ProgramEventComponent.GRIPPER_GRAB -> gripper.mode.set(GripperComponentLinear.MODE_CLOSING);
            case ProgramEventComponent.GRIPPER_RELEASE -> gripper.mode.set(GripperComponentLinear.MODE_OPENING);
            default -> {
                // TODO implement other events
                logger.warn("unimplemented event type: {}", event.type.get());
            }
        }

        // assume instant finish
        return true;
    }

    /**
     * A {@link ProgramPathComponent} is made of a series of {@link PoseComponent}s.
     * @param robot the robot to move.
     * @param program the program that owns the path.
     * @param path the path to move.
     * @param dt the time step in seconds.
     * @return true if the path is finished.
     */
    private boolean executePath(RobotComponent robot, ProgramComponent program, ProgramPathComponent path, Entity programStep, double dt) {
        PoseComponent pathPose = programStep.getComponent(PoseComponent.class);

        Matrix4d robotPose = robot.getEntity().getComponent(PoseComponent.class).getWorld();
        robotPose.invert();
        Matrix4d pathPoseWorld = pathPose.getWorld();
        Matrix4d adj = new Matrix4d();
        adj.mul(pathPoseWorld,robotPose);

        if (path.moveType.get() == ProgramPathComponent.MOVE_LINEAR) {
            // linear
            robot.set(Robot.END_EFFECTOR_TARGET, adj);
        } else {
            // rapid
            robot.set(Robot.END_EFFECTOR_TARGET, adj);
        }

        // assume instant finish
        return true;
    }
}
