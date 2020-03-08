package clustervas.net;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Utils {

	public static boolean waitUntil(Supplier<Boolean> tester, int timeoutSec) {
		try {
			for (int i = 0; i < timeoutSec; i++) {
				if (tester.get()) {
					break;
				}
				TimeUnit.SECONDS.sleep(1);
			}
		} catch (InterruptedException e) {
			Context.getLogger().warn(e.getLocalizedMessage(), e);
		}

		return tester.get();
	}
}
