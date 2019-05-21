import java.time.LocalTime;
import java.util.ArrayList;

public class WaitingLine {
	
	private int smallGroupSize, middleGroupSize, largeGroupSize;
	
	private ArrayList<LocalTime> outsideGroupTimes = new ArrayList<LocalTime>();
	private ArrayList<Integer> outsideGroupSizes = new ArrayList<Integer>();
	
	private ArrayList<LocalTime> smallGroupTimes = new ArrayList<LocalTime>();
	private ArrayList<LocalTime> middleGroupTimes = new ArrayList<LocalTime>();
	private ArrayList<LocalTime> largeGroupTimes = new ArrayList<LocalTime>();
	private ArrayList<Integer> smallGroupSizes = new ArrayList<Integer>();
	private ArrayList<Integer> middleGroupSizes = new ArrayList<Integer>();
	private ArrayList<Integer> largeGroupSizes = new ArrayList<Integer>();
	private ArrayList<Integer> smallGroupIndices = new ArrayList<Integer>();
	private ArrayList<Integer> middleGroupIndices = new ArrayList<Integer>();
	private ArrayList<Integer> largeGroupIndices = new ArrayList<Integer>();
	
	public WaitingLine() {
		
	}
	
	public WaitingLine(int smallGroupSize, int middleGroupSize, int largeGroupSize) {
		super();
		this.smallGroupSize = smallGroupSize;
		this.middleGroupSize = middleGroupSize;
		this.largeGroupSize = largeGroupSize;
	}
	
	//getters
	public int getSmallGroupSize() {
		return smallGroupSize;
	}

	public int getMiddleGroupSize() {
		return middleGroupSize;
	}

	public int getLargeGroupSize() {
		return largeGroupSize;
	}
	
	public ArrayList<LocalTime> getOutsideGroupTimes() {
		return outsideGroupTimes;
	}

	public ArrayList<Integer> getOutsideGroupSizes() {
		return outsideGroupSizes;
	}

	public ArrayList<LocalTime> getSmallGroupTimes() {
		return smallGroupTimes;
	}

	public ArrayList<LocalTime> getMiddleGroupTimes() {
		return middleGroupTimes;
	}

	public ArrayList<LocalTime> getLargeGroupTimes() {
		return largeGroupTimes;
	}

	public ArrayList<Integer> getSmallGroupSizes() {
		return smallGroupSizes;
	}

	public ArrayList<Integer> getMiddleGroupSizes() {
		return middleGroupSizes;
	}

	public ArrayList<Integer> getLargeGroupSizes() {
		return largeGroupSizes;
	}
	
	public ArrayList<Integer> getSmallGroupIndices() {
		return smallGroupIndices;
	}

	public ArrayList<Integer> getMiddleGroupIndices() {
		return middleGroupIndices;
	}

	public ArrayList<Integer> getLargeGroupIndices() {
		return largeGroupIndices;
	}

	//DIY getters and setters
	public int getOutsideGroupsNumber() {
		return outsideGroupTimes.size();
	}
	
	public LocalTime getThisGroupTime(int groupNo) {
		return outsideGroupTimes.get(groupNo);
	}
	
	public LocalTime getTheLastTime() {
		return outsideGroupTimes.get(outsideGroupTimes.size() - 1);
	}

	public int getThisGroupSize(int groupNo) {
		return outsideGroupSizes.get(groupNo);
	}
	
	public int getTheLastSize() {
		return outsideGroupSizes.get(outsideGroupSizes.size() - 1);
	}

	public void addNewGroupTime(LocalTime newGroupTime) {
		this.outsideGroupTimes.add(newGroupTime);
	}

	public void addNewGroupSize(int newGroupSize) {
		this.outsideGroupSizes.add(newGroupSize);
	}
	
	//split the whole outside group line into three different sizes group lists
	//copy the original time stamp into new split lists
	//copy the original group size into new split lists
	public void splitGroups() {
		int i = 0;
		
		if(outsideGroupTimes.size() == 0)
			System.out.println("Error. There isn't any group outside.");
		else {
			for(i = 0; i < outsideGroupTimes.size(); i++) {
				//when it's a small group
				if(outsideGroupSizes.get(i) <= smallGroupSize) {
					smallGroupTimes.add(outsideGroupTimes.get(i));
					smallGroupSizes.add(outsideGroupSizes.get(i));
					smallGroupIndices.add(i);
				}
				//when it's a middle group
				else if(outsideGroupSizes.get(i) > smallGroupSize && outsideGroupSizes.get(i) <= middleGroupSize) {
					middleGroupTimes.add(outsideGroupTimes.get(i));
					middleGroupSizes.add(outsideGroupSizes.get(i));
					middleGroupIndices.add(i);
				}
				//when it's a large group
				else {
					largeGroupTimes.add(outsideGroupTimes.get(i));
					largeGroupSizes.add(outsideGroupSizes.get(i));
					largeGroupIndices.add(i);
				}
			}
		}
		
	}
	
	//define the size of a group
	public String defineSizeOfThisGroup(int groupSize) {
		String result = "no result";
		
		if(groupSize <= smallGroupSize)
			result = "small";
		else if(groupSize > smallGroupSize && groupSize <= middleGroupSize)
			result = "middle";
		else
			result = "large";
			
		return result;
	}
	
	//judge whether the group arriving time stamp is too late
	public boolean isLate(LocalTime groupTime, int groupSize) {
		LocalTime predictFinishTime;
		
		predictFinishTime = GenerateRandomFigures.timeAddMealTime(groupTime, groupSize);
		if(predictFinishTime.isAfter(LocalTime.parse("22:00")))
			return true;
		
		return false;
	}
	
	//when system finishes arranging a group of people outside
	public void finish(int groupNo, boolean splitOrNot,String groupSize) {
		//if applying split method, then remove the group from the spilt small lists
		//assuming that the system always deals with the first group in the either split list
		if(splitOrNot) {
			//if split, assuming groupNo is the No. in the split lists
			int originalNo = 0;
			if(groupSize.equalsIgnoreCase("large")) {
				originalNo = findGroupNo(largeGroupTimes.get(groupNo));
				largeGroupTimes.remove(groupNo);
				largeGroupSizes.remove(groupNo);
				largeGroupIndices.remove(groupNo);
			}
			else if(groupSize.equalsIgnoreCase("middle")) {
				originalNo = findGroupNo(middleGroupTimes.get(groupNo));
				middleGroupTimes.remove(groupNo);
				middleGroupSizes.remove(groupNo);
				middleGroupIndices.remove(groupNo);
			}
			else {
				originalNo = findGroupNo(smallGroupTimes.get(groupNo));
				smallGroupTimes.remove(groupNo);
				smallGroupSizes.remove(groupNo);
				smallGroupIndices.remove(groupNo);
			}
			//remove this group from the whole lists
			outsideGroupTimes.remove(originalNo);
			outsideGroupSizes.remove(originalNo);
		}
		else {
			//if not split, assuming groupNo is the No. in the whole outside list
			//remove this group from the whole lists
			outsideGroupTimes.remove(groupNo);
			outsideGroupSizes.remove(groupNo);
		}
	}
	
	//according to the group time stamp to find the group No 
	public int findGroupNo(LocalTime time) {
		int originalNo = 0;
		
		for(int i = 0; i < outsideGroupTimes.size(); i++) {
			if(outsideGroupTimes.get(i).equals(time))
				originalNo = i;
		}
		return originalNo;
	}
	
}
