package io.papermc.hangar.model.internal.job;

import io.papermc.hangar.model.db.JobTable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SendMailJob extends Job {

    private String subject;
    private String recipient;
    private String text;

    public SendMailJob(final String subject, final String recipient, final String text) {
        super(JobType.SEND_EMAIL);
        this.subject = subject;
        this.recipient = recipient;
        this.text = text;
    }

    SendMailJob() {
        super(JobType.SEND_EMAIL);
    }


    public String getSubject() {
        return this.subject;
    }

    public String getRecipient() {
        return this.recipient;
    }

    public String getText() {
        return this.text;
    }

    @Override
    public void loadFromProperties() {
        if (this.getJobProperties() == null) {
            return;
        }
        if (this.getJobProperties().containsKey("subject")) {
            this.subject = this.getJobProperties().get("subject");
        }
        if (this.getJobProperties().containsKey("recipient")) {
            this.recipient = this.getJobProperties().get("recipient");
        }
        if (this.getJobProperties().containsKey("text")) {
            this.text = this.getJobProperties().get("text");
        }
    }

    @Override
    public void saveIntoProperties() {
        final Map<String, String> properties = new HashMap<>();
        properties.put("subject", this.subject);
        properties.put("recipient", this.recipient);
        properties.put("text", this.text);
        this.setJobProperties(properties);
    }

    public static SendMailJob loadFromTable(final JobTable table) {
        final SendMailJob job = new SendMailJob();
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
        final SendMailJob that = (SendMailJob) o;
        return Objects.equals(this.subject, that.subject) && Objects.equals(this.recipient, that.recipient) && Objects.equals(this.text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.subject, this.recipient, this.text);
    }

    @Override
    public String toString() {
        return "SendMailJob{" +
            "subject='" + this.subject + '\'' +
            ", recipient='" + this.recipient + '\'' +
            ", text='" + this.text + '\'' +
            "} " + super.toString();
    }
}
