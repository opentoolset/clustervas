package org.opentoolset.clustervas.sdk.messages.cv;

import java.util.ArrayList;
import java.util.List;

import org.opentoolset.nettyagents.AbstractMessage;

public class GetActiveNodesResponse extends AbstractMessage {

	private static final long serialVersionUID = 639549275204808332L;

	private List<String> nodeNames = new ArrayList<>();

	public List<String> getNodeNames() {
		return nodeNames;
	}
}
