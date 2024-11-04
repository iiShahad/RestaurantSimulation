
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

class RestaurantSimulation {

    //Global variables
    static int numChefs;
    static int numWaiters;
    static int numTables;
    static HashMap<String, Integer> meals = new HashMap<>();
    //PriorityQueue to store customers in arrival time order
    static PriorityQueue<Customer> customerArrivalQueue = new PriorityQueue<>();
    //chefs array
    static Chef[] chefs;

    static CircularBuffer<String> orderBuffer;
    static CircularBuffer<Customer> tableBuffer;

    static List<Thread> customerThreads = new ArrayList<>();
    static List<Thread> chefThreads = new ArrayList<>();

    public static void main(String[] args) throws FileNotFoundException {
        //input files
        String inputFile1 = "restaurant_simulation_input1.txt";
        String inputFile2 = "restaurant_simulation_input2.txt";
        String inputFile3 = "restaurant_simulation_input3.txt";

        try {
        FileReader fr = new FileReader(inputFile1);
        BufferedReader br = new BufferedReader(fr);

        readConfig(br);

        orderBuffer = new CircularBuffer<>(numTables);
        tableBuffer = new CircularBuffer<>(numTables);

        readCustomers(br);
            
        } catch (Exception e) {
        }

        chefs = new Chef[numChefs];
        arrivalTimeToDelay();

        for (Customer customer : customerArrivalQueue) {
            Thread thread = new Thread(customer);
            customerThreads.add(thread);
            thread.start();

        }

        for (int i = 0; i < numChefs; i++) {
            chefs[i] = new Chef(i, orderBuffer, meals);
            chefThreads.add(chefs[i]);
            chefs[i].start();
        }

        // try {
        //     for (Thread thread : customerThreads) {
        //         System.out.println("join");
        //         thread.join();
        //     }
        //     for (Chef chef : chefs) {
        //         System.out.println("endShift");
        //         chef.endShift();
        //     }
        //     for (Thread thread : chefThreads) {
        //         thread.join();
        //     }
        // } catch (Exception e) {
        //     System.err.println("Exception in main method: " + e.getMessage());
        // }
        System.out.println("Waiting for all threads to finish...");
        // Wait for all threads to finish with timeout
        try {
            System.out.println("Total customer threads to wait for: " + customerThreads.size());

            for (int i = 1; i <= customerThreads.size(); i++) {
                Thread thread = customerThreads.get(i);
                System.out.println("Waiting for customer thread " + i + " to finish...");
                // Add timeout of 10 seconds for each thread
                thread.join(10000);
                if (thread.isAlive()) {
                    System.out.println("WARNING: Customer thread " + i + " did not finish within timeout!");
                    // Optional: dump thread state for debugging
                    System.out.println("Thread state: " + thread.getState());
                } else {
                    System.out.println("Customer thread " + i + " has finished.");
                }
            }

            System.out.println("Checking if chefs array is populated...");
            if (chefs == null) {
                System.out.println("Error: Chefs array is null.");
            } else {
                System.out.println("Chefs array length: " + chefs.length);
                // End shifts for all chefs
                for (int i = 0; i < chefs.length; i++) {
                    System.out.println("Ending shift for chef " + i);
                    chefs[i].endShift();
                }

                // Wait for chef threads with timeout
                for (int i = 0; i < chefThreads.size(); i++) {
                    Thread thread = chefThreads.get(i);
                    System.out.println("Waiting for chef thread " + i + " to finish...");
                    thread.join(10000);
                    if (thread.isAlive()) {
                        System.out.println("WARNING: Chef thread " + i + " did not finish within timeout!");
                        System.out.println("Thread state: " + thread.getState());
                    } else {
                        System.out.println("Chef thread " + i + " has finished.");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Exception in main method (Wait for all threads to finish): " + e.getMessage());
            e.printStackTrace();
        }

    }

    public static void arrivalTimeToDelay() {
        //Calculate the delay time for each customer
        LocalTime starTime = customerArrivalQueue.peek().getArrivalTime();
        for (Customer customer : customerArrivalQueue) {
            LocalTime arrivalTime = customer.getArrivalTime();
            int delay = (int) (arrivalTime.getMinute() - starTime.getMinute());
            customer.setDelay(delay);
        }
    }


    public static void readConfig(BufferedReader br) throws Exception {
        try {
            String firstLine = br.readLine();
            String[] firstLineValues = firstLine.split(" ");
            for (String value1 : firstLineValues) {
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
            String secondLine = br.readLine();
            String[] secondLineValues = secondLine.split(" ");
            for (String value : secondLineValues) {
                String[] meal = value.split("=");
                String mealName = meal[0];
                int mealTime = Integer.parseInt(meal[1].split(":")[1]);
                meals.put(mealName, mealTime);
            }
        } catch (Exception e) {
            throw e;
        }
    }


     public static void readCustomers(BufferedReader br) throws Exception{
        try {
            String line;
            while ((line = br.readLine()) != null) {
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
                Customer customer = new Customer(customerId, arrivalTime, order, orderBuffer, tableBuffer);
                customerArrivalQueue.add(customer);
            }
        } catch (Exception e) {
            throw e;
        }
     }

}
