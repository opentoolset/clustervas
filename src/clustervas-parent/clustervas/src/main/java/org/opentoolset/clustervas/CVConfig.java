// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas;

import org.apache.commons.lang3.ObjectUtils;
import org.opentoolset.clustervas.utils.CVConfigProvider;

public final class CVConfig {

	public enum Entry {

		ID("id", null),
		TLS_PRIVATE_KEY("tls.private_key", null),
		TLS_CERTIFICATE("tls.certificate", null),
		HOST_DATA_FOLDER("host.data_folder", CVConstants.DEFAULT_HOST_DATA_FOLDER),
		TEMPLATE_INTERNAL_SYNC_ENABLED("template.internal_sync.enabled", CVConstants.DEFAULT_TEMPLATE_INTERNAL_SYNC_ENABLED),
		ORCHESTRATOR_HOST("orchestrator.host", CVConstants.DEFAULT_SERVER_HOST),
		ORCHESTRATOR_PORT("orchestrator.port", CVConstants.DEFAULT_SERVER_PORT),
		ORCHESTRATOR_TLS_CERTIFICATE("orchestrator.tls.certificate", null);

		private String key;

		private Object defaultValue;

		private <T> Entry(String key, Object defaultValue) {
			this.key = key;
			this.defaultValue = defaultValue;
		}

		public String getKey() {
			return key;
		}

		public Object getDefaultValue() {
			return defaultValue;
		}

		public static boolean containsKey(String key) {
			for (Entry item : values()) {
				if (!ObjectUtils.notEqual(item.getKey(), key)) {
					return true;
				}
			}

			return false;
		}
	}

	private CVConfig() {
	}

	// ---

	public static void save() {
		CVConfigProvider.save();
	}

	public static String getId() {
		return getString(Entry.ID);
	}

	public static void setId(String id) {
		set(Entry.ID, id);
	}

	public static String getTLSPrivateKey() {
		return getString(Entry.TLS_PRIVATE_KEY);
	}

	public static void setTLSPrivateKey(String key) {
		set(Entry.TLS_PRIVATE_KEY, key);
	}

	public static String getTLSCertificate() {
		return getString(Entry.TLS_CERTIFICATE);
	}

	public static void setTLSCertificate(String cert) {
		set(Entry.TLS_CERTIFICATE, cert);
	}

	public static String getHostDataFolder() {
		return getString(Entry.HOST_DATA_FOLDER);
	}

	public static void setHostDataFolder(String folder) {
		set(Entry.HOST_DATA_FOLDER, folder);
	}

	public static boolean isTemplateInternalSyncEnabled() {
		return getBoolean(Entry.TEMPLATE_INTERNAL_SYNC_ENABLED);
	}

	public static void setTemplateInternalSyncEnabled(String enabled) {
		set(Entry.TEMPLATE_INTERNAL_SYNC_ENABLED, enabled);
	}

	public static String getOrchestratorHost() {
		return getString(Entry.ORCHESTRATOR_HOST);
	}

	public static void setOrchestratorHost(String host) {
		set(Entry.ORCHESTRATOR_HOST, host);
	}

	public static Integer getOrchestratorPort() {
		return getInteger(Entry.ORCHESTRATOR_PORT);
	}

	public static void setOrchestratorPort(String port) {
		set(Entry.ORCHESTRATOR_PORT, port);
	}

	public static String getOrchestratorTLSCertificate() {
		return getString(Entry.ORCHESTRATOR_TLS_CERTIFICATE);
	}

	public static void setOrchestratorTLSCertificate(String id) {
		set(Entry.ORCHESTRATOR_TLS_CERTIFICATE, id);
	}

	// ---

	private static String getString(Entry key) {
		return CVConfigProvider.getString(key);
	}

	private static boolean getBoolean(Entry key) {
		return CVConfigProvider.getBoolean(key);
	}

	private static Integer getInteger(Entry key) {
		return CVConfigProvider.getInteger(key);
	}

	private static void set(Entry key, Object value) {
		CVConfigProvider.set(key, value);
	}
}
