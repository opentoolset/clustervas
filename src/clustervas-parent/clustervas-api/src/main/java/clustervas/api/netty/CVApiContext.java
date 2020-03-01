package clustervas.api.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CVApiContext {

	private static Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

	private CVMessageSender messageSender = new CVMessageSender();

	// ---

	public static Logger getLogger() {
		return logger;
	}

	// ---

	public CVMessageSender getMessageSender() {
		return messageSender;
	}

	// ---

	public static class OperationContext {

		private MessageWrapper responseWrapper;
		private Thread thread;

		public MessageWrapper getResponseWrapper() {
			return responseWrapper;
		}

		public void setResponseWrapper(MessageWrapper responseWrapper) {
			this.responseWrapper = responseWrapper;
		}

		public Thread getThread() {
			return thread;
		}

		public void setThread(Thread thread) {
			this.thread = thread;
		}
	}
}
