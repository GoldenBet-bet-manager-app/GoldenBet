package Master;

import java.io.Serializable;

public class MasterMessage implements Serializable {

    private MasterMessageType type;
    private Object payload;

    public MasterMessage(MasterMessageType type, Object payload) {
        this.type = type;
        this.payload = payload;
    }

    public MasterMessageType getType() {
        return type;
    }

    public void setType(MasterMessageType type) {
        this.type = type;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }
}
