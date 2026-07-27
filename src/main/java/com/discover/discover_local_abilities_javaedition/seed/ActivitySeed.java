package com.discover.discover_local_abilities_javaedition.seed;

import com.discover.discover_local_abilities_javaedition.model.Activity;
import com.discover.discover_local_abilities_javaedition.model.WorkingHours;
import com.discover.discover_local_abilities_javaedition.repository.ActivityRepository;
import com.discover.discover_local_abilities_javaedition.repository.WorkingHoursRepository;
import com.opencsv.CSVReader;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("seed")
public class ActivitySeed implements CommandLineRunner {

    private final ActivityRepository activityRepository;
    private final WorkingHoursRepository workingHoursRepository;
    private final JdbcTemplate jdbcTemplate;

    public ActivitySeed(ActivityRepository activityRepository,
                      WorkingHoursRepository workingHoursRepository,
                      JdbcTemplate jdbcTemplate) {
        this.activityRepository = activityRepository;
        this.workingHoursRepository = workingHoursRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE working_hours RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE activity RESTART IDENTITY CASCADE");

        List<Activity> activities = readActivities("data_cleaning/data/output/cleaned_activities.csv");
        activities = activityRepository.saveAll(activities); // now has generated IDs

        List<WorkingHours> workingHours = readWorkingHours("data_cleaning/data/output/working_hours.csv", activities);
        workingHoursRepository.saveAll(workingHours);
    }

    private List<Activity> readActivities(String path) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {
            reader.readNext(); // skip header
            List<Activity> result = new ArrayList<>();
            String[] row;
            while ((row = reader.readNext()) != null) {
                Activity a = new Activity();
                a.setName(row[0]);
                a.setPhoneNumber(row[1]);
                a.setLatitude(parseDouble(row[2]));
                a.setLongitude(parseDouble(row[3]));
                a.setRating(parseDouble(row[4]));
                a.setUserRatingCount((int) Double.parseDouble(row[5]));
                a.setType(row[6]);
                result.add(a);
            }
            return result;
        }
    }

    // Assumes working_hours.csv has an activity index/position column
    // matching the row order in cleaned_activities.csv (0-based).
    // If it references a real original ID instead, see note below.
    private List<WorkingHours> readWorkingHours(String path, List<Activity> activities) throws Exception {
        ClassPathResource resource = new ClassPathResource(path);
        try (CSVReader reader = new CSVReader(new InputStreamReader(resource.getInputStream()))) {
            reader.readNext(); // skip header
            List<WorkingHours> result = new ArrayList<>();
            String[] row;
            while ((row = reader.readNext()) != null) {
                WorkingHours wh = new WorkingHours();

                int activityId = Integer.parseInt(row[0]); // 1-based ID from CSV
                wh.setActivityId(activities.get(activityId - 1)); // convert to 0-based

                wh.setDayOfWeek(row[1]);
                wh.setOpenTime(parseTime(row[2]));
                wh.setClosedTime(parseTime(row[3]));
                wh.setBreakTimeStart(parseTime(row[4]));
                wh.setBreakTimeEnd(parseTime(row[5]));
                wh.set24h(Boolean.parseBoolean(row[6]));
                wh.setClosed(Boolean.parseBoolean(row[7]));

                result.add(wh);
            }
            return result;
        }
    }

    private Double parseDouble(String s) {
        return (s == null || s.isBlank()) ? null : Double.parseDouble(s);
    }

    private LocalTime parseTime(String s) {
        return (s == null || s.isBlank()) ? null : LocalTime.parse(s);
    }
}