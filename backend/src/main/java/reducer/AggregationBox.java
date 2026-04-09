package reducer;

import java.math.BigDecimal;
import java.util.HashMap;

public interface AggregationBox {
    int getReceivedResponses();
    void addReceivedResponses();
    Object getGamesFromWorkers();
    void addGamesFromWorker(Object curGameProfits);
}
