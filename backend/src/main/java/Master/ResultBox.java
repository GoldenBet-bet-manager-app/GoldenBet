package Master;

import models.MasterMessage;

/**
 *  Wait box for a particular mapId
 */
public class ResultBox {

    private MasterMessage finalMessage = null;


    public MasterMessage getFinalMessage(){
        return finalMessage;
    }

    public void setFinalMessage(MasterMessage finalMessage){
        this.finalMessage = finalMessage;
    }
}
