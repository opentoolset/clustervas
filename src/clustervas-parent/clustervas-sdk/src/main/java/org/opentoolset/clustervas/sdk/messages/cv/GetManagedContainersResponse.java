package org.opentoolset.clustervas.sdk.messages.cv;

import java.util.ArrayList;
import java.util.List;

import org.opentoolset.clustervas.sdk.messages.AbstractResponse;

public class GetManagedContainersResponse extends AbstractResponse {

	private List<String> containerNames = new ArrayList<>();

	public List<String> getContainerNames() {
		return containerNames;
	}
}
