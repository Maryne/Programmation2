package prog2.concur.exercice2;

class Producteur extends Thread {

	private BufferCirculaireBasNiveau<Integer> buffer;
	private int val = 0;

	public Producteur(BufferCirculaireBasNiveau<Integer> tampon) {
		this.buffer = tampon;
	}

	public void run() {
		while (true) {
			System.out.println("je depose " + val);
			buffer.deposer(val++);
			try {
				Thread.sleep((int) (Math.random() * 100)); // attend jusqu'a 100 ms
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

class Consommateur extends Thread {

	private BufferCirculaireBasNiveau<Integer> buffer;

	public Consommateur(BufferCirculaireBasNiveau<Integer> buffer) {
		this.buffer = buffer;
	}

	public void run() {
		while (true) {
			System.out.println("je preleve " + buffer.prendre());
			try {
				Thread.sleep((int) (Math.random() * 200)); // attends jusqu'a 200 ms
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

class UtiliseTampon {

	public static void main(String args[]) {

		BufferCirculaireBasNiveau<Integer> buffer = new BufferCirculaireBasNiveau<Integer>(5);
		Producteur prod = new Producteur(buffer);
		Consommateur cons = new Consommateur(buffer);

		prod.start();
		cons.start();
	}

}
