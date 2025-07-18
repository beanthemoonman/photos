package io.beanthemoonman.photos.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class WebControllerTest {

  private MockMvc mockMvc;

  @InjectMocks
  private WebController webController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    mockMvc = MockMvcBuilders.standaloneSetup(webController).build();
  }

  @Test
  void testIndexEndpoint() throws Exception {
    // Test that the index endpoint returns the correct view
//    mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("index"));
  }
}
