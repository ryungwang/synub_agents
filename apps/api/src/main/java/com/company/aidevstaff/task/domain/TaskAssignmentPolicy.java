package com.company.aidevstaff.task.domain;

public class TaskAssignmentPolicy {
    public String assignAgent(String title, String body, TaskRiskLevel riskLevel) {
        String text = (title + " " + body).toLowerCase();
        if (riskLevel == TaskRiskLevel.HIGH) {
            return "security-reviewer";
        }
        if (containsAny(text, "manual", "document", "docs", "readme", "runbook", "checklist", "문서", "매뉴얼", "메뉴얼", "체크리스트")) {
            return "docs-writer";
        }
        if (containsAny(text, "release", "changelog", "version", "packaging", "build", "릴리스", "배포판", "패키징", "빌드")) {
            if (containsAny(text, "mac", "macos", "dmg", "darwin", "arm64", "맥")) {
                return "macos-build-dev";
            }
            if (containsAny(text, "windows", "win", "exe", "msi", "powershell", "윈도우")) {
                return "windows-build-dev";
            }
            return "release-manager";
        }
        if (containsAny(text, "plan", "planning", "requirement", "scope", "기획", "요구사항", "범위", "우선순위")) {
            return "planner";
        }
        if (containsAny(text, "electron", "desktop", "tray", "auto updater", "auto-update", "데스크톱", "트레이", "자동 업데이트")) {
            return "electron-desktop-dev";
        }
        if (containsAny(text, "next.js", "nextjs", "next ", "app router", "ssr", "ssg")) {
            return "frontend-next-dev";
        }
        if (containsAny(text, "vue", "nuxt", "composition api")) {
            return "frontend-vue-dev";
        }
        if (containsAny(text, "svelte", "sveltekit")) {
            return "frontend-svelte-dev";
        }
        if (containsAny(text, "react native", "mobile app", "ios", "android", "모바일")) {
            return "frontend-mobile-dev";
        }
        if (containsAny(text, "typescript", "ts", "tsx", "타입스크립트")) {
            return "typescript-react-dev";
        }
        if (containsAny(text, "react", "hook", "jsx")) {
            return "frontend-react-dev";
        }
        if (containsAny(text, "javascript", "js", "dom", "browser api", "자바스크립트")) {
            return "frontend-javascript-dev";
        }
        if (containsAny(text, "research", "journey", "flow", "사용성", "사용자 흐름", "정보 구조")) {
            return "ux-researcher";
        }
        if (containsAny(text, "design system", "component", "layout", "density", "디자인 시스템", "컴포넌트", "레이아웃", "밀도")) {
            return "ui-designer";
        }
        if (containsAny(text, "design", "ux", "figma", "wireframe", "prototype", "디자인", "화면 설계")) {
            return "designer";
        }
        if (containsAny(text, "accessibility", "a11y", "screen reader", "keyboard", "접근성", "키보드", "스크린리더")) {
            return "frontend-accessibility";
        }
        if (containsAny(text, "css", "scss", "responsive", "layout", "overflow", "uiux", "ui/ux", "스타일", "반응형")) {
            return "css-ui-dev";
        }
        if (containsAny(text, "performance", "bundle", "render", "memo", "성능", "번들", "렌더링")) {
            return "frontend-perf";
        }
        if (containsAny(text, "ui", "react", "css", "screen", "frontend")) {
            return "frontend";
        }
        if (containsAny(text, "playwright", "browser", "e2e", "브라우저", "시나리오")) {
            return "qa-e2e";
        }
        if (containsAny(text, "test", "regression", "qa", "테스트", "회귀", "검수")) {
            return "qa";
        }
        if (containsAny(text, "log", "metric", "health", "monitor", "로그", "메트릭", "헬스", "모니터링")) {
            return "devops-observability";
        }
        if (containsAny(text, "github actions", "workflow", "artifact", "ci", "액션", "워크플로우")) {
            return "github-actions-dev";
        }
        if (containsAny(text, "bash", "zsh", "shell", "powershell", ".sh", ".ps1", "스크립트", "쉘")) {
            return "shell-automation-dev";
        }
        if (containsAny(text, "docker", "deploy", "infra", "도커", "인프라")) {
            return "devops";
        }
        if (containsAny(text, "java", "spring", "gradle", "spring boot", "spring security", "자바", "스프링")) {
            return "java-spring-dev";
        }
        if (containsAny(text, "kotlin", "코틀린")) {
            return "kotlin-dev";
        }
        if (containsAny(text, "node", "node.js", "npm", "pnpm", "express", "nest")) {
            return "node-api-dev";
        }
        if (containsAny(text, "python", "pytest", "pip", "파이썬")) {
            return "python-worker-dev";
        }
        if (containsAny(text, "golang", "go ", " gin", "fiber")) {
            return "backend-go-dev";
        }
        if (containsAny(text, "rust", "axum", "actix")) {
            return "backend-rust-dev";
        }
        if (containsAny(text, "c#", ".net", "dotnet", "asp.net")) {
            return "backend-csharp-dev";
        }
        if (containsAny(text, "php", "laravel")) {
            return "backend-php-dev";
        }
        if (containsAny(text, "ruby", "rails")) {
            return "backend-ruby-dev";
        }
        if (containsAny(text, "security", "auth", "token", "session", "csrf", "권한", "인증", "토큰", "세션")) {
            return "backend-security";
        }
        if (containsAny(text, "migration", "flyway", "postgres", "sql", "db", "마이그레이션", "포스트그레스", "데이터베이스")) {
            if (containsAny(text, "postgres", "postgresql", "sql", "index", "query", "포스트그레스", "인덱스", "쿼리")) {
                return "postgres-sql-dev";
            }
            return "db-migration";
        }
        if (containsAny(text, "api", "contract", "endpoint", "controller", "서버", "엔드포인트")) {
            return "backend-api";
        }
        if (containsAny(text, "backend", "jpa", "domain", "query", "도메인", "쿼리")) {
            return "backend";
        }
        return "planner";
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }
}
