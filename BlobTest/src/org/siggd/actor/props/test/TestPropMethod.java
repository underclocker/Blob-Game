package org.siggd.actor.props.test;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.longThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.siggd.Level;
import org.siggd.actor.meta.PropScanner;

public class TestPropMethod {
	private FakeActor mActor;
	private PropScanner mPropScanner;
	private PropScanner.Props mProps;

	@Before
	public void setup() {
		mPropScanner = new PropScanner("org.siggd.actor.props.test");
		mActor = mock(FakeActor.class);
		mProps = mPropScanner.getProps(FakeActor.class);
	}

	@Test
	public void scanClass() {
		assertTrue(mProps.hasGetter("IntTest"));
		assertTrue(mProps.hasSetter("IntTest"));
		assertTrue(mProps.hasGetter("StringTest"));
		assertTrue(mProps.hasSetter("StringTest"));
		assertTrue(mProps.hasGetter("HalfTest"));
		assertTrue(mProps.hasGetter("InterfaceTest"));
	}
	
	@Test
	public void scanDerived() {
		PropScanner.Props props = mPropScanner.getProps(FakeDerivedActor.class);

		assertTrue(props.hasGetter("IntTest"));
		assertTrue(props.hasSetter("IntTest"));
		assertTrue(props.hasGetter("StringTest"));
		assertTrue(props.hasSetter("StringTest"));
		assertTrue(props.hasGetter("HalfTest"));
		assertTrue(props.hasGetter("InterfaceTest"));
	}
	
	
	@Test
	public void propertySameType() {
		mProps.set(mActor, "IntTest", 7);
		verify(mActor).setIntTest(7);

		mProps.get(mActor, "IntTest");
		verify(mActor).getIntTest();

		mProps.set(mActor, "StringTest", "Foo");
		verify(mActor).setStringTest("Foo");

		mProps.get(mActor, "StringTest");
		verify(mActor).getStringTest();

		mProps.get(mActor, "HalfTest");
		verify(mActor).getHalfTest();
	}
	
	@Test
	public void propertyDiffType() {
		mProps.set(mActor, "IntTest", "7");
		mProps.set(mActor, "IntTest", 7.0);
		verify(mActor, times(2)).setIntTest(7);

		mProps.set(mActor, "StringTest", 7);
		verify(mActor).setStringTest("7");
	}
	
	@Test
	public void propertyActor() {
		Level levelMock = mock(Level.class);
		when(mActor.getLevel()).thenReturn(levelMock);
		when(levelMock.getActorById(longThat(lessThan(0L)))).thenReturn(null);
		when(levelMock.getActorById(0)).thenReturn(mActor);

		mProps.set(mActor, "ActorTest", "-1");
		mProps.set(mActor, "ActorTest", "-5");
		mProps.set(mActor, "ActorTest", -1);
		verify(mActor, times(3)).setActor(null);
		
		mProps.set(mActor, "ActorTest", 0);
		verify(mActor).setActor(mActor);
		
		when(mActor.getActor()).thenReturn(null);
		assertEquals(-1L, mProps.get(mActor, "ActorTest"));
		when(mActor.getActor()).thenReturn(mActor);
		assertEquals(0L, mProps.get(mActor, "ActorTest"));
	}
}
