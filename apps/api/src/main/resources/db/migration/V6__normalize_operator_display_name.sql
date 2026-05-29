update company_users
set display_name = 'Operator',
    updated_at = now()
where id = 'operator';
