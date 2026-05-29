alter table tasks add column pr_url text;
alter table worker_jobs add column result_branch varchar(240);
alter table worker_jobs add column pull_request_url text;
alter table runs add column pull_request_url text;
