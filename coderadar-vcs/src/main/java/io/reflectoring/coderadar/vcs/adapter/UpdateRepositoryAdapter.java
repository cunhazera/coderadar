package io.reflectoring.coderadar.vcs.adapter;

import io.reflectoring.coderadar.vcs.UnableToResetRepositoryException;
import io.reflectoring.coderadar.vcs.UnableToUpdateRepositoryException;
import io.reflectoring.coderadar.vcs.port.driven.UpdateRepositoryPort;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UpdateRepositoryAdapter implements UpdateRepositoryPort {
  private ResetRepositoryAdapter resetRepositoryAdapter;

  @Autowired
  public UpdateRepositoryAdapter(ResetRepositoryAdapter resetRepositoryAdapter) {
    this.resetRepositoryAdapter = resetRepositoryAdapter;
  }

  @Override
  public boolean updateRepository(Path repositoryRoot, URL url)
      throws UnableToUpdateRepositoryException {
    try {
      return updateInternal(repositoryRoot, url);
    } catch (CheckoutConflictException e) {
      // When having a checkout conflict, someone or something fiddled with the working directory.
      // Since the working directory is designed to be read only, we just revert it and try again.
      try {
        resetRepositoryAdapter.resetRepository(repositoryRoot);
        return updateInternal(repositoryRoot, url);
      } catch (UnableToResetRepositoryException | IOException | GitAPIException ex) {
        throw createException(repositoryRoot, e.getMessage());
      }
    } catch (IOException | GitAPIException e) {
      throw createException(repositoryRoot, e.getMessage());
    }
  }

  private UnableToUpdateRepositoryException createException(Path repositoryRoot, String error) {
    return new UnableToUpdateRepositoryException(
        String.format(
            "Error updating local GIT repository at %s. Reason: %s", repositoryRoot, error));
  }

  private boolean updateInternal(Path repositoryRoot, URL url) throws GitAPIException, IOException {
    FileRepositoryBuilder builder = new FileRepositoryBuilder();
    Repository repository = builder.setWorkTree(repositoryRoot.toFile()).build();
    Git git = new Git(repository);
    StoredConfig config = git.getRepository().getConfig();
    config.setString("remote", "origin", "url", url.toString());
    config.save();
    ObjectId oldHead = git.getRepository().resolve(Constants.HEAD);
    ObjectId newHead =
        git.pull().setStrategy(MergeStrategy.THEIRS).call().getMergeResult().getNewHead();
    git.getRepository().close();
    git.close();
    return (oldHead == null && newHead != null) || !oldHead.equals(newHead);
  }
}
