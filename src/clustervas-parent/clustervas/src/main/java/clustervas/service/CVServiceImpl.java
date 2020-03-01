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
import clustervas.api.messages.SampleResponse;
import clustervas.net.MessageReceiver;

@Service
public class CVServiceImpl extends AbstractService {

	@Autowired
	private CVAgent cvAgent;

	@PostConstruct
	private void postConstruct() {
		MessageReceiver messageReceiver = this.cvAgent.getContext().getMessageReceiver();
		messageReceiver.setRequestHandler(SampleRequest1.class, request -> handle(request));
	}

	@PreDestroy
	private void preDestroy() {
	}

	private SampleResponse handle(SampleRequest1 request) {
		return new SampleResponse();
	}
}
