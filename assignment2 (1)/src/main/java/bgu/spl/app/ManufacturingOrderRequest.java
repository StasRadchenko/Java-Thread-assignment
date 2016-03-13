package main.java.bgu.spl.app;

import main.java.bgu.spl.mics.Request;

/**
 * the request from the management service to the factory about the amount of shoes we need to create (tick%5+1)
 *
 */
public class ManufacturingOrderRequest implements Request<Receipt> {
     String shoeType;
     private int tick;
     private int amount;
     
     /**
     * @param  String shoeType- the name of the shoe
     * @param  int tick- current tick which the order was created at
     * @param int amount of the shoes we need to create
     * 
     **/
    public ManufacturingOrderRequest(String type,int time,int num){
    	 shoeType=type;
    	 tick=time;
    	 amount=num;
     }
     /**
      * the shoe type
     * @return String shoeType
     */
    public String getType(){
    	 return shoeType;
     }
     /**
     * @return int tick
     */
    public int getTick(){
    	 return tick;
     }
     /**
     * @return int amount
     */
    public int getAmount(){
    	 return amount;
     }
     
}
