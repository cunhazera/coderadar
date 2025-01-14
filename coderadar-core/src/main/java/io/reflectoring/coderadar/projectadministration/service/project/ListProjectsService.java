package io.reflectoring.coderadar.projectadministration.service.project;

import io.reflectoring.coderadar.projectadministration.domain.Project;
import io.reflectoring.coderadar.projectadministration.port.driven.project.ListProjectsPort;
import io.reflectoring.coderadar.projectadministration.port.driver.project.get.GetProjectResponse;
import io.reflectoring.coderadar.projectadministration.port.driver.project.get.ListProjectsUseCase;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ListProjectsService implements ListProjectsUseCase {

  private final ListProjectsPort listProjectsPort;

  @Autowired
  public ListProjectsService(ListProjectsPort listProjectsPort) {
    this.listProjectsPort = listProjectsPort;
  }

  @Override
  public List<GetProjectResponse> listProjects() {
    List<GetProjectResponse> response = new ArrayList<>();
    for (Project project : listProjectsPort.getProjects()) {
      GetProjectResponse resource = new GetProjectResponse();
      resource.setId(project.getId());
      resource.setName(project.getName());
      resource.setVcsUsername(project.getVcsUsername());
      resource.setVcsPassword(project.getVcsPassword());
      resource.setVcsOnline(project.isVcsOnline());
      resource.setVcsUrl(project.getVcsUrl());
      resource.setStartDate(project.getVcsStart());
      resource.setEndDate(project.getVcsEnd());
      response.add(resource);
    }
    return response;
  }
}
