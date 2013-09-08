package org.siggd;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;

/**
 * How to use this class:
 * 
 * When you want to log a debug message:
 * 
 * DebugOutput.info(this, "Message goes here");
 * 
 * instead of info, you can also use warning and severe.
 * 
 * 
 * 
 * 
 * @author hgentry
 * 
 */
public class DebugOutput {
	private static boolean enabled = false;
	private static boolean heavyEnabled = false;
	private static boolean fineEnabled = false;
	private static boolean finerEnabled = false;
	private static DebugFormatter format = new DebugFormatter();
	private static ConsoleHandler handler;

	public static void info(Object o, String s) {
		Logger logger = Logger.getLogger(o.getClass().getSimpleName());
		logger.setUseParentHandlers(false);
		Handler[] handlers = Logger.getLogger(o.getClass().getSimpleName()).getHandlers();
		if (handlers.length == 0) {
			if (handler == null) {
				handler = new ConsoleHandler();
				handler.setFormatter(format);
				handler.setLevel(Level.FINEST);
			}
			Logger.getLogger(o.getClass().getSimpleName()).addHandler(handler);
			Logger.getLogger(o.getClass().getSimpleName()).setLevel(Level.FINEST);
		}
		Logger.getLogger(o.getClass().getSimpleName()).getHandlers()[0].setFormatter(format);
		if (enabled)
			logger.info(s);
	}

	public static void severe(Object o, String s) {
		Logger logger = Logger.getLogger(o.getClass().getSimpleName());
		logger.setUseParentHandlers(false);
		Handler[] handlers = Logger.getLogger(o.getClass().getSimpleName()).getHandlers();
		if (handlers.length == 0) {
			if (handler == null) {
				handler = new ConsoleHandler();
				handler.setFormatter(format);
				handler.setLevel(Level.FINEST);
			}
			Logger.getLogger(o.getClass().getSimpleName()).addHandler(handler);
			Logger.getLogger(o.getClass().getSimpleName()).setLevel(Level.FINEST);
		}
		Logger.getLogger(o.getClass().getSimpleName()).getHandlers()[0].setFormatter(format);
		if (enabled)
			logger.severe(s);
	}

	public static void warning(Object o, String s) {
		Logger logger = Logger.getLogger(o.getClass().getSimpleName());
		logger.setUseParentHandlers(false);
		Handler[] handlers = Logger.getLogger(o.getClass().getSimpleName()).getHandlers();
		if (handlers.length == 0) {
			if (handler == null) {
				handler = new ConsoleHandler();
				handler.setFormatter(format);
				handler.setLevel(Level.FINEST);
			}
			Logger.getLogger(o.getClass().getSimpleName()).addHandler(handler);
			Logger.getLogger(o.getClass().getSimpleName()).setLevel(Level.FINEST);
		}
		Logger.getLogger(o.getClass().getSimpleName()).getHandlers()[0].setFormatter(format);
		if (enabled)
			logger.warning(s);
	}

	public static void fine(Object o, String s) {
		Logger logger = Logger.getLogger(o.getClass().getSimpleName());
		logger.setUseParentHandlers(false);
		Handler[] handlers = Logger.getLogger(o.getClass().getSimpleName()).getHandlers();
		if (handlers.length == 0) {
			if (handler == null) {
				handler = new ConsoleHandler();
				handler.setFormatter(format);
				handler.setLevel(Level.FINEST);
			}
			Logger.getLogger(o.getClass().getSimpleName()).addHandler(handler);
			Logger.getLogger(o.getClass().getSimpleName()).setLevel(Level.FINEST);
		}
		Logger.getLogger(o.getClass().getSimpleName()).getHandlers()[0].setFormatter(format);
		if (fineEnabled)
			logger.fine(s);
	}

	public static void finer(Object o, String s) {
		Logger logger = Logger.getLogger(o.getClass().getSimpleName());
		logger.setUseParentHandlers(false);
		Handler[] handlers = Logger.getLogger(o.getClass().getSimpleName()).getHandlers();
		if (handlers.length == 0) {
			if (handler == null) {
				handler = new ConsoleHandler();
				handler.setFormatter(format);
				handler.setLevel(Level.FINEST);
			}
			Logger.getLogger(o.getClass().getSimpleName()).addHandler(handler);
			Logger.getLogger(o.getClass().getSimpleName()).setLevel(Level.FINEST);
		}
		Logger.getLogger(o.getClass().getSimpleName()).getHandlers()[0].setFormatter(format);
		if (finerEnabled)
			logger.finer(s);
	}

	public static void finest(Object o, String s) {
		Logger logger = Logger.getLogger(o.getClass().getSimpleName());
		logger.setUseParentHandlers(false);
		Handler[] handlers = Logger.getLogger(o.getClass().getSimpleName()).getHandlers();
		if (handlers.length == 0) {
			if (handler == null) {
				handler = new ConsoleHandler();
				handler.setFormatter(format);
				handler.setLevel(Level.FINEST);
			}
			Logger.getLogger(o.getClass().getSimpleName()).addHandler(handler);
			Logger.getLogger(o.getClass().getSimpleName()).setLevel(Level.FINEST);
		}
		Logger.getLogger(o.getClass().getSimpleName()).getHandlers()[0].setFormatter(format);
		if (heavyEnabled)
			logger.finest(s);
	}

	public static void disable() {
		enabled = false;
	}

	public static void enable() {
		enabled = true;
	}

	public static void heavyDisable() {
		heavyEnabled = false;
	}

	public static void heavyEnable() {
		heavyEnabled = true;
	}

	public static void fineDisable() {
		fineEnabled = false;
	}

	public static void fineEnable() {
		fineEnabled = true;
	}

	public static void finerDisable() {
		finerEnabled = false;
	}

	public static void finerEnable() {
		finerEnabled = true;
	}

	public static boolean isEnabled() {
		return enabled;
	}

	public static boolean isHeavyEnabled() {
		return heavyEnabled;
	}

	public static boolean isFineEnabled() {
		return heavyEnabled;
	}

	public static boolean isFinerEnabled() {
		return heavyEnabled;
	}
}