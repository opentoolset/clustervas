// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.messages;

import clustervas.net.AbstractRequest;

public class SampleRequest1 extends AbstractRequest<SampleResponse> {

	@Override
	public Class<SampleResponse> getResponseClass() {
		return SampleResponse.class;
	}
}
