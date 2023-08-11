-- Add the sends_notifications flag to all existing channels
UPDATE project_channels SET flags = flags || ARRAY[3]
