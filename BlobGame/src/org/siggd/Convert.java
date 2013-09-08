package org.siggd;

public class Convert {
	/**
	 * Attempt to dynamically convert an object to target type, as intelligently as possible
	 * @param val  The object
	 * @param type The target type
	 * @return An object in either the target type, or a boxed version of that type
	 */
	public static Object convertTo(Object val, Class<?> type)
	{
		if (type == int.class || type == Integer.class) {
			return getInt(val);
		} else if (type == float.class || type == Float.class) {
			return getFloat(val);
		} else if (type == long.class || type == Long.class) {
			return getLong(val);
		} else if (type == double.class || type == Double.class) {
			return getDouble(val);
		} else if (type == String.class) {
			return val.toString();
		} else {
			return type.cast(val);
		}
	}

	// Get primitive types
	/**
	 * Will return 0 if the requested property is null, and truncate the value
	 * if it is not an integer. These will now return primitive types.
	 * 
	 * @param object
	 *            Prop
	 * @return Requested int primitive type
	 */
	public static int getInt(Object prop) {
		if (prop == null)
			return 0;
		if (prop instanceof Float) {
			return (int) (float) (Float) prop;
		}
		if (prop instanceof Long) {
			return (int) (long) (Long) prop;
		}
		if (prop instanceof Double) {
			return (int) (double) (Double) prop;
		}
		if (prop instanceof Integer) {
			return (int) (Integer) prop;
		}
		if (prop instanceof String) {
			try {
				return Integer.parseInt((String) prop);
			} catch (NumberFormatException n) {
				return -1;
			}
		}
		return 0;
	}

	/**
	 * Will return 0 if the requested property is null, and cast the value if it
	 * is not float. These will now return primitive types.
	 * 
	 * @param object
	 *            Prop
	 * @return Requested float primitive type
	 */
	public static float getFloat(Object prop) {
		if (prop == null)
			return 0;
		if (prop instanceof Integer) {
			return (float) (int) (Integer) prop;
		}
		if (prop instanceof Long) {
			return (float) (long) (Long) prop;
		}
		if (prop instanceof Double) {
			return (float) (double) (Double) prop;
		}
		if (prop instanceof Float) {
			return (float) (Float) prop;
		}
		if (prop instanceof String) {
			try {
				return Float.parseFloat((String) prop);
			} catch (NumberFormatException n) {
				return -1;
			}
		}
		return 0;
	}

	/**
	 * Will return 0 if the requested property is null, and truncate the value
	 * if it is not long. These will now return primitive types.
	 * 
	 * @param object
	 *            Prop
	 * @return Requested long primitive type
	 */
	public static long getLong(Object prop) {
		if (prop == null)
			return 0;
		if (prop instanceof Integer) {
			return (long) (int) (Integer) prop;
		}
		if (prop instanceof Float) {
			return (long) (float) (Float) prop;
		}
		if (prop instanceof Double) {
			return (long) (double) (Double) prop;
		}
		if (prop instanceof Long) {
			return (long) (Long) prop;
		}
		if (prop instanceof String) {
			try {
				return Long.parseLong((String) prop);
			} catch (NumberFormatException n) {
				return -1;
			}
		}
		return 0;
	}

	/**
	 * Will return 0 if the requested property is null, and cast the value if it
	 * is not double. These will now return primitive types.
	 * 
	 * @param object
	 *            Prop
	 * @return Requested double primitive type
	 */
	public static double getDouble(Object prop) {
		if (prop == null)
			return 0;
		if (prop instanceof Integer) {
			return (double) (int) (Integer) prop;
		}
		if (prop instanceof Float) {
			return (double) (float) (Float) prop;
		}
		if (prop instanceof Long) {
			return (double) (Long) (Long) prop;
		}
		if (prop instanceof Double) {
			return (double) (Double) prop;
		}
		if (prop instanceof String) {
			try {
				return Double.parseDouble((String) prop);
			} catch (NumberFormatException n) {
				return -1;
			}
		}
		return 0;
	}

	/**
	 * Converts radians to degrees
	 * 
	 * @param r
	 *            angle in radians
	 * @return
	 */
	public static float getDegrees(float r) {
		return (float) (r * 180f / (Math.PI));
	}

	/**
	 * Converts degrees to radians
	 * 
	 * @param d
	 *            angle in degrees
	 * @return
	 */
	public static float getRadians(float d) {
		return (float) (d * Math.PI / 180f);
	}
}
