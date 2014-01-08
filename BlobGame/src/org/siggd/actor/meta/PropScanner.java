package org.siggd.actor.meta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Logger;

import org.siggd.Convert;
import org.siggd.Game;
import org.siggd.actor.Actor;

public class PropScanner {
	private static final Logger LOGGER = Logger.getLogger(ActorEnum.class.getName());

	/**
	 * Holder for property getters and setters for an actor
	 * @author mysterymath
	 *
	 */
	public class Props
	{
		private HashMap<String, Method> mGetters;
		private HashMap<String, Method> mSetters;

		public Props() {
			mGetters = new HashMap<String, Method>();
			mSetters = new HashMap<String, Method>();
		}

		public boolean hasGetter(String name) {
			return mGetters.containsKey(name);
		}

		public boolean hasSetter(String name) {
			return mSetters.containsKey(name);
		}
		

		public Set<String> getGetterNames() {
			return mGetters.keySet();
		}
		
		public Set<String> getSetterNames() {
			return mSetters.keySet();
		}


		public Object get(Actor a, String name) {
			Method m = mGetters.get(name);
			Object val;

			try {
				val = m.invoke(a);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException("Could not invoke property getter.", e);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				throw new RuntimeException("Could not invoke property getter.", e);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new RuntimeException("Could not invoke property getter.", e);
			}

			// Actor id conversion
			if (m.getReturnType() == Actor.class) {
				if (val != null) {
					val = ((Actor)val).getId();
				} else {
					val = (long)-1;
				}
			}
			
			return val;
		}

		public void set(Actor a, String name, Object val) {
			Method m = mSetters.get(name);
			
			// Actor id conversion
			Class<?> paramType = m.getParameterTypes()[0];
			if (paramType == Actor.class) {
				val = a.getLevel().getActorById(Convert.getLong(val));
			} else {
				// General type conversion
				val = Convert.convertTo(val, paramType);
			}
			
			try {
				m.invoke(a, val);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
				throw new RuntimeException("Could not invoke property setter.", e);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				throw new RuntimeException("Could not invoke property setter.", e);
			} catch (InvocationTargetException e) {
				e.printStackTrace();
				throw new RuntimeException("Could not invoke property setter.", e);
			}
		}
	}
	
	private HashMap<Class<? extends Actor>, Props> mProps;

	public PropScanner(String namespace) {
		long time = System.nanoTime();

		mProps = new HashMap<Class<? extends Actor>, Props>();

		Set<Class<? extends Actor>> s = Game.get().getReflector().getActorSubTypes();

		for (Class c : s) {
			Props propMethods = new Props();
			ArrayList<Method> methods = new ArrayList<Method>();
			searchForMethods(methods, c);
			for (Method m : methods) {
				Prop propInfo = m.getAnnotation(Prop.class);
				if (propInfo == null) {
					continue;
				}
				
				Class<?>[] paramTypes = m.getParameterTypes();
				Class<?> returnType = m.getReturnType();
				if (paramTypes.length == 1 && returnType == void.class) {
					// Setter
					propMethods.mSetters.put(propInfo.name(), m);
				} else if (paramTypes.length == 0 && returnType != void.class) {
					// Getter
					propMethods.mGetters.put(propInfo.name(), m);
				} else {
					LOGGER.warning("Detected property that is neither setter nor getter: " + m);
				}
			}
			
			mProps.put(c, propMethods);
		}
		LOGGER.info("Actor property enumeration took " + (System.nanoTime() - time) / 1000.0  / 1000 + "ms.");
	}

	/**
	 * Recursively collect all the methods 
	 * @param ret
	 * @param c
	 */
	private void searchForMethods(ArrayList<Method> ret, Class c) {
		if (c == null) {
			return;
		}
		
		searchForMethods(ret, c.getSuperclass());
		
		for (Class iface : c.getInterfaces()) {
			searchForMethods(ret, iface);
		}
		
		ret.addAll(Arrays.asList(c.getMethods()));
	}
	
	/**
	 * Returns the property accessor and mutator methods for a class c
	 * @param c The class to get methods for
	 * @return The methods, or null if none
	 */
	public Props getProps(Class c) {
		return mProps.get(c);
	}

}
