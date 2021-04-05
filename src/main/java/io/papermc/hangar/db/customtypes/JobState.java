package io.papermc.hangar.db.customtypes;

import org.postgresql.util.PGobject;

import java.util.Objects;

public class JobState extends PGobject {

    public static final JobState NOT_STARTED = new JobState("not_started");
    public static final JobState STARTED = new JobState("started");
    public static final JobState DONE = new JobState("done");
    public static final JobState FATAL_FAILURE = new JobState("fatal_failure");

    private String value;

    public JobState(String value) {
        setType("job_state");
        this.value = value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;

        JobState that = (JobState) obj;

        return Objects.equals(this.value, that.value);
    }

    @Override
    public String toString() {
        return "JobState{value:" + this.value + "}";
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
