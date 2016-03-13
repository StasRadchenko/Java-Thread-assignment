package main.java.bgu.spl.app;

import main.java.bgu.spl.mics.Request;

/**
 * a request to order more shoes that is send from selling service to managment service 
 * (after that a not in stock answer was send to selling service) 
 *
 */
public class RestockRequest implements Request<Boolean> {
	String shoeType;
	
	/**
	 * @param shoeType- the type of shoe the selling service wants to order
	 */
	public RestockRequest(String type){
		shoeType=type;
	}
	/**
	 * @return String shoeType
	 */
	public String getType(){
		return shoeType;
	}
	
    
    
}
