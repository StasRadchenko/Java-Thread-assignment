package main.java.bgu.spl.app;

/**
 * info about the purchases of the client (which shoe he
 * needs to buy in which tick
 *
 */
public class PurchaseSchedule {
  private String shoeType;
  private int tick;
  
  /**
 * @param String shoeType- the type of the shoe 
 * @param int tick- the current tick
 */
public PurchaseSchedule(String type,int time){
	  shoeType=type;
	  tick=time;
	  }
  /**
 * @return int tick
 */
public int getTick(){
	  return tick;
  }
  /**
 * @return String shoeType
 */
public String getType(){
	  return shoeType;
  }
  
}
