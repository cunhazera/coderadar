package io.reflectoring.coderadar.rest.query;

import io.reflectoring.coderadar.query.port.driver.GetMetricValuesOfCommitUseCase;
import io.reflectoring.coderadar.query.port.driver.GetMetricsForCommitCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Transactional
public class GetMetricValuesOfCommitController {
  private final GetMetricValuesOfCommitUseCase getMetricValuesOfCommitUseCase;

  @Autowired
  public GetMetricValuesOfCommitController(
      GetMetricValuesOfCommitUseCase getMetricValuesOfCommitUseCase) {
    this.getMetricValuesOfCommitUseCase = getMetricValuesOfCommitUseCase;
  }

  @RequestMapping(method = {RequestMethod.POST, RequestMethod.GET}, path = "/projects/{projectId}/metricvalues/perCommit")
  public ResponseEntity getMetricValues(@Validated @RequestBody GetMetricsForCommitCommand command, @PathVariable("projectId") Long projectId){
    return new ResponseEntity<>(getMetricValuesOfCommitUseCase.get(command, projectId), HttpStatus.OK);
  }
}
