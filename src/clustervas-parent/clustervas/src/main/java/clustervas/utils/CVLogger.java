// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.utils;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

public class CVLogger {

	private static ch.qos.logback.classic.Logger logger;

	static {
		Logger genericLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

		if (genericLogger instanceof ch.qos.logback.classic.Logger) {
			logger = (ch.qos.logback.classic.Logger) genericLogger;

			boolean addSyslogAppender = true;
			Iterator<Appender<ILoggingEvent>> appenders = logger.iteratorForAppenders();
			while (appenders.hasNext()) {
				Appender<ILoggingEvent> appender = appenders.next();
				if (appender instanceof SyslogAppender) {
					addSyslogAppender = false;
					break;
				}
			}

			if (addSyslogAppender) {
				SyslogAppender appender = new SyslogAppender();
				logger.addAppender(appender);
			}
		} else {
			throw new IllegalStateException(String.format("ch.qos.logback.classic.Logger is required. Current one is: %s", logger));
		}
	}

	public static void debug(String format, Object... args) {
		logger.debug(format, args);
	}

	public static void info(String format, Object... args) {
		logger.info(format, args);
	}

	public static void warn(String format, Object... args) {
		logger.warn(format, args);
	}

	public static void error(String format, Object... args) {
		logger.error(format, args);
	}

	public static void debug(Throwable e, String format, Object... args) {
		String msg = MessageFormatter.format(format, args).getMessage();
		logger.debug(msg, e);
	}

	public static void warn(Throwable e, String format, Object... args) {
		String msg = MessageFormatter.format(format, args).getMessage();
		logger.warn(msg, e);
	}

	public static void error(Throwable e, String format, Object... args) {
		String msg = MessageFormatter.format(format, args).getMessage();
		logger.error(msg, e);
	}

	public static void warn(Throwable e) {
		logger.warn("", e);
	}

	public static void error(Throwable e) {
		logger.error("", e);
	}

	public static void entering() {
		StackTraceElement element = getCallingMethod();
		logger.info("entering {}.{}", element.getClassName(), element.getMethodName());
	}

	public static void exiting() {
		StackTraceElement element = getCallingMethod();
		logger.info("exiting {}.{}", element.getClassName(), element.getMethodName());
	}

	// ---

	private static StackTraceElement getCallingMethod() {
		StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[3];
		return stackTraceElement;
	}
}
