package io.beanthemoonman.photos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service for retrieving Git information from build-time properties.
 */
@Service
public class GitInfoService {
  
  @Value("${git.commit.id.abbrev:unknown}")
  private String commitIdAbbrev;
  
  @Value("${git.commit.id.full:unknown}")
  private String commitIdFull;
  
  @Value("${git.branch:unknown}")
  private String branch;
  
  @Value("${git.build.time:unknown}")
  private String buildTime;
  
  /**
   * Get the abbreviated Git commit hash (7 characters).
   * 
   * @return The abbreviated commit hash
   */
  public String getCommitIdAbbrev() {
    return commitIdAbbrev;
  }
  
  /**
   * Get the full Git commit hash.
   * 
   * @return The full commit hash
   */
  public String getCommitIdFull() {
    return commitIdFull;
  }
  
  /**
   * Get the Git branch name.
   * 
   * @return The branch name
   */
  public String getBranch() {
    return branch;
  }
  
  /**
   * Get the build time.
   * 
   * @return The build time
   */
  public String getBuildTime() {
    return buildTime;
  }
}