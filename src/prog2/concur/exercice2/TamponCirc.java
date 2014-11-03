package prog2.concur.exercice2;
class tamponCirc {

    private Object tampon[];
    private int taille;
    private int en, hors, nMess;

    /** constructeur. crée un tampon de taille éléments */
    public tamponCirc (int taille) {
    	
    	
        tampon = new Object[taille];
        this.taille = taille;
        en = 0;
        hors = 0;
        nMess = 0;
    }

    public synchronized void depose(Object obj) {
        while (nMess == taille) {    // si plein
            try {
                wait();                // attends non-plein
            } catch (InterruptedException e) {}
        }
        tampon[en] = obj;
        nMess++;
        en = (en + 1) % taille;
        notify();                // envoie un signal non-vide
    }

    public synchronized Object preleve() {
        while (nMess == 0) {    // si vide
            try {
                wait();            // attends non-vide
            } catch (InterruptedException e) {}
        }
        Object obj = tampon[hors];
        tampon[hors] = null;    // supprime la ref a l'objet
        nMess--;
        hors = (hors + 1) % taille;
        notify();                // envoie un signal non-plein
        return obj;
    }

}