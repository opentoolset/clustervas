// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Utils {

	/**
	 * Defines a timeout indicator object which its inner chronometer starts when it is initialized with duration parameters. When duration is elapsed, this indicator gives "true" as a return to its method "get".
	 */
	public static class TimeOutIndicator implements Supplier<Boolean> {

		private long duration;
		private TimeUnit timeUnit;

		private Instant start = Instant.now();

		public TimeOutIndicator(long duration, TimeUnit timeUnit) {
			this.duration = duration;
			this.timeUnit = timeUnit;
		}

		@Override
		public Boolean get() {
			return Duration.between(start, Instant.now()).toMillis() > timeUnit.toMillis(duration);
		}
	};
}
