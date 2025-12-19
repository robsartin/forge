package com.robsartin.graphs.application;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for the graph visualization editor web client.
 * Serves a React + D3 based graph editing interface.
 */
@Controller
@Tag(name = "Graph Editor", description = "Web-based graph visualization and editing interface")
public class GraphEditorController {

    @GetMapping("/edit_graph")
    @Operation(summary = "Graph Editor UI", description = "Redirects to the graph visualization editor web client built with React and D3")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "302", description = "Redirect to graph editor page")
    })
    public String getGraphEditor() {
        return "redirect:/graph-editor.html";
    }
}
