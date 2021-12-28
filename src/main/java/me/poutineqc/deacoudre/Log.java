package me.poutineqc.deacoudre;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
	private static Logger logger;

	public static void init(Logger logger, Level logLevel) {
		Log.logger = logger;
		Log.logger.setLevel(logLevel);
		for(Handler handler : Log.logger.getHandlers()) {
			handler.setLevel(logLevel);
		}
	}

	public static void config(String msg) {
		logger.log(Level.CONFIG, msg);
	}

	public static void config(String msg, Throwable thrown) {
		logger.log(Level.CONFIG, msg, thrown);
	}

	public static void finest(String msg) {
		logger.log(Level.FINEST, msg);
	}

	public static void finest(String msg, Throwable thrown) {
		logger.log(Level.FINEST, msg, thrown);
	}

	public static void finer(String msg) {
		logger.log(Level.FINER, msg);
	}

	public static void finer(String msg, Throwable thrown) {
		logger.log(Level.FINER, msg, thrown);
	}

	public static void fine(String msg) {
		logger.log(Level.FINE, msg);
	}

	public static void fine(String msg, Throwable thrown) {
		logger.log(Level.INFO, msg, thrown);
	}

	public static void info(String msg) {
		logger.log(Level.INFO, msg);
	}

	public static void info(String msg, Throwable thrown) {
		logger.log(Level.INFO, msg, thrown);
	}

	public static void warning(String msg) {
		logger.log(Level.WARNING, msg);
	}

	public static void warning(String msg, Throwable thrown) {
		logger.log(Level.WARNING, msg, thrown);
	}

	public static void severe(String msg) {
		logger.log(Level.SEVERE, msg);
	}

	public static void severe(String msg, Throwable thrown) {
		logger.log(Level.SEVERE, msg, thrown);
	}
}
