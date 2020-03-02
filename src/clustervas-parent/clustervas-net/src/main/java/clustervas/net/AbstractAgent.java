package clustervas.net;

import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;

public abstract class AbstractAgent {

	protected static Logger logger = Context.getLogger();

	protected Context context = new Context();

	protected MessageEncoder encoder = new MessageEncoder();
	protected MessageDecoder decoder = new MessageDecoder();
	protected InboundMessageHandler inboundMessageHandler = new InboundMessageHandler(context);

	public <TReq extends AbstractRequest<TResp>, TResp extends AbstractMessage> void setRequestHandler(Class<TReq> classOfRequest, Function<TReq, TResp> function) {
		this.context.getMessageReceiver().setRequestHandler(classOfRequest, function);
	}

	public <T extends AbstractMessage> void setMessageHandler(Class<T> classOfMessage, Consumer<T> consumer) {
		this.context.getMessageReceiver().setMessageHandler(classOfMessage, consumer);
	}
}
