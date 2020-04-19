// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.service;

import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.opentoolset.clustervas.CVConfig;
import org.opentoolset.clustervas.sdk.messages.cv.AbstractRequestFromNodeManager;
import org.opentoolset.nettyagents.AbstractMessage;
import org.opentoolset.nettyagents.AbstractRequest;
import org.opentoolset.nettyagents.Utils;
import org.opentoolset.nettyagents.agents.ClientAgent;
import org.springframework.stereotype.Component;

import io.netty.handler.ssl.util.SelfSignedCertificate;

@Component
public class CVNodeManager {

	private ClientAgent agent = new ClientAgent();

	public <TReq extends AbstractRequestFromNodeManager<TResp>, TResp extends AbstractMessage> TResp doRequest(TReq request) {
		request.setSenderId(CVConfig.getId());
		return this.agent.doRequest(request);
	}

	public <TReq extends AbstractRequestFromNodeManager<TResp>, TResp extends AbstractMessage> TResp doRequest(TReq request, int timeoutSec) {
		request.setSenderId(CVConfig.getId());
		return this.agent.doRequest(request, timeoutSec);
	}

	public <TReq extends AbstractRequest<TResp>, TResp extends AbstractMessage> void setRequestHandler(Class<TReq> classOfRequest, Function<TReq, TResp> function) {
		this.agent.setRequestHandler(classOfRequest, function);
	}

	@PostConstruct
	private void postContruct() throws CertificateException, InvalidKeyException {
		String id = CVConfig.getId();
		if (StringUtils.isEmpty(id)) {
			CVConfig.setId(UUID.randomUUID().toString());
			CVConfig.save();
		}

		String priKeyStr = CVConfig.getTLSPrivateKey();
		String certStr = CVConfig.getTLSCertificate();

		if (StringUtils.isEmpty(priKeyStr) || StringUtils.isEmpty(certStr)) {
			SelfSignedCertificate cert = new SelfSignedCertificate();
			priKeyStr = Utils.base64Encode(cert.key().getEncoded());
			certStr = Utils.base64Encode(cert.cert().getEncoded());

			CVConfig.setTLSPrivateKey(priKeyStr);
			CVConfig.setTLSPrivateKey(certStr);
			CVConfig.save();
		}

		this.agent.getConfig().setTlsEnabled(true);
		this.agent.getConfig().setPriKey(priKeyStr);
		this.agent.getConfig().setCert(certStr);
		this.agent.getConfig().setRemoteHost(CVConfig.getServerHost());
		this.agent.getConfig().setRemotePort(CVConfig.getServerPort());
	}

	public void startPeerIdentificationMode() {
		this.agent.startPeerIdentificationMode();
	}

	public void startup() {
		this.agent.startup();
	}
}
