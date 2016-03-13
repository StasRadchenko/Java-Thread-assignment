package main.java.bgu.spl.app;

import main.java.bgu.spl.mics.Broadcast;

/**
 * management services sends discount broadcast in current tick
 *
 */
public class NewDiscountBroadcast implements Broadcast {
	String shoeType;
	int discountAmount;
	
	/**
	 * number of shoes we need to add discount on (by shoe type)
	 * @param String shoeType
	 * @param int discountAmount
	 */
	public NewDiscountBroadcast(String type,int discount){
		shoeType=type;
		discountAmount=discount;
	}
	
	/**
	 * @return String shoeType
	 */
	public String getType(){
		return shoeType;
	}
	/**
	 * @return int discountAmount
	 */
	public int getDiscountAmount(){
		return discountAmount;
	}

}
