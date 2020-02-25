// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas;

import org.apache.commons.lang3.ObjectUtils;

import clustervas.utils.CVConfigProvider;

public final class CVConfig {

	public enum Entry {

		CONTAINER_INTERNAL_DATA_FOLDER("container.data_folder", "/clustervas/data"),
		TEMPLATE_INTERNAL_SYNC_ENABLED("template.internal_sync.enabled", true),
		MANAGER_HOST("manager.host", "127.0.0.1"),
		MANAGER_PORT("manager.port", 4444);

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

	public static String getContainerInternalDataFolder() {
		return getString(Entry.CONTAINER_INTERNAL_DATA_FOLDER);
	}

	public static boolean isTemplateInternalSyncEnabled() {
		return getBoolean(Entry.TEMPLATE_INTERNAL_SYNC_ENABLED);
	}

	public static String getManagerHost() {
		return getString(Entry.MANAGER_HOST);
	}

	public static Integer getManagerPort() {
		return getInteger(Entry.MANAGER_PORT);
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
