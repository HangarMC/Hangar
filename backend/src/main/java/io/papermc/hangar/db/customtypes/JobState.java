package io.papermc.hangar.db.customtypes;

import java.util.Objects;
import org.postgresql.util.PGobject;

public class JobState extends PGobject {

    public static final JobState NOT_STARTED = new JobState("not_started");
    public static final JobState STARTED = new JobState("started");
    public static final JobState DONE = new JobState("done");
    public static final JobState FATAL_FAILURE = new JobState("fatal_failure");

    public JobState() {
        //
    }

    public JobState(final String value) {
        this.setType("job_state");
        this.value = value;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;

        final JobState that = (JobState) obj;

        return Objects.equals(this.value, that.value);
    }

    @Override
    public String toString() {
        return "JobState{value:" + this.value + "}";
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (this.value != null ? this.value.hashCode() : 0);
        return result;
    }
}
