// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.service;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.google.common.collect.Lists;

import clustervas.CVConfig;
import clustervas.CVConstants;
import clustervas.utils.CVLogger;

@Service
public class ContainerServiceWithDockerJava extends AbstractService implements ContainerService {

	@Autowired
	private ContainerServiceLocalShell containerServiceLocalShell;

	private DockerClient dockerClient;

	// ---

	@Override
	public boolean loadTemplateContainer() {
		synchronized (getLock()) {
			if (!checkDockerImageClusterVASLoaded()) {
				return false;
			}

			if (!removeTemplateContainer()) {
				return false;
			}

			boolean dataFolderIsReadonly = !CVConfig.isTemplateInternalSyncEnabled();
			Container container = runClusterVASContainer(CVConstants.DOCKER_CONTAINER_CLUSTERVAS_TEMPLATE_NAME, dataFolderIsReadonly);
			if (container == null) {
				return false;
			}

			return true;
		}
	}

	@Override
	public boolean removeTemplateContainer() {
		return removeContainer(CVConstants.DOCKER_CONTAINER_CLUSTERVAS_TEMPLATE_NAME);
	}

	@Override
	public boolean saveClusterVASImage(Supplier<Boolean> stopRequestIndicator) {
		String templateContainerName = CVConstants.DOCKER_CONTAINER_CLUSTERVAS_TEMPLATE_NAME;
		Container container = getContainerByName(templateContainerName);
		if (container == null) {
			return false;
		}

		if (!this.containerServiceLocalShell.waitUntilGvmdIsReady(templateContainerName, stopRequestIndicator)) {
			return false;
		}

		if (!this.containerServiceLocalShell.commitDockerImage(templateContainerName, CVConstants.DOCKER_IMAGE_CLUSTERVAS_TEMP_NAME)) {
			return false;
		}

		removeTemplateContainer();

		if (!this.containerServiceLocalShell.renameDockerImage(CVConstants.DOCKER_IMAGE_CLUSTERVAS_TEMP_NAME, CVConstants.DOCKER_IMAGE_CLUSTERVAS_NAME)) {
			return false;
		}

		loadTemplateContainer();

		return true;
	}

	@Override
	public CVContainer loadNewNodeContainer() {
		synchronized (getLock()) {
			if (!checkDockerImageClusterVASLoaded()) {
				return null;
			}

			String nodeName = String.format("%s-%s", CVConstants.DOCKER_CONTAINER_CLUSTERVAS_NODE_PREFIX, UUID.randomUUID().toString());
			Container container = runClusterVASContainer(nodeName, true);
			if (container == null) {
				return null;
			}

			return new CVContainer(nodeName, container.getId());
		}
	}

	@Override
	public boolean removeNodeContainer(String containerName) {
		return removeContainer(containerName);
	}

	@Override
	public boolean isContainerRunning(String containerName) {
		Container container = getContainerIfRunning(containerName);
		return container != null;
	}

	// ---

	@PostConstruct
	private void postConstruct() {
		DefaultDockerClientConfig.Builder config = DefaultDockerClientConfig.createDefaultConfigBuilder();
		this.dockerClient = DockerClientBuilder.getInstance(config).build();
	}

	private boolean checkDockerImageClusterVASLoaded() {
		String imageName = CVConstants.DOCKER_IMAGE_CLUSTERVAS_NAME;
		Image image = getImageByName(imageName);
		if (image == null) {
			CVLogger.error("Docker image has not been loaded");
			return false;
		}

		return true;
	}

	private Container runClusterVASContainer(String containerName, boolean dataFolderIsReadonly) {
		CreateContainerCmd createContainerCmd = this.dockerClient.createContainerCmd(CVConstants.DOCKER_IMAGE_CLUSTERVAS_NAME);
		createContainerCmd = createContainerCmd.withAttachStdin(true);
		createContainerCmd = createContainerCmd.withAttachStdout(true);
		createContainerCmd = createContainerCmd.withAttachStderr(true);
		createContainerCmd = createContainerCmd.withStdinOpen(true);
		createContainerCmd = createContainerCmd.withTty(true);
		createContainerCmd = createContainerCmd.withName(containerName);

		String bindStatement = String.format("%s:%s", CVConfig.getHostDataFolder(), CVConstants.DOCKER_CONTAINER_CLUSTERVAS_DATA_FOLDER);
		if (dataFolderIsReadonly) {
			bindStatement += ":ro";
		}

		Bind bind = Bind.parse(bindStatement);
		HostConfig hostConfig = createContainerCmd.getHostConfig().withBinds(bind);
		createContainerCmd = createContainerCmd.withHostConfig(hostConfig);

		CreateContainerResponse createContainerResponse = createContainerCmd.exec();
		String id = createContainerResponse.getId();
		if (id == null) {
			return null;
		}

		StartContainerCmd startContainerCmd = this.dockerClient.startContainerCmd(id);
		startContainerCmd.exec();

		Container container = getContainerByName(containerName);

		// TODO [hadi] Wait until reaching a running container state if needed

		return container;
	}

	private Image getImageByName(String imageName) {
		List<Image> images = this.dockerClient.listImagesCmd().exec();
		Image result = images.stream().filter(image -> Lists.newArrayList(image.getRepoTags()).stream().anyMatch(tag -> tag.contains(imageName))).findFirst().orElse(null);
		return result;
	}

	private Container getContainerByName(String containerName) {
		List<Container> containers = this.dockerClient.listContainersCmd().withNameFilter(Arrays.asList(containerName)).exec();
		Container result = containers.stream().findFirst().orElse(null);
		return result;
	}

	private Container getContainerIfRunning(String containerName) {
		ListContainersCmd cmd = this.dockerClient.listContainersCmd();
		cmd = cmd.withNameFilter(Arrays.asList(containerName));
		cmd = cmd.withStatusFilter(Arrays.asList(Status.RUNNING.getName()));
		List<Container> containers = cmd.exec();
		Container container = containers.stream().findFirst().orElse(null);
		return container;
	}

	private boolean removeContainer(String containerName) {
		synchronized (getLock()) {
			try {
				Container container = getContainerByName(containerName);
				if (container != null) {
					this.dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
				}

				return true;
			} catch (Exception e) {
				CVLogger.error(e);
				return false;
			}
		}
	}
}
