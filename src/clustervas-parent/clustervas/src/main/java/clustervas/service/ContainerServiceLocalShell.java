package clustervas.service;

import org.springframework.stereotype.Service;

import clustervas.utils.Logger;
import clustervas.utils.CmdExecutor;
import clustervas.utils.CmdExecutor.Response;

@Service
public class ContainerServiceLocalShell extends AbstractService {

	private static final String CMD_DOCKER_EXEC_PREFIX_TEMPLATE = "docker exec %s bash -c";
	private static final String CMD_DOCKER_COMMIT_TEMPLATE = "docker commit %s %s";
	private static final String CMD_DOCKER_TAG_TEMPLATE = "docker tag %s %s";
	private static final String CMD_DOCKER_RMI_TEMPLATE = "docker rmi -f %s";
	private static final String CMD_GVMD_PROCESS_INFO = "'ps $(pidof gvmd) | grep gvmd'";

	public Response dockerExec(String containerName, String cmd) {
		String dockerExecPrefix = String.format(CMD_DOCKER_EXEC_PREFIX_TEMPLATE, containerName);
		String wrapperCmd = String.format("%s %s", dockerExecPrefix, cmd);

		Response response = CmdExecutor.exec(wrapperCmd);
		return response;
	}

	public String getGvmdProcessInfo(String containerName) {
		Response response = dockerExec(containerName, CMD_GVMD_PROCESS_INFO);
		if (!response.isSuccessful()) {
			Logger.warn(response.getOutput());
		}

		return response.isSuccessful() ? response.getOutput() : null;
	}

	/**
	 * Checks if the gvmd is ready (ie. in waiting state). <br />
	 * <br />
	 * 
	 * Note: gvmd process shows "Waiting for incoming connections" in waiting state. There may be more than one gvmd process line in the ps output, so we need to check all the lines in a loop.
	 * 
	 * @param containerName
	 * @return true if there is no gvmd processes in operation (ie. not waiting).
	 */
	public boolean checkIfGvmdIsReady(String containerName) {
		String processInfo = getGvmdProcessInfo(containerName);
		if (processInfo == null) {
			return false;
		}

		String[] processInfoLines = processInfo.split("\n");
		for (String processInfoLine : processInfoLines) {
			if (!processInfoLine.toLowerCase().contains("waiting")) {
				return false;
			}
		}
		return true;
	}

	public boolean commitDockerImage(String containerName, String targetImageName) {
		String cmd = String.format(CMD_DOCKER_COMMIT_TEMPLATE, containerName, targetImageName);
		Response response = CmdExecutor.exec(cmd);
		if (!response.isSuccessful()) {
			Logger.warn(response.getOutput());
		}

		return response.isSuccessful();
	}

	public boolean tagDockerImage(String imageName, String newTag) {
		String cmd = String.format(CMD_DOCKER_TAG_TEMPLATE, imageName, newTag);
		Response response = CmdExecutor.exec(cmd);
		if (!response.isSuccessful()) {
			Logger.warn(response.getOutput());
		}

		return response.isSuccessful();
	}

	public boolean removeDockerImage(String imageName) {
		String cmd = String.format(CMD_DOCKER_RMI_TEMPLATE, imageName);
		Response response = CmdExecutor.exec(cmd);
		if (!response.isSuccessful()) {
			Logger.warn(response.getOutput());
		}

		return response.isSuccessful();
	}

	public boolean renameDockerImage(String oldName, String newName) {
		boolean result;
		result = tagDockerImage(oldName, newName);
		result = result & removeDockerImage(oldName);
		return result;
	}
}
