package com.marginallyclever.robotOverlord.rotaryStewartPlatform;


import com.jogamp.opengl.GL2;

import javax.vecmath.Vector3f;

import com.marginallyclever.robotOverlord.*;
import com.marginallyclever.robotOverlord.model.Model;
import com.marginallyclever.robotOverlord.model.ModelFactory;

public class RotaryStewartPlatform3 extends RotaryStewartPlatform {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8771341196997485169L;
	
	protected transient Material matForearm = new Material();
	protected transient Model modelForearm;
	protected RotaryStewartPlatform3Dimensions dimensions;

 	public RotaryStewartPlatform3() {
		super();
		dimensions = new RotaryStewartPlatform3Dimensions();
		setDisplayName(dimensions.ROBOT_NAME);

		motionNow = new RotaryStewartPlatformKeyframe(dimensions);
		motionFuture = new RotaryStewartPlatformKeyframe(dimensions);
		
		setupBoundingVolumes();
		setHome(new Vector3f(0,0,0));
		
		// set up the initial state of the machine
		isPortConfirmed=false;
		hasArmMoved = false;
		xDir = 0.0f;
		yDir = 0.0f;
		zDir = 0.0f;
		uDir = 0.0f;
		vDir = 0.0f;
		wDir = 0.0f;

		matBase.setDiffuseColor(69.0f/255.0f,115.0f/255.0f,133.0f/255.0f,1);
		matBicep.setDiffuseColor(2.0f/255.0f,39.0f/255.0f,53.0f/255.0f,1);
		matForearm.setDiffuseColor(39.0f/255.0f,88.0f/255.0f,107.0f/255.0f,1);
		matTop.setDiffuseColor(16.0f/255.0f,62.0f/255.0f,80.0f/255.0f,1);
	}

	@Override
	protected void loadModels(GL2 gl2) {
		try {
			modelTop = ModelFactory.createModelFromFilename("/StewartPlatform3.zip:top.stl",0.1f);
			modelBicep = ModelFactory.createModelFromFilename("/StewartPlatform3.zip:bicep.stl",0.1f);
			modelBase = ModelFactory.createModelFromFilename("/StewartPlatform3.zip:base.stl",0.1f);
			modelForearm = ModelFactory.createModelFromFilename("/StewartPlatform3.zip:forearm.stl",0.1f);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void render(GL2 gl2) {
		super.render(gl2);
		
		int i;

		boolean draw_finger_star=true;
		boolean draw_base_star=false;
		boolean draw_shoulder_to_elbow=false;
		boolean draw_elbow_to_wrist=false;
		boolean draw_shoulder_star=false;
		boolean draw_elbow_star=false;
		boolean draw_wrist_star=false;
		boolean draw_stl=true;
		
		gl2.glPushMatrix();
		Vector3f p = getPosition();
		gl2.glTranslated(p.x, p.y, p.z);

		if(draw_stl) {
			// base
			matBase.render(gl2);
			gl2.glPushMatrix();
			gl2.glTranslatef(0, 0, dimensions.BASE_ADJUST_Z);
			gl2.glRotatef(60, 0, 0, 1);
			modelBase.render(gl2);
			gl2.glPopMatrix();

			// arms
			matBicep.render(gl2);
			for(i=0;i<3;++i) {
				gl2.glPushMatrix();
				gl2.glTranslatef(motionNow.arms[i*2+0].shoulder.x,
						         motionNow.arms[i*2+0].shoulder.y,
						         motionNow.arms[i*2+0].shoulder.z);
				gl2.glRotatef(120.0f*i, 0, 0, 1);
				gl2.glTranslatef(0, 0, dimensions.BASE_ADJUST_Z);
				gl2.glRotatef(90, 0, 0, 1);
				gl2.glRotatef(90, 1, 0, 0);
				gl2.glRotatef(180-motionNow.arms[i*2+0].angle,0,0,1);
				modelBicep.render(gl2);
				gl2.glPopMatrix();
	
				gl2.glPushMatrix();
				gl2.glTranslatef(motionNow.arms[i*2+1].shoulder.x,
						         motionNow.arms[i*2+1].shoulder.y,
						         motionNow.arms[i*2+1].shoulder.z);
				gl2.glRotatef(120.0f*i, 0, 0, 1);
				gl2.glTranslatef(0, 0, dimensions.BASE_ADJUST_Z);
				gl2.glRotatef(90, 0, 0, 1);
				gl2.glRotatef(90, 1, 0, 0);
				gl2.glRotatef(+motionNow.arms[i*2+1].angle,0,0,1);
				modelBicep.render(gl2);
				gl2.glPopMatrix();
			}
			//top
			matTop.render(gl2);
			gl2.glPushMatrix();
			gl2.glTranslatef(motionNow.fingerPosition.x,motionNow.fingerPosition.y,motionNow.fingerPosition.z+motionNow.relative.z+dimensions.BASE_ADJUST_Z);
			gl2.glRotatef(motionNow.rotationAngleU, 1, 0, 0);
			gl2.glRotatef(motionNow.rotationAngleV, 0, 1, 0);
			gl2.glRotatef(motionNow.rotationAngleW, 0, 0, 1);
			gl2.glRotatef(-30, 0, 0, 1);
			modelTop.render(gl2);
			gl2.glPopMatrix();
		}
		
		// draw the forearms
		matForearm.render(gl2);
		for(i=0;i<6;++i) {
			Vector3f a=new Vector3f(
					motionNow.arms[i].wrist.x-motionNow.arms[i].elbow.x,
					motionNow.arms[i].wrist.y-motionNow.arms[i].elbow.y,
					motionNow.arms[i].wrist.z-motionNow.arms[i].elbow.z
					);
			Vector3f b=new Vector3f(
					motionNow.arms[i].elbow.x,
					motionNow.arms[i].elbow.y,
					motionNow.arms[i].elbow.z
					);
			Vector3f c=new Vector3f();
			a.normalize();
			b.normalize();
			c.cross(a, b);
			//a.cross(b, c);
			//b.cross(a, c);
			c.normalize();
			b.cross(a, c);
			float [] m = new float[16];
			m[ 0]=c.x;		m[ 1]=c.y;		m[ 2]=c.z;		m[ 3]=0;
			m[ 4]=b.x;		m[ 5]=b.y;		m[ 6]=b.z;		m[ 7]=0;
			m[ 8]=a.x;		m[ 9]=a.y;		m[10]=a.z;		m[11]=0;
			m[12]=motionNow.arms[i].elbow.x;
			m[13]=motionNow.arms[i].elbow.y;
			m[14]=motionNow.arms[i].elbow.z+dimensions.BASE_ADJUST_Z;	
			m[15]=1;
			
			gl2.glPushMatrix();
			gl2.glMultMatrixf(m, 0);
			if(i%2==0) {
				gl2.glRotatef(40,0,0,1);
				//gl2.glRotatef((i/2)*120,0,0,1);
				//gl2.glTranslatef(-1,-0.5f,0);
			} else {
				gl2.glRotatef(-40,0,0,1);
				//gl2.glRotatef((i/2)*120,0,0,1);
				//gl2.glTranslatef(0,1,0);
			}
			modelForearm.render(gl2);
			gl2.glPopMatrix();
		}
		
		gl2.glDisable(GL2.GL_LIGHTING);
		// debug info
		gl2.glPushMatrix();
		gl2.glTranslatef(0, 0, dimensions.BASE_ADJUST_Z);
		for(i=0;i<6;++i) {
			gl2.glColor3f(1,1,1);
			if(draw_shoulder_star) PrimitiveSolids.drawStar(gl2, motionNow.arms[i].shoulder,15);
			if(draw_elbow_star) PrimitiveSolids.drawStar(gl2, motionNow.arms[i].elbow,13);			
			if(draw_wrist_star) PrimitiveSolids.drawStar(gl2, motionNow.arms[i].wrist,16);

			if(draw_shoulder_to_elbow) {
				gl2.glBegin(GL2.GL_LINES);
				gl2.glColor3f(0,1,0);
				gl2.glVertex3f(motionNow.arms[i].elbow.x,motionNow.arms[i].elbow.y,motionNow.arms[i].elbow.z);
				gl2.glColor3f(0,0,1);
				gl2.glVertex3f(motionNow.arms[i].shoulder.x,motionNow.arms[i].shoulder.y,motionNow.arms[i].shoulder.z);
				gl2.glEnd();
			}
			if(draw_elbow_to_wrist) {
				gl2.glBegin(GL2.GL_LINES);
				gl2.glColor3f(0,1,0);
				gl2.glVertex3f(motionNow.arms[i].elbow.x,motionNow.arms[i].elbow.y,motionNow.arms[i].elbow.z);
				gl2.glColor3f(0,0,1);
				gl2.glVertex3f(motionNow.arms[i].wrist.x,motionNow.arms[i].wrist.y,motionNow.arms[i].wrist.z);
				gl2.glEnd();
			}
		}
		gl2.glPopMatrix();
		
		if(draw_finger_star) {
	 		// draw finger orientation
			float s=20;
			gl2.glPushMatrix();
			gl2.glTranslatef(motionNow.relative.x+motionNow.fingerPosition.x,
					motionNow.relative.y+motionNow.fingerPosition.y,
					motionNow.relative.z+motionNow.fingerPosition.z+dimensions.BASE_ADJUST_Z);
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3f(1,1,1);
			gl2.glVertex3f(0,0,0);
			gl2.glVertex3f(motionNow.finger_forward.x*s,
					       motionNow.finger_forward.y*s,
					       motionNow.finger_forward.z*s);
			gl2.glVertex3f(0,0,0);
			gl2.glVertex3f(motionNow.finger_up.x*s,
					       motionNow.finger_up.y*s,
					       motionNow.finger_up.z*s);
			gl2.glVertex3f(0,0,0);
			gl2.glVertex3f(motionNow.finger_left.x*s,
					       motionNow.finger_left.y*s,
					       motionNow.finger_left.z*s);
			
			gl2.glEnd();
			gl2.glPopMatrix();
		}

		if(draw_base_star) {
	 		// draw finger orientation
			float s=2;
			gl2.glDisable(GL2.GL_DEPTH_TEST);
			gl2.glBegin(GL2.GL_LINES);
			gl2.glColor3f(1,0,0);
			gl2.glVertex3f(motionNow.base.x, motionNow.base.y, motionNow.base.z);
			gl2.glVertex3f(motionNow.base.x+motionNow.baseForward.x*s,
					       motionNow.base.y+motionNow.baseForward.y*s,
					       motionNow.base.z+motionNow.baseForward.z*s);
			gl2.glColor3f(0,1,0);
			gl2.glVertex3f(motionNow.base.x, motionNow.base.y, motionNow.base.z);
			gl2.glVertex3f(motionNow.base.x+motionNow.baseUp.x*s,
				       motionNow.base.y+motionNow.baseUp.y*s,
				       motionNow.base.z+motionNow.baseUp.z*s);
			gl2.glColor3f(0,0,1);
			gl2.glVertex3f(motionNow.base.x, motionNow.base.y, motionNow.base.z);
			gl2.glVertex3f(motionNow.base.x+motionNow.finger_left.x*s,
				       motionNow.base.y+motionNow.finger_left.y*s,
				       motionNow.base.z+motionNow.finger_left.z*s);
			
			gl2.glEnd();
			gl2.glEnable(GL2.GL_DEPTH_TEST);
		}
		
		gl2.glEnable(GL2.GL_LIGHTING);
		
		gl2.glPopMatrix();
	}
}
