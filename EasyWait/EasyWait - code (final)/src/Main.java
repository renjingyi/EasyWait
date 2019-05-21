import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalTime;
import java.util.ArrayList;

/*
 * ***************************************************************************************************************
 * ***************************************************************************************************************
 * This project is aiming to implement an auto-operating system to help restaurants with long waiting line
 *     using three methods to schedule outside groups settled at tables inside, computing and comparing three
 *     methods results, and giving a best solution under three methods.
 *     
 * Several defaulted values:
 *     1. assuming that
 *            all the naming with 'table' refer to the restaurant inside situations while
 *            all the naming with 'group' refer to the restaurant outside situations
 *     2. assuming that
 *            variables 'tableNo' and 'groupNo' are all positive integers from 0, representing indices
 *     2. assuming that 
 *            this system presently is not allowed groups to be divided or merged to fit table supplies
 *     3. assuming that
 *            the largest group size is not bigger than the largest table size, which means that a group can
 *            be fit in at least one table
 *     4. assuming that
 *            the customized restaurant only open at 9:00 AM and close at 22:00 PM, running 13 hours per day;
 *     5. assuming that 
 *            outside groups start coming from 8:30 AM, here because program is designed to generate random
 *            outside group arriving time stamp every 30 minutes(see in GenerateRandomFigures.java)
 * 
 * Customized restaurant features designed by users, those restaurant managers:
 *     1. total table number
 *     2. small middle and large table numbers
 *     3. small middle and large table sizes
 *     these figures are interactive system inputs
 * 
 * INPUT:
 *     1. interactive customized restaurant features:
 *            insideTables, smallTables, middleTables, largeTables, smallTableSize, middleTableSize, largeTableSize
 * 
 * Auto-generated figures:
 *     1. random-generated Integers to fake outside groups' sizes: 
 *            outsideGroupSizes
 *     2. random-generated LocalTimes to fake outside groups' arriving time stamp in the waiting line:
 *            outsideGroupTimes
 *     3. random-generated Integers to fake meal times which are deemed as hypothetically simulated meal times:
 *            fakeMealTime
 *     4. random-generated Integers to fake meal times which are deemed as real meal times:
 *            realMealTime
 *     5. random-generated Integers to fake earning money for different sizes of groups:
 *            moneyEarned
 *     
 * Coding process:
 *     1. interactively customizing restaurant features
 *     2. creating two ArrayLists, one storing groups' sizes and another storing groups' arriving time stamp
 *     3. once generating a outside group, applying three methods respectively to schedule this group to restaurant
 *            and calculate the method result by using objective function
 *     4. after a whole day operation, comparing and analyzing these three methods, following the rule that
 *            the total served group number: totalServedGroups
 *            the total money earned: totalMoneyEarned
 *            the total time waited by groups: totalWaitingTime
 *     5. giving a best solution for users
 *     
 * OUTPUT:
 *     1. a table shows that
 *            how many groups restaurant has served under three methods
 *            how much restaurant has earned under three methods
 *            how long that all groups has waited under three methods
 *     2. a suggestion with best performance for users to decide which method they should apply
 *     
 * 
 * Author: REN, JINGYI
 * Create time: November 19th, 2018
 * ***************************************************************************************************************
 * ***************************************************************************************************************
 */
public class Main {

	public static void main(String[] args) {
		
		/*
		 * *******************************************************************************************************
		 * *******************************************************************************************************
		 * Coding process 1:
		 *     1. interactively customizing restaurant features
		 * *******************************************************************************************************
		 * *******************************************************************************************************
		 */
		System.out.println("Welcome to your restaurant!");
		System.out.println();
		
		try {
			String inputFile = "The customized restaurant.txt";
			File file = new File(inputFile);
			
			if(file.isFile() && file.exists()) {
				FileReader read = new FileReader(file);
                BufferedReader reader = new BufferedReader(read);
                String line = null;

    			String outputFile = "The restaurant's results.txt";
                FileWriter write = new FileWriter(outputFile);
                BufferedWriter writer = new BufferedWriter(write);
                
                writer.write("Welcome to your restaurant!");
                writer.newLine();
                writer.newLine();
                
                //this counter is used to count how many lines in the file
                int counter = 0;
                //this array is used to record the numbers of recommendation for three methods
                double[] recommendationTimes = {0, 0, 0};
                
                //in the file, one line represents one customized restaurant
                while((line = reader.readLine()) != null) {
                	
                	counter += 1;
                	writer.write("***************************************SPLIT****************************************");
                	writer.newLine();
                	writer.write("System is running No." + counter + " your customized restaurant.");
                	writer.newLine();
                	writer.newLine();
                	
                	String[] figures = line.split(" ");
                	//get the restaurant figures from the file
                	int insideTables = Integer.parseInt(figures[0]);
                	int smallTables = Integer.parseInt(figures[1]);
                	int middleTables = Integer.parseInt(figures[2]);
                	int largeTables = Integer.parseInt(figures[3]);
                	int smallTableSize = Integer.parseInt(figures[4]);
                	int middleTableSize = Integer.parseInt(figures[5]);
                	int largeTableSize = Integer.parseInt(figures[6]);
                	
                	writer.write("Your personalized restaurant has "+insideTables+" tables in total which includes "
            		        +smallTables+" small tables fitting "+smallTableSize+" people, "+middleTables+" middle tables fitting "
            				+middleTableSize+" people and "+largeTables+" large tables fitting "+largeTableSize+" people.");
                	writer.newLine();
                	writer.newLine();
            		writer.write("Congrats. You can run your restaurant.");
            		writer.newLine();
            		writer.newLine();
                	
                	//read one line, create one restaurant, do once operation
            		//start the operation
            		String start = "yes";
            		
            		/*
            		 * *******************************************************************************************************
            		 * *******************************************************************************************************
            		 * Coding process 2 & 3:
            		 *     2. creating two ArrayLists, one storing groups' sizes and another storing groups' arriving time stamp
            		 *     3. once generating a outside group, applying three methods respectively to schedule this group to 
            		 *        restaurant and calculate the method result by using objective function
            		 * *******************************************************************************************************
            		 * *******************************************************************************************************
            		 */
            		//here, 'start' means user allows system to run
            		if(start.equalsIgnoreCase("Yes")) {
            			
            			//first generate objects
            			Restaurant myRestaurant1 = new Restaurant(insideTables, smallTables, middleTables, largeTables, 
            					                                       smallTableSize, middleTableSize, largeTableSize);
            			WaitingLine myWaitingLine1 = new WaitingLine(smallTableSize, middleTableSize, largeTableSize);
            			
            			//create two more Restaurant and WaitingLine objects for methodTwo and methodThree
            			Restaurant myRestaurant2 = new Restaurant(insideTables, smallTables, middleTables, largeTables, 
                                smallTableSize, middleTableSize, largeTableSize);
            			WaitingLine myWaitingLine2 = new WaitingLine(smallTableSize, middleTableSize, largeTableSize);
            			Restaurant myRestaurant3 = new Restaurant(insideTables, smallTables, middleTables, largeTables, 
                                smallTableSize, middleTableSize, largeTableSize);
            			WaitingLine myWaitingLine3 = new WaitingLine(smallTableSize, middleTableSize, largeTableSize);
            			
            			//secondly using method to generate a outside group
            			LocalTime newGroupTime = GenerateRandomFigures.randomTime(LocalTime.of(8, 30));
            			//generate a random figure as its group size and put this figure in the list
            			int newGroupSize = GenerateRandomFigures.randomNumber(1, largeTableSize);
            			
            			//here, 'start' is symbol to judge whether system should stop
            			while(start.equalsIgnoreCase("Yes")) {
            				
            				//if generated time is after closing time 22:00, then give a message and stop the system
            				if(newGroupTime.isAfter(LocalTime.of(22, 0))) {
            					start = "stop";
            				}
            				//if it is legal, then deal with this new generated group
            				else {
            					//put this group in waiting line
            					myWaitingLine1.addNewGroupTime(newGroupTime);
            					myWaitingLine1.addNewGroupSize(newGroupSize);
            					
            					//copy two more WaitingLine objects for methodTwo and methodThree
            					myWaitingLine2.addNewGroupTime(newGroupTime);
            					myWaitingLine2.addNewGroupSize(newGroupSize);
            					myWaitingLine3.addNewGroupTime(newGroupTime);
            					myWaitingLine3.addNewGroupSize(newGroupSize);
            				}
            				//generate a new group time and a new group size
            				newGroupTime = GenerateRandomFigures.randomTime(newGroupTime);
            				newGroupSize = GenerateRandomFigures.randomNumber(1, largeTableSize);
            			}
            			
            			//after generating the outside lines
            			writer.write("System has generated " + myWaitingLine1.getOutsideGroupsNumber() + " groups in total in one day.");
            			writer.newLine();
            			writer.newLine();
            			//apply three different methods to solve the scheduling problem
            			Operations.methodOne(myRestaurant1, myWaitingLine1, writer);
            			Operations.methodTwo(myRestaurant2, myWaitingLine2, writer);
            			Operations.methodThree(myRestaurant3, myWaitingLine3, writer);
            			
            			//after doing the simulations, system gets the results from three methods
            			//then it needs evaluations
            			ArrayList<Object[]> results1 = Operations.getOperateResults1();
            			ArrayList<Object[]> results2 = Operations.getOperateResults2();
            			ArrayList<Object[]> results3 = Operations.getOperateResults3();
            			//    1. evaluate the total groups the restaurant has served for each
            			writer.write("Restaurant has served " + results1.size() + " groups under Method One.");
            			writer.newLine();
            			writer.write("Restaurant has served " + results2.size() + " groups under Method Two.");
            			writer.newLine();
            			writer.write("Restaurant has served " + results3.size() + " groups under Method Three.");
            			writer.newLine();
            			int numberSuggestion = Evaluator.giveSuggestionNumber(results1.size(), results2.size(), results3.size(), "max");
            			Evaluator.printSuggestion(numberSuggestion, true, writer);
            			writer.newLine();
            			//    2. evaluate the total profit the restaurant has earned for each
            			int totalMoney1 = Evaluator.incomeCheck(results1);
            			int totalMoney2 = Evaluator.incomeCheck(results2);
            			int totalMoney3 = Evaluator.incomeCheck(results3);
            			writer.write("The total money restaurant has earned under Method One is " + totalMoney1);
            			writer.newLine();
            			writer.write("The total money restaurant has earned under Method Two is " + totalMoney2);
            			writer.newLine();
            			writer.write("The total money restaurant has earned under Method Three is " + totalMoney3);
            			writer.newLine();
            			int moneySuggestion = Evaluator.giveSuggestionNumber(totalMoney1, totalMoney2, totalMoney3, "max");
            			Evaluator.printSuggestion(moneySuggestion, true, writer);
            			writer.newLine();
            			//    3. evaluate the total waiting for each
            			int totalWaiting1 = Evaluator.methodCheck(results1);
            			int totalWaiting2 = Evaluator.methodCheck(results2);
            			int totalWaiting3 = Evaluator.methodCheck(results3);
            			writer.write("The total waiting time for Method One is " + totalWaiting1);
            			writer.newLine();
            			writer.write("The total waiting time for Method Two is " + totalWaiting2);
            			writer.newLine();
            			writer.write("The total waiting time for Method Three is " + totalWaiting3);
            			writer.newLine();
            			int timeSuggestion = Evaluator.giveSuggestionNumber(totalWaiting1, totalWaiting2, totalWaiting3, "min");
            			Evaluator.printSuggestion(timeSuggestion, true, writer);
            			writer.newLine();
            			
            			//give each method a final score
            			double score1 = Evaluator.scoreTheMethod(results1.size(), totalMoney1, totalWaiting1);
            			double score2 = Evaluator.scoreTheMethod(results2.size(), totalMoney2, totalWaiting2);
            			double score3 = Evaluator.scoreTheMethod(results3.size(), totalMoney3, totalWaiting3);
            			writer.write("The final score for Method One is " + score1);
            			writer.newLine();
            			writer.write("The final score for Method Two is " + score2);
            			writer.newLine();
            			writer.write("The final score for Method Three is " + score3);
            			writer.newLine();
            			writer.newLine();
            			writer.write("System's suggestion: ");
            			int thisRestaurantSuggestion = Evaluator.giveSuggestionNumber(score1, score2, score3, "max");
            			Evaluator.printSuggestion(thisRestaurantSuggestion, true, writer);
            			writer.newLine();
            			writer.newLine();
            			
            			//record the recommendation into the array
            			Evaluator.recordRecommendationTimes(recommendationTimes, thisRestaurantSuggestion);
            		}
            		
                } 
                //finish all the lines in the INPUT file, then system gets an modified recommendationTimes array
                //give the final output
                Evaluator.giveFinalOutput(recommendationTimes, counter);
                //close the file
                read.close();
                reader.close();
                writer.close(); 
                write.close();
			}
			else {
				System.out.println("Error. Couldn't find the file.");
			}
			
		}
		catch (Exception e) {
			System.out.println("Error. Couldn't read the file.");
            e.printStackTrace();
		}
		
	} 

}
