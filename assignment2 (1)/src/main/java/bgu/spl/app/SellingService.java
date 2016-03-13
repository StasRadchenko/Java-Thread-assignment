package main.java.bgu.spl.app;

import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import main.java.bgu.spl.mics.MicroService;


/**
 * the selling service recieves a request from a costumer about buying a shoe through the website client server  
 * than the selling service sends a restock request  to the management service 
 *
 */
public class SellingService extends MicroService {
	private static final Logger LOGGER = Logger.getLogger(SellingService.class.getName());
	private int currTick;
	private CountDownLatch cDL;
	public SellingService(String name, CountDownLatch latch) {
		super(name);
		cDL = latch;
	}

	/* 
	 * subscribes to termination broadcast
	 * subscribes to tick broadcast
	 * subscribes to purchase order request:
	 * we check the buyResult-
	 * if the result is regular price or discount price we write on the reciept the relevant info 
	 * if the result is not on discount we return a complete request with the value null
	 * if the result is not in stock we check if the order is for discount shoe or not
	 * if on discount we return a complete result with null (we cant order right now on discount because
	 * there is no discount)
	 * if not on discount we make the order-
	 * if the manager returns true -means that there was a manufacture to make the order
	 * if return false we return a complete request with null
	 */
	@Override
	protected void initialize() {
		LOGGER.info(this.getName() + " Starts selling");
		subscribeBroadcast(TerminationBroadcast.class, callback -> {
			LOGGER.info(getName() + " is terminating");

			this.terminate();
		});
		this.subscribeBroadcast(TickBroadcast.class, tick -> {
			currTick = tick.getTick();
		});

		subscribeRequest(PurchaseOrderRequest.class, order -> {
				
			
			BuyResult result = Store.getInstance().take(order.getType(), order.getOnlyDiscount());
			if (result == BuyResult.REGULAR_PRICE) {
				LOGGER.info(order.getName()+" bought on regular price "+order.getType());
				Receipt receipt = new Receipt(getName(), order.getName(), order.getType(), false, currTick,
						order.getTick(), 1);
				Store.getInstance().file(receipt);
				complete(order, receipt);
			} else if (result == BuyResult.DISCOUNTED_PRICE) {
				LOGGER.info(order.getName()+" bought on discounted price "+order.getType());
				Receipt receipt = new Receipt(getName(), order.getName(), order.getType(), true, currTick,
						order.getTick(), 1);
				Store.getInstance().file(receipt);
				complete(order, receipt);
			} else if (result == BuyResult.NOT_ON_DISCOUNT) {
				LOGGER.info(order.getName()+" wants the shoe on discount but there is none for: "+order.getType());
				complete(order, null);
			} else {
				if (order.getOnlyDiscount()) {
					LOGGER.info(order.getName()+" there is no such "+order.getType());
					complete(order, null);
					return;
				} else {
					LOGGER.info(getName()+" by request of "+order.getName()+" restocking "+order.getType());
					sendRequest(new RestockRequest(order.getType()), res -> {
						if (res == true) {
							Receipt receipt = new Receipt(getName(), order.getName(), order.getType(), false, currTick,
									order.getTick(), 1);
							Store.getInstance().file(receipt);
							//System.out.println("from store : "+order.getName());
							complete(order, receipt);
						} else
							LOGGER.info(order.getName()+" there is manufactor to create: "+order.getType());
							complete(order, null);
					}

					);
				}
			}
			
		});
			
		cDL.countDown();
	}

}
