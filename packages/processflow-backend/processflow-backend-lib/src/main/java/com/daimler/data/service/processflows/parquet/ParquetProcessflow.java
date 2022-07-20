/* LICENSE START
 * 
 * MIT License
 * 
 * Copyright (c) 2019 Daimler TSS GmbH
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 * LICENSE END 
 */

package com.daimler.data.service.processflows.parquet;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.daimler.data.dto.ParquetApproveRequestInputDto;
import com.daimler.data.dto.ProcessInstanceResponse;
import com.daimler.data.dto.TaskDetails;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class ParquetProcessflow {

	public static final String TASK_CANDIDATE_GROUP = "data-governors";
    public static final String PROCESS_DEFINITION_KEY = "parquetApprovalRequest";
    
    private RuntimeService runtimeService;
    private TaskService taskService;
    private ProcessEngine processEngine;
    private RepositoryService repositoryService;
    
    public void deployProcessDefinition() {

        Deployment deployment =
                repositoryService
                        .createDeployment()
                        .addClasspathResource("processes/parquetapproval-request.bpmn20.xml")
                        .deploy();
        System.out.println(deployment.getName());
    }
    
    public void checkProcessHistory(String processId) {

        HistoryService historyService = processEngine.getHistoryService();

        List<HistoricActivityInstance> activities =
                historyService
                        .createHistoricActivityInstanceQuery()
                        .processInstanceId(processId)
                        .finished()
                        .orderByHistoricActivityInstanceEndTime()
                        .asc()
                        .list();

        for (HistoricActivityInstance activity : activities) {
            System.out.println(
                    activity.getActivityId() + " took " + activity.getDurationInMillis() + " milliseconds");
        }

        System.out.println("\n \n \n \n");
    }
    
    public ProcessInstanceResponse submitForPublishing(ParquetApproveRequestInputDto parquetRequest) {

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("schemaName", parquetRequest.getSchemaName());
        variables.put("tableName", parquetRequest.getTableName());
        variables.put("filePath", parquetRequest.getParquetFilePath());

        ProcessInstance processInstance =
                runtimeService.startProcessInstanceByKey(PROCESS_DEFINITION_KEY, variables);

        return new ProcessInstanceResponse(processInstance.getId(), processInstance.isEnded());
        
    }
    
    public List<TaskDetails> getDataGovernorsTasks() {
        List<Task> tasks =
                taskService.createTaskQuery().taskCandidateGroup(TASK_CANDIDATE_GROUP).list();
        List<TaskDetails> taskDetails = getTaskDetails(tasks);
        return taskDetails;
    }

    private List<TaskDetails> getTaskDetails(List<Task> tasks) {
        List<TaskDetails> taskDetails = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> processVariables = taskService.getVariables(task.getId());
            taskDetails.add(new TaskDetails(task.getId(), task.getName(), processVariables));
        }
        return taskDetails;
    }
    
    public void approveParquet(String taskId,Boolean approved) {

        Map<String, Object> variables = new HashMap<String, Object>();
        variables.put("approved", approved.booleanValue());
        taskService.complete(taskId, variables);
    }
    
}
