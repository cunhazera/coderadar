package io.reflectoring.coderadar.graph.analyzer.domain;

import java.util.LinkedList;
import java.util.List;
import lombok.*;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@Data
@AllArgsConstructor
@NoArgsConstructor
@NodeEntity
@EqualsAndHashCode
public class MetricValueEntity {
  private Long id;
  private String name;
  private Long value;

  @Relationship(type = "VALID_FOR")
  @ToString.Exclude
  private CommitEntity commit;

  @Relationship(type = "LOCATED_IN")
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private List<FindingEntity> findings = new LinkedList<>();

  @Relationship(type = "MEASURED_BY", direction = Relationship.INCOMING)
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private FileEntity file;
}
