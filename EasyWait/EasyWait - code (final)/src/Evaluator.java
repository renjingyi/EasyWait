
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Evaluator {
	/*
	 * This class is used for evaluating the results from methods
	 */
	public static int methodCheck(ArrayList<Object[]> results) {
		
		//use the objective function to do the evaluation
		ArrayList<Integer> waitingTime = new ArrayList<Integer>();
		ArrayList<Integer> groupNumber = new ArrayList<Integer>();
		
		for(Object[] result: results) {
			waitingTime.add((Integer) result[3]);
			groupNumber.add((Integer) result[1]);
		}
		
		int i = 0;
		int length = waitingTime.size();
		int waitingTimeSum = 0;
		
		for(i = 0; i < length; i++) {
			int waiting = waitingTime.get(i).intValue();
			int group = groupNumber.get(i).intValue();
			waitingTimeSum = waitingTimeSum + waiting * group;
		}
		
		return waitingTimeSum;
	}
	
	public static int incomeCheck(ArrayList<Object[]> results) {
		
		int totalIncome = 0;
		
		for(Object[] result: results) {
			totalIncome = totalIncome + (int) result[4];
		}
		
		return totalIncome;
	}
	
	public static int giveSuggestionNumber(double forOne, double forTwo, double forThree, String minORmax) {
		double compare = 0;
		
		//find the largest result
		//larger the better
		if(minORmax.equalsIgnoreCase("max")) {
			compare = forOne > forTwo ? forOne : forTwo;
			compare = compare > forThree ? compare : forThree;
			if(compare == forOne)
				return 1;
			else if(compare == forTwo)
				return 2;
			else
				return 3;
		}
		//find the smallest result
		//smaller the better
		else {
			compare = forOne < forTwo ? forOne : forTwo;
			compare = compare < forThree ? compare : forThree;
			if(compare == forOne)
				return 1;
			else if(compare == forTwo)
				return 2;
			else
				return 3;
		}
		
	}
	
	public static void printSuggestion(int number, boolean writeToFile, BufferedWriter writer) throws IOException {
		//if no need to write the results into file
		if(!writeToFile) {
			switch (number) {
			case 1: System.out.println("Method one is the best.");
				break;
			case 2: System.out.println("Method two is the best.");
			    break;
			case 3: System.out.println("Method three is the best.");
			    break;
			default: System.out.println("Error. Invaild method number.");
			    break;
			}
		}
		else {
//			String outputFile = "The restaurant's results.txt";
//			FileWriter write = new FileWriter(outputFile, true);
//			BufferedWriter writer = new BufferedWriter(write);
			
			switch (number) {
			case 1: writer.write("Method one is the best.");
			        writer.newLine();
				break;
			case 2: writer.write("Method two is the best.");
	                writer.newLine();
			    break;
			case 3: writer.write("Method three is the best.");
	                writer.newLine();
			    break;
			default: writer.write("Error. Invaild method number.");
	                 writer.newLine();
			    break;
			}
			
//			writer.close();
//			write.close();
		}
		
		
	}
	
	public static double scoreTheMethod(int servedGroups, int earnedMoney, int sumedTime) {
		double score = 0;
		
		//system uses a formula to calculate a score for each method
		score = (0.2 * servedGroups + 0.3 * earnedMoney) / sumedTime;
		
		return score;
	}
	
	public static void recordRecommendationTimes(double[] records, int methodNumber) {
		//the value in the corresponding method number index will be plus 1
		records[methodNumber - 1] += 1;
	}
	
	public static void giveFinalOutput(double[] records, int counter) throws IOException {
		int highlyRecommendMethodNumber = 0;
		
		//this function also wants to give the system output
		System.out.println("MethodOne MethodTwo MethodThree");
		for(int i = 0; i < 3; i++) {
			double percentage = records[i] * 100 / counter;
			System.out.print(percentage + "% ");
			records[i] = percentage;
		}
		System.out.println(); 
		System.out.println();
		
		double max = 0;
		for(int i = 0; i < 3; i++) {
			//considering the 'equal' situation, the system prefers the afterward method if it finds two same results
			if(records[i] >= max) {
				highlyRecommendMethodNumber = i + 1;
				max = records[i];
			}
		}
		//print the final suggestion
		printSuggestion(highlyRecommendMethodNumber, false, null);
	}
}
