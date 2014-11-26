package prog2.concur.exercice2;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Classe BufferCirculaireHautNiveau
 * 
 * @author Papillon Maxence & Maryne Teissier
 */
public class BufferCirculaireHautNiveau<T> extends AbstractFileBloquanteBornee<T> {
	
	private final Lock lock = new ReentrantLock();
	private final Condition nonPlein  = lock.newCondition(); 
	private final Condition nonVide = lock.newCondition(); 
	
	/**
	 * Constructeur
	 * @param n nombre d'objets acceptés par la file
	 */
	public BufferCirculaireHautNiveau(int n) throws IllegalArgumentException {
		super(n);
	}

	/**
	 * Méthode déposer qui permet de prendre un objet dans la file. 
	 * @param T objet à déposer
	 */
	@Override
	public void deposer(T obj) throws InterruptedException {
		this.lock.lock();
		try {
			int taille = tableau.length;
			
			while (nombre == taille) {
				this.nonPlein.await();
			}
			
			tableau[tete] = obj;
			nombre++;
			tete = (tete + 1) % taille;
			
			this.nonVide.signal();
		} finally {
			this.lock.unlock();
		}
	}

	/**
	 * Méthode déposer qui permet de prendre un objet dans la file. 
	 * @param T objet à prendre
	 */
	@Override
	public T prendre() throws InterruptedException {		
		this.lock.lock();
		try {
			while (nombre == 0) {
				this.nonVide.await();
			}
			
			int taille = tableau.length;
			
			T obj = tableau[queue];
			tableau[queue] = null; // supprime la ref a l'objet
			nombre--;
			queue = (queue + 1) % taille;
			
			this.nonPlein.signal();
			
			return obj;
		} finally {
			this.lock.unlock();
		}
	}

}
