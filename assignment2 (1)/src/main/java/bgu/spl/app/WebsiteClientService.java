package main.java.bgu.spl.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import main.java.bgu.spl.mics.MicroService;

/**
 * the client with his requests to buy shoes,we stored the data in 2 data structures
 * ConcurrentHashMap scedMap for the purchases this client needs to do with a 
 * key Integer which indicates about the time tick the purchase request needs to be send
 * and a value PurchaseSchedule (info about the purchases of the client)
 *  
 * Set listWish contains name of shoe types that the client will buy only when 
 * there is a discount on them
 *
 */
public class WebsiteClientService extends MicroService {
	private static final Logger LOGGER = Logger.getLogger(WebsiteClientService.class.getName());
	private CountDownLatch cDL;
	private int currentTick;
	private ConcurrentHashMap<Integer, List<PurchaseSchedule>> scedMap;
	private Set<String> listWish;

	/**
	 * @param String name- the name of the client
	 * @param List purchaseSchedule- the purchase that the client wants to buy
	 * @param Set listWish- the set of shoe names
	 * @param CountDownLatch cDL- count down
	 */
	public WebsiteClientService(String name, List<PurchaseSchedule> purchaseSchedule, Set<String> wishList, CountDownLatch latch) {
		super(name);
		LOGGER.info("client "+name+" starting");
		cDL = latch;
		listWish = wishList;
		scedMap = new ConcurrentHashMap<Integer, List<PurchaseSchedule>>();
		for(PurchaseSchedule purchase : purchaseSchedule){
			Integer tick = new Integer(purchase.getTick()); 
			if(!scedMap.containsKey(tick)){
				List<PurchaseSchedule> tickList =Collections.synchronizedList( new ArrayList<PurchaseSchedule>());
				scedMap.put(tick, tickList);
			}
			scedMap.get(tick).add(purchase);
		}
	}

	/* 
	 * we subscribe to a tickbroadcast and update current tick
	 * search in scedMap for current tick 
	 * if found we check purchase list of that tick:
	 * for each purchase of this tick we send purchase order request
	 * if receipt is not null (done successfully) we remove this purchase
	 * at the end we remove the tick from scedMap.
	 * when there are no purchase and all ticks were removed we terminate
	 * and subscribe to termination broadcast
	 * 
	 * for wish list we subscribe to discount broad cast
	 * we try to buy discount shoe:
	 * if receipt isnt null we remove from wish list
	 * when list is empty we terminate
	 */
	@Override
	protected void initialize() {
		subscribeBroadcast(TickBroadcast.class, tick -> {
			currentTick = tick.getTick();
			Integer integerTick = new Integer(currentTick);
			if(scedMap.containsKey(integerTick)){
				List<PurchaseSchedule> tPurchaseSchedule = scedMap.get(integerTick);
				for(PurchaseSchedule purchase : tPurchaseSchedule){
					sendRequest(new PurchaseOrderRequest(purchase.getType(), false, currentTick, getName()), receipt -> {
						if(receipt!=null){
							tPurchaseSchedule.remove(purchase);
					        if(tPurchaseSchedule.isEmpty())
							scedMap.remove(integerTick);
							if(listWish.isEmpty() && scedMap.isEmpty()){
								LOGGER.info(getName()+" finnished shopping!");
								terminate();
							}}
					});
				}
				
			}
			
		});
		subscribeBroadcast(TerminationBroadcast.class, callback->{
			LOGGER.info(getName()+" finnished shopping!");
			terminate();
		});
		subscribeBroadcast(NewDiscountBroadcast.class, callback -> {
			if(listWish.contains(callback.getType())){
				sendRequest(new PurchaseOrderRequest(callback.getType(), true, currentTick, getName()), receipt -> {
					if(receipt!=null){
						LOGGER.info(getName()+" Bought on discount"+" "+ callback.getType());
						listWish.remove(callback.getType());
					}
					if(listWish.isEmpty() && scedMap.isEmpty()){
						LOGGER.info(getName()+" finnished shopping!");
						terminate();}
				});
			}
			
		});
		
		cDL.countDown();
	}
	
	
	}