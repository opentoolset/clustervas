package clustervas;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import clustervas.CVContext.Mode;
import clustervas.utils.CVLogger;

@SpringBootApplication
@EnableScheduling
@EnableAutoConfiguration()
public class CVApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplicationBuilder appBuilder = new SpringApplicationBuilder(CVApplication.class);
		appBuilder.web(WebApplicationType.NONE);
		appBuilder.run(args);
	}

	@Bean
	public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
		CommandLineRunner runner = args -> run(ctx, args);
		return runner;
	}

	@Override
	public void run(String... args) throws Exception {
		if (CVContext.mode == Mode.SERVER) {
			CVLogger.info("run-1");
		}
	}

	// ---

	private void run(ApplicationContext ctx, String[] args) throws IOException, InterruptedException {
		if (CVContext.mode == Mode.SERVER) {
			CVLogger.info("run-2");
		}
	}

	@PostConstruct
	private void start() {
		CVLogger.info("ClusterVAS Manager is starting...");
	}
}
