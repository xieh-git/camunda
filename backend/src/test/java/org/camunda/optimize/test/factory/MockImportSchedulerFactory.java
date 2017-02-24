package org.camunda.optimize.test.factory;

import org.camunda.optimize.service.importing.ImportJobExecutor;
import org.camunda.optimize.service.importing.ImportScheduleJob;
import org.camunda.optimize.service.importing.ImportScheduler;
import org.camunda.optimize.service.importing.ImportService;
import org.camunda.optimize.service.importing.ImportServiceProvider;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Import scheduler factory used in integration tests in order to allow
 * a fully synchronized import from start to end (new entries in elasticsearch).
 *
 * @author Johannes Heinemann
 */
public class MockImportSchedulerFactory implements FactoryBean<ImportScheduler> {

  @Autowired
  private ImportJobExecutor importJobExecutor;

  @Autowired
  private ImportServiceProvider importServiceProvider;

  @Override
  public ImportScheduler getObject() throws Exception {
    ImportScheduler importScheduler = mock(ImportScheduler.class);

    // execute job import immediately when triggered
    Answer<Void> answer = invocationOnMock -> {
      importJobExecutor.startExecutingImportJobs();
      for (ImportService importService : importServiceProvider.getServices()) {
        ImportScheduleJob job = new ImportScheduleJob();
        job.setImportService(importService);
        job.execute();
      }
      importJobExecutor.stopExecutingImportJobs();
      return null;
    };
    doAnswer(answer).when(importScheduler).scheduleProcessEngineImport();

    return importScheduler;
  }

  @Override
  public Class<?> getObjectType() {
    return ImportScheduler.class;
  }

  @Override
  public boolean isSingleton() {
    return true;
  }
}
