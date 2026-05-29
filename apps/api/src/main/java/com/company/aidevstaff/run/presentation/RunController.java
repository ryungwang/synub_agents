package com.company.aidevstaff.run.presentation;

import com.company.aidevstaff.run.application.RunQueryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/runs")
public class RunController {
    private final RunQueryService runQueryService;

    public RunController(RunQueryService runQueryService) {
        this.runQueryService = runQueryService;
    }

    @GetMapping
    public List<RunResponse> findRecent() {
        return runQueryService.findRecent().stream().map(RunResponse::from).toList();
    }
}
