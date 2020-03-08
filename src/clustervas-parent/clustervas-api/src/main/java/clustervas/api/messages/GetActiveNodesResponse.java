package clustervas.api.messages;

import java.util.ArrayList;
import java.util.List;

import clustervas.net.AbstractMessage;

public class GetActiveNodesResponse extends AbstractMessage {

	private List<String> nodeNames = new ArrayList<>();

	public List<String> getNodeNames() {
		return nodeNames;
	}
}
