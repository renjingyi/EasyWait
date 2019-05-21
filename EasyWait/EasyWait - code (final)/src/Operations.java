import java.io.BufferedWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.time.temporal.ChronoUnit;

public class Operations {
	/*
	 * This class is used to manage all the operations between restaurant inside tables and outside groups
	 */
	
	//the following array list is used to store results for the outside groups, because outside two group lists 
	//    are not durable, which means that figures in the lists won't always stay, since system needs remove 
	//    the groups that have already arranged
	//this list will store 5 information as {this group arriving time stamp, this group size, 
	//                                         predicted waiting time, real waiting time, money earned}
	//three lists is respectively for storing three different methods' results
	private static ArrayList<Object[]> operateResults1 = new ArrayList<Object[]>();
	private static ArrayList<Object[]> operateResults2 = new ArrayList<Object[]>();
	private static ArrayList<Object[]> operateResults3 = new ArrayList<Object[]>();
	
	//getter
	public static ArrayList<Object[]> getOperateResults1() {
		return operateResults1;
	}

	public static ArrayList<Object[]> getOperateResults2() {
		return operateResults2;
	}

	public static ArrayList<Object[]> getOperateResults3() {
		return operateResults3;
	}

	public static void printOperateResults(ArrayList<Object[]> results, BufferedWriter fileWriter) throws IOException {
		for(Object[] everyGroup: results) {
			for(int i = 0; i < everyGroup.length; i++) {
				fileWriter.write(everyGroup[i] + " ");
			}
			fileWriter.newLine();
		}
	}

	public static void methodOne(Restaurant myRst, WaitingLine myLine, BufferedWriter fileWriter) throws IOException {
		/*
		 * this method is to deal with the outside waiting line by using First Come First Serve,
		 *     which means, the second group in line, no matter what size it is, should wait the first group
		 *     to be seated and then will be considered to be arranged
		 */
		
		if(myLine.getOutsideGroupTimes().isEmpty()) {
			fileWriter.write("Sorry. There's no group outside.");
			fileWriter.newLine();
		}
		else {
			//in this method, the next group needs to wait the last group to be seated
			//    thus, system should compare the last group's seated time with the new group arriving time
			LocalTime lastGroupSeatedTime = LocalTime.of(0, 0);
			
			while(!myLine.getOutsideGroupTimes().isEmpty() &&
					lastGroupSeatedTime.isBefore(LocalTime.of(22, 0))) {
				//because it deals with every group outside one by one
				//    thus, this method will always focus on the first group in the waiting line
				
				//get this group information
				LocalTime groupTime = myLine.getThisGroupTime(0);
				int groupSize = myLine.getThisGroupSize(0);
//				System.out.println(groupTime + " " + groupSize);
//				System.out.println("Last group seated time is " + lastGroupSeatedTime);
				
				//judge whether this group is coming too late
				if(myLine.isLate(groupTime, groupSize)) 
					break;
//					System.out.println("Sorry. It's too late for you to have a meal. We are gonna close the restaurant.");
				else {
					//if groups are coming early, they should wait
					//these waiting times will be stored in the operateResults
					int predictWaiting = 0;
					int realWaiting = 0;
					int waitLastGroup = 0;
					
					if(lastGroupSeatedTime.isAfter(groupTime)) {
						//which means, the new group needs to wait until the last group is getting to sit
					    waitLastGroup = (int) groupTime.until(lastGroupSeatedTime, ChronoUnit.MINUTES);
//						System.out.println("Wait last group for " + waitLastGroup);
					}
//					System.out.println(predictWaiting + " " + realWaiting);
					
					//1. define this group's size
					String size = myLine.defineSizeOfThisGroup(groupSize);
//					System.out.println(size);
					
					//2. find the first available and strict table in the table list to match this group
					int matchTableNo = 0;
					matchTableNo = myRst.findSuitableTableNumFitGroupSize(size, true);
//					System.out.println("match table number is " + matchTableNo);
					
					//3. according to the comparison between this groupTime and the table time
					//if it finds an empty table with '00:00' that has not been sit by any group
					if(myRst.getThisTableTime(matchTableNo).equals(LocalTime.of(0, 0))) {
						if(groupTime.isBefore(LocalTime.of(9, 0))) {
//							System.out.println("You come early. We open at 9:00 AM. Please wait.");
							//then occupied table time will change from "00:00" to the opening time "9:00"
							myRst.setThisTableTime(LocalTime.of(9, 0), matchTableNo);
							myRst.setThisTableSize(groupSize, matchTableNo);
							lastGroupSeatedTime = LocalTime.of(9, 0);
							//then this group's waiting time is the different between their arriving time and the opening time
							predictWaiting = (int) groupTime.until(LocalTime.of(9, 0), ChronoUnit.MINUTES);
							realWaiting = predictWaiting;
//							System.out.println(predictWaiting + " " + realWaiting);
						}
						//    if this group is coming after 9:00 AM
						else {
							if(waitLastGroup > 0) {
								predictWaiting = waitLastGroup;
								realWaiting = predictWaiting;
								myRst.setThisTableTime(lastGroupSeatedTime, matchTableNo);
							}
							else {
//								System.out.println("No need to wait.");
								//then occupied table time will directly change to this group's arriving time
								myRst.setThisTableTime(groupTime, matchTableNo);
								lastGroupSeatedTime = groupTime;
								//besides, their waiting times both stay 0
							}
							myRst.setThisTableSize(groupSize, matchTableNo);
						}
					}
					//if matched table has a time stamp inside
					else {
						//then, this time stamp represents the time stamp when the last group is seated at this table
						//thus, when system predicts, it will generate a fake leaving time for the last group who sits this table
						LocalTime fakeLeaving = GenerateRandomFigures.timeAddMealTime(myRst.getThisTableTime(matchTableNo), myRst.getThisTableSize(matchTableNo));
						//and also, this group sit at this table will have a real leaving time
						LocalTime realLeaving = GenerateRandomFigures.timeAddMealTime(myRst.getThisTableTime(matchTableNo), myRst.getThisTableSize(matchTableNo));
//						System.out.println("This table will be clean at " + realLeaving);
						
						//system updates the inside table lists
						//all the time stamps in the inside table list are the time when a group is sitting at this table
						//compute the predicted waiting time and the real waiting time
						if(lastGroupSeatedTime.isAfter(realLeaving)) {
							predictWaiting = (int) groupTime.until(lastGroupSeatedTime, ChronoUnit.MINUTES);
							if(predictWaiting < 0) {
//								System.out.println("No need to wait.");
								predictWaiting = 0;
								realWaiting = predictWaiting;
								myRst.setThisTableTime(groupTime, matchTableNo);
							}
							else {
//								System.out.println("Please wait.");
								realWaiting = predictWaiting;
								myRst.setThisTableTime(lastGroupSeatedTime, matchTableNo);
							}
						}
						else {
							predictWaiting = (int) groupTime.until(fakeLeaving, ChronoUnit.MINUTES);
							realWaiting = (int) groupTime.until(realLeaving, ChronoUnit.MINUTES);
							myRst.setThisTableTime(realLeaving, matchTableNo);
							lastGroupSeatedTime = realLeaving;
						}
						myRst.setThisTableSize(groupSize, matchTableNo);
						
//						System.out.println(predictWaiting + " " + realWaiting);
						
					}
					
					//4. calculate this group of people's expense
					int money = GenerateRandomFigures.moneyEarned(groupSize);
					
					//5. store all the results with the group information into operateResults
					Object[] results = {groupTime, groupSize, predictWaiting, realWaiting, money};
					operateResults1.add(results);
	
//					printOperateResults(operateResults1);
//					for(int i = 0; i < myRst.getInsideTables(); i++) {
//						System.out.print(myRst.getThisTableTime(i) + " ");
//					}
//					System.out.println();
				}
				
				//6. remove this group from outside waiting line because it's already arranged
				myLine.finish(0, false, null);

//				System.out.println("check: " + myLine.getOutsideGroupsNumber());
//				System.out.println();
			}
			printOperateResults(operateResults1, fileWriter);
			fileWriter.write("Restaurant has served " + operateResults1.size() + " groups.");
			fileWriter.newLine();
			fileWriter.newLine();
		}
		
	}

	public static void methodTwo(Restaurant myRst, WaitingLine myLine, BufferedWriter fileWriter) throws IOException {
		/*
		 * This method is to deal with the outside line according to the different group sizes
		 */
		
		if(myLine.getOutsideGroupTimes().isEmpty()) {
			fileWriter.write("Sorry. There's no group outside.");
			fileWriter.newLine();
		}
		else {
			//initialize the operateResults2 in order to add group results into the operateResults2 at original location
//			System.out.println(myLine.getOutsideGroupsNumber());
			
			//use lateCheck to check should restaurant stop serving groups
			LocalTime lateCheck = LocalTime.of(0, 0);
			
			while(!myLine.getOutsideGroupTimes().isEmpty() && 
					lateCheck.isBefore(LocalTime.of(22, 0))) {
				//thus, the first thing is to split the waiting line into three lists
				myLine.splitGroups(); 
				
				//get the first groups' information in three split group lists 
				//assuming system is always doing with the first element in each group size list in this method
				LocalTime smallTime = myLine.getSmallGroupTimes().get(0);
				int smallSize = myLine.getSmallGroupSizes().get(0);
				LocalTime middleTime = myLine.getMiddleGroupTimes().get(0);
				int middleSize = myLine.getMiddleGroupSizes().get(0);
				LocalTime largeTime = myLine.getLargeGroupTimes().get(0);
				int largeSize = myLine.getLargeGroupSizes().get(0);
				
				//some variables system will use in the following operations
				int predictWaiting = 0;
				int realWaiting = 0;
				String size = null;
				int matchTableNo = 0;
				int money = 0;
				LocalTime fakeLeaving = null;
				LocalTime realLeaving = null;
				
				//although it should be at the same time to check three group size groups,
				//    technically system will check the small group firstly
//				System.out.println(smallTime+" "+smallSize);
				if(myLine.isLate(smallTime, smallSize)) {
					break;
//					System.out.println("Sorry. It's too late for you to have a meal. We are gonna close the restaurant.");
				}
				else {
					//1. define this group size
					size = myLine.defineSizeOfThisGroup(smallSize);
					
					//2. find the first available and strict table
					matchTableNo = myRst.findSuitableTableNumFitGroupSize(size, true);
//					System.out.println(matchTableNo + " " + myRst.getThisTableTime(matchTableNo));
					
					//3. according to the table time and the group time, system will do the analysis and make the decision
					if(myRst.getThisTableTime(matchTableNo).equals(LocalTime.of(0, 0))) {
						if(smallTime.isBefore(LocalTime.of(9, 0))) {
//							System.out.println("You come early. We open at 9:00 AM. Please wait.");
							myRst.setThisTableTime(LocalTime.of(9, 0), matchTableNo);
							myRst.setThisTableSize(smallSize, matchTableNo);
							predictWaiting = (int) smallTime.until(LocalTime.of(9, 0), ChronoUnit.MINUTES);
							realWaiting = predictWaiting;
						}
						else {
//							System.out.println("No need to wait.");
							//then occupied table time will directly change to this group's arriving time
							myRst.setThisTableTime(smallTime, matchTableNo);
							myRst.setThisTableSize(smallSize, matchTableNo);
							predictWaiting = 0;
							realWaiting = predictWaiting;
						}
					}
					else {
						fakeLeaving = GenerateRandomFigures.timeAddMealTime(myRst.getThisTableTime(matchTableNo), myRst.getThisTableSize(matchTableNo));
						//and also, this group sit at this table will have a real leaving time
						realLeaving = GenerateRandomFigures.timeAddMealTime(myRst.getThisTableTime(matchTableNo), myRst.getThisTableSize(matchTableNo));
//						System.out.println("This table will be clean at " + realLeaving);
						
						//compute the predicted waiting time and the real waiting time
						predictWaiting = (int) smallTime.until(fakeLeaving, ChronoUnit.MINUTES);
						realWaiting = (int) smallTime.until(realLeaving, ChronoUnit.MINUTES);
						
						//system will analyze the real time and give a message
						if(smallTime.isAfter(fakeLeaving))
							//change the negative value to 0
							predictWaiting = 0;
						
						if(smallTime.isBefore(realLeaving)) {
//							System.out.println("Please wait.");
							//system updates the inside table lists
							//all the time stamps in the inside table list are the time when a group is sitting at this table
							myRst.setThisTableTime(realLeaving, matchTableNo);
						}
						else {
//							System.out.println("No need to wait.");
							myRst.setThisTableTime(smallTime, matchTableNo);
							//change the negative value to 0
							realWaiting = 0;
						}
						myRst.setThisTableSize(smallSize, matchTableNo);
					}
					//update the lateCheck
					if(myRst.getThisTableTime(matchTableNo).isAfter(lateCheck)) {
						lateCheck = myRst.getThisTableTime(matchTableNo);
					}
					
//					for(int i = 0; i < myRst.getInsideTables(); i++) {
//						System.out.print(myRst.getThisTableTime(i) + " ");
//					}
//					System.out.println();
					
					//4. get this group's expense
					money = GenerateRandomFigures.moneyEarned(smallSize);
					
					//5. store all the results into the operationResults2 with original indices
					Object[] results = {smallTime, smallSize, predictWaiting, realWaiting, money};
					operateResults2.add(results);
					
//					printOperateResults(operateResults2);
					//6. remove this group from the whole list and also the small group list
					myLine.finish(0, true, size);
				}
//				System.out.println();
				
				//Here system is doing with middle group list
				//same thing with small group list
//				System.out.println(middleTime+" "+middleSize);
				if(myLine.isLate(middleTime, middleSize)) {
					break;
//					System.out.println("Sorry. It's too late for you to have a meal. We are gonna close the restaurant.");
				}
				else {
					//1. define this group size
					size = myLine.defineSizeOfThisGroup(middleSize);
					
					//2. find the first available and strict table
					matchTableNo = myRst.findSuitableTableNumFitGroupSize(size, true);
//					System.out.println(matchTableNo + " " + myRst.getThisTableTime(matchTableNo));
					
					//3. according to the table time and the group time, system will do the analysis and make the decision
					if(myRst.getThisTableTime(matchTableNo).equals(LocalTime.of(0, 0))) {
						if(middleTime.isBefore(LocalTime.of(9, 0))) {
//							System.out.println("You come early. We open at 9:00 AM. Please wait.");
							myRst.setThisTableTime(LocalTime.of(9, 0), matchTableNo);
							myRst.setThisTableSize(middleSize, matchTableNo);
							predictWaiting = (int) middleTime.until(LocalTime.of(9, 0), ChronoUnit.MINUTES);
							realWaiting = predictWaiting;
						}
						else {
//							System.out.println("No need to wait.");
							//then occupied table time will directly change to this group's arriving time
							myRst.setThisTableTime(middleTime, matchTableNo);
							myRst.setThisTableSize(middleSize, matchTableNo);
							predictWaiting = 0;
							realWaiting = predictWaiting;
						}
					}
					else {
						fakeLeaving = GenerateRandomFigures.timeAddMealTime(myRst.getThisTableTime(matchTableNo), myRst.getThisTableSize(matchTableNo));
						//and also, this group sit at this table will have a real leaving time
						realLeaving = GenerateRandomFigures.timeAddMealTime(myRst.getThisTableTime(matchTableNo), myRst.getThisTableSize(matchTableNo));
//						System.out.println("This table will be clean at " + realLeaving);
						
						//compute the predicted waiting time and the real waiting time
						predictWaiting = (int) middleTime.until(fakeLeaving, ChronoUnit.MINUTES);
						realWaiting = (int) middleTime.until(realLeaving, ChronoUnit.MINUTES);
						
						//system will analyze the real time and give a message
						if(middleTime.isAfter(fakeLeaving))
							//change the negative value to 0
							predictWaiting = 0;
						
						if(middleTime.isBefore(realLeaving)) {
//							System.out.println("Please wait.");
							//system updates the inside table lists
							//all the time stamps in the inside table list are the time when a group is sitting at this table
							myRst.setThisTableTime(realLeaving, matchTableNo);
						}
						else {
//							System.out.println("No need to wait.");
							myRst.setThisTableTime(middleTime, matchTableNo);
							//change the negative value to 0
							realWaiting = 0;
						}
						myRst.setThisTableSize(middleSize, matchTableNo);
					}

					//update the lateCheck
					if(myRst.getThisTableTime(matchTableNo).isAfter(lateCheck)) {
						lateCheck = myRst.getThisTableTime(matchTableNo);
					}
					
//					for(int i = 0; i < myRst.getInsideTables(); i++) {
//						System.out.print(myRst.getThisTableTime(i) + " ");
//					}
//					System.out.println();
					
					//4. get this group's expense
					money = GenerateRandomFigures.moneyEarned(middleSize);
					
					//5. store all the results into the operationResults2 with original indices
					Object[] results = {middleTime, middleSize, predictWaiting, realWaiting, money};
					operateResults2.add(results);
					
//					printOperateResults(operateResults2);
					//6. remove this group from the whole list and also the small group list
					myLine.finish(0, true, size);
				}
//				System.out.println();
				
				//Here system is doing with large group list
//				System.out.println(largeTime+" "+largeSize);
				if(myLine.isLate(largeTime, largeSize)) {
					break;
//					System.out.println("Sorry. It's too late for you to have a meal. We are gonna close the restaurant.");
				}
				else {
					//1. define this group size
					size = myLine.defineSizeOfThisGroup(largeSize);
					
					//2. find the first available and strict table
					matchTableNo = myRst.findSuitableTableNumFitGroupSize(size, true);
//					System.out.println(matchTableNo + " " + myRst.getThisTableTime(matchTableNo));
					
					//3. according to the table time and the group time, system will do the analysis and make the decision
					if(myRst.getThisTableTime(matchTableNo).equals(LocalTime.of(0, 0))) {
						if(largeTime.isBefore(LocalTime.of(9, 0))) {
//							System.out.println("You come early. We open at 9:00 AM. Please wait.");
							myRst.setThisTableTime(LocalTime.of(9, 0), matchTableNo);
							myRst.setThisTableSize(largeSize, matchTableNo);
							predictWaiting = (int) largeTime.until(LocalTime.of(9, 0), ChronoUnit.MINUTES);
							realWaiting = predictWaiting;
						}
						else {
//							System.out.println("No need to wait.");
							//then occupied table time will directly change to this group's arriving time
							myRst.setThisTableTime(largeTime, matchTableNo);
							myRst.setThisTableSize(largeSize, matchTableNo);
							predictWaiting = 0;
							realWaiting = predictWaiting;
						}
					}
					else {
						fakeLeaving = GenerateRandomFigures.timeAddMealTime(myRst.getThisTableTime(matchTableNo), myRst.getThisTableSize(matchTableNo));
						//and also, this group sit at this table will have a real leaving time
						realLeaving = GenerateRandomFigures.timeAddMealTime(myRst.getThisTableTime(matchTableNo), myRst.getThisTableSize(matchTableNo));
//						System.out.println("This table will be clean at " + realLeaving);
						
						//compute the predicted waiting time and the real waiting time
						predictWaiting = (int) largeTime.until(fakeLeaving, ChronoUnit.MINUTES);
						realWaiting = (int) largeTime.until(realLeaving, ChronoUnit.MINUTES);
						
						if(largeTime.isBefore(realLeaving)) {
//							System.out.println("Please wait.");
							//system updates the inside table lists
							//all the time stamps in the inside table list are the time when a group is sitting at this table
							myRst.setThisTableTime(realLeaving, matchTableNo);
						}
						else {
//							System.out.println("No need to wait.");
							myRst.setThisTableTime(largeTime, matchTableNo);
							//change the negative value to 0
							realWaiting = 0;
						}
						myRst.setThisTableSize(largeSize, matchTableNo);
					}

					//update the lateCheck
					if(myRst.getThisTableTime(matchTableNo).isAfter(lateCheck)) {
						lateCheck = myRst.getThisTableTime(matchTableNo);
					}
					
//					for(int i = 0; i < myRst.getInsideTables(); i++) {
//						System.out.print(myRst.getThisTableTime(i) + " ");
//					}
//					System.out.println();
					
					//4. get this group's expense
					money = GenerateRandomFigures.moneyEarned(largeSize);
					
					//5. store all the results into the operationResults2 with original indices
					Object[] results = {largeTime, largeSize, predictWaiting, realWaiting, money};
					operateResults2.add(results);
//					printOperateResults(operateResults2);
					
					//6. remove this group from the whole list and also the small group list
					myLine.finish(0, true, size);
				}
//				System.out.println("check: " + myLine.getOutsideGroupsNumber());
//				System.out.println(lateCheck);
//				System.out.println();
			}
			
		}
		printOperateResults(operateResults2, fileWriter);
		fileWriter.write("Restaurant has served " + operateResults2.size() + " groups.");
		fileWriter.newLine();
		fileWriter.newLine();
	}

	public static void methodThree(Restaurant myRst, WaitingLine myLine, BufferedWriter fileWriter) throws IOException {
		/*
		 * This method is to deal with the outside waiting line based on a priority sequence
		 */
		
		if(myLine.getOutsideGroupTimes().isEmpty()) {
			fileWriter.write("Sorry. There's no group outside.");
			fileWriter.newLine();
		}
		else {
			//first, split the outside groups
			myLine.splitGroups();
			ArrayList<LocalTime> smallT = myLine.getSmallGroupTimes();
			ArrayList<Integer> smallS = myLine.getSmallGroupSizes();
			ArrayList<LocalTime> middleT = myLine.getMiddleGroupTimes();
			ArrayList<Integer> middleS = myLine.getMiddleGroupSizes();
			ArrayList<LocalTime> largeT = myLine.getLargeGroupTimes();
			ArrayList<Integer> largeS = myLine.getLargeGroupSizes();
			
			//here system gets three group lists, 
			//    then apply computeGroupsPriority() method to compute priority for each group
			ArrayList<Double> smallPriorities = new ArrayList<Double>();
			ArrayList<Double> middlePriorities = new ArrayList<Double>();
			ArrayList<Double> largePriorities = new ArrayList<Double>();
			smallPriorities = computeGroupsPriority(myRst, myLine, smallT, smallS);
			middlePriorities = computeGroupsPriority(myRst, myLine, middleT, middleS);
			largePriorities = computeGroupsPriority(myRst, myLine, largeT, largeS);
			
			//then apply sortList() method to get the schedule order for each group size list
			ArrayList<Integer> sortedSmall = new ArrayList<Integer>();
			ArrayList<Integer> sortedMiddle = new ArrayList<Integer>();
			ArrayList<Integer> sortedLarge = new ArrayList<Integer>();
			sortedSmall = sortList(smallPriorities);
			sortedMiddle = sortList(middlePriorities);
			sortedLarge = sortList(largePriorities);
			
//			System.out.println("Here is small groups:");
//			for(int i = 0; i < smallPriorities.size(); i++) {
//				System.out.print(myLine.getSmallGroupTimes().get(i) + " ");
//			}
//			System.out.println();
//			for(int i = 0; i < smallPriorities.size(); i++) {
//				System.out.print(myLine.getSmallGroupSizes().get(i) + " ");
//			}
//			System.out.println();
//			for(int i = 0; i < smallPriorities.size(); i++) {
//				System.out.print(smallPriorities.get(i) + " ");
//			}
//			System.out.println();
//			for(int i = 0; i < smallPriorities.size(); i++) {
//				System.out.print(sortedSmall.get(i) + " ");
//			}
//			System.out.println();
//			System.out.println();
//			System.out.println("Here is middle groups:");
//			for(int i = 0; i < middlePriorities.size(); i++) {
//				System.out.print(myLine.getMiddleGroupTimes().get(i) + " ");
//			}
//			System.out.println();
//			for(int i = 0; i < middlePriorities.size(); i++) {
//				System.out.print(myLine.getMiddleGroupSizes().get(i) + " ");
//			}
//			System.out.println();
//			for(int i = 0; i < middlePriorities.size(); i++) {
//				System.out.print(middlePriorities.get(i) + " ");
//			}
//			System.out.println();
//			for(int i = 0; i < middlePriorities.size(); i++) {
//				System.out.print(sortedMiddle.get(i) + " ");
//			}
//			System.out.println();
//			System.out.println();
//			System.out.println("Here is large groups:");
//			for(int i = 0; i < largePriorities.size(); i++) {
//				System.out.print(myLine.getLargeGroupTimes().get(i) + " ");
//			}
//			System.out.println();
//			for(int i = 0; i < largePriorities.size(); i++) {
//				System.out.print(myLine.getLargeGroupSizes().get(i) + " ");
//			}
//			System.out.println();
//			for(int i = 0; i < largePriorities.size(); i++) {
//				System.out.print(largePriorities.get(i) + " ");
//			}
//			System.out.println();
//			for(int i = 0; i < largePriorities.size(); i++) {
//				System.out.print(sortedLarge.get(i) + " ");
//			}
//			System.out.println();
			
			//then start scheduling
			LocalTime lateCheck = LocalTime.of(0, 0);
			int smallIndex = 0, middleIndex = 0, largeIndex = 0;
			LocalTime smallTime = null, middleTime = null, largeTime = null;
			int smallSize = 0, middleSize = 0, largeSize = 0;
			
			while(!myLine.getOutsideGroupTimes().isEmpty() &&
					lateCheck.isBefore(LocalTime.of(22, 0))) {
				
				//some variables system will use in the following operations
				int predictWaiting = 0;
				int realWaiting = 0;
				String size = null;
				int matchTableNo = 0;
				int money = 0;
				LocalTime fakeLeaving = null;
				LocalTime realLeaving = null;
				
				//get the first groups' information in three split group lists 
				if(!sortedSmall.isEmpty()) {
					smallIndex = sortedSmall.get(0);
					smallTime = myLine.getSmallGroupTimes().get(smallIndex);
					smallSize = myLine.getSmallGroupSizes().get(smallIndex);
					
					//although it should be at the same time to check three group size groups,
					//    technically system will check the small group firstly
//					System.out.println(smallTime+" "+smallSize);
					if(myLine.isLate(smallTime, smallSize)) {
//						System.out.println("Sorry. It's too late for you to have a meal. We are gonna close the restaurant.");
						break;
					}
					else {
						//1. define this group size
						size = myLine.defineSizeOfThisGroup(smallSize);
						
						//2. find the first available and strict table
						matchTableNo = myRst.findSuitableTableNumFitGroupSize(size, true);
//						System.out.println(matchTableNo + " " + myRst.getThisTableTime(matchTableNo));
						
						//3. according to the table time and the group time, system will do the analysis and make the decision
						if(myRst.getThisTableTime(matchTableNo).equals(LocalTime.of(0, 0))) {
							if(smallTime.isBefore(LocalTime.of(9, 0))) {
//								System.out.println("You come early. We open at 9:00 AM. Please wait.");
								myRst.setThisTableTime(LocalTime.of(9, 0), matchTableNo);
								myRst.setThisTableSize(smallSize, matchTableNo);
								predictWaiting = (int) smallTime.until(LocalTime.of(9, 0), ChronoUnit.MINUTES);
								realWaiting = predictWaiting;
							}
							else {
//								System.out.println("No need to wait.");
								//then occupied table time will directly change to this group's arriving time
								myRst.setThisTableTime(smallTime, matchTableNo);
								myRst.setThisTableSize(smallSize, matchTableNo);
								predictWaiting = 0;
								realWaiting = predictWaiting;
							}
						}
						else {
							fakeLeaving = GenerateRandomFigures.timeAddMealTime(myRst.getThisTableTime(matchTableNo), myRst.getThisTableSize(matchTableNo));
							//and also, this group sit at this table will have a real leaving time
							realLeaving = GenerateRandomFigures.timeAddMealTime(myRst.getThisTableTime(matchTableNo), myRst.getThisTableSize(matchTableNo));
//							System.out.println("This table will be clean at " + realLeaving);
							
							//compute the predicted waiting time and the real waiting time
							predictWaiting = (int) smallTime.until(fakeLeaving, ChronoUnit.MINUTES);
							realWaiting = (int) smallTime.until(realLeaving, ChronoUnit.MINUTES);
							
							//system will analyze the real time and give a message
							if(smallTime.isAfter(fakeLeaving))
								//change the negative value to 0
								predictWaiting = 0;
							
							if(smallTime.isBefore(realLeaving)) {
//								System.out.println("Please wait.");
								//system updates the inside table lists
								//all the time stamps in the inside table list are the time when a group is sitting at this table
								myRst.setThisTableTime(realLeaving, matchTableNo);
							}
							else {
//								System.out.println("No need to wait.");
								myRst.setThisTableTime(smallTime, matchTableNo);
								//change the negative value to 0
								realWaiting = 0;
							}
							myRst.setThisTableSize(smallSize, matchTableNo);
						}
						//update the lateCheck
						if(myRst.getThisTableTime(matchTableNo).isAfter(lateCheck)) {
							lateCheck = myRst.getThisTableTime(matchTableNo);
						}
						
//						for(int i = 0; i < myRst.getInsideTables(); i++) {
//							System.out.print(myRst.getThisTableTime(i) + " ");
//						}
//						System.out.println();
						
						//4. get this group's expense
						money = GenerateRandomFigures.moneyEarned(smallSize);
						
						//5. store all the results into the operationResults2 with original indices
						Object[] results = {smallTime, smallSize, predictWaiting, realWaiting, money};
						operateResults3.add(results);
						
//						printOperateResults(operateResults3);
						//6. remove this group from the whole list and also the small group list
						sortedSmall.remove(0);
//						myLine.finish(smallIndex, true, size);
					}
//					System.out.println();
				}
				
				
				if(!sortedMiddle.isEmpty()) {
					middleIndex = sortedMiddle.get(0);
					middleTime = myLine.getMiddleGroupTimes().get(middleIndex);
					middleSize = myLine.getMiddleGroupSizes().get(middleIndex);
					
					//Here system is doing with middle group list
					//same thing with small group list
//					System.out.println(middleTime+" "+middleSize);
					if(myLine.isLate(middleTime, middleSize)) {
						break;
//						System.out.println("Sorry. It's too late for you to have a meal. We are gonna close the restaurant.");
					}
					else {
						//1. define this group size
						size = myLine.defineSizeOfThisGroup(middleSize);
						
						//2. find the first available and strict table
						matchTableNo = myRst.findSuitableTableNumFitGroupSize(size, true);
//						System.out.println(matchTableNo + " " + myRst.getThisTableTime(matchTableNo));
						
						//3. according to the table time and the group time, system will do the analysis and make the decision
						if(myRst.getThisTableTime(matchTableNo).equals(LocalTime.of(0, 0))) {
							if(middleTime.isBefore(LocalTime.of(9, 0))) {
//								System.out.println("You come early. We open at 9:00 AM. Please wait.");
								myRst.setThisTableTime(LocalTime.of(9, 0), matchTableNo);
								myRst.setThisTableSize(middleSize, matchTableNo);
								predictWaiting = (int) middleTime.until(LocalTime.of(9, 0), ChronoUnit.MINUTES);
								realWaiting = predictWaiting;
							}
							else {
//								System.out.println("No need to wait.");
								//then occupied table time will directly change to this group's arriving time
								myRst.setThisTableTime(middleTime, matchTableNo);
								myRst.setThisTableSize(middleSize, matchTableNo);
								predictWaiting = 0;
								realWaiting = predictWaiting;
							}
						}
						else {
							fakeLeaving = GenerateRandomFigures.timeAddMealTime(myRst.getThisTableTime(matchTableNo), myRst.getThisTableSize(matchTableNo));
							//and also, this group sit at this table will have a real leaving time
							realLeaving = GenerateRandomFigures.timeAddMealTime(myRst.getThisTableTime(matchTableNo), myRst.getThisTableSize(matchTableNo));
//							System.out.println("This table will be clean at " + realLeaving);
							
							//compute the predicted waiting time and the real waiting time
							predictWaiting = (int) middleTime.until(fakeLeaving, ChronoUnit.MINUTES);
							realWaiting = (int) middleTime.until(realLeaving, ChronoUnit.MINUTES);
							
							//system will analyze the real time and give a message
							if(middleTime.isAfter(fakeLeaving))
								//change the negative value to 0
								predictWaiting = 0;
							
							if(middleTime.isBefore(realLeaving)) {
//								System.out.println("Please wait.");
								//system updates the inside table lists
								//all the time stamps in the inside table list are the time when a group is sitting at this table
								myRst.setThisTableTime(realLeaving, matchTableNo);
							}
							else {
//								System.out.println("No need to wait.");
								myRst.setThisTableTime(middleTime, matchTableNo);
								//change the negative value to 0
								realWaiting = 0;
							}
							myRst.setThisTableSize(middleSize, matchTableNo);
						}

						//update the lateCheck
						if(myRst.getThisTableTime(matchTableNo).isAfter(lateCheck)) {
							lateCheck = myRst.getThisTableTime(matchTableNo);
						}
						
//						for(int i = 0; i < myRst.getInsideTables(); i++) {
//							System.out.print(myRst.getThisTableTime(i) + " ");
//						}
//						System.out.println();
						//4. get this group's expense
						money = GenerateRandomFigures.moneyEarned(middleSize);
						
						//5. store all the results into the operationResults2 with original indices
						Object[] results = {middleTime, middleSize, predictWaiting, realWaiting, money};
						operateResults3.add(results);
						
//						printOperateResults(operateResults3);
						//6. remove this group from the whole list and also the small group list
						sortedMiddle.remove(0);
//						myLine.finish(middleIndex, true, size);
					}
//					System.out.println();
				}
				
				
				if(!sortedLarge.isEmpty()) {
					largeIndex = sortedLarge.get(0);
					largeTime = myLine.getLargeGroupTimes().get(largeIndex);
					largeSize = myLine.getLargeGroupSizes().get(largeIndex);
					
					//Here system is doing with large group list
//					System.out.println(largeTime+" "+largeSize);
					if(myLine.isLate(largeTime, largeSize)) {
						break;
//						System.out.println("Sorry. It's too late for you to have a meal. We are gonna close the restaurant.");
					}
					else {
						//1. define this group size
						size = myLine.defineSizeOfThisGroup(largeSize);
						
						//2. find the first available and strict table
						matchTableNo = myRst.findSuitableTableNumFitGroupSize(size, true);
//						System.out.println(matchTableNo + " " + myRst.getThisTableTime(matchTableNo));
						
						//3. according to the table time and the group time, system will do the analysis and make the decision
						if(myRst.getThisTableTime(matchTableNo).equals(LocalTime.of(0, 0))) {
							if(largeTime.isBefore(LocalTime.of(9, 0))) {
//								System.out.println("You come early. We open at 9:00 AM. Please wait.");
								myRst.setThisTableTime(LocalTime.of(9, 0), matchTableNo);
								myRst.setThisTableSize(largeSize, matchTableNo);
								predictWaiting = (int) largeTime.until(LocalTime.of(9, 0), ChronoUnit.MINUTES);
								realWaiting = predictWaiting;
							}
							else {
//								System.out.println("No need to wait.");
								//then occupied table time will directly change to this group's arriving time
								myRst.setThisTableTime(largeTime, matchTableNo);
								myRst.setThisTableSize(largeSize, matchTableNo);
								predictWaiting = 0;
								realWaiting = predictWaiting;
							}
						}
						else {
							fakeLeaving = GenerateRandomFigures.timeAddMealTime(myRst.getThisTableTime(matchTableNo), myRst.getThisTableSize(matchTableNo));
							//and also, this group sit at this table will have a real leaving time
							realLeaving = GenerateRandomFigures.timeAddMealTime(myRst.getThisTableTime(matchTableNo), myRst.getThisTableSize(matchTableNo));
//							System.out.println("This table will be clean at " + realLeaving);
							
							//compute the predicted waiting time and the real waiting time
							predictWaiting = (int) largeTime.until(fakeLeaving, ChronoUnit.MINUTES);
							realWaiting = (int) largeTime.until(realLeaving, ChronoUnit.MINUTES);
							
							if(largeTime.isBefore(realLeaving)) {
//								System.out.println("Please wait.");
								//system updates the inside table lists
								//all the time stamps in the inside table list are the time when a group is sitting at this table
								myRst.setThisTableTime(realLeaving, matchTableNo);
							}
							else {
//								System.out.println("No need to wait.");
								myRst.setThisTableTime(largeTime, matchTableNo);
								//change the negative value to 0
								realWaiting = 0;
							}
							myRst.setThisTableSize(largeSize, matchTableNo);
						}

						//update the lateCheck
						if(myRst.getThisTableTime(matchTableNo).isAfter(lateCheck)) {
							lateCheck = myRst.getThisTableTime(matchTableNo);
						}
						
//						for(int i = 0; i < myRst.getInsideTables(); i++) {
//							System.out.print(myRst.getThisTableTime(i) + " ");
//						}
//						System.out.println();
						
						//4. get this group's expense
						money = GenerateRandomFigures.moneyEarned(largeSize);
						
						//5. store all the results into the operationResults2 with original indices
						Object[] results = {largeTime, largeSize, predictWaiting, realWaiting, money};
						operateResults3.add(results);
//						printOperateResults(operateResults3);
						
						//6. remove this group from the whole list and also the small group list
						sortedLarge.remove(0);
//						myLine.finish(largeIndex, true, size);
					}
					
				}
//				System.out.println("check: " + myLine.getOutsideGroupsNumber());
//				System.out.println(lateCheck);
//				System.out.println();
			}
			printOperateResults(operateResults3, fileWriter);
			fileWriter.write("Restaurant has served " + operateResults3.size() + " groups.");
			fileWriter.newLine();
			fileWriter.newLine();
		}
		
	}
	
	public static ArrayList<Integer> sortList(ArrayList<Double> list) {
		/*
		 * through bubble sorting the list, give each corresponding element a descending order priority 
		 *     and record it in a list
		 */
		ArrayList<Double> listTemp = new ArrayList<Double>();
		ArrayList<Integer> sortIndices = new ArrayList<Integer>();
		int i = 0, j = 0;
		int length = list.size();
		Double temp;
		
		for(i = 0; i < length; i++) {
			listTemp.add(list.get(i));
			sortIndices.add(i);
		}
		
		for(i = 0; i < (length - 1); i++) {
			for(j = i + 1; j < length; j++) {
				//system wants a descending order list
				if(listTemp.get(i).doubleValue() > listTemp.get(j).doubleValue()) {
					temp = listTemp.get(i);
					listTemp.set(i, listTemp.get(j));
					listTemp.set(j, temp);
				}
			} 
		}
		
		for(i = 0; i < length; i++) {
			for(j = 0; j < length; j++) {
				if(list.get(i).equals(listTemp.get(j)))
					sortIndices.set(i, j);
			}
		}
		
		return sortIndices;
	}
	
	public static ArrayList<Double> computeGroupsPriority(Restaurant rst, WaitingLine line, 
			ArrayList<LocalTime> groupTimes, ArrayList<Integer> groupSizes) {
		/*
		 * This method is computing every group's priority result in the list 
		 */
		long predictForEachGroup = 0;
		double priorityForEachGroup = 0;
		ArrayList<Double> priorityResults = new ArrayList<Double>();
		
		int length = groupTimes.size();
		
		for(int i = 0; i < length; i++) {
			predictForEachGroup = LocalTime.of(9, 0).until(groupTimes.get(i), ChronoUnit.MINUTES);
			if(predictForEachGroup < 0)
				predictForEachGroup = 0;
			//use this formula to give priority to each group
			priorityForEachGroup = (predictForEachGroup + 1) * (1 / Math.log(groupSizes.get(i) + 1));
			priorityResults.add(i, priorityForEachGroup);
		}
		
		return priorityResults;
	}
	
	public static int groupMatchTable(Restaurant rst, WaitingLine line, LocalTime groupTime, int groupSize) {
		/*
		 * This method is used for the methodThree
		 * This method will be called when a known group is being arranged to a table
		 */
		int predictWaiting = 0;
		
		if(line.isLate(groupTime, groupSize)) {
//			System.out.println("Sorry. It's too late for you to have a meal. We are gonna close the restaurant.");
		}
		else {
			//1. define this group size
			String size = line.defineSizeOfThisGroup(groupSize);
			
			//2. find the first available and strict table
			int matchTableNo = rst.findSuitableTableNumFitGroupSize(size, true);
			
			//3. according to the table time and the group time, system will do the analysis and make the decision
			if(rst.getThisTableTime(matchTableNo).equals(LocalTime.of(0, 0))) {
				if(groupTime.isBefore(LocalTime.of(9, 0))) {
					rst.setThisTableTime(LocalTime.of(9, 0), matchTableNo);
					rst.setThisTableSize(groupSize, matchTableNo);
					predictWaiting = 0;
				}
				else {
					rst.setThisTableTime(groupTime, matchTableNo);
					rst.setThisTableSize(groupSize, matchTableNo);
					predictWaiting = 0;
				}
			}
			else {
				LocalTime fakeLeaving = GenerateRandomFigures.timeAddMealTime(rst.getThisTableTime(matchTableNo), rst.getThisTableSize(matchTableNo));
//				System.out.println("This table will be clean at " + fakeLeaving);
				
				//compute the predicted waiting time and the real waiting time
				predictWaiting = (int) groupTime.until(fakeLeaving, ChronoUnit.MINUTES);
				
				if(groupTime.isAfter(fakeLeaving)) {
					rst.setThisTableTime(groupTime, matchTableNo);
					rst.setThisTableSize(groupSize, matchTableNo);
					predictWaiting = 0;
				}
				else {
					rst.setThisTableTime(fakeLeaving, matchTableNo);
					rst.setThisTableSize(groupSize, matchTableNo);
				}
			}
		}
		
		return predictWaiting;
	}
	
}
