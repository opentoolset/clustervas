package org.opentoolset.clustervas.demo;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;

@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration()
public class CVDemoOrchestratorApplication {

	public static final Logger logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

	public enum Mode {
		LIVE,
		UNIT_TEST,
	}

	public static Mode mode = Mode.LIVE;

	// ---

	@Autowired
	private ConfigurableEnvironment environment;

	// ---

	public static void main(String[] args) {
		SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(CVDemoOrchestratorApplication.class);
		{
			appBuilder.web(WebApplicationType.NONE);
			appBuilder.logStartupInfo(false);
			appBuilder.run();
		}
	}

	// ---

	@PostConstruct
	private void start() {
		logger.info("ClusterVAS Demo Application is starting...");
		if (CVDemoOrchestratorApplication.mode == Mode.UNIT_TEST) {
			InteractiveShellApplicationRunner.disable(environment);
		}
	}
}
