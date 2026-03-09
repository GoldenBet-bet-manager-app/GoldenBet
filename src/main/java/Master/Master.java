package Master;


public class Master{


    private int hashGameToWorker(String gameName,int numbersofWorkers){
        int hash=Math.abs ( gameName.hashCode () );
        return hash % numbersofWorkers;
    }

}
