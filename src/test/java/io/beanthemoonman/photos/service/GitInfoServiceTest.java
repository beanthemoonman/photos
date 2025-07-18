package io.beanthemoonman.photos.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class GitInfoServiceTest {

    @Autowired
    private GitInfoService gitInfoService;

    @Test
    void testGetCommitIdAbbrev() {
        String commitId = gitInfoService.getCommitIdAbbrev();
        assertNotNull(commitId);
        assertNotEquals("unknown", commitId);
        // Should be 7 characters for abbreviated commit
        assertTrue(commitId.length() >= 7);
    }

    @Test
    void testGetCommitIdFull() {
        String commitId = gitInfoService.getCommitIdFull();
        assertNotNull(commitId);
        assertNotEquals("unknown", commitId);
        // Should be 40 characters for full commit hash
        assertTrue(commitId.length() >= 40);
    }

    @Test
    void testGetBranch() {
        String branch = gitInfoService.getBranch();
        assertNotNull(branch);
        assertNotEquals("unknown", branch);
    }

    @Test
    void testGetBuildTime() {
        String buildTime = gitInfoService.getBuildTime();
        assertNotNull(buildTime);
        assertNotEquals("unknown", buildTime);
    }
}