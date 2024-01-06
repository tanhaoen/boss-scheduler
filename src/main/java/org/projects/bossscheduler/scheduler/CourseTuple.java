package org.projects.bossscheduler.scheduler;

import java.util.Objects;

public class CourseTuple {
    private int c;
    private int d;
    private int s;

    public CourseTuple(int c, int d, int s) {
        this.c = c;
        this.d = d;
        this.s = s;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CourseTuple other = (CourseTuple) obj;
        return c == other.c && d == other.d && s == other.s;
    }

    @Override
    public int hashCode() {
        return Objects.hash(c, d, s);
    }
}
