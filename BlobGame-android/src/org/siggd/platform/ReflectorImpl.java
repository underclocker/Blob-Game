package org.siggd.platform;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import org.siggd.actor.Actor;

import android.content.Context;
import android.util.Log;
import dalvik.system.DexFile;

public class ReflectorImpl implements Reflector {
	private Context mContext;

	public ReflectorImpl(Context context) {
		mContext = context;
	}

	public Set<Class<? extends Actor>> getActorSubTypes() {
		Set<Class<? extends Actor>> ret = new HashSet<Class<? extends Actor>>();
		try {
			DexFile df = new DexFile(mContext.getPackageCodePath());
			for (Enumeration<String> iter = df.entries(); iter.hasMoreElements();) {
				String str = iter.nextElement();
				
				if (!str.startsWith("org.siggd.actor")) {
					continue;
				}
				
				try {
					Class<?> cls = Class.forName(str);
					
					if (Actor.class.isAssignableFrom(cls)) {
						ret.add(cls.asSubclass(Actor.class));
					}

				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				}
			}
			
			return ret;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
