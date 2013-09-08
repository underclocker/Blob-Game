package org.siggd.actor.props.test;

import org.siggd.Level;

public class FakeDerivedActor extends FakeActor {
	public FakeDerivedActor(Level level, long id) {
		super(level, id);
	}
	
	@Override
	public int getIntTest() {
		return 0;
	}
}
