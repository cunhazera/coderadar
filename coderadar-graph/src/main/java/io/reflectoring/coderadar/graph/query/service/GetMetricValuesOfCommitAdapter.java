package io.reflectoring.coderadar.graph.query.service;

import io.reflectoring.coderadar.graph.analyzer.domain.CommitEntity;
import io.reflectoring.coderadar.graph.analyzer.repository.CommitRepository;
import io.reflectoring.coderadar.graph.projectadministration.domain.MetricValueForCommitQueryResult;
import io.reflectoring.coderadar.graph.query.repository.GetMetricValuesOfCommitRepository;
import io.reflectoring.coderadar.projectadministration.CommitNotFoundException;
import io.reflectoring.coderadar.query.domain.MetricValueForCommit;
import io.reflectoring.coderadar.query.port.driven.GetMetricValuesOfCommitPort;
import io.reflectoring.coderadar.query.port.driver.GetMetricsForCommitCommand;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class GetMetricValuesOfCommitAdapter implements GetMetricValuesOfCommitPort {

  private final GetMetricValuesOfCommitRepository getMetricValuesOfCommitRepository;
  private final CommitRepository commitRepository;

  public GetMetricValuesOfCommitAdapter(
      GetMetricValuesOfCommitRepository getMetricValuesOfCommitRepository,
      CommitRepository commitRepository) {
    this.getMetricValuesOfCommitRepository = getMetricValuesOfCommitRepository;
    this.commitRepository = commitRepository;
  }

  @Override
  public List<MetricValueForCommit> get(GetMetricsForCommitCommand command, Long projectId) {
    CommitEntity commitEntity =
        commitRepository
            .findByNameAndProjectId(command.getCommit(), projectId)
            .orElseThrow(() -> new CommitNotFoundException(command.getCommit()));
    List<MetricValueForCommitQueryResult> result =
        getMetricValuesOfCommitRepository.getMetricValuesForCommit(
            projectId, command.getMetrics(), commitEntity.getTimestamp().toInstant().toString());
    List<MetricValueForCommit> values = new ArrayList<>();
    for (MetricValueForCommitQueryResult queryResult : result) {
      values.add(new MetricValueForCommit(queryResult.getName(), queryResult.getValue()));
    }
    return values;
  }
}
