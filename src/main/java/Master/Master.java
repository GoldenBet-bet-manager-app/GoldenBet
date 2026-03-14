package Master;


import Worker.Worker;

public class Master{

    private int hashGameToWorker(String gameName,int numbersofWorkers){
        int hash=Math.abs ( gameName.hashCode () );
        return hash % numbersofWorkers;
    }

    public static void main(String[] args){
        int numworkers=Integer.parseInt ( args[ 0 ] );
        
    }
}
