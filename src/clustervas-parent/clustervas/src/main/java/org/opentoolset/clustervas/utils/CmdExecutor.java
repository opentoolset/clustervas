// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;

public class CmdExecutor {

	public static final int DEFAULT_TIMEOUT_SEC = 30;

	private static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;

	public static Response exec(String cmd) {
		return exec(cmd, DEFAULT_TIMEOUT_SEC);
	}

	public static Response exec(String cmd, long timeoutSec) {
		return exec(cmd, new HashMap<>(), timeoutSec);
	}

	public static Response exec(String cmd, Map<String, String> envMap, long timeoutSec) {
		return execAndWrite(cmd, envMap, null, timeoutSec);
	}

	public static Response execAndWrite(String cmd, String dataToStdin) {
		return execAndWrite(cmd, dataToStdin, DEFAULT_TIMEOUT_SEC);
	}

	public static Response execAndWrite(String cmd, String dataToStdin, long timeoutSec) {
		return execAndWrite(cmd, new HashMap<>(), dataToStdin, timeoutSec);
	}

	public static Response execAndWrite(String cmd, Map<String, String> envMap, String dataToStdin, long timeoutSec) {
		Response response = new Response();
		response.setExitStatus(Response.FAILURE);
		try {
			Process process = start(cmd, envMap);
			IOUtils.write(dataToStdin, process.getOutputStream(), DEFAULT_CHARSET);
			process.getOutputStream().close();
			if (process.waitFor(timeoutSec, TimeUnit.SECONDS)) {
				int exitValue = process.exitValue();
				response.setExitStatus(exitValue);

				String output = IOUtils.toString(exitValue == Response.SUCCESS ? process.getInputStream() : process.getErrorStream(), Charset.defaultCharset());
				response.setOutput(output);

				CVLogger.debug("Execution finished. Exit value: %s", exitValue);
			} else {
				CVLogger.debug("Timeout was occured");
			}

			if (process.isAlive()) {
				process.destroyForcibly();
			}
		} catch (InterruptedException e) {
			CVLogger.debug(e, "Execution was interrupted");
		} catch (Exception e) {
			CVLogger.error(e);
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
