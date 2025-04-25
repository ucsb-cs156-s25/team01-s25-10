package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBArticles;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBArticlesRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.time.LocalDateTime;

/**
 * This is a REST controller for UCSBDates
 */

@Tag(name = "UCSBArticles")
@RequestMapping("/api/ucsbarticles")
@RestController
@Slf4j
public class ArticlesController extends ApiController {

    @Autowired
    UCSBArticlesRepository ucsbArticlesRepository;

    /**
     * List all UCSB articles
     * 
     * @return an iterable of UCSBArticles
     */
    @Operation(summary= "List all ucsb articles")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBArticles> allUCSBArticles() {
        Iterable<UCSBArticles> articles = ucsbArticlesRepository.findAll();
        return articles;
    }

    /**
     * Get a single article by id
     * 
     * @param id the id of the article
     * @return a UCSBArticle
     */
    @Operation(summary= "Get a single article")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBArticles getById(
            @Parameter(name="id") @RequestParam Long id) {
        UCSBArticles ucsbArticle = ucsbArticlesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBArticles.class, id));

        return ucsbArticle;
    }

    /**
     
     */
    @Operation(summary= "Create a new article")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBArticles postUCSBArticles(
            @Parameter(name="title") @RequestParam String title,
            @Parameter(name="url") @RequestParam String url,
            @Parameter(name="explanation") @RequestParam String explanation,
            @Parameter(name="email") @RequestParam String email,
            @Parameter(name="localDateTime", description="date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("localDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime localDateTime)
            throws JsonProcessingException {

        log.info("localDateTime={}", localDateTime);

        UCSBArticles ucsbArticle = new UCSBArticles();
        ucsbArticle.setTitle(title);
        ucsbArticle.setUrl(url);
        ucsbArticle.setExplanation(explanation);
        ucsbArticle.setEmail(email);
        ucsbArticle.setLocalDateTime(localDateTime);

        UCSBArticles savedUcsbArticle = ucsbArticlesRepository.save(ucsbArticle);

        return savedUcsbArticle;
    }
}