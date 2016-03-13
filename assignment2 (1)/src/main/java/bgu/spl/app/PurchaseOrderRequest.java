package main.java.bgu.spl.app;

import main.java.bgu.spl.mics.Request;

/**
 * a request to buy a shoe that the webclientservice send to the selling service 
 *
 */
public class PurchaseOrderRequest implements Request<Receipt> {
	private String shoeType;
	private boolean onlyDiscount;
	private int tick;
	private String customer;
	
	/**
	 * @param String ShoeType-the name of the shoe
	 * @param Boolean onlyDiscount-if the client want to buy only on discount
	 * @param int tick-current tick
	 * @param String customer- the name of the customer
	 */
	public PurchaseOrderRequest(String type,boolean discount,int time,String name){
		shoeType=type;
		onlyDiscount=discount;
		tick=time;
		customer=name;
	}
	/**
	 * @return String customer
	 */
	public String getName(){
		return customer;
	}
	/**
	 * @return String shoeType
	 */
	public String getType(){
		return shoeType;
	}
	/**
	 * @return Boolean onlyDiscount
	 */
	public boolean getOnlyDiscount(){
		return onlyDiscount;
	}
	/**
	 * @return int tick
	 */
	public int getTick(){
		return tick;
	}
	
	
	

}
