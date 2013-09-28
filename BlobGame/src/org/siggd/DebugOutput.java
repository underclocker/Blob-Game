package org.siggd;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
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
	private static PrintWriter pw;
	private static boolean useFile = false;
	private static int outCounter = 0;
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
		if (enabled && !useFile)
		{
			logger.info(s);
		}
		else
		{
			if(enabled && useFile)
			{
				pw.println(outCounter + " INFO: " + s);
				outCounter++;
			}
		}
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
		if (enabled && !useFile)
		{
			logger.severe(s);
		}
		else
		{
			if(enabled && useFile)
			{
				pw.println(outCounter + " SEVERE: " + s);
				outCounter++;
			}
		}
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
		if (enabled && !useFile)
		{
			logger.warning(s);
		}
		else
		{
			if(enabled && useFile)
			{
				pw.println(outCounter + " WARNING: " + s);
				outCounter++;
			}
		}
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
		if (fineEnabled && !useFile)
		{
			logger.fine(s);
		}
		else
		{
			if(enabled && useFile)
			{
				pw.println(outCounter + " FINE: " + s);
				outCounter++;
			}
		}
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
		if (finerEnabled && !useFile)
		{
			logger.finer(s);
		}
		else
		{
			if(enabled && useFile)
			{
				pw.println(outCounter + " FINER: " + s);
				outCounter++;
			}
		}
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
		if (heavyEnabled && !useFile)
		{
			logger.finest(s);
		}
		else
		{
			if(enabled && useFile)
			{
				pw.println(outCounter + " FINEST: " + s);
				outCounter++;
			}
		}
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
	
	
	public static void setFile()
	{
		Date d = new Date();
		setFile(d.getTime()+"");
	}
	public static void setFile(String filename)
	{
		if(Game.DEBUG)
		{
		try{
			pw = new PrintWriter(filename + ".txt");
			useFile = true;
		}catch(Exception e){System.out.println(filename);e.printStackTrace();}
		
		}
	}
	public static void close()
	{
		try{
		pw.flush();
		pw.close();
		}catch(Exception e){}
		
	}
	
}