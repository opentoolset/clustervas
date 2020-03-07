package clustervas.utils;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Utils {

	public static class TimeOutIndicator implements Supplier<Boolean> {

		private long duration;
		private TimeUnit timeUnit;

		public TimeOutIndicator(long duration, TimeUnit timeUnit) {
			this.duration = duration;
			this.timeUnit = timeUnit;
		}

		private Instant start = Instant.now();

		@Override
		public Boolean get() {
			return Duration.between(start, Instant.now()).toMillis() > timeUnit.toMillis(duration);
		}
	};
}
