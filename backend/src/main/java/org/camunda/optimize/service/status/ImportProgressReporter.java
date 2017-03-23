package org.camunda.optimize.service.status;

import org.camunda.optimize.service.importing.EngineEntityFetcher;
import org.camunda.optimize.service.importing.ImportService;
import org.camunda.optimize.service.importing.ImportServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImportProgressReporter {

  @Autowired
  private EngineEntityFetcher engineEntityFetcher;

  @Autowired
  private ImportServiceProvider importServiceProvider;

  public boolean allEntitiesAreImported() {
    return computeImportProgress() == 100;
  }

  /**
   * @return an integer representing the progress of the import. The number states a
   * percentage value in range [0, 100] rounded to next whole number.
   */
  public int computeImportProgress() {
    int totalEngineEntityCount =
      engineEntityFetcher.fetchHistoricActivityInstanceCount() +
        2 * engineEntityFetcher.fetchProcessDefinitionCount();
    double alreadyImportedCount = getAlreadyImportedCount();
    if (totalEngineEntityCount > 0) {
      int tempResult = (int) (Math.floor(alreadyImportedCount / totalEngineEntityCount * 100));
      return Math.min(tempResult, 100);
    } else {
      return 0;
    }
  }

  private double getAlreadyImportedCount() {
    double alreadyImportedCount = 0;
    for (ImportService importService : importServiceProvider.getServices()) {
      alreadyImportedCount += importService.getImportStartIndex();
    }
    return alreadyImportedCount;
  }
}
