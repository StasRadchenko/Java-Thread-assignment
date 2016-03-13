package main.java.bgu.spl.mics.impl;

import java.util.concurrent.ConcurrentHashMap;

import main.java.bgu.spl.mics.Broadcast;
import main.java.bgu.spl.mics.Message;
import main.java.bgu.spl.mics.MessageBus;
import main.java.bgu.spl.mics.MicroService;
import main.java.bgu.spl.mics.Request;
import main.java.bgu.spl.mics.RequestCompleted;

import java.util.concurrent.*;

/**
 * we created 3 data structures for the message bus ConcurrentHashMap microMap -
 * key is microservice and value is LinkedBlockingQueue of messages handles all
 * the messages of the micro services ConcurrentHashMap messages- key is
 * messages and value is RoundRobin of microservices handles all the micro
 * services of the messages ConcurrentHashMap pending- key is request and value
 * is microservice handles all the requests of the micro services that needs an
 * answer
 */

public class MessageBusImpl implements MessageBus {
	private ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> microMap;
	private ConcurrentHashMap<Class<? extends Message>, RoundRobin<MicroService>> messages;
	private ConcurrentHashMap<Request, MicroService> pending;
	// private Object lock=new Object();
	private Object broadcastLock = new Object();
	private Object requestLock = new Object();

	/**
	 * instance of the message bus
	 */
	private static class SingletonHolder {
		private static MessageBusImpl instance = new MessageBusImpl();
	}

	/**
	 * constructor for data structures described above
	 */
	public MessageBusImpl() {
		messages = new ConcurrentHashMap<Class<? extends Message>, RoundRobin<MicroService>>();
		microMap = new ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>>();
		pending = new ConcurrentHashMap<Request, MicroService>();
	}

	/*
	 * if messages contains the type request we add the microservice to the
	 * RoundRobin of the micro services otherwise we create a new cell with the
	 * new data
	 */
	public void subscribeRequest(Class<? extends Request> type, MicroService m) {
		synchronized (requestLock) {
			if (messages.containsKey(type))
				(messages.get(type)).add(m);

			else {
				RoundRobin<MicroService> temp = new RoundRobin<MicroService>();
				temp.add(m);
				messages.put(type, temp);
			}
		}

	}

	/*
	 * we create a new RoundRobin microservice and add the type and microservice
	 * if messages doesnt contain type (synchronized) otherwise if type exists
	 * but m isnt in the round robin we add it to messages
	 * 
	 */
	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		synchronized (broadcastLock) {
			if (!(messages.containsKey(type))) {
				RoundRobin<MicroService> services = new RoundRobin<MicroService>();
				messages.put(type, services);
				services.add(m);

			}
			if (!((messages.get(type)).exists(m)))
				(messages.get(type)).add(m);
		}
	}

	/*
	 * if pending contains the req and microMap contains microservice we remove
	 * micro service from pending and add it as request complete
	 * 
	 */
	@Override
	public <T> void complete(Request<T> r, T result) {
		if (pending.containsKey(r) && microMap.containsKey(pending.get(r))) {
			MicroService temp = pending.get(r);
			pending.remove(r);
			Message m = new RequestCompleted<T>(r, result);
			LinkedBlockingQueue<Message> s = microMap.get(temp);
			s.add(m);

		}

	}

	/*
	 * if messages contains wanted broadcast we apply broad cast to
	 * microservices in microMap
	 */
	@Override
	public void sendBroadcast(Broadcast b) {
		if (messages.containsKey(b.getClass())) {
			RoundRobin temp = messages.get(b.getClass());
			temp.applyBroadcast(microMap, b);
		}

	}

	/*
	 * if messages contains wanted request we add it to pending if micromap
	 * doesnt contain microservice we return false else we add the services and
	 * the wanted request to the mocroMap
	 */
	@Override
	public boolean sendRequest(Request<?> r, MicroService requester) {
		if ((messages.containsKey(r.getClass()))) {
			pending.put(r, requester);
			RoundRobin<MicroService> temp = messages.get(r.getClass());
			MicroService s = temp.nextMicroService();
			if (s == null || (!(microMap.containsKey(s)))) {
				return false;
			}
			(microMap.get(s)).add(r);
			return true;
		}
		return false;
	}

	/*
	 * we add to microMap wanted microservice with an empty LinkedBlockingQueue
	 * of messages
	 */
	@Override
	public void register(MicroService m) {
		LinkedBlockingQueue<Message> queue = new LinkedBlockingQueue<Message>();
		microMap.putIfAbsent(m, queue);

	}

	/*
	 * we unregister-remove (synchronized) wanted microservice from all data
	 * structures
	 */
	@Override
	public synchronized void unregister(MicroService m) {
		microMap.remove(m);
		messages.forEach((k, v) -> v.delete(m));
		pending.forEach((k, v) -> {
			if (pending.get(k) == m) {
				pending.remove(k);
			}
		});

	}

	/*
	 * we try to take the microservice from microMap, if not possible we wait
	 */
	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		try {
			return microMap.get(m).take();
		} catch (InterruptedException e) {
			throw new IllegalStateException();
		}
	}

	/**
	 * @return MessageBusImpl instance singletone of the messageBus
	 */
	public static MessageBusImpl getInstance() {
		return SingletonHolder.instance;
	}

}
