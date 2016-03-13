package main.java.bgu.spl.app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.Stack;
import java.util.logging.Logger;

import main.java.bgu.spl.mics.MicroService;

/**
 * This micro-service can add discount to shoes in the store and send
 * NewDiscountBroadcast{@link NewDiscountBroadcast} to notify clients about
 * them.In addition, the ManagementService handles RestockRequests
 * {@link RestockRequest} that is being sent by the SellingService
 * {@link SellingService} when there is a customer trying to buy specified shoe
 * but there is none in the store.The manager hold 3 data structures all of them
 * from type ConcurrentHashMap.The first map contains key by String and the
 * value is Stack of {@link ManufacturingOrderRequest}. When adding a new
 * restock request we will get the stack by shoe type in the restock request out
 * of the map the we will get the ManufactturingOrderRequest out of the stack by
 * using the peek function on the stack, if there such
 * ManufactturingOrderRequest we will get the ArrayList for this specific
 * ManufactturingOrderRequest out of our second map(the key for this map
 * ManufactturingOrderRequest and the value ArrayList of restock requests),we
 * will check if the size of the array list equals to the amount of the shoes we
 * ordered it means that the only ManufactturingOrderRequest the restock request
 * could join is full and we need to create new ManufactturingOrderRequest add
 * it to stack for the specified shoe type create new arraylist add the current
 * restock request'add it to the arraylist,and the add it to the map using the
 * ManufactturingOrderRequest we created with array list we created as
 * value.this way we will always check only one ManufactturingOrderRequest and
 * we will need to access each map only once. in addition we store map for the
 * discound schedule,the key is the tick in which we need to send the broadcast
 * about the discount on shoe type and the value is array which contains all the
 * broadcasts in the tick.
 */
public class ManagementService extends MicroService {
	private static final Logger LOGGER = Logger.getLogger(ManagementService.class.getName());
	private int tick;
	private CountDownLatch cDL;
	private ConcurrentHashMap<String, Stack<ManufacturingOrderRequest>> ordersByString;
	private ConcurrentHashMap<ManufacturingOrderRequest, ArrayList<RestockRequest>> sentOrder;
	private ConcurrentHashMap<Integer, ArrayList<DiscountSchedule>> discountSchedule;

	/**
	 * In the constructor we "downloading" all the data from the given list to
	 * the data structure we mentioned before in addition we add CountDownLetch
	 * from our run so that all the threads will wait till the others will
	 * finnish constructing before running
	 * 
	 * @param list
	 * @param cDL
	 */
	public ManagementService(List<DiscountSchedule> list, CountDownLatch latch) {
		super("manager");
		cDL = latch;
		sentOrder = new ConcurrentHashMap<ManufacturingOrderRequest, ArrayList<RestockRequest>>();
		ordersByString = new ConcurrentHashMap<String, Stack<ManufacturingOrderRequest>>();
		discountSchedule = new ConcurrentHashMap<Integer, ArrayList<DiscountSchedule>>();
		list.forEach((sced) -> {
			int key = sced.tick;
			if (discountSchedule.containsKey(key)) {
				(discountSchedule.get(key)).add(sced);
			}
			ArrayList<DiscountSchedule> temp = new ArrayList<DiscountSchedule>();
			temp.add(sced);
			discountSchedule.put(key, temp);
		});
		LOGGER.info("Manager created");

	}

	/*
	 * the manager subscribes to recieve termination broadcast, which means that
	 * when the timer arrives to duration time, the timer will send to all of
	 * the services that are subscribed to termination broadcast a termination
	 * message which will cause termination of the service
	 * 
	 * 
	 * subscribe by tick request checks by the key (the tick),which was send by
	 * the timer if there is an array list with discountSchedual requests. if it
	 * was found than we send (with send request method) to all the serviecs
	 * that are subscribed to it.
	 * 
	 * subscribe by restock request: we handle all the requests with a
	 * concurrent hash map ordersByString with a string key(shoe type) and a
	 * stack value (all the requesters to this shoe: if shoe was found we check
	 * the stock: we peek to the first cell in the stack (only there we can add
	 * a request) we seacrh in the cuncurrent hash map sentOrder (which her key
	 * is ManufacturingOrderReques) and get the array list of all requesters
	 * (the value of the hash map) we check the size of the array list: if the
	 * size is smaller than tick%5+1 than we add this request to the list else
	 * (mean that there is no more space in the current manufacture request) we
	 * create a new stack and add the new request to the stack with one cell
	 * which has a list with the current data of the request. if the shoe wasnt
	 * found we create a new stack with a new array list and add the request.
	 * 
	 * we check the result of the reqeust: if the result is null (means that the
	 * order could not be completed) we go through all the requsts (in
	 * sendOrder) and add the complete result as false to all the requesters of
	 * the request after that we delete the array list if the result is not null
	 * it means that the order was completed as excpected we return true to the
	 * completed requests and the receipt should be added to receipts data
	 * storage (LinkedBlockingQueue) in the store.
	 * 
	 * 
	 */
	@Override
	protected void initialize() {
		LOGGER.info("Initializing manager");
		this.subscribeBroadcast(TerminationBroadcast.class, callback -> {
			LOGGER.info("The manager is terminating");
			this.terminate();
		});
		subscribeBroadcast(TickBroadcast.class, time -> {
			tick = time.getTick();
			if (discountSchedule.containsKey(tick)) {
				(discountSchedule.get(tick)).forEach((disSced) -> {
					Store.getInstance().addDiscount(disSced.getType(), disSced.discountAmount);
					NewDiscountBroadcast newDis = new NewDiscountBroadcast(disSced.getType(), disSced.discountAmount);
					sendBroadcast(newDis);
					LOGGER.info("Discount Broadcast Message:Discount on Shoe: " + disSced.getType());
				});
			}
		});
		subscribeRequest(RestockRequest.class, restock -> {
			if (ordersByString.containsKey(restock.getType())) {
				Stack<ManufacturingOrderRequest> manStack = ordersByString.get(restock.getType());
				ArrayList<RestockRequest> arrRestock = sentOrder.get(manStack.peek());
				if (arrRestock.size() < manStack.peek().getAmount()) {
					arrRestock.add(restock);
					LOGGER.info("Restock request on shoe Type " + restock.getType() + "," + " ADDED TO EXISTING ORDER");
				} else {
					LOGGER.info("Store created new restock request for shoe type: " + restock.getType());
					ManufacturingOrderRequest newReq = new ManufacturingOrderRequest(restock.getType(), tick,
							(tick % 5) + 1);
					ArrayList<RestockRequest> newRestock = new ArrayList<RestockRequest>();
					newRestock.add(restock);
					ordersByString.get(restock.getType()).push(newReq);
					sentOrder.put(newReq, newRestock);
					sendRequest(newReq, result -> {
						if (result == null) {
							(sentOrder.get(newReq)).forEach((restockReq) -> {
								complete(restockReq, false);
							});
							sentOrder.remove(newReq);
							ordersByString.get(newReq.getType()).remove(newReq);
							if (ordersByString.get(newReq.getType()).isEmpty())
								ordersByString.remove(newReq.getType());
						} else if (result != null) {
							LOGGER.info("Store bought succefuly " + newReq.getType() + " amout " + newReq.getAmount());
							int size = newReq.getAmount() - (sentOrder.get(newReq)).size();
							Store.getInstance().add(newReq.getType(), size);
							(sentOrder.get(newReq)).forEach((restockReq) -> {
								complete(restockReq, true);
							});
							Store.getInstance().file(result);
							sentOrder.remove(newReq);
							ordersByString.get(newReq.getType()).remove(newReq);
							if (ordersByString.get(newReq.getType()).isEmpty())
								ordersByString.remove(newReq.getType());

						}

					});
				}

			} else if (!(ordersByString.containsKey(restock.getType()))) {
				LOGGER.info("Store created new restock request for shoe type: " + restock.getType());
				ManufacturingOrderRequest newReq = new ManufacturingOrderRequest(restock.getType(), tick,
						(tick % 5) + 1);
				Stack<ManufacturingOrderRequest> newStack = new Stack<ManufacturingOrderRequest>();
				newStack.push(newReq);
				ArrayList<RestockRequest> newRestock = new ArrayList<RestockRequest>();
				newRestock.add(restock);
				sentOrder.put(newReq, newRestock);
				ordersByString.put(newReq.getType(), newStack);
				sendRequest(newReq, result -> {
					
					if (result == null) {
						(sentOrder.get(newReq)).forEach((restockReq) -> {
							complete(restockReq, false);
						});
						sentOrder.remove(newReq);
						ordersByString.get(newReq.getType()).remove(newReq);
						if (ordersByString.get(newReq.getType()).isEmpty())
							ordersByString.remove(newReq.getType());

					} else if (result != null) {
						LOGGER.info("Store bought succefuly " + newReq.getType() + " amout " + newReq.getAmount());
						int size = newReq.getAmount() - (sentOrder.get(newReq)).size();
						Store.getInstance().add(newReq.getType(), size);
						(sentOrder.get(newReq)).forEach((restockReq) -> {
							complete(restockReq, true);
						});
						
						Store.getInstance().file(result);
						sentOrder.remove(newReq);
						ordersByString.get(newReq.getType()).remove(newReq);
						if (ordersByString.get(newReq.getType()).isEmpty())
							ordersByString.remove(newReq.getType());

					}

				});

			}
		});

		cDL.countDown();
	}

}
