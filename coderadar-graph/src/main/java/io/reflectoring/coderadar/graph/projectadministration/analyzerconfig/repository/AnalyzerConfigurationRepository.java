package io.reflectoring.coderadar.graph.projectadministration.analyzerconfig.repository;

import io.reflectoring.coderadar.graph.projectadministration.domain.AnalyzerConfigurationEntity;
import java.util.List;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalyzerConfigurationRepository
    extends Neo4jRepository<AnalyzerConfigurationEntity, Long> {
  @Query(
      "MATCH (p:ProjectEntity)-[:HAS]->(c:AnalyzerConfigurationEntity) WHERE ID(p) = {projectId} RETURN c")
  List<AnalyzerConfigurationEntity> findByProjectId(@Param("projectId") Long projectId);
}
