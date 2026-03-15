package generator;

import java.security.SecureRandom;
import java.util.LinkedList;

public class RandomNumberGenerator extends Thread{

    private final LinkedList<Integer> buffer;
    private int maxCapacity;

    public RandomNumberGenerator(){

        buffer = new LinkedList<>();
        maxCapacity = 10;
    }

    public RandomNumberGenerator(int maxCapacity){

        buffer = new LinkedList<>();
        this.maxCapacity = maxCapacity;
    }

    private synchronized void addNumberToBuffer(int number){

        while(buffer.size() == maxCapacity) {
            try {
                wait();
            }catch(InterruptedException e) {
                System.err.println("Generator thread was interrupted while waiting to add a number.");
            }

        }
        buffer.add(number);
        notifyAll();
    }

    public synchronized int getNumberFromBuffer(){

        while(buffer.isEmpty()){
            try {
                wait();
            }catch(InterruptedException e){
                System.err.println("Handler thread was interrupted while waiting for a number.");
            }
        }
        int number = buffer.removeFirst();
        notifyAll();
        return number;
    }


    @Override
    public void run(){

        SecureRandom r = new SecureRandom();
        while(true) addNumberToBuffer(Math.abs(r.nextInt()));
    }


    public int getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }
}
