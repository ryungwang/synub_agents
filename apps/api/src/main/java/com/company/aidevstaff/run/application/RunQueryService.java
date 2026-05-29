package com.company.aidevstaff.run.application;

import com.company.aidevstaff.run.domain.Run;
import com.company.aidevstaff.run.infrastructure.RunRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class RunQueryService {
    private final RunRepository runRepository;

    public RunQueryService(RunRepository runRepository) {
        this.runRepository = runRepository;
    }

    public List<Run> findRecent() {
        return runRepository.findTop50ByOrderByCreatedAtDesc();
    }
}
