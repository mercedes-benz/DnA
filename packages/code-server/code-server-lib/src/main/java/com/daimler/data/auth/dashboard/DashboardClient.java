package com.daimler.data.auth.dashboard;

import com.daimler.data.controller.exceptions.GenericMessage;

public interface DashboardClient {

    public GenericMessage createDepartment(String value);

    public GenericMessage getDepartment(String departmentName);

}
