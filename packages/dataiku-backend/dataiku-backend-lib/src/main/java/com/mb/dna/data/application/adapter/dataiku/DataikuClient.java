package com.mb.dna.data.application.adapter.dataiku;

import com.mb.dna.data.api.controller.exceptions.MessageDescription;

public interface DataikuClient {

	MessageDescription updateScenario(String projectName);

	MessageDescription runScenario(String projectName);

	MessageDescription deleteProject(String projectName);

	DataikuUserDto getDataikuUser(String loginName);

	MessageDescription addUser(DataikuUserDto user);

	MessageDescription updateUser(DataikuUserDto user);

}
