// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.opentoolset.clustervas.CVContext.Mode;
import org.opentoolset.clustervas.utils.CVLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.util.StringUtils;

@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration()
public class CVApplication implements CommandLineRunner {

	@Autowired
	private ConfigurableEnvironment environment;

	public static void main(String[] args) {
		SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(CVApplication.class);
		{
			String[] disabledCommands = { "--spring.shell.command.quit.enabled=false" };
			String[] fullArgs = StringUtils.concatenateStringArrays(args, disabledCommands);

			appBuilder.web(WebApplicationType.NONE);
			appBuilder.logStartupInfo(false);
			appBuilder.run(fullArgs);
		}
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		CommandLineRunner runner = args -> run(ctx, args);
		return runner;
	}

	@Override
	public void run(String... args) throws Exception {
		if (CVContext.mode == Mode.LIVE) {
			CVLogger.info("run-1");
		}
	}

	// ---

	private void run(ApplicationContext ctx, String[] args) throws IOException, InterruptedException {
		if (CVContext.mode == Mode.LIVE) {
			CVLogger.info("run-2");
		}
	}

	@PostConstruct
	private void start() {
		CVLogger.info("ClusterVAS Manager is starting...");
		if (CVContext.mode == Mode.UNIT_TEST) {
			InteractiveShellApplicationRunner.disable(environment);
		}
	}
}
