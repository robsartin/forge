package com.robsartin.graphs.application;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.csrf.CsrfToken;
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
    public String getGraphEditor(Authentication authentication) {
        if (authentication != null) {
            logger.info("User '{}' accessing graph editor", authentication.getName());
        }
        return "redirect:/graph-editor.html";
    }

    @GetMapping("/api/csrf")
    @ResponseBody
    @Operation(summary = "CSRF Token", description = "Returns CSRF token for form submissions")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "CSRF token returned successfully")
    })
    public Map<String, String> getCsrfToken(CsrfToken token) {
        return Map.of(
            "token", token.getToken(),
            "headerName", token.getHeaderName(),
            "parameterName", token.getParameterName()
        );
    }

    @GetMapping("/api/user")
    @ResponseBody
    @Operation(summary = "Current User Info", description = "Returns current authenticated user information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User info returned successfully"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    public Map<String, Object> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Attempt to access user info without authentication");
            return Map.of("authenticated", false);
        }

        Object principal = authentication.getPrincipal();

        // Handle OAuth2 user (Google login)
        if (principal instanceof OAuth2User oauth2User) {
            String name = oauth2User.getAttribute("name");
            String email = oauth2User.getAttribute("email");
            String picture = oauth2User.getAttribute("picture");

            logger.info("User info requested for OAuth2 user: {}", name);

            return Map.of(
                "authenticated", true,
                "name", name != null ? name : "",
                "email", email != null ? email : "",
                "picture", picture != null ? picture : ""
            );
        }

        // Handle form login user
        String username = authentication.getName();
        logger.info("User info requested for form login user: {}", username);

        return Map.of(
            "authenticated", true,
            "name", username,
            "email", "",
            "picture", ""
        );
    }
}
