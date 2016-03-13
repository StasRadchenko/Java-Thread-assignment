package main.java.bgu.spl.mics.impl;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;


import main.java.bgu.spl.mics.Broadcast;
import main.java.bgu.spl.mics.Message;
import main.java.bgu.spl.mics.MicroService;

/**
 * Round Robin algorithm in order to handle the list as requiered:
 * an arrangement of choosing all elements in a group equally in some rational order,
 * from the top to the bottom of a list and then starting again at the top of the list 
 * and so on. if a new microservice enters the list the current one will pass after him
 * in order to handle the new one before the old one
 *
 * 
 */
public class RoundRobin<T> {
	private CopyOnWriteArrayList<MicroService> list;
	private int index;
	
	/**
	 * constractor for calss
	 */
	public RoundRobin(){
		index = 0;
		list = new CopyOnWriteArrayList<MicroService> ();
	}
	
	/**
	 * @param MicroService ms- chekcs if microservice exists
	 * @return boolean true if exists in list
	 */
	public boolean exists(MicroService ms){
		return list.contains(ms);
	}
	
	/**
	 * @param MicroService ms- the nocroservice we want to delete
	 * we update index of list
	 */
	public synchronized void delete(MicroService ms) {
		if(list.size()!=0 && list.indexOf(ms)!=(-1)){
			int x= list.indexOf(ms);
			list.remove(ms);
			if(index>x) index--;
		}
	}
	
	/**
	 * @param MicroService ms- the mocroservice we want to add to the list
	 */
	public synchronized void add(MicroService ms){
		list.add(ms);
	}
	

	/**
	 * @return MicroService - the next microservice.if we arrive to the end of list we
	 * return to start
	 */
	public synchronized MicroService nextMicroService(){
		MicroService ans = null;
		if(list.size() > 0){
			ans = list.get(index);
			if(list.size()!=0)
				index = (index + 1) % list.size();
		}
		return ans;
	}
	/**
	 * @param ConcurrentHashMap microMap- map of microservices and their message
	 * @param Broadcast b-wanted broadcast to apply
	 */
	public synchronized void applyBroadcast(ConcurrentHashMap<MicroService, LinkedBlockingQueue<Message>> microMap,Broadcast b){
		if(!(list.isEmpty())){
			for(int i=0;i<list.size();i++){
				microMap.get(list.get(i)).add(b);
			}
		}
	}
	/**
	 * @return int size- the size of the list
	 */
	public synchronized int size(){
		return list.size();
	}
}