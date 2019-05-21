import java.time.LocalTime;
import java.util.Random;

public class GenerateRandomFigures {
	
	public static int randomNumber(int min, int max) {
		/*
		 * Function aims to generate a random number between [min, max]
		 *   which means that each group outside should be fit in one corresponding inside table
		 * Function parameters:
		 *   max: the largest number for one group which depends on the inside large table size
		 *   min: which has the default value as 1 when generate personalized outside groups
		 */
		
		Random rand = new Random();
		//rand.nextInt(bound) generates the random number between [0, bound]
		//       so that '+1' makes the limit as [1, bound]
		int randNum = rand.nextInt(max) % (max - min + 1) + min;
		
		return randNum;
	}

	public static LocalTime randomTime(LocalTime lastTime) {
		/*
		 * Function aims to generate a new random time after the last group time
		 *     use 30 minutes as a time range limit
		 */
		LocalTime limit = lastTime.plusMinutes(30);
		
		long time = random(lastTime.toNanoOfDay(), limit.toNanoOfDay());
		LocalTime randTime = LocalTime.ofNanoOfDay(time).withNano(0);
		
		return randTime;
	}

	private static long random(long from, long to) {
		/*
		 * This function is serving the function above, 'randomTime(Time,Time)'
		 */
		long rtn = from + (long) (Math.random() * (to - from));
		//recursive
		if (rtn == from || rtn == to) {
			return random(from, to);
		}
		return rtn;
	}
	
	public static LocalTime timeAddMealTime(LocalTime beforeMealtime, int groupSize) {
		/*
		 * This function is to get a new time after adding a hypothetical meal time
		 */
		long mealTime = 0;
		LocalTime afterMealTime;
		
		//we'd like to designate a rough time range for generating a random meal time
		//lower and upper are computed in minutes
		int lower = 20 * groupSize;
		int upper = lower + 30;
		
		mealTime = (long)randomNumber(lower, upper);
		
		afterMealTime = beforeMealtime.plusMinutes(mealTime);
		
		return afterMealTime;
	}
	
	public static int moneyEarned(int groupSize) {
		/*
		 * This function is to simulate a fake figure for the money restaurant earns from customers
		 */
		int money = 0;
		//restaurant at least earns $15 per person, at most $30 per person
		int lower = 15 * groupSize;
		int upper = 30 * groupSize;
		
		money = randomNumber(lower, upper);
		
		return money;
	}
	
}
