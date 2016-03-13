package main.java.bgu.spl.app;

/**
 * An object which describes a schedule of a single discount that the manager{@link ManagementService} will add to a specific
 * shoe at a specific tick.The manager sends a broadcast{@link#NewDiscountBroadcast(bgu.spl.app)} about a discount according to 
 * current tick,meaning that if current tick equals to tick Discount Schedule contains(checking this by getting the tick in the schedule using function {
 * @link DiscountSchedule#getTick(bgu.spl.app)} 
 */
public class DiscountSchedule {
   String shoeType;
   int tick;
   int discountAmount;
   
   /**
 * @param String type
 * @param int time
 * @param int discount
 */
public DiscountSchedule(String type,int time,int discount){
	   shoeType=type;
	   tick=time;
	   discountAmount=discount;
   }
   /**
 * @return int tick
 */
public int getTick(){
	   return tick;
   }//getter to get the tick in the discount schedule
   /**
 * @return String shoeType
 */
public String getType(){
	   return shoeType;
   }//getter to get the type of the shoe from the discount schedule
   /**
 * @return int discountAmount
 */
public int getDiscountAmount(){
	   return discountAmount;
   }//getter to get the amount of shoes we was to discount it the specific tick
}
