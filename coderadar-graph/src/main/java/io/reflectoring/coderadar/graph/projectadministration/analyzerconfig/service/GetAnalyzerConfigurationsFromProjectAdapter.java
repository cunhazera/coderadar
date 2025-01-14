package io.reflectoring.coderadar.graph.projectadministration.analyzerconfig.service;

import io.reflectoring.coderadar.graph.projectadministration.analyzerconfig.AnalyzerConfigurationMapper;
import io.reflectoring.coderadar.graph.projectadministration.analyzerconfig.repository.AnalyzerConfigurationRepository;
import io.reflectoring.coderadar.graph.projectadministration.project.repository.ProjectRepository;
import io.reflectoring.coderadar.projectadministration.ProjectNotFoundException;
import io.reflectoring.coderadar.projectadministration.domain.AnalyzerConfiguration;
import io.reflectoring.coderadar.projectadministration.port.driven.analyzerconfig.GetAnalyzerConfigurationsFromProjectPort;
import java.util.Collection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetAnalyzerConfigurationsFromProjectAdapter
    implements GetAnalyzerConfigurationsFromProjectPort {
  private final ProjectRepository projectRepository;
  private final AnalyzerConfigurationRepository analyzerConfigurationRepository;
  private final AnalyzerConfigurationMapper analyzerConfigurationMapper =
      new AnalyzerConfigurationMapper();

  @Autowired
  public GetAnalyzerConfigurationsFromProjectAdapter(
      ProjectRepository projectRepository,
      AnalyzerConfigurationRepository analyzerConfigurationRepository) {
    this.projectRepository = projectRepository;
    this.analyzerConfigurationRepository = analyzerConfigurationRepository;
  }

  @Override
  public Collection<AnalyzerConfiguration> get(Long projectId) throws ProjectNotFoundException {
    projectRepository
        .findById(projectId)
        .orElseThrow(() -> new ProjectNotFoundException(projectId));
    return analyzerConfigurationMapper.mapNodeEntities(
        analyzerConfigurationRepository.findByProjectId(projectId));
  }
}
