package jimm.datavision.testdata;
import java.util.*;

/**
 * Generates test data for the jobs table. Used by the <code>CreateData</code>
 * classes found in the database subdirectories.
 *
 * @author Jim Menard, <a href="mailto:jim@jimmenard.com">jim@jimmenard.com</a>
 */
public class Job {

// public static final int NUM_JOBS = 73;
public static final int NUM_JOBS = 2000;
protected static final long MILLISECS_PER_DAY = 24L * 60L * 60L * 1000L;
protected static final String[] CITIES = {
    "New York", "Chicago", "London", "Tokyo", "Paris"
};

public int id, fk_office_id;
public Integer hourly_rate;
public String title, company, location, description;
public Calendar post_date;
public boolean visible;

public static List<Job> jobs() {
    Random rand = new Random();
    ArrayList<Job> jobs = new ArrayList<Job>();
    for (int i = 0; i < NUM_JOBS; ++i)
	jobs.add(new Job(rand, i));
    return jobs;
}

public Job(Random rand, int i)
{
    id = i;
    title = "This is the short description of job " + i;
    if (rand.nextInt(20) == 0) title += " " + title;
    fk_office_id = rand.nextInt(3) + 1;
    company = "Company " + i;
    location = CITIES[rand.nextInt(CITIES.length)];
    description = "This is the description of job " + i
	+ ". It could be much longer.";
    hourly_rate = i == 0 ? null : new Integer(i * 100);
    visible = true;

    long randomDaysInMillisecs = rand.nextInt(20) * MILLISECS_PER_DAY;
    Date t = new Date(System.currentTimeMillis() - randomDaysInMillisecs);
    post_date = Calendar.getInstance();
    post_date.setTime(t);
}

public String hourlyRateAsString() {
    return hourly_rate == null ? "NULL" : hourly_rate.toString();
}

}
