package org.camunda.optimize.service.importing.provider;

import org.camunda.optimize.service.importing.impl.ActivityImportService;
import org.camunda.optimize.service.importing.impl.PaginatedImportService;
import org.camunda.optimize.service.importing.impl.ProcessDefinitionImportService;
import org.camunda.optimize.service.importing.impl.ProcessDefinitionXmlImportService;
import org.camunda.optimize.service.importing.impl.ProcessInstanceImportService;
import org.camunda.optimize.service.importing.impl.VariableImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class ImportServiceProvider {

  @Autowired
  private ProcessInstanceImportService processInstanceImportService;

  @Autowired
  private VariableImportService variableImportService;

  @Autowired
  private ActivityImportService activityImportService;

  @Autowired
  private ProcessDefinitionImportService processDefinitionImportService;

  @Autowired
  private ProcessDefinitionXmlImportService processDefinitionXmlImportService;

  private List<PaginatedImportService> services;

  @PostConstruct
  public void init() {
    services = new ArrayList<>();
    services.add(processDefinitionImportService);
    services.add(processDefinitionXmlImportService);
    services.add(activityImportService);
  }

  public ProcessInstanceImportService getProcessInstanceImportService() {
    return processInstanceImportService;
  }

  public VariableImportService getVariableImportService() {
    return variableImportService;
  }

  public List<PaginatedImportService> getPagedServices() {
    return services;
  }

}
