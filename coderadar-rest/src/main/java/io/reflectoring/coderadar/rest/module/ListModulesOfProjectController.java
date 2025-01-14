package io.reflectoring.coderadar.rest.module;

import io.reflectoring.coderadar.projectadministration.port.driver.module.get.ListModulesOfProjectUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Transactional
public class ListModulesOfProjectController {
  private final ListModulesOfProjectUseCase listModulesOfProjectUseCase;

  @Autowired
  public ListModulesOfProjectController(ListModulesOfProjectUseCase listModulesOfProjectUseCase) {
    this.listModulesOfProjectUseCase = listModulesOfProjectUseCase;
  }

  @GetMapping(path = "/projects/{projectId}/modules")
  public ResponseEntity listModules(@PathVariable Long projectId) {
    return new ResponseEntity<>(listModulesOfProjectUseCase.listModules(projectId), HttpStatus.OK);
  }
}
