package org.siggd.actor.props.test;
import org.siggd.Level;
import org.siggd.actor.Actor;
import org.siggd.actor.props.Prop;


public class FakeActor extends Actor implements IFake {
	public FakeActor(Level level, long id) {
		super(level, id);
	}

	@Override
	public void loadResources() {
	}

	@Override
	public void loadBodies() {
	}

	@Override
	public void postLoad() {
	}
	
	@Prop(name = "IntTest")
	public int getIntTest() {
		return 0;
	}
	
	@Prop(name = "IntTest")
	public void setIntTest(int test) {
	}
	
	@Prop(name = "StringTest")
	public String getStringTest() {
		return null;
	}

	@Prop(name = "StringTest")
	public void setStringTest(String test) {
	}
	
	@Prop(name = "HalfTest")
	public int getHalfTest() {
		return 0;
	}
	
	@Prop(name = "ActorTest")
	public Actor getActor() {
		return null;
	}

	@Prop(name = "ActorTest")
	public void setActor(Actor a) {
	}

	public int getITest()
	{
		return 0;
	}
}
