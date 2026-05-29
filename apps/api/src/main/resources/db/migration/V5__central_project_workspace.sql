create table company_users (
  id varchar(80) primary key,
  display_name varchar(120) not null,
  email varchar(200),
  role varchar(40) not null,
  active boolean not null,
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);

create table company_projects (
  id bigserial primary key,
  name varchar(160) not null,
  repository varchar(200) not null,
  workspace_path text not null,
  description text,
  status varchar(40) not null,
  created_by varchar(80) references company_users(id),
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);

create table project_members (
  id bigserial primary key,
  project_id bigint not null references company_projects(id),
  user_id varchar(80) not null references company_users(id),
  role varchar(40) not null,
  created_at timestamp with time zone not null,
  constraint uq_project_members_project_user unique(project_id, user_id)
);

create table project_work_requests (
  id bigserial primary key,
  project_id bigint not null references company_projects(id),
  requester_id varchar(80) not null references company_users(id),
  title varchar(500) not null,
  description text not null,
  request_type varchar(40) not null,
  priority varchar(40) not null,
  risk_level varchar(40) not null,
  status varchar(40) not null,
  task_id bigint references tasks(id),
  created_at timestamp with time zone not null,
  updated_at timestamp with time zone not null
);

create index idx_company_projects_status on company_projects(status);
create index idx_project_members_project_id on project_members(project_id);
create index idx_project_work_requests_project_id on project_work_requests(project_id);
create index idx_project_work_requests_status on project_work_requests(status);

insert into company_users(id, display_name, email, role, active, created_at, updated_at)
select 'operator', '운영자', null, 'ADMIN', true, now(), now()
where not exists (select 1 from company_users where id = 'operator');
