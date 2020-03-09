// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas;

public class CVContext {

	public enum Mode {
		SERVER, // Server mode
		UNIT_TEST, // Unit test mode
	}

	public static Mode mode = Mode.SERVER;

}
