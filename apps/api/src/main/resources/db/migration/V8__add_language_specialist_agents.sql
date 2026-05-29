insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'java-spring-dev', 'BACKEND', 'Java Spring 개발자', 'Java 21, Spring Boot, Spring Security, Gradle', 'AVAILABLE', 94, now(), now()
where not exists (select 1 from agents where id = 'java-spring-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'kotlin-dev', 'BACKEND', 'Kotlin 개발자', 'Kotlin, JVM, 코루틴, 서버 모듈', 'AVAILABLE', 88, now(), now()
where not exists (select 1 from agents where id = 'kotlin-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'node-api-dev', 'BACKEND', 'Node API 개발자', 'Node.js, Express/Nest, API 서버, npm 패키지', 'AVAILABLE', 89, now(), now()
where not exists (select 1 from agents where id = 'node-api-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'typescript-react-dev', 'FRONTEND', 'TypeScript React 개발자', 'TypeScript, React, Vite, 상태 관리', 'AVAILABLE', 94, now(), now()
where not exists (select 1 from agents where id = 'typescript-react-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'electron-desktop-dev', 'FRONTEND', 'Electron 데스크톱 개발자', 'Electron, macOS/Windows 빌드, 자동 업데이트', 'AVAILABLE', 91, now(), now()
where not exists (select 1 from agents where id = 'electron-desktop-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'css-ui-dev', 'FRONTEND', 'CSS UI 개발자', 'CSS, 레이아웃, 반응형, 디자인 구현', 'AVAILABLE', 92, now(), now()
where not exists (select 1 from agents where id = 'css-ui-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'python-worker-dev', 'BACKEND', 'Python 워커 개발자', 'Python, CLI 워커, 파일 처리, 자동화 스크립트', 'AVAILABLE', 90, now(), now()
where not exists (select 1 from agents where id = 'python-worker-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'postgres-sql-dev', 'BACKEND', 'PostgreSQL 개발자', 'PostgreSQL, SQL 튜닝, 인덱스, 트랜잭션', 'AVAILABLE', 93, now(), now()
where not exists (select 1 from agents where id = 'postgres-sql-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'shell-automation-dev', 'DEVOPS', 'Shell 자동화 개발자', 'bash, zsh, PowerShell, 로컬 실행 스크립트', 'AVAILABLE', 89, now(), now()
where not exists (select 1 from agents where id = 'shell-automation-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'github-actions-dev', 'DEVOPS', 'GitHub Actions 개발자', 'workflow, artifact, release, CI 캐시', 'AVAILABLE', 90, now(), now()
where not exists (select 1 from agents where id = 'github-actions-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'macos-build-dev', 'DEVOPS', 'macOS 빌드 담당', 'macOS, dmg, arm64, notarization 준비', 'AVAILABLE', 88, now(), now()
where not exists (select 1 from agents where id = 'macos-build-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'windows-build-dev', 'DEVOPS', 'Windows 빌드 담당', 'Windows, exe, installer, PowerShell', 'AVAILABLE', 88, now(), now()
where not exists (select 1 from agents where id = 'windows-build-dev');
