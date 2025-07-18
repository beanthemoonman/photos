package io.beanthemoonman.photos.controller;

import io.beanthemoonman.photos.config.PhotosConfig;
import io.beanthemoonman.photos.service.GitInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for web pages.
 */
@Controller
public class WebController {

  private final PhotosConfig config;
  private final GitInfoService gitInfoService;

  @Autowired
  public WebController(PhotosConfig config, GitInfoService gitInfoService) {
    this.config = config;
    this.gitInfoService = gitInfoService;
  }

  /**
   * Serve the main page with the photo carousel.
   *
   * @param model The model to pass data to the template
   * @return The name of the Thymeleaf template
   */
  @GetMapping("/")
  public String index(Model model) {
    model.addAttribute("website", config.getWebsite());
    model.addAttribute("commitId", gitInfoService.getCommitIdAbbrev());
    return "index";
  }
}