package com.mb.dna.data.application.adapter.dataiku;

import com.mb.dna.data.api.controller.exceptions.MessageDescription;

public interface DataikuClient {

	MessageDescription updateScenario(String projectName, String cloudProfile);

	MessageDescription runScenario(String projectName, String cloudProfile);

	MessageDescription deleteProject(String projectName, String cloudProfile);

	DataikuUserDto getDataikuUser(String loginName, String cloudProfile);

	MessageDescription addUser(DataikuUserDto user, String cloudProfile);

	MessageDescription updateUser(DataikuUserDto user, String cloudProfile);

}
