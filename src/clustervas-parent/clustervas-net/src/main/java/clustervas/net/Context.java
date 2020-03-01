package clustervas.net;

// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Context {

	private static Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

	private MessageSender messageSender = new MessageSender();

	private MessageReceiver messageReceiver = new MessageReceiver();

	// ---

	public static Logger getLogger() {
		return logger;
	}

	// ---

	public MessageSender getMessageSender() {
		return messageSender;
	}

	public MessageReceiver getMessageReceiver() {
		return messageReceiver;
	}

	// ---

	public static class OperationContext {

		private Thread thread;
		private MessageWrapper responseWrapper;

		public Thread getThread() {
			return thread;
		}

		public void setThread(Thread thread) {
			this.thread = thread;
		}

		public MessageWrapper getResponseWrapper() {
			return responseWrapper;
		}

		public void setResponseWrapper(MessageWrapper responseWrapper) {
			this.responseWrapper = responseWrapper;
		}
	}
}
