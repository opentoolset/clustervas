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
import org.opentoolset.clustervas.utils.CVLogger;
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

	boolean isConfigured() {
		Config config = agent.getConfig();

		boolean configured = true;
		configured = configured && config.isTlsEnabled();
		configured = configured && config.getPriKey() != null;
		configured = configured && config.getCert() != null;
		configured = configured && config.getRemoteHost() != null;
		configured = configured && config.getRemotePort() > 0;

		return configured;
	}

	public boolean hasTrustedOrchestrator() {
		return !this.agent.getContext().getTrustedCerts().isEmpty();
	}

	public void build() throws CertificateException, InvalidKeyException {
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
			CVConfig.setTLSCertificate(certStr);
			CVConfig.save();
		}

		Config config = agent.getConfig();
		config.setTlsEnabled(true);
		config.setPriKey(priKeyStr);
		config.setCert(certStr);
		config.setRemoteHost(CVConfig.getOrchestratorHost());
		config.setRemotePort(CVConfig.getOrchestratorPort());

		String orchestratorCertStr = CVConfig.getOrchestratorTLSCertificate();
		if (!StringUtils.isBlank(orchestratorCertStr)) {
			X509Certificate orchestratorCert = Utils.buildCert(orchestratorCertStr);
			String orchestratorFingerprint = Utils.getFingerprintAsHex(orchestratorCert);
			agent.getContext().getTrustedCerts().put(orchestratorFingerprint, orchestratorCert);
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

	public boolean startup() {
		synchronized (this) {
			try {
				if (!this.started && isConfigured()) {
					this.agent.startup();
					this.started = true;
					return true;
				}
			} catch (Exception e) {
				CVLogger.error(e);
			}

			return false;
		}
	}

	public boolean shutdown() {
		synchronized (this) {
			if (this.started) {
				this.agent.shutdown();
				this.started = false;
				return true;
			} else {
				return false;
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
		build();
	}
}
