package Master;

import models.MasterMessage;

/**
 *  Κουτί αναμονής για ενα συγκεκριμένο mapId
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
