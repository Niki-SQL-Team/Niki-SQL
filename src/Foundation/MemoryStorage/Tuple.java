package Foundation.MemoryStorage;

import java.util.Vector;

public class Tuple {

    public Vector<String> dataItems;

    public Tuple(Vector<String> dataItems) {
        this.dataItems = dataItems;
    }

    public String get(Integer index) {
        return this.dataItems.get(index);
    }

    public Integer size() {
        return this.dataItems.size();
    }

}
