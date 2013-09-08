package org.siggd;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class DebugFormatter extends Formatter {

	private int mCounter = 0;

	public String format(LogRecord record) {
		StringBuilder sb = new StringBuilder();

		sb.append(record.getLevel()).append(" " + mCounter++ + ": ")
				.append(record.getMessage() + "\n");

		if (record.getThrown() != null) {
			try {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				record.getThrown().printStackTrace(pw);
				pw.close();
				sb.append(sw.toString());
			} catch (Exception ex) {
				// ignore
			}
		}

		return sb.toString();
	}
}