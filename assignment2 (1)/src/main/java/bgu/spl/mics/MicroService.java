package main.java.bgu.spl.mics;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.omg.CORBA.Object;

import main.java.bgu.spl.mics.Broadcast;
import main.java.bgu.spl.mics.Callback;
import main.java.bgu.spl.mics.Request;
import main.java.bgu.spl.mics.RequestCompleted;
import main.java.bgu.spl.mics.impl.MessageBusImpl;

//import bgu.spl.mics.impl.MessageBusImpl;

/**
 * The MicroService is an abstract class that any micro-service in the system
 * must extend. The abstract MicroService class is responsible to get and
 * manipulate the singleton {@link MessageBus} instance.
 * <p>
 * Derived classes of MicroService should never directly touch the message-bus.
 * Instead, they have a set of internal protected wrapping methods (e.g.,
 * {@link #sendBroadcast(bgu.spl.mics.Broadcast)}, {@link #sendBroadcast(bgu.spl.mics.Broadcast)},
 * etc.) they can use . When subscribing to message-types,
 * the derived class also supplies a {@link Callback} that should be called when
 * a message of the subscribed type was taken from the micro-service
 * message-queue (see {@link MessageBus#register(bgu.spl.mics.MicroService)}
 * method). The abstract MicroService stores this callback together with the
 * type of the
 * message is related to.
 * <p>
 */
/**
 * we added 2 data structures for the microservice
 * ConcurrentHashMap MessageCall with a key of messages and data of the corresponding callback
 * ConcurrentHashMap compCall with a key of requests and data of the corresponding callback
 */
public abstract class MicroService implements Runnable {

    private boolean terminated = false;
    private final String name;
    private ConcurrentHashMap<Class<? extends Message>, Callback>MessageCall;
    private ConcurrentHashMap<Request<?>,Callback> compCall;
    
    
   
    /**
     * @param String name- the name of the micro service
     * creates an instace of microservice
     */
    public MicroService(String name) {
        this.name = name;
        MessageCall=new ConcurrentHashMap<>();
        compCall=new ConcurrentHashMap< Request<?>,Callback>();
        (MessageBusImpl.getInstance()).register(this);
    }
    
    /**
     * @param <R>type- type of request
     * @param Callback<R> callback- callback to request
     */
    protected final <R extends Request> void subscribeRequest(Class<R> type, Callback<R> callback) {
    	
        (MessageBusImpl.getInstance()).subscribeRequest(type, this);
        MessageCall.put(type, callback);
        
    }
    
    /**
     * @param <R>type- type of request
     * @param Callback<R> callback- callback to request
     */
    protected final <B extends Broadcast> void subscribeBroadcast(Class<B> type, Callback<B> callback) {
        (MessageBusImpl.getInstance()).subscribeBroadcast(type, this);
        MessageCall.put(type, callback);
        
    }
    
    
    /**
     * @param Request<T> r- the request
     * @param Callback<T> onComplete- callback to request
     * @return Boolean ans- the ans to request
     */
    protected final <T> boolean sendRequest(Request<T> r, Callback<T> onComplete) {
	boolean ans=(MessageBusImpl.getInstance()).sendRequest(r,this);
	compCall.put(r, onComplete);
    return ans; 
}

    /**
     * send the broadcast message {@code b} using the message-bus.
     * <p>
     * @param b the broadcast message to send
     */
    protected final void sendBroadcast(Broadcast b) {
       (MessageBusImpl.getInstance()).sendBroadcast(b);
    }

    /**
     * complete the received request {@code r} with the result {@code result}
     * using the message-bus.
     * <p>
     * @param <T>    the type of the expected result of the received request
     *               {@code r}
     * @param r      the request to complete
     * @param result the result to provide to the micro-service requesting
     *               {@code r}.
     */
    protected final <T> void complete(Request<T> r, T result) {
    	(MessageBusImpl.getInstance()).complete(r,result);
    }

    /**
     * this method is called once when the event loop starts.
     */
    protected abstract void initialize();

    /**
     * signal the event loop that it must terminate after handling the current
     * message.
     */
    protected final void terminate() {
        this.terminated = true;
    }

    /**
     * @return the name of the service - the service name is given to it in the
     *         construction time and is used mainly for debugging purposes.
     */
    public final String getName() {
        return name;
    }

    /**
     * the entry point of the micro-service. TODO: you must complete this code
     * otherwise you will end up in an infinite loop.
     * while microservice isnt terminated we check if request was handled
     * if hadled we we call request complete and if not we call another callback to the message
     * at the end we unregister from microserveice
     */
    @Override
    public final void run() {
        initialize();
   while (!terminated) {
	try {
		Message message = MessageBusImpl.getInstance().awaitMessage(this);
	
	    if (message.getClass()==RequestCompleted.class){  //req was handled,start a call back that prints ans  
    	  compCall.get(((RequestCompleted)message).getCompletedRequest()).call(((RequestCompleted)message).getResult());
      }else{
    	  MessageCall.get(message.getClass()).call(message); //starts another callback that prints another message
      }
	}
	catch(InterruptedException e) {}
	
    }//end while
   (MessageBusImpl.getInstance()).unregister(this);
    }//end function
    
}