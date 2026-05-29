update agents
set name = '백엔드 Java 개발자',
    role = 'Java, Spring Boot, Spring Security, Gradle',
    updated_at = now()
where id = 'java-spring-dev';

update agents
set name = '백엔드 Kotlin 개발자',
    role = 'Kotlin, JVM, 코루틴, Spring 서버 모듈',
    updated_at = now()
where id = 'kotlin-dev';

update agents
set name = '백엔드 Node.js 개발자',
    role = 'Node.js, TypeScript, Express/Nest, API 서버',
    updated_at = now()
where id = 'node-api-dev';

update agents
set name = '백엔드 Python 개발자',
    role = 'Python, FastAPI/Django, CLI 워커, 자동화',
    updated_at = now()
where id = 'python-worker-dev';

update agents
set name = '프론트 TypeScript 개발자',
    role = 'TypeScript, React, Vite, 상태 관리',
    updated_at = now()
where id = 'typescript-react-dev';

update agents
set name = '프론트 CSS 개발자',
    role = 'CSS, 레이아웃, 반응형, 디자인 구현',
    updated_at = now()
where id = 'css-ui-dev';

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'backend-go-dev', 'BACKEND', '백엔드 Go 개발자', 'Go, Gin/Fiber, 병렬 처리, 경량 API 서버', 'AVAILABLE', 90, now(), now()
where not exists (select 1 from agents where id = 'backend-go-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'backend-rust-dev', 'BACKEND', '백엔드 Rust 개발자', 'Rust, Axum/Actix, 안정성, 고성능 서비스', 'AVAILABLE', 89, now(), now()
where not exists (select 1 from agents where id = 'backend-rust-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'backend-csharp-dev', 'BACKEND', '백엔드 C#/.NET 개발자', 'C#, .NET, ASP.NET Core, Windows 서비스', 'AVAILABLE', 88, now(), now()
where not exists (select 1 from agents where id = 'backend-csharp-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'backend-php-dev', 'BACKEND', '백엔드 PHP 개발자', 'PHP, Laravel, REST API, 서버 렌더링', 'AVAILABLE', 86, now(), now()
where not exists (select 1 from agents where id = 'backend-php-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'backend-ruby-dev', 'BACKEND', '백엔드 Ruby 개발자', 'Ruby, Rails, ActiveRecord, 웹 API', 'AVAILABLE', 86, now(), now()
where not exists (select 1 from agents where id = 'backend-ruby-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'frontend-javascript-dev', 'FRONTEND', '프론트 JavaScript 개발자', 'JavaScript, DOM, 브라우저 API, 번들링', 'AVAILABLE', 89, now(), now()
where not exists (select 1 from agents where id = 'frontend-javascript-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'frontend-react-dev', 'FRONTEND', '프론트 React 개발자', 'React, Hooks, 컴포넌트 구조, SPA', 'AVAILABLE', 93, now(), now()
where not exists (select 1 from agents where id = 'frontend-react-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'frontend-next-dev', 'FRONTEND', '프론트 Next.js 개발자', 'Next.js, App Router, SSR/SSG, 라우팅', 'AVAILABLE', 91, now(), now()
where not exists (select 1 from agents where id = 'frontend-next-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'frontend-vue-dev', 'FRONTEND', '프론트 Vue 개발자', 'Vue, Nuxt, Composition API, 상태 관리', 'AVAILABLE', 88, now(), now()
where not exists (select 1 from agents where id = 'frontend-vue-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'frontend-svelte-dev', 'FRONTEND', '프론트 Svelte 개발자', 'Svelte, SvelteKit, 반응형 상태, 컴파일 최적화', 'AVAILABLE', 87, now(), now()
where not exists (select 1 from agents where id = 'frontend-svelte-dev');

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'frontend-mobile-dev', 'FRONTEND', '프론트 모바일 개발자', 'React Native, 모바일 UI, 앱 패키징', 'AVAILABLE', 87, now(), now()
where not exists (select 1 from agents where id = 'frontend-mobile-dev');
