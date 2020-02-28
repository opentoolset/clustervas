// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api.netty;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface CVApiConstants {

	String DEFAULT_MANAGER_HOST = "127.0.0.1";
	int DEFAULT_MANAGER_PORT = 4444;
	Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	long REQUST_TIMEOUT_MILLIS = 30 * 1000;
}
