package io.beanthemoonman.photos.controller;

import io.beanthemoonman.photos.config.PhotosConfig;
import io.beanthemoonman.photos.service.GitInfoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class WebControllerTest {

  private MockMvc mockMvc;

  @InjectMocks
  private WebController webController;

  @Mock
  private PhotosConfig photosConfig;

  @Mock
  private GitInfoService gitInfoService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(webController).build();
  }

  @Test
  void testIndexEndpoint() throws Exception {
    // Setup mock responses
    PhotosConfig.Website website = new PhotosConfig.Website();
    website.setTitle("Test Gallery");
    website.setDescription("Test Description");
    
    when(photosConfig.getWebsite()).thenReturn(website);
    when(gitInfoService.getCommitIdAbbrev()).thenReturn("40f5f66");
    
    // Test that the index endpoint returns the correct view and model attributes
    mockMvc.perform(get("/"))
        .andExpect(status().isOk())
        .andExpect(view().name("index"))
        .andExpect(model().attributeExists("website"))
        .andExpect(model().attributeExists("commitId"))
        .andExpect(model().attribute("commitId", "40f5f66"));
  }
}
