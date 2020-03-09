// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.service;

public class AbstractService {

	private Object lock = new Object();

	public Object getLock() {
		return lock;
	}
}
