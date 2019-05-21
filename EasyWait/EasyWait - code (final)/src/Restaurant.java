import java.time.LocalTime;
import java.util.Arrays;

public class Restaurant {
	
	 private int insideTables;
	 private int smallTables, middleTables, largeTables;
	 private int smallTableSize, middleTableSize, largeTableSize;
	 
	 private LocalTime[] insideTableTimes;
	 private int[] insideTableSizes;
	 private LocalTime[] smallTableTimes;
	 private LocalTime[] middleTableTimes;
	 private LocalTime[] largeTableTimes;
	 
	 public Restaurant() {
		 
	 }
	 
	 public Restaurant(int insideTables, int smallTables, int middleTables, int largeTables, int smallTableSize,
				int middleTableSize, int largeTableSize) {
		super();
		this.insideTables = insideTables;
		this.smallTables = smallTables;
		this.middleTables = middleTables;
		this.largeTables = largeTables;
		this.smallTableSize = smallTableSize;
		this.middleTableSize = middleTableSize;
		this.largeTableSize = largeTableSize;
		
		insideTableTimes = new LocalTime[this.insideTables];
		insideTableSizes = new int[this.insideTables];
		smallTableTimes = new LocalTime[this.smallTables];
		middleTableTimes = new LocalTime[this.middleTables];
		largeTableTimes = new LocalTime[this.largeTables];
		
		//at the very beginning, initialize the restaurant table time stamp array, making all elements as "00:00"
		//assuming "00:00" means this table is available to serve a group
		for(int i = 0; i < this.insideTables; i++) {
			setThisTableTime(LocalTime.of(0, 0), i);
			setThisTableSize(0, i);
		}
	 }
	 
	 //getters
	 public int getInsideTables() {
		 return insideTables;
	 }
	 
	 public int getSmallTables() {
		 return smallTables;
	 }

	 public int getMiddleTables() {
		 return middleTables;
	 }

	 public int getLargeTables() {
		 return largeTables;
	 }

	 public int getSmallTableSize() {
		 return smallTableSize;
	 }

	 public int getMiddleTableSize() {
		 return middleTableSize;
	 }

	 public int getLargeTableSize() {
		 return largeTableSize;
	 }
	 
	 public LocalTime[] getInsideTableTimes() {
		 return insideTableTimes;
	 }
	 
	 public int[] getInsideTableSizes() {
		 return insideTableSizes;
	 }

	 public LocalTime[] getSmallTableTimes() {
		 return smallTableTimes;
	 }
	 
	 public LocalTime[] getMiddleTableTimes() {
		 return middleTableTimes;
	 }

	 public LocalTime[] getLargeTableTimes() {
		 return largeTableTimes;
	 }
	 
	 public void setInsideTableSizes(int[] insideTableSizes) {
		 this.insideTableSizes = insideTableSizes;
	 }

	 //DIY getters and setters
	 //get table No.x time stamp and size in the array
	 public LocalTime getThisTableTime(int tableNo) {
		return insideTableTimes[tableNo];
	 }
	 
	 public int getThisTableSize(int tableNo) {
		return insideTableSizes[tableNo];
	 }
	 
	 //set new time stamp and size to the table No.x
	 public void setThisTableTime(LocalTime newTableTime, int tableNo) {
		insideTableTimes[tableNo] = newTableTime;
	 }
	 
	 public void setThisTableSize(int size, int tableNo) {
		insideTableSizes[tableNo] = size;
	 }
	 
	 //split the whole table array into three different table sizes arrays
	 public void splitTables() {
		 int i = 0;
		 
		 for(i = 0; i < insideTables; i++) {
			 if(i < smallTables) {
				 smallTableTimes = Arrays.copyOfRange(insideTableTimes, 0, smallTables);
			 }
			 else if(i >= smallTables && i < (smallTables + middleTables)) {
				 middleTableTimes = Arrays.copyOfRange(insideTableTimes, smallTables, (smallTables + middleTables));
			 }
			 else {
				 largeTableTimes = Arrays.copyOfRange(insideTableTimes, (smallTables + middleTables), insideTables);
			 }
		 }
		 
	 }
	 
	 //according to the size judging result, find the corresponding suitable table lists
	 public int findSuitableTableNumFitGroupSize(String groupSize, boolean strictOrNot) {
		 int matchTableNo = 0;
		 
		 splitTables();
		 
		 //small groups only can be seated at small tables
		 if(strictOrNot) {
			 //firstly find the suitable table list
			 if(groupSize.equalsIgnoreCase("small")) {
				 matchTableNo = findEarliestTableNo(smallTableTimes);
			 }
			 else if(groupSize.equalsIgnoreCase("middle")) {
				 matchTableNo = findEarliestTableNo(middleTableTimes) + smallTables;
			 }
			 else {
				 matchTableNo = findEarliestTableNo(largeTableTimes) + middleTables + smallTables;
			 }
		 }
		 //small groups can be seated at any table 
		 else {
			 if(groupSize.equalsIgnoreCase("large")) {
				 matchTableNo = findEarliestTableNo(largeTableTimes) + middleTables + smallTables;
			 }
			 else if(groupSize.equalsIgnoreCase("middle")) {
				 int tableM = 0, tableL = 0;
				 tableM = findEarliestTableNo(middleTableTimes);
				 tableL = findEarliestTableNo(largeTableTimes);
				 if(middleTableTimes[tableM].equals(largeTableTimes[tableL]))
					 matchTableNo = tableM + smallTables;
				 else if(middleTableTimes[tableM].isBefore(largeTableTimes[tableL]))
					 matchTableNo = tableM + smallTables;
				 else
					 matchTableNo = tableL + middleTables + smallTables;
			 }
			 else {
				 int tableS = 0, tableM = 0, tableL = 0, match = 0;
				 tableS = findEarliestTableNo(smallTableTimes);
				 tableM = findEarliestTableNo(middleTableTimes);
				 tableL = findEarliestTableNo(largeTableTimes);
				 
				 LocalTime[] compare = {smallTableTimes[tableS], middleTableTimes[tableM], largeTableTimes[tableL]};
				 match = findEarliestTableNo(compare);
				 
				 switch (match) {
				 case 0: 
					 matchTableNo = tableS;
					 break;
				 case 1:
					 matchTableNo = tableM + smallTables;
					 break;
				 case 2:
					 matchTableNo = tableL + middleTables + smallTables;
				 }
			 }

		 }
		 return matchTableNo;
	 }
	 
	 //given a time list, find the earliest willing leaving table number
	 public int findEarliestTableNo(LocalTime[] tables) {
		 int i = 0;
		 int totalTable = tables.length;
		 int earliestTable = 0;
		 LocalTime earliest = tables[0];

		 //get the earliest timestamp in the table list
		 for(i = 1; i < totalTable; i++) {
			 if(tables[i].isBefore(earliest)) {
				 earliest = tables[i];
				 earliestTable = i;
			 }
		 }
		 return earliestTable;
	 }
	 
	 //judge whether there's an available table in the list
	 public boolean isAvailable(LocalTime[] checkList) {
		 //for loop, if find the first 00:00, then there is an available table in this list
		 for(int i = 0; i < checkList.length; i++) {
			 if(checkList[i].equals(LocalTime.of(0, 0)))
				 return true;
		 }
		 return false;
	 }
	 
	 //judge whether there is an available and strictly same size table suitable for the group
	 public boolean isAvailableIfStrictOrNot(int groupSize, boolean strict) {
		 
		 splitTables();
		 
		 if(strict) {
			//if we find a value stored as "00:00", we assume this table is empty and available to serve the next
			 if(groupSize > 0 && groupSize <= smallTableSize) {
				 return isAvailable(smallTableTimes);
			 }
			 else if(groupSize > smallTableSize && groupSize <= middleTableSize) {
				 return isAvailable(middleTableTimes);
			 }
			 else {
				 return isAvailable(largeTableTimes);
			 }
		 }
		 else {
			 if(groupSize > middleTableSize && groupSize <= largeTableSize) {
				 return isAvailable(largeTableTimes);
			 }
			 else if(groupSize > smallTableSize && groupSize <= middleTableSize) {
				 return isAvailable(middleTableTimes) || isAvailable(largeTableTimes);
			 }
			 else {
				 return isAvailable(insideTableTimes);
			 }
		 }
		 
	 }
	 
	
}
