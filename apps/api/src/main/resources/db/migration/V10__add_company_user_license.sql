alter table company_users
  add column if not exists license_status varchar(40) not null default 'UNASSIGNED';

alter table company_users
  add column if not exists license_assigned_at timestamp with time zone;

update company_users
set license_status = 'ACTIVE',
    license_assigned_at = coalesce(license_assigned_at, now()),
    updated_at = now()
where id = 'operator';
