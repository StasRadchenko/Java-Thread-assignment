package main.java.bgu.spl.app;

/**
 * the info about a shoe in store
 *
 */
public class ShoeStorageInfo {
   private String shoeType;
   private int amountOnStorage;
   private int discountedAmount;
   
   /**
 * @param String shoeType- the type of the shoe
 * @param int amountOnStorage- the current amount on storage
 * @param int discountedAmount- the amount of shoes on discount
 */
public ShoeStorageInfo(String type,int amount,int discount){
	   shoeType=type;
	   amountOnStorage=amount;
	   discountedAmount=discount;
   }
   /**
 * @param int amountOnStorage
 * add amount of shoes on storage
 */
public void addAmount(int amount){
	   amountOnStorage=amountOnStorage+amount;
   }
   /**
 * @return int amountOnStorage
 */
public int getAmount(){
	   return amountOnStorage;
   }
   
   /**
 * @param int discountedAmount- the amount of shoes on discount
 * we add shoes on discount to discountedAmount
 */
public void addDiscount(int discount){
	   discountedAmount=discountedAmount+discount;
	   if(discountedAmount>amountOnStorage){
			  discountedAmount=amountOnStorage;
		   }
   }
   /**
 * @return int getDiscountAmount
 */
public int getDiscountAmount(){
	   return discountedAmount;
   }
   
   /**
 * @return String shoeType
 */
public String getType(){
	   return shoeType;
   }
   
   /**
 * @return Boolean discountedAmount
 */
public boolean isOnDiscount(){
	   return discountedAmount!=0;
   }
   /**
 * buy a shoe and update discountedAmount,amountOnStorage
 */
public void shoeBuy(){
	   if(discountedAmount!=0){
		   discountedAmount--;
		   amountOnStorage--;
	   }
	   else{
	   amountOnStorage--;
	   }
   }
   /**
 * prints all the info on this shoe
 */
public void printShoe(){
	   System.out.println();
	   System.out.println("shoe type: "+shoeType);
	   System.out.println("amount of this shoe in storage: "+amountOnStorage);
	   System.out.println("discounted amount of this shoe in store: "+discountedAmount);
	   System.out.println();
   }
   
       
}
