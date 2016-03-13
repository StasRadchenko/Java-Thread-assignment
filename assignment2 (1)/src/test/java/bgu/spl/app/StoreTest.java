package test.java.bgu.spl.app;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import main.java.bgu.spl.app.BuyResult;
import main.java.bgu.spl.app.ShoeStorageInfo;
import main.java.bgu.spl.app.Store;

public class StoreTest {
	private static Store store;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		store = Store.getInstance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		store.print();                                        //also checks file
	}

	@Before
	public void setUp() throws Exception {
		ShoeStorageInfo[] stock = { new ShoeStorageInfo("red-sandals", 7, 0), new ShoeStorageInfo("green-boots", 9, 0),
				new ShoeStorageInfo("black-sneakers", 5, 0), new ShoeStorageInfo("pink-flip-flops", 8, 0) };
		store.load(stock);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetInstance() {
		assertTrue(store.getInstance() != null);
	}

	@Test
	public void testTake() {
		assertEquals(store.take("yellow-vans", false), BuyResult.NOT_IN_STOCK);
		assertEquals(store.take("green-boots", false), BuyResult.REGULAR_PRICE); //also checks load
	}

	@Test
	public void testAdd() {
		assertEquals(store.take("blue-vans", false), BuyResult.NOT_IN_STOCK);
		store.add("blue-vans", 3);
		assertEquals(store.take("blue-vans", false), BuyResult.REGULAR_PRICE);
	}

	@Test
	public void testAddDiscount() {
		store.add("blue-vans", 3);
		assertEquals(store.take("blue-vans", true), BuyResult.NOT_ON_DISCOUNT);
		store.addDiscount("blue-vans", 4);
		assertEquals(store.take("blue-vans", true), BuyResult.DISCOUNTED_PRICE);
	}

}