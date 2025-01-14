package io.reflectoring.coderadar.graph.analyzer;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import io.reflectoring.coderadar.analyzer.port.driver.StartAnalyzingCommand;
import io.reflectoring.coderadar.graph.analyzer.domain.AnalyzingJobEntity;
import io.reflectoring.coderadar.graph.analyzer.repository.AnalyzingJobRepository;
import io.reflectoring.coderadar.graph.analyzer.service.StartAnalyzingAdapter;
import io.reflectoring.coderadar.graph.projectadministration.domain.ProjectEntity;
import io.reflectoring.coderadar.graph.projectadministration.project.repository.ProjectRepository;
import io.reflectoring.coderadar.projectadministration.ProjectNotFoundException;
import java.util.Date;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Start analyzing")
class StartAnalyzingAdapterTest {
  private AnalyzingJobRepository analyzingJobRepository = mock(AnalyzingJobRepository.class);

  private ProjectRepository projectRepository = mock(ProjectRepository.class);

  private StartAnalyzingAdapter startAnalyzingAdapter;

  @BeforeEach
  void setUp() {
    startAnalyzingAdapter = new StartAnalyzingAdapter(projectRepository, analyzingJobRepository);
  }

  @Test
  @DisplayName("Should throw exception when a project with the passing ID doesn't exists")
  void shouldThrowExceptionWhenAProjectWithThePassingIdDoesntExists() {
    Assertions.assertThrows(
        ProjectNotFoundException.class,
        () -> startAnalyzingAdapter.start(new StartAnalyzingCommand(new Date(), true), 1L));
  }

  @Test
  @DisplayName("Should return ID when saving an analyzing job")
  void shouldReturnIdWhenSavingAnAnalyzingJob() {
    Date mockDate = new Date();
    ProjectEntity mockProject = new ProjectEntity();
    mockProject.setId(1L);
    AnalyzingJobEntity mockItem = new AnalyzingJobEntity();
    mockItem.setId(10L);
    mockItem.setActive(true);
    mockItem.setProject(mockProject);
    mockItem.setRescan(true);
    mockItem.setFrom(mockDate);
    when(analyzingJobRepository.save(any(AnalyzingJobEntity.class))).thenReturn(mockItem);
    when(projectRepository.findById(anyLong())).thenReturn(java.util.Optional.of(mockProject));

    StartAnalyzingCommand item = new StartAnalyzingCommand(mockDate, true);
    Long idFromItem = startAnalyzingAdapter.start(item, 1L);

    verify(projectRepository, times(1)).findById(1L);
    verify(analyzingJobRepository, times(1)).save(any(AnalyzingJobEntity.class));
    verifyNoMoreInteractions(projectRepository);
    verifyNoMoreInteractions(analyzingJobRepository);
    org.assertj.core.api.Assertions.assertThat(idFromItem).isEqualTo(10L);
  }
}
