package com.daimler.data.util;

import java.util.ArrayList;
import java.util.List;

public class CommonUtils {

    public static List<String> getRepoNameFromGitUrl(String gitUrl) {
		List<String> repoDetails = new ArrayList<>();
		if(null != gitUrl && !gitUrl.isBlank()) {
			String[] codespaceSplitValues = gitUrl.split("/");
			int length = codespaceSplitValues.length;
			repoDetails.add(codespaceSplitValues[length-2]);
			repoDetails.add(codespaceSplitValues[length-1].replaceAll(".git",""));
		} else {
			repoDetails.add("DNA");
		}
		return repoDetails;
	}
    
}
