package io.beanthemoonman.photos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for web pages.
 */
@Controller
public class WebController {

  /**
   * Serve the main page with the photo carousel.
   *
   * @return The name of the Thymeleaf template
   */
  @GetMapping("/")
  public String index() {
    return "index";
  }
}