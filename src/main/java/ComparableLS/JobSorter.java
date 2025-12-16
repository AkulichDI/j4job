package ComparableLS;

import java.util.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
public class JobSorter {

    public static void main(String[] args) {
        List<Job> jobs = Arrays.asList(
                new Job("Fix bug", 1),
                new Job("Fix bug", 4),
                new Job("Fix bug", 2),
                new Job("X task", 0)
        );
        Comparator<Job> combine = new JobAscByName()
                .thenComparing(new JobAscByPriority())
                .thenComparing(new JobDescByName())
                .thenComparing(new JobDescByPriority());
        Collections.sort(jobs, combine);
        System.out.println(jobs);
    }
    
}
