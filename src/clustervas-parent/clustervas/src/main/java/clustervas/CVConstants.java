// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.util.StringUtils;

import clustervas.utils.CVLogger;

public interface CVConstants {

	String HOME_ENVIRONMENT_VARIABLE = "CLUSTERVAS_HOME";
	String HOME_DEFAULT = "/clustervas";
	String HOME_FOLDER = getHomeFolder();
	String CONFIG_FOLDER = "config";
	String CONFIG_FILE = "clustervas.conf";

	Path PATH_OF_CONFIG_FOLDER = Paths.get(HOME_FOLDER, CONFIG_FOLDER);
	Path PATH_OF_CONFIG_FILE = PATH_OF_CONFIG_FOLDER.resolve(CONFIG_FILE);

	String DOCKER_IMAGE_CLUSTERVAS_NAME = "clustervas";
	String DOCKER_IMAGE_CLUSTERVAS_TEMP_NAME = "clustervas-temp";
	String DOCKER_CONTAINER_CLUSTERVAS_NODE_PREFIX = "clustervas-node";
	String DOCKER_CONTAINER_CLUSTERVAS_TEMPLATE_NAME = "clustervas-template";
	String DOCKER_CONTAINER_CLUSTERVAS_DATA_FOLDER = "/clustervas/data";

	String DOCKER_CONTAINER_STATUS_RUNNING = "running";

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
