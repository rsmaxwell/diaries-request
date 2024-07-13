package com.rsmaxwell.diaries.request.buildinfo;

import com.rsmaxwell.mqtt.rpc.common.buildinfo.AbstractBuildInfo;

public class BuildInfo extends AbstractBuildInfo {

	public BuildInfo() {
		name = "diaries-request";
		version = "$VERSION";
		buildID = "$BUILD_ID";
		builddate = "$TIMESTAMP";
		gitCommit = "$GIT_COMMIT";
		gitBranch = "$GIT_BRANCH";
		gitURL = "$GIT_URL";
	}
}
