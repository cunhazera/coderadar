package io.reflectoring.coderadar.rest.filepattern;

import io.reflectoring.coderadar.graph.projectadministration.domain.FilePatternEntity;
import io.reflectoring.coderadar.graph.projectadministration.domain.ProjectEntity;
import io.reflectoring.coderadar.graph.projectadministration.filepattern.repository.FilePatternRepository;
import io.reflectoring.coderadar.graph.projectadministration.project.repository.ProjectRepository;
import io.reflectoring.coderadar.projectadministration.domain.InclusionType;
import io.reflectoring.coderadar.projectadministration.port.driver.filepattern.update.UpdateFilePatternCommand;
import io.reflectoring.coderadar.rest.ControllerTestTemplate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class UpdateFilePatternControllerIntegrationTest extends ControllerTestTemplate {

  @Autowired private ProjectRepository projectRepository;

  @Autowired private FilePatternRepository filePatternRepository;

  @Test
  void updateFilePatternWithId() throws Exception {
    // Set up
    ProjectEntity testProject = new ProjectEntity();
    testProject.setVcsUrl("https://valid.url");
    testProject = projectRepository.save(testProject);

    FilePatternEntity filePattern = new FilePatternEntity();
    filePattern.setInclusionType(InclusionType.INCLUDE);
    filePattern.setPattern("**/*.java");
    filePattern.setProject(testProject);
    filePattern = filePatternRepository.save(filePattern);
    final Long id = filePattern.getId();

    // Test
    UpdateFilePatternCommand command =
        new UpdateFilePatternCommand("**/*.xml", InclusionType.EXCLUDE);
    mvc()
        .perform(
            post("/projects/" + testProject.getId() + "/filePatterns/" + filePattern.getId())
                .content(toJson(command))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andDo(
            result -> {
              FilePatternEntity configuration = filePatternRepository.findById(id).get();
              Assertions.assertEquals("**/*.xml", configuration.getPattern());
              Assertions.assertEquals(InclusionType.EXCLUDE, configuration.getInclusionType());
            });
  }

  @Test
  void updateFilePatternReturnsErrorWhenNotFound() throws Exception {
    UpdateFilePatternCommand command =
        new UpdateFilePatternCommand("**/*.java", InclusionType.EXCLUDE);
    mvc()
        .perform(
            post("/projects/0/filePatterns/2")
                .content(toJson(command))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isNotFound())
        .andExpect(
            MockMvcResultMatchers.jsonPath("errorMessage")
                .value("FilePattern with id 2 not found."));
  }

  @Test
  void updateFilePatternReturnsErrorWhenRequestIsInvalid() throws Exception {
    UpdateFilePatternCommand command = new UpdateFilePatternCommand("", null);
    mvc()
        .perform(
            post("/projects/0/filePatterns/1")
                .content(toJson(command))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isBadRequest());
  }
}
