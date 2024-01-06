package org.projects.bossscheduler.scheduler;

import java.util.Map;
import java.util.List;

public class SchedulerPojo {
    public Map<Integer,String> courseIdMap;
    public Map<Integer,String> dayMap;
    public Map<Integer,String> slotMap;
    public List<List<List<Integer>>> solutions;
    public int numSolutions;

    public SchedulerPojo(Map<Integer,String> courseIdMap, Map<Integer,String> dayMap, Map<Integer,String> slotMap,
                         List<List<List<Integer>>> solutions) {
        this.courseIdMap = courseIdMap;
        this.dayMap = dayMap;
        this.slotMap = slotMap;
        this.solutions = solutions;
        this.numSolutions = solutions.size();
    }
}
