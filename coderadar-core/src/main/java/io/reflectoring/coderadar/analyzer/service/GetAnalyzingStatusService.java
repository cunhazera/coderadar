package io.reflectoring.coderadar.analyzer.service;

import io.reflectoring.coderadar.analyzer.port.driven.GetAnalyzingStatusPort;
import io.reflectoring.coderadar.analyzer.port.driver.GetAnalyzingStatusUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetAnalyzingStatusService implements GetAnalyzingStatusUseCase {
  private final GetAnalyzingStatusPort getAnalyzingStatusPort;

  @Autowired
  public GetAnalyzingStatusService(GetAnalyzingStatusPort getAnalyzingStatusPort) {
    this.getAnalyzingStatusPort = getAnalyzingStatusPort;
  }

  @Override
  public Boolean get(Long projectId) {
    return getAnalyzingStatusPort.get(projectId);
  }
}
