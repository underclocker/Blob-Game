package org.siggd.actor;

import org.siggd.Convert;
import org.siggd.Level;

import com.badlogic.gdx.math.Vector2;

public class RotoJiggle extends JiggleBall {

	private Vector2 mRotatePosition;
	private float mRotateSpeed;
	public RotoJiggle(Level level, long id) {
		super(level, id);
		mRotatePosition = new Vector2();
		setProp("centerX", 0);
		setProp("centerY", 0);
		setProp("Rotation Speed (deg)", 1.0f);
	}
	
	public void update()
	{
		
		Vector2 f = mStartPosition.sub(mRotatePosition);
		float L = f.len();
		float a = f.angle();
		
		
		mStartPosition.x = (float)(mRotatePosition.x+L*Math.cos(a*Math.PI/180.0f + mRotateSpeed*Math.PI/180.0f));
		mStartPosition.y = (float)(mRotatePosition.y+L*Math.sin(a*Math.PI/180.0f + mRotateSpeed*Math.PI/180.0f));
		super.update();
	}
	
	@Override
	public void setProp(String name, Object val) {
		if (name.equals("centerX")) {
			mRotatePosition.x = Convert.getFloat(val);
		}
		if (name.equals("centerY")) {
			mRotatePosition.y = Convert.getFloat(val);
		}
		if (name.equals("Rotation Speed (deg)")) {
			mRotateSpeed = Convert.getFloat(val);
		}
		
		super.setProp(name, val);
	}

}
