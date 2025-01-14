package io.reflectoring.coderadar.rest.unit.filepattern;

import io.reflectoring.coderadar.projectadministration.domain.InclusionType;
import io.reflectoring.coderadar.projectadministration.port.driver.filepattern.create.CreateFilePatternCommand;
import io.reflectoring.coderadar.projectadministration.port.driver.filepattern.create.CreateFilePatternUseCase;
import io.reflectoring.coderadar.rest.IdResponse;
import io.reflectoring.coderadar.rest.filepattern.CreateFilePatternController;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.Mockito.mock;

class CreateFilePatternControllerTest {

  private CreateFilePatternUseCase createFilePatternUseCase = mock(CreateFilePatternUseCase.class);

  @Test
  void createFilePatternSuccessfully() {
    CreateFilePatternController testSubject =
        new CreateFilePatternController(createFilePatternUseCase);

    CreateFilePatternCommand command =
        new CreateFilePatternCommand("**/*.java", InclusionType.INCLUDE);
    Mockito.when(createFilePatternUseCase.createFilePattern(command, 5L)).thenReturn(1L);

    ResponseEntity<IdResponse> responseEntity = testSubject.createFilePattern(command, 5L);

    Assertions.assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    Assertions.assertEquals(1L, responseEntity.getBody().getId().longValue());
  }
}
