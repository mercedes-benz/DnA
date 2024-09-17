package com.daimler.data.util;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommonUtils {

	public static List<String> getRepoNameFromGitUrl(String gitUrl) {
		List<String> repoDetails = new ArrayList<>();
		try {
			if (null != gitUrl && !gitUrl.isBlank()) {
				String[] codespaceSplitValues = gitUrl.split("/");
				int length = codespaceSplitValues.length;
				repoDetails.add(codespaceSplitValues[length - 2]);
				repoDetails.add(codespaceSplitValues[length - 1].replaceAll(".git", ""));
			} else {
				repoDetails.add("DNA");
			}
		} catch (Exception e) {
			log.error("Exception occured in utils",e);
		}
		return repoDetails;
	}
}
