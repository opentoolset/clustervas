// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package org.opentoolset.clustervas.sdk;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opentoolset.nettyagents.PeerContext;

public class NodeManagerContext {

	private SocketAddress socketAddress;

	private Date connectionStartTime;

	private PeerContext peerContext;

	private List<String> managedNodes = new ArrayList<>();

	public SocketAddress getSocketAddress() {
		return socketAddress;
	}

	public Date getConnectionStartTime() {
		return connectionStartTime;
	}

	public PeerContext getPeerContext() {
		return peerContext;
	}

	public List<String> getManagedNodes() {
		return managedNodes;
	}

	// ---

	public void setSocketAddress(SocketAddress socketAddress) {
		this.socketAddress = socketAddress;
	}

	public void setPeerContext(PeerContext peerContext) {
		this.peerContext = peerContext;
	}
}
