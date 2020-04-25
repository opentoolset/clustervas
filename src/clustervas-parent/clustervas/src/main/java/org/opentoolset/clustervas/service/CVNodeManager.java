// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.service;

import java.security.InvalidKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.UUID;
import java.util.function.Function;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.opentoolset.clustervas.CVConfig;
import org.opentoolset.clustervas.sdk.messages.cv.AbstractRequestFromNodeManager;
import org.opentoolset.nettyagents.AbstractMessage;
import org.opentoolset.nettyagents.AbstractRequest;
import org.opentoolset.nettyagents.PeerContext;
import org.opentoolset.nettyagents.Utils;
import org.opentoolset.nettyagents.agents.ClientAgent;
import org.opentoolset.nettyagents.agents.ClientAgent.Config;
import org.springframework.stereotype.Component;

import io.netty.handler.ssl.util.SelfSignedCertificate;

@Component
public class CVNodeManager {

	private ClientAgent agent;

	private boolean started = false;

	public void prepare() throws CertificateException, InvalidKeyException {
		ClientAgent agent = new ClientAgent();

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

		agent.getConfig().setTlsEnabled(true);
		agent.getConfig().setPriKey(priKeyStr);
		agent.getConfig().setCert(certStr);
		agent.getConfig().setRemoteHost(CVConfig.getOrchestratorHost());
		agent.getConfig().setRemotePort(CVConfig.getOrchestratorPort());

		String orchestratorCert = CVConfig.getOrchestratorTLSCertificate();
		if (!StringUtils.isBlank(orchestratorCert)) {
			X509Certificate cert = Utils.buildCert(orchestratorCert);
			setTrustedCert(cert);
		}

		this.agent = agent;
	}

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

	public void startPeerIdentificationMode() {
		this.agent.startPeerIdentificationMode();
	}

	public void stopPeerIdentificationMode() {
		this.agent.stopPeerIdentificationMode();
	}

	public Config getConfig() {
		return this.agent.getConfig();
	}

	public void setTrustedCert(X509Certificate orchestratorCert) {
		String fingerprint = Utils.getFingerprintAsHex(orchestratorCert);

		this.agent.getContext().getTrustedCerts().clear();
		this.agent.getContext().getTrustedCerts().put(fingerprint, orchestratorCert);
	}

	public void startup() {
		synchronized (this) {
			if (!this.started) {
				this.agent.startup();
				this.started = true;
			}
		}
	}

	public void shutdown() {
		synchronized (this) {
			if (this.started) {
				this.agent.shutdown();
				this.started = false;
			}
		}
	}

	public PeerContext getServer() {
		return this.agent.getServer();
	}

	public void setTrusted(PeerContext server, String orchestratorFingerprint, X509Certificate orchestratorCert) {
		this.agent.getContext().getTrustedCerts().clear();
		this.agent.getContext().getTrustedCerts().put(orchestratorFingerprint, orchestratorCert);
		server.setTrusted(true);
	}

	// ---

	@PostConstruct
	private void postContruct() throws InvalidKeyException, CertificateException {
		prepare();
	}
}
