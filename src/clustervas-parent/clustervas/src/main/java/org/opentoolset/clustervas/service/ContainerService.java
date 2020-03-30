// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.opentoolset.clustervas.CVConfig;
import org.opentoolset.clustervas.CVConstants;
import org.opentoolset.clustervas.sdk.messages.cv.GetActiveNodesRequest;
import org.opentoolset.clustervas.sdk.messages.cv.GetActiveNodesResponse;
import org.opentoolset.clustervas.utils.CVLogger;
import org.opentoolset.clustervas.utils.CmdExecutor.Response;
import org.opentoolset.clustervas.utils.ContainerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ListContainersCmd;
import com.github.dockerjava.api.command.ListImagesCmd;
import com.github.dockerjava.api.command.StartContainerCmd;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Image;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientBuilder;
import com.google.common.collect.Lists;

@Service
public class ContainerService extends AbstractService {

	private static final String CMD_INTERNAL_NVT_SYNC = "greenbone-nvt-sync";

	@Autowired
	private CVNodeManager cvAgent;

	private DockerClient dockerClient;

	private boolean nodeImageChangeRequired = false;

	private Object lockForTempImage = new Object();

	private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

	// ---

	public boolean loadTemplateContainerIfNeeded() {
		synchronized (getLock()) {
			if (!checkDockerImageClusterVASLoaded()) {
				return false;
			}

			if (isContainerRunning(CVConstants.DOCKER_CONTAINER_CLUSTERVAS_TEMPLATE_NAME)) {
				return true;
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

	public boolean removeTemplateContainer() {
		return removeContainer(CVConstants.DOCKER_CONTAINER_CLUSTERVAS_TEMPLATE_NAME);
	}

	public boolean doInternalNVTSync() {
		if (!CVConfig.isTemplateInternalSyncEnabled()) {
			// TODO [hadi] Inform about illegal state for doing this operation
			return false;
		}

		if (!loadTemplateContainerIfNeeded()) {
			return false;
		}

		if (!ContainerUtils.waitUntilGvmdIsReady(CVConstants.DOCKER_CONTAINER_CLUSTERVAS_TEMPLATE_NAME, () -> false)) {
			return false;
		}

		Response response = ContainerUtils.dockerExec(CVConstants.DOCKER_CONTAINER_CLUSTERVAS_TEMPLATE_NAME, CMD_INTERNAL_NVT_SYNC, 1200);
		if (!response.isSuccessful()) {
			return false;
		}

		return doPostSyncOperations();
	}

	public boolean doPostSyncOperations() {
		if (!loadTemplateContainerIfNeeded()) {
			return false;
		}

		if (!ContainerUtils.waitUntilGvmdIsReady(CVConstants.DOCKER_CONTAINER_CLUSTERVAS_TEMPLATE_NAME, () -> false)) {
			return false;
		}

		return saveClusterVASImage(() -> false);
	}

	public boolean saveClusterVASImage(Supplier<Boolean> stopRequestIndicator) {
		String templateContainerName = CVConstants.DOCKER_CONTAINER_CLUSTERVAS_TEMPLATE_NAME;
		Container container = getContainerByName(templateContainerName);
		if (container == null) {
			return false;
		}

		if (!ContainerUtils.waitUntilGvmdIsReady(templateContainerName, stopRequestIndicator)) {
			return false;
		}

		synchronized (lockForTempImage) {
			removeImage(CVConstants.DOCKER_IMAGE_CLUSTERVAS_TEMP_NAME, true);

			if (!ContainerUtils.commitDockerImage(templateContainerName, CVConstants.DOCKER_IMAGE_CLUSTERVAS_TEMP_NAME)) {
				return false;
			}

			this.nodeImageChangeRequired = true;
			maintaintenance();
		}

		return true;
	}

	public boolean removeImage(String imageName, boolean force) {
		try {
			Image tempImage = getImageByName(imageName);
			if (tempImage != null) {
				this.dockerClient.removeImageCmd(tempImage.getId()).withForce(force).exec();
			}
			return true;
		} catch (Exception e) {
			CVLogger.warn(e);
		}
		return false;
	}

	public CVContainer loadNewNodeContainer() {
		synchronized (getLock()) {
			if (!checkDockerImageClusterVASNodeLoaded()) {
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

	public boolean removeNodeContainer(String containerName) {
		return removeContainer(containerName);
	}

	public boolean isContainerRunning(String containerName) {
		Container container = getContainerIfRunning(containerName);
		return container != null;
	}

	// ---

	@PostConstruct
	private void postConstruct() {
		DefaultDockerClientConfig.Builder config = DefaultDockerClientConfig.createDefaultConfigBuilder();
		this.dockerClient = DockerClientBuilder.getInstance(config).build();

		this.scheduledExecutor.scheduleWithFixedDelay(() -> maintaintenance(), 0, 10, TimeUnit.SECONDS);
	}

	private void maintaintenance() {
		try {
			GetActiveNodesResponse response = this.cvAgent.doRequest(new GetActiveNodesRequest());
			if (response.isSuccessfull()) {
				List<String> nodeNames = response.getNodeNames();
				// TODO [hadi] Remove inactive node containers here
			}
		} catch (Exception e) {
			CVLogger.debug(e, "Active nodes couln't be gathered");
		}

		synchronized (lockForTempImage) {
			if (this.nodeImageChangeRequired) {
				this.nodeImageChangeRequired = !ContainerUtils.renameDockerImage(CVConstants.DOCKER_IMAGE_CLUSTERVAS_TEMP_NAME, CVConstants.DOCKER_IMAGE_CLUSTERVAS_NODE_NAME);
			}
		}

		removeNotRunningContainers();
		removeUnnecessaryImages();
	}

	private void removeNotRunningContainers() {
		ListContainersCmd cmd = this.dockerClient.listContainersCmd();
		List<String> statuses = new ArrayList<>();
		statuses.add(Status.DEAD.getName());
		statuses.add(Status.EXITED.getName());
		statuses.add(Status.PAUSED.getName());

		cmd = cmd.withStatusFilter(statuses);
		List<Container> containers = cmd.exec();

		for (Container container : containers) {
			this.dockerClient.removeContainerCmd(container.getId()).withForce(true).exec();
		}
	}

	private void removeUnnecessaryImages() {
		ListImagesCmd cmd = this.dockerClient.listImagesCmd();
		cmd = cmd.withDanglingFilter(true);
		List<Image> images = cmd.exec();

		for (Image image : images) {
			this.dockerClient.removeImageCmd(image.getId()).exec();
		}
	}

	private boolean checkDockerImageClusterVASLoaded() {
		Image image = getImageByName(CVConstants.DOCKER_IMAGE_CLUSTERVAS_NAME);
		if (image == null) {
			CVLogger.error("Docker image has not been loaded");
			return false;
		}

		return true;
	}

	private boolean checkDockerImageClusterVASNodeLoaded() {
		Image image = getImageByName(CVConstants.DOCKER_IMAGE_CLUSTERVAS_NODE_NAME);
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
		List<String> allStatuses = Arrays.asList(Status.values()).stream().map(status -> status.getName()).collect(Collectors.toList());
		List<Container> containers = this.dockerClient.listContainersCmd().withStatusFilter(allStatuses).withNameFilter(Arrays.asList(containerName)).exec();
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

	// ---

	public enum Status {
		CREATED("created"),
		RESTARTING("restarting"),
		RUNNING("running"),
		REMOVING("removing"),
		PAUSED("paused"),
		EXITED("exited"),
		DEAD("dead");

		private String name;

		private Status(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public static class CVContainer {

		private String name;
		private String id;

		public CVContainer(String name, String id) {
			this.name = name;
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public String getId() {
			return id;
		}

		@Override
		public String toString() {
			return ReflectionToStringBuilder.toString(this, ToStringStyle.DEFAULT_STYLE);
		}
	}
}
