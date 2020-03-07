// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.service;

import java.util.function.Supplier;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public interface ContainerService {

	boolean loadTemplateContainer();

	boolean removeTemplateContainer();

	boolean saveClusterVASImage(Supplier<Boolean> stopRequestIndicator);

	CVContainer loadNewNodeContainer();

	boolean removeNodeContainer(String containerName);

	// ---

	enum Status {
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

	class CVContainer {

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
