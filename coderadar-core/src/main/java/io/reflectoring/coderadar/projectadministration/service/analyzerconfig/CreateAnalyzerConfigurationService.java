package io.reflectoring.coderadar.projectadministration.service.analyzerconfig;

import io.reflectoring.coderadar.analyzer.service.ListAnalyzerService;
import io.reflectoring.coderadar.plugin.api.AnalyzerConfigurationException;
import io.reflectoring.coderadar.projectadministration.AnalyzerNotFoundException;
import io.reflectoring.coderadar.projectadministration.ProjectNotFoundException;
import io.reflectoring.coderadar.projectadministration.domain.AnalyzerConfiguration;
import io.reflectoring.coderadar.projectadministration.port.driven.analyzerconfig.CreateAnalyzerConfigurationPort;
import io.reflectoring.coderadar.projectadministration.port.driver.analyzerconfig.create.CreateAnalyzerConfigurationCommand;
import io.reflectoring.coderadar.projectadministration.port.driver.analyzerconfig.create.CreateAnalyzerConfigurationUseCase;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateAnalyzerConfigurationService implements CreateAnalyzerConfigurationUseCase {

  private final CreateAnalyzerConfigurationPort createAnalyzerConfigurationPort;
  private final ListAnalyzerService listAnalyzerService;
  private final ListAnalyzerConfigurationsFromProjectService
      listAnalyzerConfigurationsFromProjectService;
  private final Logger logger = LoggerFactory.getLogger(CreateAnalyzerConfigurationService.class);

  @Autowired
  public CreateAnalyzerConfigurationService(
      CreateAnalyzerConfigurationPort createAnalyzerConfigurationPort,
      ListAnalyzerService listAnalyzerService,
      ListAnalyzerConfigurationsFromProjectService listAnalyzerConfigurationsFromProjectService) {

    this.createAnalyzerConfigurationPort = createAnalyzerConfigurationPort;
    this.listAnalyzerService = listAnalyzerService;
    this.listAnalyzerConfigurationsFromProjectService =
        listAnalyzerConfigurationsFromProjectService;
  }

  @Override
  public Long create(CreateAnalyzerConfigurationCommand command, Long projectId)
      throws ProjectNotFoundException {
    List<String> analyzers = listAnalyzerService.listAvailableAnalyzers();
    if (analyzers.contains(command.getAnalyzerName())) {
      if (listAnalyzerConfigurationsFromProjectService
          .get(projectId)
          .stream()
          .noneMatch(a -> a.getAnalyzerName().equals(command.getAnalyzerName()))) {
        AnalyzerConfiguration analyzerConfiguration = new AnalyzerConfiguration();
        analyzerConfiguration.setEnabled(command.getEnabled());
        analyzerConfiguration.setAnalyzerName(command.getAnalyzerName());
        Long id = createAnalyzerConfigurationPort.create(analyzerConfiguration, projectId);
        logger.info(
            String.format(
                "Added analyzerConfiguration %s for project with id %d",
                analyzerConfiguration.getAnalyzerName(), projectId));
        return id;
      } else {
        throw new AnalyzerConfigurationException(
            "An analyzer with this name is already configured for the project!");
      }
    } else {
      throw new AnalyzerNotFoundException(
          "Analyzer with name " + command.getAnalyzerName() + " not found");
    }
  }
}
