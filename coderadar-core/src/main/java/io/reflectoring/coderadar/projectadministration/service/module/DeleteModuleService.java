package io.reflectoring.coderadar.projectadministration.service.module;

import io.reflectoring.coderadar.projectadministration.ModuleNotFoundException;
import io.reflectoring.coderadar.projectadministration.ProjectIsBeingProcessedException;
import io.reflectoring.coderadar.projectadministration.domain.Module;
import io.reflectoring.coderadar.projectadministration.port.driven.module.DeleteModulePort;
import io.reflectoring.coderadar.projectadministration.port.driven.module.GetModulePort;
import io.reflectoring.coderadar.projectadministration.port.driver.module.delete.DeleteModuleUseCase;
import io.reflectoring.coderadar.projectadministration.service.ProcessProjectService;
import io.reflectoring.coderadar.projectadministration.service.project.DeleteProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeleteModuleService implements DeleteModuleUseCase {

  private final DeleteModulePort deleteModulePort;
  private final ProcessProjectService processProjectService;
  private final GetModulePort getModulePort;
  private final Logger logger = LoggerFactory.getLogger(DeleteProjectService.class);

  @Autowired
  public DeleteModuleService(
      DeleteModulePort deleteModulePort,
      ProcessProjectService processProjectService,
      GetModulePort getModulePort) {
    this.deleteModulePort = deleteModulePort;
    this.processProjectService = processProjectService;
    this.getModulePort = getModulePort;
  }

  @Override
  public void delete(Long id, Long projectId)
      throws ModuleNotFoundException, ProjectIsBeingProcessedException {
    Module module = getModulePort.get(id);
    processProjectService.executeTask(
        () -> {
          deleteModulePort.delete(id, projectId);
          logger.info(
              String.format(
                  "Deleted module %s for project with id %d", module.getPath(), projectId));
        },
        projectId);
  }
}
