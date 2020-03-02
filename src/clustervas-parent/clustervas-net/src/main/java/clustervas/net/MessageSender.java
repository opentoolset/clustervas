// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.net;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

public class MessageSender {

	private Logger logger = Context.getLogger();

	private Map<String, OperationContext> waitingRequests = new ConcurrentHashMap<>();

	// ---

	public <TReq extends AbstractRequest<TResp>, TResp extends AbstractMessage> TResp doRequest(TReq request, PeerContext peerContext) {
		return doRequest(request, peerContext, Constants.DEFAULT_REQUEST_TIMEOUT_MILLIS);
	}

	public <TReq extends AbstractRequest<TResp>, TResp extends AbstractMessage> TResp doRequest(TReq request, PeerContext peerContext, long timeoutMillis) {
		try {
			while (peerContext.getChannelHandlerContext() == null) {
				TimeUnit.SECONDS.sleep(1);
			}

			MessageWrapper requestWrapper = MessageWrapper.createRequest(request);
			Thread currentThread = Thread.currentThread();

			OperationContext operationContext = new OperationContext();
			operationContext.setThread(currentThread);

			this.waitingRequests.put(requestWrapper.getId(), operationContext);

			peerContext.getChannelHandlerContext().writeAndFlush(requestWrapper);
			synchronized (currentThread) {
				currentThread.wait(timeoutMillis);
			}

			operationContext = this.waitingRequests.remove(requestWrapper.getId());
			if (operationContext != null) {
				MessageWrapper responseWrapper = operationContext.getResponseWrapper();
				if (responseWrapper != null) {
					TResp responseMessage = responseWrapper.deserializeMessage(request.getResponseClass());
					return responseMessage;
				}
			}
		} catch (InterruptedException e) {
			this.logger.error("Interrupted", e);
		}

		return null;
	}

	public <T extends AbstractMessage> boolean sendMessage(T message, PeerContext peerContext) {
		try {
			MessageWrapper messageWrapper = MessageWrapper.create(message);
			peerContext.getChannelHandlerContext().writeAndFlush(messageWrapper);
			return true;
		} catch (Exception e) {
			// TODO [hadi] Handle exception
			this.logger.error(e.getLocalizedMessage(), e);
		}
		return false;
	}

	public void shutdown() {
		for (OperationContext operationContext : this.waitingRequests.values()) {
			Thread thread = operationContext.getThread();
			synchronized (thread) {
				thread.notify();
			}
		}
	}

	// ---

	Map<String, OperationContext> getWaitingRequests() {
		return waitingRequests;
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