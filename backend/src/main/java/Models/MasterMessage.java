package Models;

import java.io.Serializable;

public class MasterMessage implements Serializable {

    private final MasterMessageType type;
    private Object payload;
    private int mapId = -1;

    public MasterMessage(MasterMessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
        this.mapId = -1;
    }

    public MasterMessage(Object payload, int mapId) {
        this.type = null;
        this.payload = payload;
        this.mapId = mapId;
    }

    public MasterMessageType getType(){
        return type;
    }

    public Object getPayload(){
        return payload;
    }

    public void setPayload(Object payload){
        this.payload = payload;
    }

    public int getMapId(){
        return mapId;
    }

    public void setMapId(int mapId){
        this.mapId = mapId;
    }

}