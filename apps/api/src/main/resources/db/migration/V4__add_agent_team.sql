alter table agents add column if not exists team varchar(60) not null default 'ENGINEERING';

update agents set team = 'FRONTEND' where id = 'frontend';
update agents set team = 'BACKEND' where id = 'backend';
update agents set team = 'QA' where id = 'qa';
update agents set team = 'DEVOPS' where id = 'devops';
update agents set team = 'REVIEW' where id = 'reviewer';
update agents set team = 'PLANNING' where id = 'tech-pm';

update agents set name = '프론트엔드 엔지니어', role = 'React, UI, 접근성' where id = 'frontend';
update agents set name = '백엔드 엔지니어', role = 'API, DB, 인증' where id = 'backend';
update agents set name = '검수 자동화', role = '테스트 자동화, 회귀 검수' where id = 'qa';
update agents set name = 'DevOps 엔지니어', role = 'CI/CD, 인프라' where id = 'devops';
update agents set name = '코드 리뷰어', role = '보안, 유지보수성' where id = 'reviewer';
update agents set id = 'planner', name = '기획 매니저', role = '요구사항 정리, 우선순위, 릴리스 범위' where id = 'tech-pm';

insert into agents(id, team, name, role, status, quality_score, created_at, updated_at)
select 'designer', 'DESIGN', '제품 디자이너', 'UX, UI, 접근성, 화면 설계', 'AVAILABLE', 90, now(), now()
where not exists (select 1 from agents where id = 'designer');
