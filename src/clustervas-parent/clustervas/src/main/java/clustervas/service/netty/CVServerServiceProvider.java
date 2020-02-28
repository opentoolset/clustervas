// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.service.netty;

import org.springframework.stereotype.Service;

import clustervas.api.CVServerService;
import clustervas.api.messages.SampleRequest;
import clustervas.api.messages.SampleResponse;

@Service
public class CVServerServiceProvider implements CVServerService {

	@Override
	public SampleResponse getSampleResponse(SampleRequest request) {
		return new SampleResponse();
	}
}
