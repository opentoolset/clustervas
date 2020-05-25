// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.opentoolset.clustervas.CVContext.Mode;
import org.opentoolset.clustervas.utils.CVLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.util.StringUtils;

@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration()
public class CVApplication {

	@Autowired
	private ConfigurableEnvironment environment;

	public static void main(String[] args) {
		CVLogger.info("ClusterVAS is starting...");
		SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(CVApplication.class);
		{
			List<String> disabledCommands = new ArrayList<>();
			disabledCommands.add("--spring.shell.command.quit.enabled=false");
			String[] fullArgs = StringUtils.concatenateStringArrays(args, ArrayUtils.toStringArray(disabledCommands.toArray(), ""));

			appBuilder.web(WebApplicationType.NONE);
			appBuilder.logStartupInfo(false);
			appBuilder.run(fullArgs);
		}
	}

	// ---

	@PostConstruct
	private void start() {
		CVLogger.info("ClusterVAS Manager is starting...");
		if (CVContext.mode == Mode.UNIT_TEST) {
			InteractiveShellApplicationRunner.disable(environment);
		}
	}
}
