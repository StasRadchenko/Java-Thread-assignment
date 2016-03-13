package main.java.bgu.spl.app;

/**
 * the answer about the purchase that was executed (could be for the client or the store)
 *
 */
public class Receipt {
    private String seller;
	private String customer;
	private String shoeType;
	private boolean discount;
	private int issuedTick;
	private int requestTick;
	private int amountSold;
	
	/**
	 * @param String seller- the name of the seller of this buy
	 * @param String customer-the name of buyer
	 * @param String shoeType- the type of the shoe to buy
	 * @param Boolean discount- true if the buyer want to buy only on discount
	 * @param int issuedTick- the time when the reciepet was started to be created
	 * @param int requestTick- the time when the request was send
	 * @param int amountSold- the amount that was sold (1 if to the client)
	 */
	public Receipt(String sellerA,String customerA,String shoeTypeA,boolean discountA,int issuedTickA,int requestTickA,int amountSoldA){
		seller=sellerA;
		customer=customerA;
		shoeType=shoeTypeA;
		discount=discountA;
		issuedTick=issuedTickA;
		requestTick=requestTickA;
		amountSold=amountSoldA;
	}
	/**
	 * @return String customer
	 */
	public String getName(){
		return customer;
	}
	
	/**
	 * prints all the info from the reciept
	 */
	public void printReceipt(){
		System.out.println();
		System.out.println( "sellers name: "+seller);
		System.out.println("customers name: "+customer);
		System.out.println("shoe type bought: "+shoeType);
		System.out.println("if the shoe was on discount: "+discount);
		System.out.println("receipt was issued in: "+issuedTick);
		System.out.println("customer requested to buy the shoe in: "+requestTick);
		System.out.println("amount sold to customer: "+amountSold);
		System.out.println();
	}
	
	public String getSeller(){
		return seller;
	}
}
