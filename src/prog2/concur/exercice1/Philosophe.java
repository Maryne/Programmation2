package prog2.concur.exercice1;

class Fourchette
{
    private boolean prise = false;

    final synchronized void prendre() 
    {
        try 
        {
            while (prise) 
            {
                wait();
            }
        } 
        catch(InterruptedException e) 
        {
            e.printStackTrace();
            System.exit(-1);
        }
        prise = true;
    }

    final synchronized void relacher() 
    {
        prise = false;
        notifyAll();
    }
}

public class Philosophe implements Runnable
{
    private String nom;
    private Fourchette fGauche, fDroite;

    public Philosophe(String n, Fourchette g, Fourchette d)
    {
        nom = n;
        fGauche = g;
        fDroite = d;
    }

    public void run()
    {
        while(true)
        {
            penser();
            fGauche.prendre();
            fDroite.prendre();
            manger();
            fDroite.relacher();
            fGauche.relacher();
        }
    }

    final void manger() 
    {
        System.out.println(nom + " mange.");
    }

    final void penser() 
    {
        System.out.println(nom + " pense.");
    }

    public static void main(String args[])
    {
        final String[] noms = {"Platon", "Socrate", "Aristote", "Diogène", "Sénèque"};
        final Fourchette[] fourchettes = {new Fourchette(), new Fourchette(), new Fourchette(), new Fourchette(), new Fourchette()}; 
        Philosophe[] table;

        table = new Philosophe[5];
        for(char cpt = 0; cpt < table.length; cpt ++)
        {
            table[cpt] = new Philosophe(noms[cpt], fourchettes[cpt], fourchettes[(cpt + 1) % table.length]);
            new Thread(table[cpt]).start();
        }
    }
}

