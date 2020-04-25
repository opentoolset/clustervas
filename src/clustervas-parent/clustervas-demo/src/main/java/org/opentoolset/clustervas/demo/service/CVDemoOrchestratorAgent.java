package org.opentoolset.clustervas.demo.service;

import java.security.InvalidKeyException;
import java.security.cert.CertificateException;

import javax.annotation.PostConstruct;

import org.opentoolset.clustervas.sdk.CVAgent;
import org.opentoolset.nettyagents.Constants;
import org.opentoolset.nettyagents.Utils;
import org.springframework.stereotype.Component;

import io.netty.handler.ssl.util.SelfSignedCertificate;

@Component
public class CVDemoOrchestratorAgent extends CVAgent {

	@PostConstruct
	private void postContruct() throws CertificateException, InvalidKeyException {
		SelfSignedCertificate cert = new SelfSignedCertificate();
		String priKeyStr = Utils.base64Encode(cert.key().getEncoded());
		String certStr = Utils.base64Encode(cert.cert().getEncoded());

		getConfig().setPriKey(priKeyStr);
		getConfig().setCert(certStr);
		getConfig().setLocalPort(Constants.DEFAULT_SERVER_PORT);
	}
}
