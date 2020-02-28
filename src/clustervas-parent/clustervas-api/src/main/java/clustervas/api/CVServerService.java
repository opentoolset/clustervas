// ---
// Copyright 2020 ClusterVAS Team
// All rights reserved
// ---
package clustervas.api;

import clustervas.api.messages.SampleRequest;
import clustervas.api.messages.SampleResponse;

public interface CVServerService {

	SampleResponse getSampleResponse(SampleRequest request);
}
