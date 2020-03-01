package clustervas.api.netty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

import clustervas.api.netty.CVApiContext.OperationContext;
import io.netty.channel.ChannelHandlerContext;

public class CVMessageSender {

	private Logger logger = CVApiContext.getLogger();

	private Map<String, OperationContext> waitingRequests = new ConcurrentHashMap<>();

	private ChannelHandlerContext channelHandlerContext;

	// ---

	public <TReq extends AbstractMessage, TResp extends AbstractMessage> TResp doRequest(TReq request, Class<TResp> classOfResponse) {
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
				currentThread.wait(CVApiConstants.REQUST_TIMEOUT_MILLIS);
			}

			operationContext = this.waitingRequests.remove(requestWrapper.getId());
			MessageWrapper responseWrapper = operationContext.getResponseWrapper();
			if (responseWrapper != null) {
				TResp responseMessage = responseWrapper.deserializeMessage(classOfResponse);
				return responseMessage;
			}
		} catch (InterruptedException e) {
			this.logger.error("Interrupted", e);
		}

		return null;
	}

	public <TReq extends AbstractMessage> boolean sendMessage(TReq message) {
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