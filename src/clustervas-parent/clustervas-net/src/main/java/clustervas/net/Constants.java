// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.net;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface Constants {

	Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	long DEFAULT_REQUEST_TIMEOUT_MILLIS = 10 * 1000;
	String DEFAULT_SERVER_HOST = "127.0.0.1";
	int DEFAULT_SERVER_PORT = 4444;
	String DEFAULT_GVM_DEFAULT_USER = "admin";
	String DEFAULT_GVM_DEFAULT_PASSWORD = "admin";
}
