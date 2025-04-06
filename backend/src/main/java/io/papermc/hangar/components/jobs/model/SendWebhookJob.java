package io.papermc.hangar.components.jobs.model;

import io.papermc.hangar.components.jobs.db.JobTable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public class SendWebhookJob extends Job {

    private String id;
    private String url;
    private String type;
    private String secret;
    private String payload;

    public SendWebhookJob(final String id, final String url, final String type, final String secret, String payload) {
        super(JobType.SEND_WEBHOOK);
        this.id = id;
        this.url = url;
        this.type = type;
        this.secret = secret;
        this.payload = payload;
    }

    public SendWebhookJob() {
        super(JobType.SEND_WEBHOOK);
    }

    public String getId() {
        return this.id;
    }

    public String getUrl() {
        return this.url;
    }

    public String getType() {
        return this.type;
    }

    public String getSecret() {
        return this.secret;
    }

    public String getPayload() {
        return this.payload;
    }

    @Override
    public void loadFromProperties() {
        if (this.getJobProperties() == null) {
            return;
        }
        if (this.getJobProperties().containsKey("id")) {
            this.id = this.getJobProperties().get("id");
        }
        if (this.getJobProperties().containsKey("url")) {
            this.url = this.getJobProperties().get("url");
        }
        if (this.getJobProperties().containsKey("type")) {
            this.type = this.getJobProperties().get("type");
        }
        if (this.getJobProperties().containsKey("secret")) {
            this.secret = this.getJobProperties().get("secret");
        }
        if (this.getJobProperties().containsKey("payload")) {
            this.payload = this.getJobProperties().get("payload");
        }
    }

    @Override
    public void saveIntoProperties() {
        final Map<String, String> properties = new HashMap<>();
        properties.put("id", this.id);
        properties.put("url", this.url);
        properties.put("type", this.type);
        properties.put("secret", this.secret);
        properties.put("payload", this.payload);
        this.setJobProperties(properties);
    }

    public static SendWebhookJob loadFromTable(final JobTable table) {
        final SendWebhookJob job = new SendWebhookJob();
        job.fromTable(table);
        job.setJobProperties(table.getJobProperties().getMap());
        job.loadFromProperties();
        return job;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        final SendWebhookJob that = (SendWebhookJob) o;
        return Objects.equals(this.id, that.id) && Objects.equals(this.url, that.url) && Objects.equals(this.type, that.type) && Objects.equals(this.secret, that.secret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.id, this.url, this.type, this.secret);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SendWebhookJob.class.getSimpleName() + "[", "]")
            .add("id='" + this.id + "'")
            .add("url='" + this.url + "'")
            .add("type='" + this.type + "'")
            .add("secret='" + this.secret + "'")
            .add("createdAt=" + this.createdAt)
            .toString();
    }
}
