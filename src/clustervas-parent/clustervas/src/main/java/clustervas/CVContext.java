// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas;

public class CVContext {

	public enum Mode {
		SERVER, // Server mode
		UNIT_TEST, // Unit test mode
	}

	public static Mode mode = Mode.SERVER;

}
