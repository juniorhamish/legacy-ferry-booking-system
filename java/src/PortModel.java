import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PortModel {

    public int id;
    public String name;

    final Map<Integer, Long> boatAvailability = new HashMap<>();
    final List<Ferry> boats = new ArrayList<>();

    public PortModel(Port port) {
        id = port.id;
        name = port.name;
    }

    public void addBoat(long available, Ferry boat) {
        if (boat != null) {
            boats.add(boat);
            boatAvailability.put(boat.id, available);
        }
    }

    public Ferry getNextAvailable(long time) {
        Ferry result = null;
        for (Map.Entry<Integer, Long> entry : boatAvailability.entrySet()) {
            if (time >= entry.getValue()) {
                boatAvailability.remove(entry.getKey());
                for (Ferry boat : boats) {
                    if (boat.id == entry.getKey()) {
                        boats.remove(boat);
                        result = boat;
                        break;
                    }
                }
                break;
            }
        }
        return result;
    }
}
