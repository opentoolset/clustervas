// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import clustervas.api.messages.SampleRequest1;
import clustervas.api.messages.SampleRequest2;
import clustervas.api.messages.SampleResponse;
import clustervas.service.netty.CVAgentRequestHandler;

@Service
public class CVServiceImpl extends AbstractService {

	@Autowired
	private CVAgentRequestHandler requestHandler;

	@PostConstruct
	private void postConstruct() {
		requestHandler.setHandler(SampleRequest1.class, request -> new SampleResponse());
		requestHandler.setHandler(SampleRequest2.class, request -> new SampleResponse());
	}

	@PreDestroy
	private void preDestroy() {
	}
}
