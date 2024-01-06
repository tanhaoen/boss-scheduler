package org.projects.bossscheduler.scheduler;

public class Course {
    private String id;
    private String section;
    private String day;
    private int slot;
    private String instructor;

    public Course(String id, String section, String day, int slot, String instructor) {
        this.id = id;
        this.section = section;
        this.day = day;
        this.slot = slot;
        this.instructor = instructor;
    }

    public String getId() {
        return this.id;
    }

    public String getSection() {
        return this.section;
    }

    public String getDay() {
        return this.day;
    }

    public int getSlot() {
        return this.slot;
    }

}
