package com.company.aidevstaff.localops.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("local")
public class LocalServiceControlService {
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path runDir = root.resolve(".run");
    private final Path logDir = runDir.resolve("logs");
    private final Path workerPidFile = runDir.resolve("worker.pid");

    public LocalServicesStatus status() {
        return new LocalServicesStatus(
                true,
                isPortListening(3002),
                workerStatus(),
                root.toString(),
                logDir.toString(),
                Instant.now()
        );
    }

    public LocalServicesStatus startWorker() {
        if (workerStatus().running()) {
            return status();
        }

        try {
            Files.createDirectories(logDir);
            Path script = root.resolve("infra").resolve("scripts").resolve("start-worker-local.ps1");
            Path stdout = logDir.resolve("worker.out.log");
            Path stderr = logDir.resolve("worker.err.log");
            Process process = new ProcessBuilder(
                    "powershell",
                    "-NoProfile",
                    "-ExecutionPolicy",
                    "Bypass",
                    "-File",
                    script.toString()
            )
                    .directory(root.toFile())
                    .redirectOutput(stdout.toFile())
                    .redirectError(stderr.toFile())
                    .start();

            Files.writeString(workerPidFile, Long.toString(process.pid()));
            return status();
        } catch (IOException exception) {
            throw new IllegalStateException("워커 실행에 실패했습니다: " + exception.getMessage(), exception);
        }
    }

    public LocalServicesStatus stopWorker() {
        WorkerProcessStatus worker = workerStatus();
        if (!worker.running()) {
            deletePidFile();
            return status();
        }

        try {
            new ProcessBuilder("taskkill", "/PID", Long.toString(worker.pid()), "/T", "/F")
                    .redirectErrorStream(true)
                    .start()
                    .waitFor();
        } catch (IOException exception) {
            throw new IllegalStateException("워커 종료에 실패했습니다: " + exception.getMessage(), exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("워커 종료가 중단되었습니다.", exception);
        } finally {
            deletePidFile();
        }

        return status();
    }

    private WorkerProcessStatus workerStatus() {
        Optional<Long> pid = readPid(workerPidFile);
        if (pid.isEmpty()) {
            return new WorkerProcessStatus(false, null);
        }

        boolean running = ProcessHandle.of(pid.get()).map(ProcessHandle::isAlive).orElse(false);
        if (!running) {
            deletePidFile();
            return new WorkerProcessStatus(false, null);
        }

        return new WorkerProcessStatus(true, pid.get());
    }

    private Optional<Long> readPid(Path pidFile) {
        if (!Files.exists(pidFile)) {
            return Optional.empty();
        }

        try {
            String value = Files.readString(pidFile).trim();
            if (value.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(Long.parseLong(value));
        } catch (IOException | NumberFormatException exception) {
            return Optional.empty();
        }
    }

    private void deletePidFile() {
        try {
            Files.deleteIfExists(workerPidFile);
        } catch (IOException ignored) {
            // Status can still be reported even when cleanup fails.
        }
    }

    private boolean isPortListening(int port) {
        try {
            Process process = new ProcessBuilder(
                    "powershell",
                    "-NoProfile",
                    "-Command",
                    "if (Get-NetTCPConnection -LocalPort " + port + " -State Listen -ErrorAction SilentlyContinue) { exit 0 } else { exit 1 }"
            )
                    .redirectErrorStream(true)
                    .start();
            return process.waitFor() == 0;
        } catch (IOException exception) {
            return false;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public record LocalServicesStatus(
            boolean apiRunning,
            boolean webRunning,
            WorkerProcessStatus worker,
            String workspaceRoot,
            String logDirectory,
            Instant checkedAt
    ) {
    }

    public record WorkerProcessStatus(boolean running, Long pid) {
    }
}
