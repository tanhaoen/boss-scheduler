package org.projects.bossscheduler.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;

@ResponseBody
@RestController
@RequestMapping(path = "/scheduler")
public class SchedulerController {

	@GetMapping
	@ResponseBody
	public SchedulerPojo getSchedule(@RequestParam(name="num_courses") String numCourses,
												 @RequestParam(name="limit", required = false, defaultValue = "5") String solutionLimit,
												 @RequestParam(name="verbose", required = false, defaultValue = "true") String verbose) {
		Scheduler scheduler = new Scheduler(Integer.parseInt(numCourses), new HashSet<>(Set.of(0, 1, 2)));
		String[] arguments = new String[] {verbose, solutionLimit};
		scheduler.main(arguments);
		List<List<List<Integer>>> allSolutions = scheduler.getAllSolutions();

		Map<Integer, String> invDayMap = Scheduler.invDayMap;
		Map<Integer, String> invSlotMap = Scheduler.invSlotMap;
		Map<Integer, String> invCourseIdMap = scheduler.getInverseCourseIdMappings();

		System.out.println(invCourseIdMap);

		SchedulerPojo response = new SchedulerPojo(invCourseIdMap, invDayMap, invSlotMap, allSolutions);

		return response;
	}
}
