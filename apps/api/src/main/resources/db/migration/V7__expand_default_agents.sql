insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'product-analyst', 'PLANNING', '프로덕트 분석가', '사용자 요구 분석, 이슈 분류, 영향도 판단', 'AVAILABLE', 91, now(), now()
where not exists (select 1 from agents where id = 'product-analyst');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'release-manager', 'PLANNING', '릴리스 매니저', '릴리스 범위, 배포 체크리스트, 변경 공지', 'AVAILABLE', 90, now(), now()
where not exists (select 1 from agents where id = 'release-manager');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'ux-researcher', 'DESIGN', 'UX 리서처', '사용자 흐름, 사용성 검토, 정보 구조', 'AVAILABLE', 89, now(), now()
where not exists (select 1 from agents where id = 'ux-researcher');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'ui-designer', 'DESIGN', 'UI 시스템 디자이너', '디자인 시스템, 화면 밀도, 컴포넌트 규칙', 'AVAILABLE', 92, now(), now()
where not exists (select 1 from agents where id = 'ui-designer');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'frontend-perf', 'FRONTEND', '프론트엔드 성능 엔지니어', '렌더링 성능, 번들 최적화, 상태 관리', 'AVAILABLE', 93, now(), now()
where not exists (select 1 from agents where id = 'frontend-perf');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'frontend-platform', 'FRONTEND', '프론트엔드 플랫폼 엔지니어', '공통 컴포넌트, 라우팅, 빌드 도구', 'AVAILABLE', 92, now(), now()
where not exists (select 1 from agents where id = 'frontend-platform');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'frontend-accessibility', 'FRONTEND', '접근성 엔지니어', '키보드 탐색, 스크린리더, 대비 검수', 'AVAILABLE', 91, now(), now()
where not exists (select 1 from agents where id = 'frontend-accessibility');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'backend-api', 'BACKEND', 'API 설계 엔지니어', 'REST API, 계약 설계, 오류 응답 표준', 'AVAILABLE', 93, now(), now()
where not exists (select 1 from agents where id = 'backend-api');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'backend-data', 'BACKEND', '데이터 모델링 엔지니어', 'JPA, 쿼리, 도메인 모델, 정합성', 'AVAILABLE', 92, now(), now()
where not exists (select 1 from agents where id = 'backend-data');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'backend-security', 'BACKEND', '인증 보안 엔지니어', 'Spring Security, 토큰, 권한 정책', 'AVAILABLE', 94, now(), now()
where not exists (select 1 from agents where id = 'backend-security');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'db-migration', 'BACKEND', 'DB 마이그레이션 엔지니어', 'Flyway, PostgreSQL, 데이터 이전, 롤백 계획', 'AVAILABLE', 90, now(), now()
where not exists (select 1 from agents where id = 'db-migration');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'qa-regression', 'QA', '회귀 테스트 엔지니어', '기능 회귀, 테스트 케이스, 릴리스 검수', 'AVAILABLE', 90, now(), now()
where not exists (select 1 from agents where id = 'qa-regression');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'qa-e2e', 'QA', 'E2E 자동화 엔지니어', 'Playwright, 사용자 플로우, 브라우저 검증', 'AVAILABLE', 91, now(), now()
where not exists (select 1 from agents where id = 'qa-e2e');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'qa-accessibility', 'QA', '접근성 검수 엔지니어', 'WCAG, 폼 검증, 시각 회귀', 'AVAILABLE', 88, now(), now()
where not exists (select 1 from agents where id = 'qa-accessibility');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'devops-observability', 'DEVOPS', '관측성 엔지니어', '로그, 메트릭, 헬스체크, 알림', 'AVAILABLE', 89, now(), now()
where not exists (select 1 from agents where id = 'devops-observability');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'devops-release', 'DEVOPS', '배포 자동화 엔지니어', 'GitHub Actions, 패키징, 릴리스 파이프라인', 'AVAILABLE', 90, now(), now()
where not exists (select 1 from agents where id = 'devops-release');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'security-reviewer', 'REVIEW', '보안 리뷰어', '취약점, 시크릿, 권한 상승, 감사 로그', 'AVAILABLE', 96, now(), now()
where not exists (select 1 from agents where id = 'security-reviewer');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'architecture-reviewer', 'REVIEW', '아키텍처 리뷰어', '모듈 경계, 확장성, 장애 격리', 'AVAILABLE', 95, now(), now()
where not exists (select 1 from agents where id = 'architecture-reviewer');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'docs-writer', 'ENGINEERING', '기술 문서 담당', '운영 매뉴얼, 체크리스트, 변경 이력', 'AVAILABLE', 88, now(), now()
where not exists (select 1 from agents where id = 'docs-writer');
