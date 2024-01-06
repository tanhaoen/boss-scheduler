package org.projects.bossscheduler;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.util.*;

import static java.lang.Math.abs;

public class ConstraintTests {
    private static Scheduler scheduler;
    private static List<List<List<Integer>>> allSolutions;
    @BeforeAll
    public static void setup() {
        Scheduler scheduler = new Scheduler(4, new HashSet<>(Set.of(0, 1, 2)));
        String[] arguments = new String[] {"true", "9999"};
        scheduler.main(arguments);
        ConstraintTests.scheduler = scheduler;
        ConstraintTests.allSolutions = scheduler.getAllSolutions();
        System.out.println("Size of solutions is " + allSolutions.size());
    }

    @Test
    @DisplayName("Check if each solution is unique")
    public void unique() {
        Set<List<List<Integer>>> solutionSet = new HashSet<>();
//        System.out.println("Size of solutions is " + allSolutions.size());

//         Failed test example
//        List<List<List<Integer>>> allSolutions = new ArrayList<>(Arrays.asList(
//                Arrays.asList(
//                Arrays.asList(0, 0, 1),
//                Arrays.asList(1, 0, 1),
//                Arrays.asList(2, 2, 0)
//        ),
//                Arrays.asList(
//                        Arrays.asList(0, 0, 1),
//                        Arrays.asList(1, 0, 1),
//                        Arrays.asList(2, 2, 0)
//                )));

        for (List<List<Integer>> solution : allSolutions) {
            if (!solutionSet.contains(solution)) {
                solutionSet.add(solution);
            } else {
                System.out.println(solution);
                Assertions.fail("Repeated solution found");
            }
        }
    }

    @Test
    @DisplayName("Test constraint 1: Each day/slot has only one course")
    public void constraint1() {
        // Failed test example
//        List<List<List<Integer>>> allSolutions = new ArrayList<>(Arrays.asList(
//                Arrays.asList(
//                Arrays.asList(0, 0, 1),
//                Arrays.asList(1, 0, 1),
//                Arrays.asList(2, 2, 0)
//        )));

        for (List<List<Integer>> solution : allSolutions) {
            Set<List<Integer>> daySlots = new HashSet<>();
            for (List<Integer> record : solution) {
                List<Integer> slice = record.subList(1, 3);
                if (!daySlots.contains(slice)) {
                    daySlots.add(slice);
                } else {
                    System.out.println(solution);
                    Assertions.fail("Constraint failed");
                }
            }
        }
    }

    @Test
    @DisplayName("Test constraint 2: Each course has only one day/slot")
    public void constraint2() {
        for (List<List<Integer>> solution : allSolutions) {
            Set<Integer> courses = new HashSet<>();
            for (List<Integer> record : solution) {
                Integer slice = record.get(0);
                if (!courses.contains(slice)) {
                    courses.add(slice);
                } else {
                    System.out.println(solution);
                    Assertions.fail("Constraint failed");
                }
            }
        }
    }

    @Test
    @DisplayName("Test constraint 3: Max 2 courses per day")
    public void constraint3() {
        System.out.println("Refer to constraint 4 instead");
    }

    @Test
    @DisplayName("Test constraint 4: No back-to-back classes")
    public void constraint4() {
        for (List<List<Integer>> solution : allSolutions) {
            Map<Integer, Integer> daySlots = new HashMap<>();
            for (List<Integer> record : solution) {
                Integer day = record.get(1);
                Integer slot = record.get(2);
                if (daySlots.containsKey(day)) {
                    if (abs(slot-daySlots.get(day)) <= 1) {
                        Assertions.fail("Constraint failed");
                    }
                } else {
                    daySlots.put(day, slot);
                }
            }
        }
    }
}
