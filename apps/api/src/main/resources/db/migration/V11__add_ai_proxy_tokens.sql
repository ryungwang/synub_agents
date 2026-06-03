create table proxy_tokens (
  id varchar(64) primary key,
  token_hash varchar(128) not null,
  employee_id varchar(255) not null,
  provider varchar(40) not null,
  issued_at timestamp with time zone not null,
  expires_at timestamp with time zone not null,
  revoked_at timestamp with time zone,
  last_used_at timestamp with time zone
);

create unique index ux_proxy_tokens_token_hash on proxy_tokens(token_hash);
create index ix_proxy_tokens_employee on proxy_tokens(employee_id);
