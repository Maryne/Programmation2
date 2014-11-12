class producteur extends Thread {

	private FIFOBasNiveau buffer;
	private int val = 0;

	public producteur(FIFOBasNiveau tampon) {
		this.buffer = tampon;
	}

	public void run() {
		while (true) {
			System.out.println("je depose " + val);
			buffer.deposer(val++);
			try {
				Thread.sleep((int) (Math.random() * 100)); // attend jusqu'a 100
															// ms
			} catch (InterruptedException e) {
			}
		}
	}
}

class consommateur extends Thread {

	private FIFOBasNiveau buffer;

	public consommateur(FIFOBasNiveau buffer) {
		this.buffer = buffer;
	}

	public void run() {
		while (true) {
			System.out.println("je preleve " + buffer.prendre());
			try {
				Thread.sleep((int) (Math.random() * 200)); // attends jusqu'a
															// 200 ms
			} catch (InterruptedException e) {
			}
		}
	}
}

class utiliseTampon {

	public static void main(String args[]) {

		FIFOBasNiveau<String> buffer = new FIFOBasNiveau<String>(5);
		producteur prod = new producteur(buffer);
		consommateur cons = new consommateur(buffer);

		prod.start();
		cons.start();
	}

}
