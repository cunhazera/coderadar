package io.reflectoring.coderadar.projectadministration.port.driven.filepattern;

import io.reflectoring.coderadar.projectadministration.ProjectNotFoundException;
import io.reflectoring.coderadar.projectadministration.domain.FilePattern;

public interface CreateFilePatternPort {
  Long createFilePattern(FilePattern filePattern, Long projectId) throws ProjectNotFoundException;
}
