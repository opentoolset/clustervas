package clustervas.api.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Context {

	// --- Singleton

	private static Context INSTANCE = new Context();

	public static Context getInstance() {
		return INSTANCE;
	}

	private Context() {
	}

	// ---

	private Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

	public Logger getLogger() {
		return logger;
	}
}
