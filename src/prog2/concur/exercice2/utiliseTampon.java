package prog2.concur.exercice2;

class producteur extends Thread {

    private tamponCirc tampon;
    private int val = 0;
    
    public producteur(tamponCirc tampon) {
        this.tampon = tampon;
    }

    public void run() {
        while (true) {
            System.out.println("je depose "+val);
            tampon.depose(new Integer(val++));
            try {
                Thread.sleep((int)(Math.random()*100));    // attend jusqu'a 100 ms
            } catch (InterruptedException e) {}
        }
    }
}

class consommateur extends Thread {

    private tamponCirc tampon;
    
    public consommateur(tamponCirc tampon) {
        this.tampon = tampon;
    }

    public void run() {
        while (true) {
            System.out.println("je preleve "+((Integer)tampon.preleve()).toString());
            try {
                Thread.sleep((int)(Math.random()*200));    // attends jusqu'a 200 ms
            } catch (InterruptedException e) {}
        }
    }
}

class utiliseTampon {

    public static void main(String args[]) {
        System.out.print("tto");
        tamponCirc tampon = new tamponCirc(5);
        producteur prod = new producteur(tampon);
        consommateur cons = new consommateur(tampon);
    
        prod.start();
        cons.start();
        try {
            Thread.sleep(30000);    // s'execute pendant 30 secondes
        } catch (InterruptedException e) {}
    }

}