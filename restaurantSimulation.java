import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.PriorityQueue;

class RestaurantSimulation {

    //Global variables
    static int numChefs;
    static int numWaiters;
    static int numTables;
    static HashMap<String, Integer> meals = new HashMap<>();
    //PriorityQueue to store customers in arrival time order
    static PriorityQueue<Customer> customerArrivalQueue = new PriorityQueue<>();

    static CircularBuffer<String> orderBuffer = new CircularBuffer<>(5);

    public static void main(String[] args) {
        //input files
        String inputFile1 = "restaurant_simulation_input1.txt";
        String inputFile2 = "restaurant_simulation_input2.txt";
        String inputFile3 = "restaurant_simulation_input3.txt";

        readInputFile(inputFile1);
        arrivalTimeToDelay();

        for (Customer customer : customerArrivalQueue) {
            new Thread(customer).start();
        }


    }

    public static void arrivalTimeToDelay() {
        //Calculate the delay time for each customer
        LocalTime starTime = customerArrivalQueue.peek().getArrivalTime();
        for (Customer customer : customerArrivalQueue) {
            LocalTime arrivalTime = customer.getArrivalTime();
            int delay = (int) (arrivalTime.getHour()- starTime.getMinute());
            customer.setDelay(delay);
        }
    }

    public static void readInputFile(String inputFile) {
        try {
            FileReader fr = new FileReader(inputFile);
            try (BufferedReader br = new BufferedReader(fr)) {
                String line;
                int currentLine = 1;
                while ((line = br.readLine()) != null) {
                    //Reading the first line: NC NW NT
                    switch (currentLine) {
                        case 1 -> {
                            String[] values = line.split(" ");
                            for (String value1 : values) {
                                String[] value = value1.split("=");
                                switch (value[0]) {
                                    case "NC" ->
                                        numChefs = Integer.parseInt(value[1]);
                                    case "NW" ->
                                        numWaiters = Integer.parseInt(value[1]);
                                    case "NT" ->
                                        numTables = Integer.parseInt(value[1]);
                                }
                            }
                        }
                        case 2 -> {
                            String[] values = line.split(" ");
                            for (String value : values) {
                                String[] meal = value.split("=");
                                String mealName = meal[0];
                                int mealTime = Integer.parseInt(meal[1].split(":")[1]);
                                meals.put(mealName, mealTime);
                            }
                        }
                        default -> {
                            String[] values = line.split(" ");
                            //Customer ID, Arrival Time, Order
                            Integer customerId = null;
                            LocalTime arrivalTime = null;
                            String order = null;
                            for (String value : values) {
                                String[] data = value.split("=");
                                switch (data[0]) {
                                    case "CustomerID" ->
                                        customerId = Integer.valueOf(data[1]);
                                    case "ArrivalTime" ->
                                        arrivalTime = LocalTime.of(Integer.parseInt(data[1].split(":")[0]), Integer.parseInt(data[1].split(":")[1]));
                                    case "Order" ->
                                        order = data[1];
                                }
                            }
                            if (customerId == null || arrivalTime == null || order == null) {
                                throw new Exception("Invalid data");
                            }
                            Customer customer = new Customer(customerId, arrivalTime, order, orderBuffer);
                            customerArrivalQueue.add(customer);
                        }
                    }
                    currentLine++;
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
}
