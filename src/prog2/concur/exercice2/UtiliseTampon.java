package prog2.concur.exercice2;

/**
 * Classe Producteur
 * 
 * @author Papillon Maxence & Maryne Teissier
 */
class Producteur extends Thread {

	private AbstractFileBloquanteBornee<Integer> buffer;
	private int val = 0;

	public Producteur(AbstractFileBloquanteBornee<Integer> tampon) {
		this.buffer = tampon;
	}

	public void run() {
		while (true) {
			System.out.println("je depose " + val);
			try {
				buffer.deposer(val++);
				Thread.sleep((int) (Math.random() * 100)); // attend jusqu'a 100 ms
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

/**
 * Classe Consommateur
 * 
 * @author Papillon Maxence & Maryne Teissier
 */
class Consommateur extends Thread {

	private AbstractFileBloquanteBornee<Integer> buffer;

	/**
	 * Constructeur
	 * @param buffer nom de la file
	 */
	public Consommateur(AbstractFileBloquanteBornee<Integer> buffer) {
		this.buffer = buffer;
	}

	/**
     * Méthode run qui définit ce que le thread doit éxécuter
     * 
     */
	public void run() {
		while (true) {
			try {
				System.out.println("je preleve " + buffer.prendre());
				Thread.sleep((int) (Math.random() * 200)); // attends jusqu'a 200 ms
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}

/**
 * Classe UtiliseTampon
 * 
 * @author Papillon Maxence & Maryne Teissier
 */
class UtiliseTampon {

	public static void main(String args[]) {

		//BufferCirculaireBasNiveau<Integer> buffer = new BufferCirculaireBasNiveau<>(5);
		BufferCirculaireHautNiveau<Integer> buffer = new BufferCirculaireHautNiveau<>(5);
		Producteur prod = new Producteur(buffer);
		Consommateur cons = new Consommateur(buffer);

		prod.start();
		cons.start();
	}

}
