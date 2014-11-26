package prog2.concur.exercice1;

/**
 * Classe fourchette
 * 
 * @author Papillon Maxence & Maryne Teissier
 */
class Fourchette {
	private boolean prise = false;

	final synchronized void prendre() {
		try {
			while (prise) {
				wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		prise = true;
	}

	final synchronized void relacher() {
		prise = false;
		notifyAll();
	}

	public boolean estPrise() {
		return prise;
	}
}

/**
 * Classe Philosophe
 * 
 * @return un philosophe
 * @author Papillon Maxence & Maryne Teissier
 */
public class Philosophe implements Runnable {
	private String nom;
	private Fourchette fGauche, fDroite;

	/**
     * Constructeur de la classe philosophe
     * 
     * @param n nom
     * @param g fourchette gauche
     * @param d fourchette droite
     */
	
	public Philosophe(String n, Fourchette g, Fourchette d) {
		nom = n;
		fGauche = g;
		fDroite = d;
	}

	/**
     * Méthode run qui définit ce que le thread doit éxécuter
     * 
     */
	public void run() {
		while (true) {
			penser();
			if (!fGauche.estPrise() && !fDroite.estPrise()) {
				fGauche.prendre();
				fDroite.prendre();

				manger();
				fDroite.relacher();
				fGauche.relacher();
			}
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
     * Méthode manger qui fait manger un philosophe
     * 
     */
	final void manger() {
		System.out.println(nom + " mange.");
	}
	
	/**
     * Méthode penser qui fait penser le philosophe
     * 
     */
	final void penser() {
		System.out.println(nom + " pense.");
	}

	public static void main(String args[]) {
		final String[] noms = { "Platon  ", "Socrate ", "Aristote", "Diogène ", "Sénèque " };
		final Fourchette[] fourchettes = { new Fourchette(), new Fourchette(),
				new Fourchette(), new Fourchette(), new Fourchette() };
		Philosophe[] table;

		table = new Philosophe[5];
		for (char cpt = 0; cpt < table.length; cpt++) {
			table[cpt] = new Philosophe(noms[cpt], fourchettes[cpt],
					fourchettes[(cpt + 1) % table.length]);
			new Thread(table[cpt]).start();
		}
	}
}
