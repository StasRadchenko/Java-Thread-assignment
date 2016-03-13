package main.java.bgu.spl.app;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ShoeStoreRunner {

	public static void main(String[] args) {
		System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s%6$s%n");
		Set<Thread> marks = new HashSet<Thread>();// we will hold list of all
													// thread that we create so
													// we will know to print the
													// store when they finnish
													// using the join
		FileReader reader;
		try {
			reader = new FileReader(args[0]);
			JsonParser jsonParser = new JsonParser();
			JsonObject jasontree = (JsonObject) jsonParser.parse(reader);

			JsonElement storageinitial = jasontree.get("initialStorage");// reading
																			// line
																			// of
																			// initialStorage
			JsonArray arr = storageinitial.getAsJsonArray();
			ShoeStorageInfo[] storage = new ShoeStorageInfo[arr.size()];// creating
																		// no
																		// array
																		// of
																		// shoes
																		// that
																		// we
																		// gonna
																		// load
																		// to
																		// our
																		// store
			ArrayList<ShoeStorageInfo> shoes = new ArrayList<ShoeStorageInfo>();
			arr.forEach((initial) -> {
				JsonObject shoandamont = initial.getAsJsonObject();
				JsonElement st = shoandamont.get("shoeType");
				JsonElement amount = shoandamont.get("amount");
				ShoeStorageInfo shoe = new ShoeStorageInfo(st.getAsString(), amount.getAsInt(), 0);// creation
																									// of
																									// new
																									// shoe
																									// according
																									// to
																									// info
																									// we
																									// got
				shoes.add(shoe);

			});
			shoes.toArray(storage);
			Store.getInstance().load(storage);// loading to ste store

			// initializing the services
			JsonElement servis = jasontree.get("services");
			JsonObject servicetype = servis.getAsJsonObject();
			// time
			int timespeed = servicetype.get("time").getAsJsonObject().get("speed").getAsInt();
			int duration = servicetype.get("time").getAsJsonObject().get("duration").getAsInt();
			// manager Schedule
			JsonArray arr1 = servicetype.get("manager").getAsJsonObject().get("discountSchedule").getAsJsonArray();
			ArrayList<DiscountSchedule> list = new ArrayList<DiscountSchedule>();// Discount
																					// schedules
																					// for
																					// the
																					// manager
			arr1.forEach((sced) -> {
				JsonObject fordiscount = sced.getAsJsonObject();
				String nameofshotodis = fordiscount.get("shoeType").getAsString();
				int howmany = fordiscount.get("amount").getAsInt();
				int when = fordiscount.get("tick").getAsInt();
				DiscountSchedule newDiscountSced = new DiscountSchedule(nameofshotodis, when, howmany);
				list.add(newDiscountSced);
			});

			int factNum = servicetype.get("factories").getAsInt();
			int sellersNum = servicetype.get("sellers").getAsInt();

			// customers info
			JsonArray arr2 = servicetype.get("customers").getAsJsonArray();
			int customersNum = arr2.size();
			int servicesNum = 1 + factNum + sellersNum + customersNum;
			CountDownLatch countLat = new CountDownLatch(servicesNum);// countLatch
																		// creating
			Thread manager = new Thread(new ManagementService(list, countLat));
			marks.add(manager);
			manager.start();
			// new Thread(new ManagementService(list,countLat)).start();//start
			// the manager
			for (int k = 0; k < sellersNum; k++) {
				Thread seller = new Thread(new SellingService("SellingService" + " " + k, countLat));
				marks.add(seller);
				seller.start();
				// new Thread(new SellingService("SellingService" +" "+k,
				// countLat)).start();
			} // creating and starting selling service
			for (int f = 0; f < factNum; f++) {
				Thread factory = new Thread(new ShoeFactoryService("ShoeFactoryService" + " " + f, countLat));
				marks.add(factory);
				factory.start();
				// new Thread(new ShoeFactoryService("ShoeFactoryService"+" "+
				// f, countLat)).start();

			} // creating and starting the factory services
			arr2.forEach((action) -> {
				JsonObject buyer = action.getAsJsonObject();
				String buyerName = buyer.get("name").getAsString();
				JsonArray wishList = buyer.get("wishList").getAsJsonArray();
				Set wishSet = new HashSet();
				wishList.forEach((wish) -> {
					String shoeName = wish.getAsString();
					wishSet.add(shoeName);
				});
				JsonArray purchaseSchedule = buyer.get("purchaseSchedule").getAsJsonArray();
				ArrayList<PurchaseSchedule> purchases = new ArrayList<PurchaseSchedule>();
				purchaseSchedule.forEach((purSced) -> {
					JsonObject purchase = purSced.getAsJsonObject();
					JsonElement shoeType = purchase.get("shoeType");
					JsonElement tick = purchase.get("tick");
					PurchaseSchedule purchSched = new PurchaseSchedule(shoeType.getAsString(), tick.getAsInt());
					purchases.add(purchSched);
				});
				Thread customer = new Thread(new WebsiteClientService(buyerName, purchases, wishSet, countLat));
				marks.add(customer);
				customer.start();
			}); // running over the customers creating them and adding to our
				// list of threads

			try {
				countLat.await();// after creating the count down leech we will
									// wait till all the services initialized
									// and after that we will initialize the
									// timer
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			Thread timer = new Thread(new TimeService(timespeed, duration));
			marks.add(timer);
			timer.start();
			// new Thread(new TimeService(timespeed,duration)).start();//start
			// the timer service
			marks.forEach((thread) -> {
				try {
					thread.join();// using the join function so that at the end
									// of the running of the thread we will
									// print the store
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
			Store.getInstance().print();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JsonIOException e) {
			System.out.println("Invalid jason file input");
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.out.println("Inner Function Problem on running the jason");
			e.printStackTrace();
		} catch (RuntimeException e) {
			System.out.println("unexpect jason running error");
			e.printStackTrace();
		}

	}

}
