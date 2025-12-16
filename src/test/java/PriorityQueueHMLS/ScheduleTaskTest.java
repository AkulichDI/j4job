package PriorityQueueHMLS;

import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static PriorityQueueHMLS.Position.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class ScheduleTaskTest {

    @Test
    void whenTwoDirectorTaskThenReadMethodTest() {
        Comparator<Task> comparator = new TaskByPositionAsc().thenComparing(new TaskByUrgencyDesc());
        ScheduleTask scheduleTask = new ScheduleTask(comparator);
        scheduleTask.addTask(new Task(DEPARTMENT_HEAD, "description_1", 10));
        scheduleTask.addTask(new Task(DIRECTOR, "description_2", 4));
        scheduleTask.addTask(new Task(DIRECTOR, "description_3", 8));
        String expected = "description_3";
        String result = scheduleTask.readTask().description();
        assertThat(result).isEqualTo(expected);
    }
    @Test
    void whenTwoDepartmentHeadTaskAndEqualUrgencyThenGetMethodTest() {
        Comparator<Task> comparator = new TaskByPositionAsc().thenComparing(new TaskByUrgencyDesc());
        ScheduleTask scheduleTask = new ScheduleTask(comparator);
        scheduleTask.addTask(new Task(DEPARTMENT_HEAD, "description_1", 7));
        scheduleTask.addTask(new Task(MANAGER, "description_2", 10));
        scheduleTask.addTask(new Task(DEPARTMENT_HEAD, "description_3", 7));
        String expected = "description_1";
        String result = scheduleTask.getTask().description();
        assertThat(result).isEqualTo(expected);
        scheduleTask.getTask();
        expected = "description_2";
        result = scheduleTask.getTask().description();
        assertThat(result).isEqualTo(expected);
    }




}