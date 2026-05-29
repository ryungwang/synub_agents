create table agents (
  id varchar(64) primary key,
  name varchar(120) not null,
  role varchar(200) not null,
  status varchar(40) not null,
  current_task_id bigint,
  quality_score integer not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);

create table tasks (
  id bigserial primary key,
  source varchar(60) not null,
  source_url text not null,
  github_issue_number integer,
  title varchar(500) not null,
  description text,
  priority varchar(40) not null,
  risk_level varchar(40) not null,
  status varchar(60) not null,
  assigned_agent_id varchar(64),
  repository varchar(200) not null,
  branch_name varchar(200),
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null,
  constraint uq_tasks_github_issue unique(repository, github_issue_number)
);

create table worker_jobs (
  id bigserial primary key,
  task_id bigint not null references tasks(id),
  status varchar(60) not null,
  worker_type varchar(60) not null,
  workspace_path text,
  command text not null,
  started_at timestamp with time zone,
  finished_at timestamp with time zone,
  error_message text,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);

create table runs (
  id bigserial primary key,
  task_id bigint not null references tasks(id),
  worker_job_id bigint references worker_jobs(id),
  status varchar(60) not null,
  summary text,
  diff_summary text,
  test_result text,
  log_path text,
  started_at timestamp with time zone,
  finished_at timestamp with time zone,
  created_at timestamp with time zone not null
);

create table approvals (
  id bigserial primary key,
  task_id bigint not null references tasks(id),
  approval_type varchar(80) not null,
  risk_level varchar(40) not null,
  status varchar(40) not null,
  requested_at timestamp with time zone not null,
  approved_at timestamp with time zone,
  approved_by varchar(120)
);

create table audit_logs (
  id bigserial primary key,
  actor_type varchar(60) not null,
  actor_id varchar(120),
  action varchar(120) not null,
  target_type varchar(80) not null,
  target_id varchar(120),
  metadata_json text,
  created_at timestamp with time zone not null
);

create index idx_tasks_status on tasks(status);
create index idx_tasks_risk_level on tasks(risk_level);
create index idx_worker_jobs_status on worker_jobs(status);
create index idx_audit_logs_created_at on audit_logs(created_at);
