// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.service;

public class AbstractService {

	private Object lock = new Object();

	public Object getLock() {
		return lock;
	}
}
