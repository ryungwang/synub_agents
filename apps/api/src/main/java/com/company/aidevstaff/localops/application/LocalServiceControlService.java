package com.company.aidevstaff.localops.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"local", "local-postgres"})
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
            boolean windows = isWindows();
            Path script = root.resolve("infra").resolve("scripts").resolve(windows ? "start-worker-local.ps1" : "start-worker-local.sh");
            Path stdout = logDir.resolve("worker.out.log");
            Path stderr = logDir.resolve("worker.err.log");

            ProcessBuilder processBuilder = windows
                    ? new ProcessBuilder("powershell", "-NoProfile", "-ExecutionPolicy", "Bypass", "-File", script.toString())
                    : new ProcessBuilder("bash", script.toString());

            Process process = processBuilder
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
            ProcessBuilder processBuilder = isWindows()
                    ? new ProcessBuilder("taskkill", "/PID", Long.toString(worker.pid()), "/T", "/F")
                    : new ProcessBuilder("kill", Long.toString(worker.pid()));

            processBuilder
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
            ProcessBuilder processBuilder = isWindows()
                    ? new ProcessBuilder(
                            "powershell",
                            "-NoProfile",
                            "-Command",
                            "if (Get-NetTCPConnection -LocalPort " + port + " -State Listen -ErrorAction SilentlyContinue) { exit 0 } else { exit 1 }")
                    : new ProcessBuilder("bash", "-lc", "lsof -iTCP:" + port + " -sTCP:LISTEN -Pn >/dev/null 2>&1");

            Process process = processBuilder
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

    private boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
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
