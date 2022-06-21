ALTER TABLE project_channels ADD COLUMN flags int[] DEFAULT '{}';

UPDATE project_channels SET flags = array_append(flags, 0) WHERE NOT editable; -- 0 = ChannelFlag#FROZEN
UPDATE project_channels SET flags = array_append(flags, 2) WHERE non_reviewed; -- 2 = ChannelFlag#SKIP_REVIEW_QUEUE

ALTER TABLE project_channels DROP COLUMN editable;
ALTER TABLE project_channels DROP COLUMN non_reviewed;
