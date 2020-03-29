// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas;

import org.apache.commons.lang3.ObjectUtils;
import org.opentoolset.clustervas.utils.CVConfigProvider;

public final class CVConfig {

	public enum Entry {

		TLS_PRIVATE_KEY("tls.private_key", null),
		TLS_CERTIFICATE("tls.certificate", null),
		HOST_DATA_FOLDER("host.data_folder", CVConstants.DEFAULT_HOST_DATA_FOLDER),
		TEMPLATE_INTERNAL_SYNC_ENABLED("template.internal_sync.enabled", CVConstants.DEFAULT_TEMPLATE_INTERNAL_SYNC_ENABLED),
		SERVER_HOST("server.host", CVConstants.DEFAULT_SERVER_HOST),
		SERVER_PORT("server.port", CVConstants.DEFAULT_SERVER_PORT);

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

	public static String getTLSPrivateKey() {
		return getString(Entry.TLS_PRIVATE_KEY);
	}

	public static String getTLSCertificate() {
		return getString(Entry.TLS_CERTIFICATE);
	}

	public static String getHostDataFolder() {
		return getString(Entry.HOST_DATA_FOLDER);
	}

	public static boolean isTemplateInternalSyncEnabled() {
		return getBoolean(Entry.TEMPLATE_INTERNAL_SYNC_ENABLED);
	}

	public static String getServerHost() {
		return getString(Entry.SERVER_HOST);
	}

	public static Integer getServerPort() {
		return getInteger(Entry.SERVER_PORT);
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
}
