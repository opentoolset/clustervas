// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.opentoolset.clustervas.utils.CVLogger;
import org.springframework.util.StringUtils;

public interface CVConstants {

	String HOME_ENVIRONMENT_VARIABLE = "CLUSTERVAS_HOME";
	String HOME_DEFAULT = "/clustervas";
	String HOME_FOLDER = getHomeFolder();
	String CONFIG_FOLDER = "config";
	String CONFIG_FILE = "clustervas.conf";

	Path PATH_OF_CONFIG_FOLDER = Paths.get(HOME_FOLDER, CONFIG_FOLDER);
	Path PATH_OF_CONFIG_FILE = PATH_OF_CONFIG_FOLDER.resolve(CONFIG_FILE);

	String DOCKER_TEMPLATE_IMAGE_NAME = "clustervas-template";
	String DOCKER_OPERATIONAL_IMAGE_NAME = "clustervas";
	String DOCKER_TEMP_IMAGE_NAME = "clustervas-temp";

	String DOCKER_NODE_MANAGER_CONTAINER_NAME = "clustervas-manager";
	String DOCKER_TEMPLATE_CONTAINER_NAME = "clustervas-template";
	String DOCKER_OPERATIONAL_CONTAINER_NAME_PREFIX = "clustervas";
	String DOCKER_DATA_FOLDER_IN_CONTAINERS = "/clustervas/data";

	String DOCKER_CONTAINER_STATUS_RUNNING = "running";

	String DEFAULT_HOST_DATA_FOLDER = "/clustervas/data";
	boolean DEFAULT_TEMPLATE_INTERNAL_SYNC_ENABLED = true;

	String DEFAULT_SERVER_HOST = "127.0.0.1";
	int DEFAULT_SERVER_PORT = 4444;
	String DEFAULT_GVM_DEFAULT_USER = "admin";
	String DEFAULT_GVM_DEFAULT_PASSWORD = "admin";

	static String getHomeFolder() {
		String homeFolderEnv = System.getenv(HOME_ENVIRONMENT_VARIABLE);

		if (!StringUtils.hasText(homeFolderEnv)) {
			CVLogger.info("The environment variable ({}) is undefined, using default location ({}) as home folder.", HOME_ENVIRONMENT_VARIABLE, HOME_DEFAULT);
			return HOME_DEFAULT;
		} else {
			CVLogger.info("({}) as home folder using environment variable ({}).", homeFolderEnv, HOME_ENVIRONMENT_VARIABLE);
			return homeFolderEnv;
		}
	}
}
