// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.FileBasedConfiguration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.ConfigurationBuilderEvent;
import org.apache.commons.configuration2.builder.ReloadingFileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.FileBasedBuilderParameters;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.event.ConfigurationEvent;
import org.apache.commons.configuration2.event.EventListener;
import org.apache.commons.configuration2.event.EventType;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.reloading.PeriodicReloadingTrigger;
import org.apache.commons.configuration2.reloading.ReloadingController;
import org.apache.commons.configuration2.reloading.ReloadingEvent;
import org.glassfish.jersey.internal.guava.Lists;
import org.opentoolset.clustervas.CVConfig;
import org.opentoolset.clustervas.CVConfig.Entry;
import org.opentoolset.clustervas.CVConstants;

public final class CVConfigProvider {

	private static ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> configBuilder;

	private CVConfigProvider() {
	}

	static {
		try {
			CVLogger.info("Home folder: {}", CVConstants.HOME_FOLDER);
			CVLogger.info("Settings file: {}", CVConstants.PATH_OF_CONFIG_FILE);

			Files.createDirectories(CVConstants.PATH_OF_CONFIG_FOLDER);
			CVLogger.info("Config directory is {}", CVConstants.PATH_OF_CONFIG_FOLDER);
			if (!Files.exists(CVConstants.PATH_OF_CONFIG_FILE)) {
				try {
					Files.createFile(CVConstants.PATH_OF_CONFIG_FILE);
					CVLogger.info("Config file was created {}", CVConstants.PATH_OF_CONFIG_FILE);
				} catch (IOException e) {
					CVLogger.warn("Config file couldn't be created", e);
				}
			}
		} catch (IOException e) {
			CVLogger.warn("Config directory couldn't be created", e);
		}

		loadConfig();
	}

	// ---

	public static String getString(Entry key) {
		try {
			return getFileBasedConfig().getString(key.getKey(), (String) key.getDefaultValue());
		} catch (Exception e) {
			return null;
		}
	}

	public static Integer getInteger(Entry key) {
		try {
			return getFileBasedConfig().getInteger(key.getKey(), (Integer) key.getDefaultValue());
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean getBoolean(Entry key) {
		try {
			return getFileBasedConfig().getBoolean(key.getKey(), (boolean) key.getDefaultValue());
		} catch (Exception e) {
			return false;
		}
	}

	public static void setAndSave(Entry key, Object value) {
		set(key, value);
		save();
	}

	public static void set(Entry key, Object value) {
		getFileBasedConfig().setProperty(key.getKey(), Optional.ofNullable(value).orElse(""));
	}

	public static void save() {
		try {
			configBuilder.save();
		} catch (ConfigurationException e) {
			CVLogger.error(e);
		}
	}

	public static <T extends ConfigurationEvent> void addConfigurationEventListener(EventType<T> eventType, EventListener<? super T> listener) {
		configBuilder.addEventListener(eventType, listener);
	}

	public static <T extends ReloadingEvent> void addReloadingEventListener(EventType<T> eventType, EventListener<? super T> listener) {
		configBuilder.getReloadingController().addEventListener(eventType, listener);
	}

	public static Configuration getConfig() {
		return getFileBasedConfig();
	}

	// ---

	private static void loadConfig() {
		try {
			boolean changed = false;
			FileBasedConfiguration config = getFileBasedConfig();
			for (CVConfig.Entry item : CVConfig.Entry.values()) {
				String key = item.getKey();
				if (!config.containsKey(key)) {
					config.setProperty(key, Optional.ofNullable(item.getDefaultValue()).orElse(""));
					changed = true;
				}
			}

			for (String key : Lists.newArrayList(config.getKeys())) {
				if (!CVConfig.Entry.containsKey(key)) {
					config.clearProperty(key);
					changed = true;
				}
			}

			if (changed) {
				configBuilder.save();
				CVLogger.info("Config file was completed with defaults");
			}
		} catch (ConfigurationException e) {
			CVLogger.error(e);
		}
	}

	private static FileBasedConfiguration getFileBasedConfig() {
		if (configBuilder == null) {
			configBuilder = createConfigBuilder();
		}

		try {
			return configBuilder.getConfiguration();
		} catch (ConfigurationException e) {
			CVLogger.error(e);
			System.exit(-1);
			return null;
		}
	}

	private static ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> createConfigBuilder() {
		ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration> configBuilder = new ReloadingFileBasedConfigurationBuilder<FileBasedConfiguration>(PropertiesConfiguration.class);
		{
			File configFile = CVConstants.PATH_OF_CONFIG_FILE.toFile();
			FileBasedBuilderParameters params = new Parameters().fileBased().setFile(configFile);
			configBuilder.configure(params);
			CVLogger.info("Config file was loaded {}", CVConstants.PATH_OF_CONFIG_FILE);
		}

		configBuilder.addEventListener(ConfigurationBuilderEvent.RESET, event -> configBuilder.getReloadingController().resetReloadingState());

		ReloadingController reloadingController = configBuilder.getReloadingController();
		PeriodicReloadingTrigger trigger = new PeriodicReloadingTrigger(reloadingController, null, 10, TimeUnit.SECONDS);
		trigger.start();

		return configBuilder;
	}
}
