ALTER TABLE projects RENAME COLUMN donation_email TO donation_subject;
ALTER TABLE projects DROP COLUMN donation_default_amount, DROP COLUMN donation_monthly_amounts, DROP COLUMN donation_onetime_amounts
