package prog2.concur.exercice2;

import java.util.ArrayList;

/**
 * Une file de communication bornee bloquante.
 *
 * Les threads peuvent déposer (resp. prendre) des objets dans une telle file.
 * Cette opérations peut être bloquante si la file est pleine (resp. vide).
 */
public abstract class AbstractFileBloquanteBornee<E>
{

    E[] tableau;
    int tete;
    int queue;
    boolean estVide;
    boolean estPleine;

    /**
     * Créer une file de capacité maximale n.
     *
     * param n - la capacité maximale de la file.
     * n devrait être supérieur ou égal à 1.
     */
    @SuppressWarnings({"unchecked"})
    public AbstractFileBloquanteBornee (int n) throws IllegalArgumentException 
    {
        if(n<1) 
            throw new 
                IllegalArgumentException(
                        "AbstractFileBloquanteBornee : la capacité de la file doit être > 0");
        else {
            /* Création d'un tableau pseudo-générique.
             * Fonctionne tant que le tableau reste "interne" à la classe.
             * Cf. http://stackoverflow.com/questions/529085/how-to-generic-array-creation
             */
            tableau = (E[]) new Object[n];
            tete = queue = 0;
            estVide = true;
            estPleine = false;
        }
    }

    /**
     * Déposer une référence dans la file.
     *
     * Le dépôt est fait en fin de file.
     * L'objet référencé n'est pas copié au moment du dépôt.
     * Le dépôt est bloquant lorsque la file est pleine
     *
     * param e - l'élément à ajouter à la file
     */
     abstract public void deposer (E e) throws InterruptedException;

    /**
     * Prendre une référence dans la file.
     *
     * La prise est faite en tête de file.
     * L'objet référencé n'est pas copié au moment du dépôt.
     * La prise est bloquante lorsque la file est vide.
     *
     * returns la référence de tête de la file
     */
    abstract public E prendre () throws InterruptedException;

}
