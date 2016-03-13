package main.java.bgu.spl.app;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import main.java.bgu.spl.mics.MicroService;

/**
 * recieves the manufacture request from management service we created a
 * linkedBlockingQueue for all the requests we need to handle in this servic
 */
public class ShoeFactoryService extends MicroService {
	private static final Logger LOGGER = Logger.getLogger(ShoeFactoryService.class.getName());
	private int tick;
	private LinkedBlockingQueue<ManufacturingOrderRequest> requests;
	private int shoesManufactured;
	private CountDownLatch cDL;

	/**
	 * @param String
	 *            name- the name of the shoe factory service
	 * @param CountDownLatch
	 *            latch- the count down to initialize
	 */
	public ShoeFactoryService(String name, CountDownLatch latch) {
		super(name);
		requests = new LinkedBlockingQueue<ManufacturingOrderRequest>();
		shoesManufactured = 0;
		cDL = latch;

	}

	/*
	 * we first subscribe to termination broadcast and tick broadcast
	 * 
	 * we check in current tick if we still have requests in queue if we have
	 * requests: we check the num of shoes that were created (shoesManufactured)
	 * if lower than what was requested, we create another shoe (each tick one
	 * shoe is created) if its equal we poll from the queue, create a
	 * reciept,send a complete request and initialize shoesManufactured
	 * 
	 * 
	 */
	@Override
	protected void initialize() {
		LOGGER.info(getName() + " starts working!!!");
		subscribeBroadcast(TerminationBroadcast.class, callback -> {
			LOGGER.info(getName() + " is terminating");
			this.terminate();
		});
		subscribeBroadcast(TickBroadcast.class, time -> {
			tick = time.getTick();
			if (!requests.isEmpty()) {

				if (shoesManufactured == (requests.peek()).getAmount()) {
					ManufacturingOrderRequest s = requests.poll();
					Receipt receipt = new Receipt(getName(), "Store", s.getType(), false, tick, s.getTick(),
							s.getAmount());
					complete(s, receipt);
					if (!(requests.isEmpty()))
						shoesManufactured = 1;
					else {
						shoesManufactured = 0;
					}
				}
				else{
					shoesManufactured ++;
				}
			}

		});

		subscribeRequest(ManufacturingOrderRequest.class, orderReq -> {
			LOGGER.info(getName() + " got order of: " + orderReq.getType() + " amount " + orderReq.getAmount());
			requests.add(orderReq);
		});
		cDL.countDown();

	}

}
