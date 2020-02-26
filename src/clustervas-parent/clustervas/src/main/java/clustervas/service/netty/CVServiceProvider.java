// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.service.netty;

import org.springframework.stereotype.Service;

import clustervas.api.CVService;
import clustervas.api.messages.SampleRequest;
import clustervas.api.messages.SampleResponse;

@Service
public class CVServiceProvider implements CVService {

	@Override
	public SampleResponse getSampleResponse(SampleRequest request) {
		return null;
	}
}
