package io.reflectoring.coderadar.rest.analyzing;

import io.reflectoring.coderadar.analyzer.port.driver.ListAnalyzerUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Transactional
public class ListAnalyzerController {
  private final ListAnalyzerUseCase listAnalyzerUseCase;

  @Autowired
  public ListAnalyzerController(ListAnalyzerUseCase listAnalyzerUseCase) {
    this.listAnalyzerUseCase = listAnalyzerUseCase;
  }

  @GetMapping(path = "/analyzers")
  public ResponseEntity listAvailableAnalyzers() {
    return new ResponseEntity<>(listAnalyzerUseCase.listAvailableAnalyzers(), HttpStatus.OK);
  }
}
