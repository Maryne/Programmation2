package prog2.concur.exercice2;

public class Start {

	public static void main(String[] args) {
		FIFOBasNiveau<String> fbn = new FIFOBasNiveau<String>(4);
		
		try {
			fbn.deposer("0");
			fbn.deposer("1");
			fbn.deposer("2");
			fbn.deposer("3");
			fbn.deposer("4");
			//fbn.deposer("5");
			
			System.out.println(fbn.prendre());
			System.out.println(fbn.prendre());
			System.out.println(fbn.prendre());
			System.out.println(fbn.prendre());
			System.out.println(fbn.prendre());
			System.out.println(fbn.prendre());
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
