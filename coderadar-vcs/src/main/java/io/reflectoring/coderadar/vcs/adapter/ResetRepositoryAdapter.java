package io.reflectoring.coderadar.vcs.adapter;

import io.reflectoring.coderadar.vcs.UnableToResetRepositoryException;
import io.reflectoring.coderadar.vcs.port.driven.ResetRepositoryPort;
import java.io.IOException;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.stereotype.Service;

@Service
public class ResetRepositoryAdapter implements ResetRepositoryPort {
  @Override
  public void resetRepository(Path repositoryRoot) throws UnableToResetRepositoryException {
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    Repository repository;
    try {
      repository = builder.setWorkTree(repositoryRoot.toFile()).build();
      Git git = new Git(repository);
      git.reset().setMode(ResetCommand.ResetType.HARD).call();
      git.getRepository().close();
      git.close();
    } catch (IOException | GitAPIException e) {
      throw new UnableToResetRepositoryException(
          "Error resetting git repository " + repositoryRoot + " " + e.getMessage());
    }
  }
}
