package prog2.concur.exercice2;

public class FIFOBasNiveau<T> extends AbstractFileBloquanteBornee<T> {

	public FIFOBasNiveau(int n) throws IllegalArgumentException {
		super(n);
	}

	public synchronized void deposer(T obj) {
		int taille = tableau.length;

		while (nombre == taille) { // si plein
			try {
				wait(); // attends non-plein
			} catch (InterruptedException e) {

			}
		}
		tableau[tete] = obj;
		nombre++;
		tete = (tete + 1) % taille;
		notify(); // envoie un signal non-vide
	}

	public synchronized T prendre() {
		int taille = tableau.length;
		while (nombre == 0) { // si vide
			try {
				wait(); // attends non-vide
			} catch (InterruptedException e) {
			}
		}
		T obj = tableau[queue];
		tableau[queue] = null; // supprime la ref a l'objet
		nombre--;
		queue = (queue + 1) % taille;
		notify(); // envoie un signal non-plein
		return obj;
	}

}
