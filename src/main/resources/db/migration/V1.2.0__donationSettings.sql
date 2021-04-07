alter table projects
    add donation_enabled boolean default false;

alter table projects
    add donation_default_amount int default 5;

alter table projects
    add donation_email varchar(255);

alter table projects
    add donation_onetime_amounts integer[] default ARRAY[]::integer[];

alter table projects
    add donation_monthly_amounts integer[] default ARRAY[]::integer[];
