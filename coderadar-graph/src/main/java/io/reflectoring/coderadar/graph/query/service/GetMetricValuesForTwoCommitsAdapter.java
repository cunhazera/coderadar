package io.reflectoring.coderadar.graph.query.service;

import io.reflectoring.coderadar.graph.analyzer.repository.CommitRepository;
import io.reflectoring.coderadar.graph.analyzer.repository.FileRepository;
import io.reflectoring.coderadar.projectadministration.CommitNotFoundException;
import io.reflectoring.coderadar.query.domain.*;
import io.reflectoring.coderadar.query.port.driven.GetMetricValuesOfTwoCommitsPort;
import io.reflectoring.coderadar.query.port.driver.GetMetricsForCommitCommand;
import io.reflectoring.coderadar.query.port.driver.GetMetricsForTwoCommitsCommand;
import java.util.*;
import org.springframework.stereotype.Service;

@Service
public class GetMetricValuesForTwoCommitsAdapter implements GetMetricValuesOfTwoCommitsPort {

  private final CommitRepository commitRepository;
  private final FileRepository fileRepository;
  private final GetMetricsForAllFilesInCommitAdapter getMetricsForAllFilesInCommitAdapter;

  public GetMetricValuesForTwoCommitsAdapter(
      CommitRepository commitRepository,
      FileRepository fileRepository,
      GetMetricsForAllFilesInCommitAdapter getMetricsForAllFilesInCommitAdapter) {
    this.commitRepository = commitRepository;
    this.fileRepository = fileRepository;
    this.getMetricsForAllFilesInCommitAdapter = getMetricsForAllFilesInCommitAdapter;
  }

  @Override
  public DeltaTree get(GetMetricsForTwoCommitsCommand command, Long projectId) {
    MetricTree commit1Tree =
        getMetricsForAllFilesInCommitAdapter.get(
            new GetMetricsForCommitCommand(command.getCommit1(), command.getMetrics()), projectId);
    MetricTree commit2Tree =
        getMetricsForAllFilesInCommitAdapter.get(
            new GetMetricsForCommitCommand(command.getCommit2(), command.getMetrics()), projectId);
    Date commit1Time =
        commitRepository
            .findByNameAndProjectId(command.getCommit1(), projectId)
            .orElseThrow(() -> new CommitNotFoundException(command.getCommit1()))
            .getTimestamp();
    Date commit2Time =
        commitRepository
            .findByNameAndProjectId(command.getCommit2(), projectId)
            .orElseThrow(() -> new CommitNotFoundException(command.getCommit2()))
            .getTimestamp();

    if (commit1Time.after(commit2Time)) {
      MetricTree temp = commit1Tree;
      commit1Tree = commit2Tree;
      commit2Tree = temp;

      Date tempDate = commit1Time;
      commit1Time = commit2Time;
      commit2Time = tempDate;
    }

    List<String> addedFiles = new ArrayList<>();
    List<String> removedFiles = new ArrayList<>();

    DeltaTree deltaTree =
        createDeltaTree(
            commit1Tree,
            commit2Tree,
            commit1Time.toInstant().toString(),
            commit2Time.toInstant().toString(),
            projectId,
            addedFiles,
            removedFiles);

    // Renames are processed here
    for (String addedFile : addedFiles) {
      String oldPath =
          fileRepository.wasRenamedBetweenCommits(
              addedFile,
              commit1Time.toInstant().toString(),
              commit2Time.toInstant().toString(),
              projectId);
      if (removedFiles.contains(oldPath)) {
        DeltaTree oldName = findChildInDeltaTree(deltaTree, oldPath);
        DeltaTree newName = findChildInDeltaTree(deltaTree, addedFile);
        if (oldName != null && newName != null) {
          newName.setCommit1Metrics(oldName.getCommit1Metrics());
          newName.getChanges().setAdded(false);
          newName.getChanges().setRenamed(true);
          newName.setRenamedFrom(oldPath);
          newName.setRenamedTo(addedFile);
          removeChildFromDeltaTree(deltaTree, oldName);
        }
      }
    }
    return deltaTree;
  }

  /**
   * Create a new delta tree for two commits, given their individual metric tress.
   *
   * @param commit1Tree The metric tree of the first commit.
   * @param commit2Tree The metric tree of the second commit.
   * @param commit1Time The commit time of the first commit.
   * @param commit2Time The commit time of the second commit.
   * @param projectId The project id.
   * @param addedFiles A list, that must be filled with all files added in the newest commit.
   * @param removedFiles A list, that must be filled with all files removed in the newest commit.
   * @return A delta tree, which contains no information about renames. Renames must be processed
   *     separately using the added and removed files lists.
   */
  private DeltaTree createDeltaTree(
      MetricTree commit1Tree,
      MetricTree commit2Tree,
      String commit1Time,
      String commit2Time,
      Long projectId,
      List<String> addedFiles,
      List<String> removedFiles) {
    DeltaTree deltaTree = new DeltaTree();
    deltaTree.setName(commit2Tree.getName());
    deltaTree.setType(commit2Tree.getType());
    deltaTree.setCommit1Metrics(commit1Tree.getMetrics());
    deltaTree.setCommit2Metrics(commit2Tree.getMetrics());
    int tree1Counter = 0;
    int tree2Counter = 0;
    while (tree1Counter < commit1Tree.getChildren().size()
        || tree2Counter < commit2Tree.getChildren().size()) {
      MetricTree metricTree2 =
          tree2Counter < commit2Tree.getChildren().size()
              ? commit2Tree.getChildren().get(tree2Counter)
              : null;
      MetricTree metricTree1 =
          tree1Counter < commit1Tree.getChildren().size()
              ? commit1Tree.getChildren().get(tree1Counter)
              : null;

      if (metricTree1 != null
          && metricTree2 != null
          && metricTree1.getName().equals(metricTree2.getName())) {
        if (metricTree1.getType().equals(MetricsTreeNodeType.MODULE)
            && metricTree2.getType().equals(MetricsTreeNodeType.MODULE)) {
          deltaTree
              .getChildren()
              .add(
                  createDeltaTree(
                      metricTree1,
                      metricTree2,
                      commit1Time,
                      commit2Time,
                      projectId,
                      addedFiles,
                      removedFiles));
        } else {
          DeltaTree child = new DeltaTree();
          child.setName(metricTree1.getName());
          child.setType(metricTree1.getType());
          child.setCommit1Metrics(metricTree1.getMetrics());
          child.setCommit2Metrics(metricTree2.getMetrics());

          Changes changes = new Changes();
          if (!metricTree1.getMetrics().equals(metricTree2.getMetrics())) {
            changes.setModified(true);
          } else {
            changes.setModified(
                fileRepository.wasModifiedBetweenCommits(
                    metricTree1.getName(), commit1Time, commit2Time, projectId));
          }
          child.setChanges(changes);
          deltaTree.getChildren().add(child);
        }
        tree1Counter++;
        tree2Counter++;
      } else if (metricTree1 != null
          && (tree2Counter >= commit2Tree.getChildren().size()
              || metricTree1.getName().compareTo(metricTree2.getName()) < 0)) {
        DeltaTree child = new DeltaTree();
        child.setName(metricTree1.getName());
        child.setType(MetricsTreeNodeType.FILE);
        child.setCommit1Metrics(metricTree1.getMetrics());
        child.setCommit2Metrics(new ArrayList<>());
        Changes changes = new Changes();
        changes.setDeleted(true);
        child.setChanges(changes);
        deltaTree.getChildren().add(child);
        removedFiles.add(metricTree1.getName());
        tree1Counter++;
      } else if (metricTree2 != null
          && (tree1Counter >= commit1Tree.getChildren().size()
              || metricTree1.getName().compareTo(metricTree2.getName()) > 0)) {
        DeltaTree child = new DeltaTree();
        child.setName(metricTree2.getName());
        child.setType(MetricsTreeNodeType.FILE);
        child.setCommit1Metrics(new ArrayList<>());
        child.setCommit2Metrics(metricTree2.getMetrics());
        Changes changes = new Changes();
        changes.setAdded(true);
        child.setChanges(changes);
        deltaTree.getChildren().add(child);
        addedFiles.add(metricTree2.getName());
        tree2Counter++;
      }
    }
    return deltaTree;
  }

  /**
   * Finds a child with the given path in a delta tree.
   *
   * @param deltaTree The tree to look in.
   * @param path The child path to look for.
   * @return The tree with the given path or null if nothing is found.
   */
  private DeltaTree findChildInDeltaTree(DeltaTree deltaTree, String path) {
    for (DeltaTree child : deltaTree.getChildren()) {
      if (child.getType().equals(MetricsTreeNodeType.FILE)) {
        if (child.getName().equals(path)) {
          return child;
        }
      } else {
        DeltaTree result = findChildInDeltaTree(child, path);
        if (result != null) {
          return result;
        }
      }
    }
    return null;
  }

  /**
   * Removes a child from a delta tree.
   *
   * @param deltaTree The tree to remove from.
   * @param childTree The child to remove.
   */
  private void removeChildFromDeltaTree(DeltaTree deltaTree, DeltaTree childTree) {
    for (DeltaTree child : deltaTree.getChildren()) {
      if (child.getType().equals(MetricsTreeNodeType.FILE)) {
        if (child.getName().equals(childTree.getName())) {
          deltaTree.getChildren().remove(child);
          deltaTree.setCommit1Metrics(aggregateChildMetrics(deltaTree.getChildren()));
          return;
        }
      } else {
        removeChildFromDeltaTree(child, childTree);
      }
    }
  }

  /**
   * Aggregates all of the metrics in the delta tress.
   *
   * @param children The trees whose metrics to aggregate
   * @return A list of aggregated metric values.
   */
  private List<MetricValueForCommit> aggregateChildMetrics(List<DeltaTree> children) {
    List<MetricValueForCommit> resultList = new ArrayList<>();
    Map<String, Long> aggregatedMetrics = new HashMap<>();
    for (DeltaTree deltaTree : children) {
      for (MetricValueForCommit val : aggregateChildMetrics(deltaTree.getChildren())) {
        if (deltaTree
            .getCommit1Metrics()
            .stream()
            .noneMatch(metric -> metric.getMetricName().equals(val.getMetricName()))) {
          deltaTree
              .getCommit1Metrics()
              .add(new MetricValueForCommit(val.getMetricName(), val.getValue()));
        }
      }
      for (MetricValueForCommit value : deltaTree.getCommit1Metrics()) {
        aggregatedMetrics.putIfAbsent(value.getMetricName(), 0L);
        aggregatedMetrics.put(
            value.getMetricName(), aggregatedMetrics.get(value.getMetricName()) + value.getValue());
      }
    }
    for (Map.Entry<String, Long> metric : aggregatedMetrics.entrySet()) {
      resultList.add(new MetricValueForCommit(metric.getKey(), metric.getValue()));
    }
    return resultList;
  }
}
