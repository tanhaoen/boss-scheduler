package org.projects.bossscheduler.scheduler;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.stream.IntStream;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import com.google.ortools.sat.*;
import com.google.ortools.Loader;

public class Scheduler {
    private static int numCourses;
    final private static int numDays = 5;
    final private static int numSlots = 4;
    final private int[] allCourses;
    final private static int[] allDays = IntStream.range(0, numDays).toArray();
    final private static int[] allSlots = IntStream.range(0, numSlots).toArray();
    final private static Map<String, Integer> dayMapping = new HashMap<>() {{
        put("MON", 0);
        put("TUE", 1);
        put("WED", 2);
        put("THU", 3);
        put("FRI", 4);
    }};
    final public static Map<Integer, String> invDayMap = new HashMap<>() {{
        put(0, "MON");
        put(1, "TUE");
        put(2, "WED");
        put(3, "THU");
        put(4, "FRI");
    }};
    final public static Map<Integer, String> invSlotMap = new HashMap<>() {{
        put(0, "0815-1130");
        put(1, "1200-1315");
        put(2, "1530-1845");
    }};
    private static Set<Integer> preferredDays;
    private Map<String, Integer> courseIdMapping = new HashMap<>();
    private static Map<Integer, String> invCourseIdMap = new HashMap<>();
    private Literal[][][] slotsLiteral;
    private int[][][] preferences;
    private Set<CourseTuple> solutionSpace;
    private static CpModel model = new CpModel();
    private static CpSolver solver = new CpSolver();
    private static List<List<List<Integer>>> allSolutions = new ArrayList<>();

    public Scheduler (int numCourses, Set<Integer> preferredDays) {
        Scheduler.numCourses = numCourses;
        this.allCourses = IntStream.range(0, numCourses).toArray();
        Scheduler.preferredDays = preferredDays;
        this.slotsLiteral = new Literal[numCourses][numDays][numSlots];
        this.preferences = new int[numCourses][numDays][numSlots];
        solver.getParameters().setLinearizationLevel(0);
        solver.getParameters().setEnumerateAllSolutions(true);
    }

    public void setSolutionSpace() throws IOException {
        String line;
        Set<CourseTuple> solutionSpace = new HashSet<>();

        BufferedReader br = new BufferedReader(new FileReader("/Users/nathaniel/boss-scheduler/sample_data.csv"));

        br.readLine();

        while ((line = br.readLine()) != null) {
            String[] course = line.split(",");
            String courseId = course[0];
            String day = course[2];
            int slot = Integer.parseInt(course[3]);

//                Course courseObject = new Course(courseId, course[1], day, slot, course[4]);
//                allCourseObjects.add(courseObject);

            if (!courseIdMapping.containsKey(courseId)) {
                int currIdx = courseIdMapping.size();
                courseIdMapping.put(courseId, currIdx);
            }

            int c = courseIdMapping.get(courseId);
            int d = dayMapping.get(day);
            int s = slot-1;
            solutionSpace.add(new CourseTuple(c, d, s));
        }

        this.solutionSpace = solutionSpace;
    }

    public void limitSolutionSpace() {
        // Limit the solution space
        // TODO: Figure out addAllowedAssignments or addForbiddenAssignments
        for (int c : this.allCourses) {
            for (int d : allDays) {
                for (int s : allSlots) {
                    this.slotsLiteral[c][d][s] = model.newBoolVar(
                            "slots_c" + c + "d" + d + "s" + s);
                    if (!this.solutionSpace.contains(new CourseTuple(c, d, s))) {
                        model.addEquality(this.slotsLiteral[c][d][s], 0);
                    }
                }
            }
        }
    }

    public void addConstraints() {
        // Each daySlot should have at most one course
        for (int d : allDays) {
            for (int s : allSlots) {
                List<Literal> daySlot = new ArrayList<>();
                for (int c : this.allCourses) {
                    daySlot.add(this.slotsLiteral[c][d][s]);
                }
                model.addAtMostOne(daySlot);
            }
        }

        System.out.println("Added constraint: At most one course per day/slot");

        // Each course should only be assigned one daySlot
        for (int c : this.allCourses) {
            List<Literal> course = new ArrayList<>();
            for (int d : allDays) {
                for (int s : allSlots) {
                    course.add(this.slotsLiteral[c][d][s]);
                }
            }
            model.addExactlyOne(course);
        }

        System.out.println("Added constraint: Exactly one day/slot per course");

        // Each day should be assigned max 2 courses
        // TODO: Figure out addMaxEquality
        for (int d : allDays) {
            LinearExprBuilder day = LinearExpr.newBuilder();
            for (int c : this.allCourses) {
                for (int s : allSlots) {
                    day.add(this.slotsLiteral[c][d][s]);
                }
            }
            model.addLinearConstraint(day, 0, 2);
        }

        System.out.println("Added constraint: Max 2 courses per day");

        // There should not be back-to-back classes
        // i.e. the sum of two adjacent slots on the same day should be at most one
        for (int d : allDays) {
            for (int s = 1; s < numSlots; s++) {
                List<Literal> daySlot = new ArrayList<>();
                for (int c : this.allCourses) {
                    daySlot.add(this.slotsLiteral[c][d][s]);
                    daySlot.add(this.slotsLiteral[c][d][s-1]);
                }
                model.addAtMostOne(daySlot);
            }
        }

        System.out.println("Added constraint: No back-to-back classes");

//        // Soft constraints (preferences)
//        // Prefer certain days
        for (int d : allDays) {
            if (preferredDays.contains(d)) {
                for (int c : this.allCourses) {
                    for (int s : allSlots) {
                        this.preferences[c][d][s] = 1;
                    }
                }
            }
        }
    }

    public void setInverseCourseIdMappings() {
        for (HashMap.Entry<String, Integer> entry : this.courseIdMapping.entrySet()) {
            invCourseIdMap.put(entry.getValue(), entry.getKey());
        }
    }

    public Map<Integer,String> getInverseCourseIdMappings() {
        return invCourseIdMap;
    }

    public void startSolver(Scheduler scheduler, boolean verbose, int solutionLimit) {
        // Register solutions callback
        SolutionCollector cb = new SolutionCollector(scheduler.allCourses, Scheduler.allDays, Scheduler.allSlots,
                scheduler.slotsLiteral, solutionLimit, verbose);

        // Maximize objective function according to soft constraints
        LinearExprBuilder obj = LinearExpr.newBuilder();
        for (int c : allCourses) {
            for (int d : allDays) {
                for (int s : allSlots) {
                    obj.addTerm(this.slotsLiteral[c][d][s], this.preferences[c][d][s]);
                }
            }
        }
        model.maximize(obj);

        CpSolverStatus status = solver.solve(model);

        int result = (int) solver.objectiveValue();

        System.out.println("Objective value is " + result);

        model.addEquality(obj, result);

        model.clearObjective();

        Scheduler.allSolutions = new ArrayList<>();

        solver.solve(model,cb);

        System.out.println("Status: " + status);
        System.out.println(cb.getSolutionCount() + " solutions found in " + solver.wallTime() + "milliseconds");
    }

    public List<List<List<Integer>>> getAllSolutions() {
        return allSolutions;
    }

    class SolutionCollector extends CpSolverSolutionCallback {
        private int solutionCount;
        private final int[] allCourses;
        private final int[] allDays;
        private final int[] allSlots;
        private final Literal[][][] slots;
        private final int solutionLimit;
        private final boolean verbose;

        public SolutionCollector(
                int[] allCourses, int[] allDays, int[] allSlots, Literal[][][] slots, int limit, boolean verbose) {
            this.solutionCount = 0;
            this.allCourses = allCourses;
            this.allDays = allDays;
            this.allSlots = allSlots;
            this.slots = slots;
            this.solutionLimit = limit;
            this.verbose = verbose;
        }

        @Override
        public void onSolutionCallback() {
            if (verbose) {System.out.printf("Solution #%d:%n", this.solutionCount+1);}
            List<List<Integer>> solution = new ArrayList<>();
            for (int d : allDays) {
                for (int s : allSlots) {
                    for (int c : allCourses) {
                        if (slots[c][d][s] != null && booleanValue(slots[c][d][s])) {
                            String stringCourseId = invCourseIdMap.get(c);
                            String stringDay = invDayMap.get(d);
                            String stringSlot = invSlotMap.get(s);
                            if (verbose) {System.out.printf("  %s: %s %s\n", stringCourseId, stringDay, stringSlot);}
                            List<Integer> current = Arrays.asList(c, d, s);
                            solution.add(current);
                        }
                    }
                }
            }
            solutionCount++;
            allSolutions.add(solution);
            if (solutionCount >= solutionLimit) {
                System.out.printf("Stop search after %d solutions%n", solutionLimit);
                stopSearch();
            }
        }

        public int getSolutionCount() {
            return solutionCount;
        }
    }

    public static void main(String[] args) {
        Scheduler scheduler = new Scheduler(Scheduler.numCourses, Scheduler.preferredDays);

        boolean verbose = Boolean.parseBoolean(args[0]);
        int limit = Integer.parseInt(args[1]);

        Loader.loadNativeLibraries();

        try {
            scheduler.setSolutionSpace();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        scheduler.limitSolutionSpace();
        scheduler.addConstraints();
        scheduler.setInverseCourseIdMappings();

        scheduler.startSolver(scheduler, verbose, limit);
    }
}