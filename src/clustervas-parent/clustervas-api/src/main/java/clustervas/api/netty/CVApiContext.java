package clustervas.api.netty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.Channel;

public class CVApiContext {

	// --- Singleton

	private static CVApiContext INSTANCE = new CVApiContext();

	public static CVApiContext getInstance() {
		return INSTANCE;
	}

	private CVApiContext() {
	}

	// ---

	private static Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

	private Map<String, OperationContext> waitingRequests = new ConcurrentHashMap<>();

	private Channel nettyChannel;

	// ---

	public static Logger getLogger() {
		return logger;
	}

	// ---

	public Map<String, OperationContext> getWaitingRequests() {
		return waitingRequests;
	}

	public Channel getNettyChannel() {
		return nettyChannel;
	}

	public void setNettyChannel(Channel nettyChannel) {
		this.nettyChannel = nettyChannel;
	}

	// ---

	public static class OperationContext {

		private ResponseWrapper responseWrapper;
		private Thread thread;

		public ResponseWrapper getResponseWrapper() {
			return responseWrapper;
		}

		public void setResponseWrapper(ResponseWrapper responseWrapper) {
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
