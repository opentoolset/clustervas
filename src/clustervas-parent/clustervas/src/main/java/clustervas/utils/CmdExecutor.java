// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;

public class CmdExecutor {

	private static final int DEFAULT_TIMEOUT_SEC = 30;

	public static Response exec(String cmd) {
		return exec(cmd, DEFAULT_TIMEOUT_SEC);
	}

	public static Response exec(String cmd, long timeoutSec) {
		return exec(cmd, new HashMap<>(), timeoutSec);
	}

	public static Response exec(String cmd, Map<String, String> envMap, long timeoutSec) {
		Response response = new Response();
		response.setExitStatus(Response.FAILURE);
		try {
			Process process = start(cmd, envMap);
			if (process.waitFor(timeoutSec, TimeUnit.SECONDS)) {
				int exitValue = process.exitValue();
				response.setExitStatus(exitValue);

				String output = IOUtils.toString(exitValue == Response.SUCCESS ? process.getInputStream() : process.getErrorStream(), Charset.defaultCharset());
				response.setOutput(output);

				Logger.debug("Execution finished. Exit value: %s", exitValue);
			} else {
				Logger.debug("Timeout was occured");
			}

			if (process.isAlive()) {
				process.destroyForcibly();
			}
		} catch (InterruptedException e) {
			Logger.debug(e, "Execution was interrupted");
		} catch (Exception e) {
			Logger.error(e);
		}

		return response;
	}

	// ---

	private static Process start(String cmd, Map<String, String> envMap) throws IOException {
		ProcessBuilder builder = new ProcessBuilder(new String[] { "/bin/bash", "-c", cmd });
		builder.environment().putAll(envMap);
		Process process = builder.start();
		return process;
	}

	// ---

	public static class Response {

		private static final int SUCCESS = 0;
		private static final int FAILURE = -1;

		private int exitStatus = SUCCESS;
		private String output;

		public Response() {
			super();
		}

		public Response(String output) {
			this.output = output;
		}

		public boolean isSuccessful() {
			return exitStatus == SUCCESS ? true : false;
		}

		public Response setSucceeded() {
			this.exitStatus = SUCCESS;
			return this;
		}

		public Response setFailed() {
			this.exitStatus = FAILURE;
			return this;
		}

		public int getExitStatus() {
			return exitStatus;
		}

		public Response setExitStatus(int exitStatus) {
			this.exitStatus = exitStatus;
			return this;
		}

		public String getOutput() {
			return output;
		}

		public Response setOutput(String output) {
			this.output = output;
			return this;
		}

		@Override
		public String toString() {
			return String.format("status:%s, output:\n%s", exitStatus, output);
		}
	}
}
