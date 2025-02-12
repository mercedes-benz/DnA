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

	public static List<String> getDetailsFromUrl(String url){
		List<String> details = new ArrayList<>();
		try{
			String[] codespaceSplitValues = url.split("/");
			int length = codespaceSplitValues.length;
			String repoName = codespaceSplitValues[length - 1];
			String repoOwner = codespaceSplitValues[length - 2];
			String gitUrl = url.replace("/" + repoOwner, "");
			gitUrl = gitUrl.replace("/" + repoName, "");
			details.add(gitUrl);
			details.add(repoOwner);
			details.add(repoName);
		}catch(Exception e){
			log.error("Exception occured in utils",e);
		}
		return details;

	}
}
