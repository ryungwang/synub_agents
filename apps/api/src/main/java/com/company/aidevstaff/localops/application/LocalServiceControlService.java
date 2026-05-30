package com.company.aidevstaff.localops.application;

import com.company.aidevstaff.worker.domain.WorkerType;
import com.company.aidevstaff.worker.infrastructure.WorkerProperties;
import java.io.IOException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"local", "local-postgres"})
public class LocalServiceControlService {
    private final WorkerProperties workerProperties;
    private final Path root = Path.of("").toAbsolutePath().normalize();
    private final Path runDir = root.resolve(".run");
    private final Path logDir = runDir.resolve("logs");
    private final Path workerPidFile = runDir.resolve("worker.pid");

    public LocalServiceControlService(WorkerProperties workerProperties) {
        this.workerProperties = workerProperties;
    }

    public LocalServicesStatus status() {
        return new LocalServicesStatus(
                true,
                isPortListening(3002),
                workerStatus(),
                centralAiStatus(),
                root.toString(),
                logDir.toString(),
                Instant.now()
        );
    }

    public AiProviderStatus testAiProvider(WorkerType workerType) {
        String command = commandFor(workerType);
        AiProviderStatus installedStatus = inspectProvider(workerType, command);
        if (!installedStatus.installed()) {
            return installedStatus;
        }

        ProcessResult result = runProcess(testCommand(workerType, command), root, Duration.ofSeconds(60));
        boolean ok = result.exitCode() == 0 && result.output().toUpperCase(Locale.ROOT).contains("PONG");
        String message = ok
                ? "중앙 실행 테스트 성공"
                : trimMessage(result.output().isBlank() ? "테스트 실행 실패" : result.output());
        return new AiProviderStatus(
                workerType,
                displayName(workerType),
                command,
                true,
                installedStatus.version(),
                ok,
                message,
                Instant.now()
        );
    }

    public AiProviderStatus connectAiProvider(WorkerType workerType) {
        String command = commandFor(workerType);
        AiProviderStatus installedStatus = inspectProvider(workerType, command);
        if (!installedStatus.installed()) {
            return installedStatus;
        }

        ProcessResult result = openInteractiveLoginTerminal(workerType, command);
        String message = result.exitCode() == 0
                ? "중앙 PC에서 인증 터미널을 열었습니다. 로그인 완료 후 새로고침 또는 연결 테스트를 실행하세요."
                : trimMessage(result.output().isBlank() ? "인증 터미널 실행 실패" : result.output());
        return new AiProviderStatus(
                workerType,
                displayName(workerType),
                command,
                true,
                installedStatus.version(),
                installedStatus.testPassed(),
                message,
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

    private List<AiProviderStatus> centralAiStatus() {
        return List.of(
                inspectProvider(WorkerType.CODEX, commandFor(WorkerType.CODEX)),
                inspectProvider(WorkerType.CLAUDE, commandFor(WorkerType.CLAUDE))
        );
    }

    private AiProviderStatus inspectProvider(WorkerType workerType, String command) {
        Optional<String> resolved = resolveCommand(command);
        if (resolved.isEmpty()) {
            return new AiProviderStatus(workerType, displayName(workerType), command, false, null, false, "CLI를 찾을 수 없습니다", Instant.now());
        }

        ProcessResult versionResult = runProcess(List.of(command, "--version"), root, Duration.ofSeconds(8));
        String version = versionResult.exitCode() == 0 ? firstLine(versionResult.output()) : null;
        ProcessResult authResult = runProcess(authStatusCommand(workerType, command), root, Duration.ofSeconds(8));
        boolean authenticated = authResult.exitCode() == 0 && isAuthenticatedStatus(workerType, authResult.output());
        String message;
        if (authenticated) {
            message = "중앙 인증 연결됨";
        } else if (authResult.exitCode() == 0 && !authResult.output().isBlank()) {
            message = trimMessage(authResult.output());
        } else {
            message = versionResult.exitCode() == 0 ? "CLI 설치 확인, 중앙 인증 필요" : trimMessage(versionResult.output());
        }
        return new AiProviderStatus(workerType, displayName(workerType), command, true, version, authenticated, message, Instant.now());
    }

    private Optional<String> resolveCommand(String command) {
        if (command == null || command.isBlank()) {
            return Optional.empty();
        }
        Path path = Path.of(command);
        if (path.isAbsolute() || command.contains("/") || command.contains("\\")) {
            return Files.isExecutable(path) ? Optional.of(path.toString()) : Optional.empty();
        }
        ProcessResult result = isWindows()
                ? runProcess(List.of("where", command), root, Duration.ofSeconds(5))
                : runProcess(List.of("bash", "-lc", "command -v " + shellQuote(command)), root, Duration.ofSeconds(5));
        if (result.exitCode() != 0 || result.output().isBlank()) {
            return Optional.empty();
        }
        return Optional.of(firstLine(result.output()));
    }

    private List<String> testCommand(WorkerType workerType, String command) {
        return switch (workerType) {
            case CODEX -> List.of(command, "exec", "--cd", root.toString(), "--sandbox", "read-only", "Output only PONG.");
            case CLAUDE -> List.of(command, "-p", "Output only PONG.", "--output-format", "text", "--max-turns", "1", "--no-session-persistence");
        };
    }

    private List<String> authStatusCommand(WorkerType workerType, String command) {
        return switch (workerType) {
            case CODEX -> List.of(command, "login", "status");
            case CLAUDE -> List.of(command, "auth", "status");
        };
    }

    private List<String> loginCommand(WorkerType workerType, String command) {
        return switch (workerType) {
            case CODEX -> List.of(command, "login");
            case CLAUDE -> List.of(command, "auth", "login");
        };
    }

    private boolean isAuthenticatedStatus(WorkerType workerType, String output) {
        String normalized = output == null ? "" : output.toLowerCase(Locale.ROOT);
        return switch (workerType) {
            case CODEX -> normalized.contains("logged in");
            case CLAUDE -> normalized.contains("\"loggedin\": true") || normalized.contains("\"loggedIn\": true".toLowerCase(Locale.ROOT));
        };
    }

    private ProcessResult openInteractiveLoginTerminal(WorkerType workerType, String command) {
        String loginCommand = shellJoin(loginCommand(workerType, command));
        List<String> launcher = terminalLaunchCommand(loginCommand);
        return runProcess(launcher, root, Duration.ofSeconds(10));
    }

    private List<String> terminalLaunchCommand(String loginCommand) {
        if (isWindows()) {
            String escaped = loginCommand.replace("'", "''");
            return List.of(
                    "powershell",
                    "-NoProfile",
                    "-Command",
                    "Start-Process powershell -ArgumentList '-NoExit','-Command','" + escaped + "'"
            );
        }

        if (System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("mac")) {
            String script = "tell application \"Terminal\" to do script " + appleScriptString(loginCommand);
            return List.of("osascript", "-e", script);
        }

        return List.of("bash", "-lc", "x-terminal-emulator -e " + shellQuote(loginCommand) + " >/dev/null 2>&1 &");
    }

    private String commandFor(WorkerType workerType) {
        return switch (workerType) {
            case CODEX -> blankToDefault(workerProperties.codexCommand(), "codex");
            case CLAUDE -> blankToDefault(workerProperties.claudeCommand(), "claude");
        };
    }

    private String displayName(WorkerType workerType) {
        return workerType == WorkerType.CLAUDE ? "Claude" : "Codex";
    }

    private String blankToDefault(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private ProcessResult runProcess(List<String> command, Path directory, Duration timeout) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command)
                    .directory(directory.toFile())
                    .redirectInput(nullDevice())
                    .redirectErrorStream(true);
            Process process = processBuilder
                    .start();
            boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new ProcessResult(124, "명령 시간이 초과되었습니다.");
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
            return new ProcessResult(process.exitValue(), output);
        } catch (IOException exception) {
            return new ProcessResult(127, exception.getMessage());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return new ProcessResult(130, "명령 실행이 중단되었습니다.");
        }
    }

    private String shellQuote(String value) {
        return "'" + value.replace("'", "'\"'\"'") + "'";
    }

    private String shellJoin(List<String> command) {
        return command.stream().map(this::shellQuote).reduce((left, right) -> left + " " + right).orElse("");
    }

    private String appleScriptString(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private ProcessBuilder.Redirect nullDevice() {
        return ProcessBuilder.Redirect.from(new File(isWindows() ? "NUL" : "/dev/null"));
    }

    private String firstLine(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.lines().findFirst().orElse("").trim();
    }

    private String trimMessage(String value) {
        String firstLine = firstLine(value);
        if (firstLine.length() <= 220) {
            return firstLine;
        }
        return firstLine.substring(0, 220);
    }

    public record LocalServicesStatus(
            boolean apiRunning,
            boolean webRunning,
            WorkerProcessStatus worker,
            List<AiProviderStatus> centralAi,
            String workspaceRoot,
            String logDirectory,
            Instant checkedAt
    ) {
    }

    public record WorkerProcessStatus(boolean running, Long pid) {
    }

    public record AiProviderStatus(
            WorkerType workerType,
            String displayName,
            String command,
            boolean installed,
            String version,
            boolean testPassed,
            String message,
            Instant checkedAt
    ) {
    }

    private record ProcessResult(int exitCode, String output) {
    }
}
