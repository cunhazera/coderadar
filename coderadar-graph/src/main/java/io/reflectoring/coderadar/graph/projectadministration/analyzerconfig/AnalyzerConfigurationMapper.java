package io.reflectoring.coderadar.graph.projectadministration.analyzerconfig;

import io.reflectoring.coderadar.graph.AbstractMapper;
import io.reflectoring.coderadar.graph.projectadministration.domain.AnalyzerConfigurationEntity;
import io.reflectoring.coderadar.graph.projectadministration.domain.AnalyzerConfigurationFileEntity;
import io.reflectoring.coderadar.projectadministration.domain.AnalyzerConfiguration;
import io.reflectoring.coderadar.projectadministration.domain.AnalyzerConfigurationFile;

public class AnalyzerConfigurationMapper
    extends AbstractMapper<AnalyzerConfiguration, AnalyzerConfigurationEntity> {

  @Override
  public AnalyzerConfiguration mapNodeEntity(AnalyzerConfigurationEntity nodeEntity) {
    AnalyzerConfiguration analyzerConfiguration = new AnalyzerConfiguration();
    analyzerConfiguration.setId(nodeEntity.getId());
    analyzerConfiguration.setAnalyzerName(nodeEntity.getAnalyzerName());
    analyzerConfiguration.setEnabled(nodeEntity.getEnabled());
    if (nodeEntity.getAnalyzerConfigurationFile() != null) {
      analyzerConfiguration.setAnalyzerConfigurationFile(
          mapConfigurationFileEntity(nodeEntity.getAnalyzerConfigurationFile()));
    }
    return analyzerConfiguration;
  }

  @Override
  public AnalyzerConfigurationEntity mapDomainObject(AnalyzerConfiguration domainObject) {
    AnalyzerConfigurationEntity analyzerConfiguration = new AnalyzerConfigurationEntity();
    analyzerConfiguration.setId(domainObject.getId());
    analyzerConfiguration.setAnalyzerName(domainObject.getAnalyzerName());
    analyzerConfiguration.setEnabled(domainObject.getEnabled());
    if (domainObject.getAnalyzerConfigurationFile() != null) {
      analyzerConfiguration.setAnalyzerConfigurationFile(
          mapConfigurationFileDomainObject(domainObject.getAnalyzerConfigurationFile()));
    }
    return analyzerConfiguration;
  }

  private AnalyzerConfigurationFile mapConfigurationFileEntity(
      AnalyzerConfigurationFileEntity entity) {
    AnalyzerConfigurationFile configurationFile = new AnalyzerConfigurationFile();
    configurationFile.setContentType(entity.getContentType());
    configurationFile.setFileData(entity.getFileData());
    configurationFile.setFileName(entity.getFileName());
    configurationFile.setId(entity.getId());
    configurationFile.setSizeInBytes(entity.getSizeInBytes());
    return configurationFile;
  }

  public AnalyzerConfigurationFileEntity mapConfigurationFileDomainObject(
      AnalyzerConfigurationFile entity) {
    AnalyzerConfigurationFileEntity configurationFile = new AnalyzerConfigurationFileEntity();
    configurationFile.setContentType(entity.getContentType());
    configurationFile.setFileData(entity.getFileData());
    configurationFile.setFileName(entity.getFileName());
    configurationFile.setId(entity.getId());
    configurationFile.setSizeInBytes(entity.getSizeInBytes());
    return configurationFile;
  }
}
