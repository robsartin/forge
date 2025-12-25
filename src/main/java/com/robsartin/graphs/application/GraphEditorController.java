package com.robsartin.graphs.application;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

/**
 * Controller for the graph visualization editor web client.
 * Serves a React + D3 based graph editing interface.
 */
@Controller
@Tag(name = "Graph Editor", description = "Web-based graph visualization and editing interface")
public class GraphEditorController {

    private static final Logger logger = LoggerFactory.getLogger(GraphEditorController.class);

    @GetMapping("/edit_graph")
    @Operation(summary = "Graph Editor UI", description = "Redirects to the graph visualization editor web client built with React and D3")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirect to graph editor page")
    })
    public String getGraphEditor(@AuthenticationPrincipal OAuth2User principal) {
        if (principal != null) {
            String name = principal.getAttribute("name");
            logger.info("User '{}' accessing graph editor", name);
        }
        return "redirect:/graph-editor.html";
    }

    @GetMapping("/api/user")
    @ResponseBody
    @Operation(summary = "Current User Info", description = "Returns current authenticated user information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User info returned successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public Map<String, Object> getCurrentUser(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            logger.warn("Attempt to access user info without authentication");
            return Map.of("authenticated", false);
        }

        String name = principal.getAttribute("name");
        String email = principal.getAttribute("email");
        String picture = principal.getAttribute("picture");

        logger.info("User info requested for: {}", name);

        return Map.of(
            "authenticated", true,
            "name", name != null ? name : "",
            "email", email != null ? email : "",
            "picture", picture != null ? picture : ""
        );
    }
}
