package ComparableLS;

import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
class JobSorterTest {
    @Test
    void whenAscByName() {
        Comparator<Job> cmp = new JobAscByName();
        int rsl = cmp.compare(
                new Job("Fix bug", 1),
                new Job("Impl task", 1)
        );
        assertThat(rsl).isLessThan(0);
    }

    @Test
    void whenDescByName() {
        Comparator<Job> cmp = new JobDescByName();
        int rsl = cmp.compare(
                new Job("Fix bug", 1),
                new Job("Impl task", 1)
        );
        assertThat(rsl).isGreaterThan(0);
    }

    @Test
    void whenAscByPriority() {
        Comparator<Job> cmp = new JobAscByPriority();
        int rsl = cmp.compare(
                new Job("Task", 1),
                new Job("Task", 5)
        );
        assertThat(rsl).isLessThan(0);
    }

    @Test
    void whenDescByPriority() {
        Comparator<Job> cmp = new JobDescByPriority();
        int rsl = cmp.compare(
                new Job("Task", 1),
                new Job("Task", 5)
        );
        assertThat(rsl).isGreaterThan(0);
    }

    @Test
    void whenDescByNameThenDescByPriority() {
        Comparator<Job> cmp = new JobDescByName()
                .thenComparing(new JobDescByPriority());
        int rsl = cmp.compare(
                new Job("Fix bug", 1),
                new Job("Fix bug", 2)
        );
        assertThat(rsl).isGreaterThan(0);
    }

    @Test
    void whenAscByNameThenAscByPriority() {
        Comparator<Job> cmp = new JobAscByName()
                .thenComparing(new JobAscByPriority());
        int rsl = cmp.compare(
                new Job("Fix bug", 1),
                new Job("Fix bug", 2)
        );
        assertThat(rsl).isLessThan(0);
    }
}