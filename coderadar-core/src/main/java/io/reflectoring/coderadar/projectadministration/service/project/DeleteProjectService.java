package io.reflectoring.coderadar.projectadministration.service.project;

import io.reflectoring.coderadar.projectadministration.ProjectIsBeingProcessedException;
import io.reflectoring.coderadar.projectadministration.ProjectNotFoundException;
import io.reflectoring.coderadar.projectadministration.domain.Project;
import io.reflectoring.coderadar.projectadministration.port.driven.project.DeleteProjectPort;
import io.reflectoring.coderadar.projectadministration.port.driven.project.GetProjectPort;
import io.reflectoring.coderadar.projectadministration.port.driver.project.delete.DeleteProjectUseCase;
import io.reflectoring.coderadar.projectadministration.service.ProcessProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteProjectService implements DeleteProjectUseCase {

  private final DeleteProjectPort deleteProjectPort;
  private final ProcessProjectService processProjectService;
  private final GetProjectPort getProjectPort;

  private final Logger logger = LoggerFactory.getLogger(DeleteProjectService.class);

  @Autowired
  public DeleteProjectService(
      DeleteProjectPort deleteProjectPort,
      ProcessProjectService processProjectService,
      GetProjectPort getProjectPort) {
    this.deleteProjectPort = deleteProjectPort;
    this.processProjectService = processProjectService;
    this.getProjectPort = getProjectPort;
  }

  @Override
  public void delete(Long id) throws ProjectNotFoundException, ProjectIsBeingProcessedException {
    Project project = getProjectPort.get(id);
    processProjectService.executeTask(
        () -> {
          deleteProjectPort.delete(id);
          logger.info(String.format("Deleted project %s with id %d", project.getName(), id));
        },
        id);
  }
}
