package org.opentoolset.clustervas.sdk.messages.cv;

import java.util.ArrayList;
import java.util.List;

import org.opentoolset.clustervas.sdk.messages.AbstractResponse;

public class GetActiveNodesResponse extends AbstractResponse {

	private List<String> nodeNames = new ArrayList<>();

	public List<String> getNodeNames() {
		return nodeNames;
	}
}
