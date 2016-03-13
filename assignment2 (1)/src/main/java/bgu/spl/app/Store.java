package main.java.bgu.spl.app;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * the store which saves the info on the shoes
 * the info is saved on ConcurrentHashMap with a key String of the shoe name and the info as the data
 * we also save the reciepts in LinkedBlockingQueue 
 */
public class Store {
	private Object lock1 = new Object();
	private static final Logger LOGGER=Logger.getLogger(Store.class.getName());
    private ConcurrentHashMap<String, ShoeStorageInfo> storeStorage;
    LinkedBlockingQueue<Receipt>receipts;
    private static class StoreHolder {
        private static Store instance = new Store();
    }
    
    
    /**
     * ConcurrentHashMap storeStorage- the shoes in store
     * LinkedBlockingQueue receipts- the reciepts on the sells.
     */
    protected Store(){
    	storeStorage=new ConcurrentHashMap<String, ShoeStorageInfo>();
    	receipts=new LinkedBlockingQueue<Receipt>();
    }
    /**
     * @param storeStorage- we load all the shoe info to storeStorage
     */
    public void load ( ShoeStorageInfo [ ] storage ){
    	    storeStorage=new ConcurrentHashMap<String, ShoeStorageInfo>();
    	    receipts=new LinkedBlockingQueue<Receipt>();
        	for(int i=0;i<storage.length;i++){
        	    storeStorage.put(storage[i].getType(), storage[i]);
        	}
        	LOGGER.info("The Store is open for busines!");
        	
        	
    }
    
    /**
     * @param String shoeType- the type of the shoe
     * @param Boolean onlyDiscount- if its on discount or not
     * @return BuyResult- 
     */
    public BuyResult take(String shoeType,boolean onlyDiscount){
    	synchronized (lock1) {
			
		
    	if(!(storeStorage.containsKey(shoeType))||storeStorage.get(shoeType).getAmount()==0)
    		return BuyResult.NOT_IN_STOCK;
    	else if((onlyDiscount==true)&&(!(storeStorage.get(shoeType)).isOnDiscount()))
    		return BuyResult.NOT_ON_DISCOUNT;
    	else if((storeStorage.get(shoeType)).isOnDiscount()){
    		(storeStorage.get(shoeType)).shoeBuy();
    		return BuyResult.DISCOUNTED_PRICE;
    	}
    	(storeStorage.get(shoeType)).shoeBuy();
    	return BuyResult.REGULAR_PRICE;
    	}
    }
    
    public void add(String shoeType,int amount){
    	if(storeStorage.containsKey(shoeType))
    		storeStorage.get(shoeType).addAmount(amount);
    	ShoeStorageInfo temp=new ShoeStorageInfo(shoeType, amount, 0);
    	storeStorage.put(temp.getType(), temp);
    	LOGGER.info(shoeType+" added to Store,amount: "+amount);
    }
    public void addDiscount(String shoeType,int amount){
    	if(!(storeStorage.containsKey(shoeType))){
    		return;
    	}	
    	(storeStorage.get(shoeType)).addDiscount(amount);
    	LOGGER.info("Added discount shoe for "+shoeType+". amount on discount: "+amount);
    	
    }
    public void file (Receipt receipt){
    	receipts.add(receipt);
    	LOGGER.info("riceipt added to store receipts");
    }

    public void print(){
    	LOGGER.info("Printing Store Info");
    	storeStorage.forEach((k,v) ->v.printShoe());
    	receipts.forEach((receipe)->receipe.printReceipt()); 
    }

    
    
    
    public static Store getInstance() {
        return StoreHolder.instance;
    }
}
