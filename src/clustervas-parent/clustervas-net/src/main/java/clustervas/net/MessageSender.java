// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.net;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import clustervas.net.Context.OperationContext;
import io.netty.channel.ChannelHandlerContext;

public class MessageSender {

	private Logger logger = Context.getLogger();

	private Map<String, OperationContext> waitingRequests = new ConcurrentHashMap<>();

	private ChannelHandlerContext channelHandlerContext;

	// ---

	public <TReq extends AbstractRequest<TResp>, TResp extends AbstractMessage> TResp doRequest(TReq request) {
		try {
			while (this.channelHandlerContext == null) {
				TimeUnit.SECONDS.sleep(1);
			}

			MessageWrapper requestWrapper = MessageWrapper.createRequest(request);
			Thread currentThread = Thread.currentThread();

			OperationContext operationContext = new OperationContext();
			operationContext.setThread(currentThread);

			this.waitingRequests.put(requestWrapper.getId(), operationContext);

			this.channelHandlerContext.writeAndFlush(requestWrapper);
			synchronized (currentThread) {
				currentThread.wait(Constants.REQUST_TIMEOUT_MILLIS);
			}

			operationContext = this.waitingRequests.remove(requestWrapper.getId());
			MessageWrapper responseWrapper = operationContext.getResponseWrapper();
			if (responseWrapper != null) {
				TResp responseMessage = responseWrapper.deserializeMessage(request.getResponseClass());
				return responseMessage;
			}
		} catch (InterruptedException e) {
			this.logger.error("Interrupted", e);
		}

		return null;
	}

	public <T extends AbstractMessage> boolean sendMessage(T message) {
		try {
			MessageWrapper messageWrapper = MessageWrapper.create(message);
			this.channelHandlerContext.writeAndFlush(messageWrapper);
			return true;
		} catch (Exception e) {
			// TODO [hadi] Handle exception
			this.logger.error(e.getLocalizedMessage(), e);
		}
		return false;
	}

	public void shutdown() {
		this.channelHandlerContext.close();
	}

	// ---

	void setChannelHandlerContext(ChannelHandlerContext channelHandlerContext) {
		this.channelHandlerContext = channelHandlerContext;
	}

	ChannelHandlerContext getChannelHandlerContext() {
		return channelHandlerContext;
	}

	Map<String, OperationContext> getWaitingRequests() {
		return waitingRequests;
	}
}