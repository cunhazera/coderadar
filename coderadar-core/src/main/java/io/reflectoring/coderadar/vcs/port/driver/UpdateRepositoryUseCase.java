package io.reflectoring.coderadar.vcs.port.driver;

import io.reflectoring.coderadar.vcs.UnableToUpdateRepositoryException;
import java.net.URL;
import java.nio.file.Path;

public interface UpdateRepositoryUseCase {

  /**
   * Updates a local git repository (git pull against remote)
   *
   * @param repositoryRoot The path of the local repository.
   * @throws UnableToUpdateRepositoryException Thrown if there is an error while updating the
   *     repository.
   * @return Returns true if new commits were added and false otherwise
   */
  boolean updateRepository(Path repositoryRoot, URL url) throws UnableToUpdateRepositoryException;
}
