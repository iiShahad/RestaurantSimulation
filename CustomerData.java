
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

//This class is a wrapper class, which stores the data of a customer, including the customer ID, chef ID, waiter ID, events, order, and table index.
class CustomerData {

    private final int customerId;
    private final int chefId;
    private final int waiterId;
    private final HashMap<String, LocalTime> events;
    private final Order order;
    private final int tableIndex;

    public CustomerData(int customerId, int chefId, int waiterId, HashMap<String, LocalTime> events, Order order, int tableIndex) {
        this.customerId = customerId;
        this.chefId = chefId;
        this.waiterId = waiterId;
        this.events = events;
        this.order = order;
        this.tableIndex = tableIndex;
    }

    public int getCustomerId() {
        return customerId;
    }

    public int getChefId() {
        return chefId;
    }

    public int getWaiterId() {
        return waiterId;
    }

    public Order getOrder() {
        return order;
    }

    public HashMap<String, LocalTime> getEvents() {
        return events;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        StringBuilder events = new StringBuilder();

        if (this.events.containsKey("Arrival")) {
            events.append(String.format("[%s] Customer %d arrives.%n",
                    this.events.get("Arrival").format(formatter), this.customerId));
        }
        if (this.events.containsKey("Seated")) {
            events.append(String.format("[%s] Customer %d is seated at Table %d%n",
                    this.events.get("Seated").format(formatter), this.customerId, this.tableIndex));
        }
        if (this.events.containsKey("Order")) {
            events.append(String.format("[%s] Customer %d places an order: %s%n",
                    this.events.get("Order").format(formatter), this.customerId, this.order.getMealName()));
        }
        if (this.events.containsKey("ChefStart")) {
            events.append(String.format("[%s] Chef %d starts preparing %s for Customer %d%n",
                    this.events.get("ChefStart").format(formatter), this.chefId,
                    this.order.getMealName(), this.customerId));
        }
        if (this.events.containsKey("ChefFinish")) {
            events.append(String.format("[%s] Chef %d finishes preparing %s for Customer %d%n",
                    this.events.get("ChefFinish").format(formatter), this.chefId,
                    this.order.getMealName(), this.customerId));
        }
        if (this.events.containsKey("Serve")) {
            events.append(String.format("[%s] Waiter %d serves %s to Customer %d at Table %d%n",
                    this.events.get("Serve").format(formatter), this.waiterId,
                    this.order.getMealName(), this.customerId, this.tableIndex));
        }
        if (this.events.containsKey("Leave")) {
            events.append(String.format("[%s] Customer %d finishes eating and leaves the restaurant.%n",
                    this.events.get("Leave").format(formatter), this.customerId));
            events.append(String.format("[%s] Table %d is now available.%n",
                    this.events.get("Leave").format(formatter), this.tableIndex));
        }

        events.append("\n");

        return events.toString();
    }

}
