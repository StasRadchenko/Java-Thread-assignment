package main.java.bgu.spl.app;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import main.java.bgu.spl.mics.MicroService;

/**
 * the global system timer which handles clock ticks in the system count the
 * ticks in the system (using TIMER class in java) notify to all services which
 * are interested in current tick (TickBroadcast)
 */
public class TimeService extends MicroService {
	private static final Logger LOGGER = Logger.getLogger(TimeService.class.getName());
	private int duration;
	private int speed;
	private int currentTick;

	/**
	 * @param int
	 *            duration-number of ticks before termination
	 * @param int
	 *            speed-number of milliseconds each clock tick takes current
	 *            tick starts with 1
	 */
	public TimeService(int lap, int limit) {
		super("Timer");
		currentTick = 1;
		duration = limit;
		speed = lap;

	}

	/*
	 * sends broadcast of the current tick to all subscribers if current tick is
	 * bigger than duration (means we passed the time for this service to run)
	 * we send termination broadcast,terminate and cancel timer for it
	 * 
	 */
	@Override
	protected void initialize() {
		LOGGER.info("The clock start ticking");
		Timer timer = new Timer();

		timer.schedule(new TimerTask() {
			public void run() {
				if (currentTick > duration) {
					sendBroadcast(new TerminationBroadcast());
					timer.cancel();
					terminate();
				} else {
					LOGGER.info("Broadcasting Tick: " + currentTick);
					sendBroadcast(new TickBroadcast(currentTick));
					currentTick = currentTick + 1;
				}
			}

		}, speed, speed);
		// end while curr<dur
		this.subscribeBroadcast(TerminationBroadcast.class, callback -> {
			LOGGER.info("The clock is terminating");
			this.terminate();
		});

	}// end of the initialize

}
