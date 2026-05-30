package com.company.aidevstaff.localops.presentation;

import com.company.aidevstaff.localops.application.LocalServiceControlService;
import com.company.aidevstaff.localops.application.LocalServiceControlService.AiProviderStatus;
import com.company.aidevstaff.localops.application.LocalServiceControlService.LocalServicesStatus;
import com.company.aidevstaff.worker.domain.WorkerType;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile({"local", "local-postgres"})
@RequestMapping("/api/local-services")
public class LocalServiceController {
    private final LocalServiceControlService service;

    public LocalServiceController(LocalServiceControlService service) {
        this.service = service;
    }

    @GetMapping("/status")
    public LocalServicesStatus status() {
        return service.status();
    }

    @PostMapping("/worker/start")
    public LocalServicesStatus startWorker() {
        return service.startWorker();
    }

    @PostMapping("/worker/stop")
    public LocalServicesStatus stopWorker() {
        return service.stopWorker();
    }

    @PostMapping("/ai/{workerType}/test")
    public AiProviderStatus testAiProvider(@PathVariable WorkerType workerType) {
        return service.testAiProvider(workerType);
    }

    @PostMapping("/ai/{workerType}/connect")
    public AiProviderStatus connectAiProvider(@PathVariable WorkerType workerType) {
        return service.connectAiProvider(workerType);
    }
}
