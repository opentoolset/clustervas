package clustervas.api.netty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;

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

	private ChannelHandlerContext channelHandlerContext;

	// ---

	public static Logger getLogger() {
		return logger;
	}

	// ---

	public Map<String, OperationContext> getWaitingRequests() {
		return waitingRequests;
	}

	public ChannelHandlerContext getChannelHandlerContext() {
		return channelHandlerContext;
	}

	public void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
		this.channelHandlerContext = channelHandlerContext;
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
