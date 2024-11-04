
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

class RestaurantSimulation {

    //Global variables --------------------------------------------------------------
    static int numChefs;
    static int numWaiters;
    static int numTables;
    static HashMap<String, Integer> meals = new HashMap<>();

    static PriorityQueue<Customer> customerArrivalQueue = new PriorityQueue<>(); //PriorityQueue to store customers in arrival time order

    static Chef[] chefs;

    static CircularBuffer<Order> orderBuffer; //CircularBuffer to store orders
    static CircularBuffer<Customer> tableBuffer; //CircularBuffer to store customers in tables

    static List<Thread> customerThreads = new ArrayList<>(); //List to store customer threads
    static List<Thread> chefThreads = new ArrayList<>(); //List to store chef threads

    public static void main(String[] args) throws FileNotFoundException {
        //input files --------------------------------------------------------------
        String inputFile1 = "restaurant_simulation_input1.txt";
        String inputFile2 = "restaurant_simulation_input2.txt";
        String inputFile3 = "restaurant_simulation_input3.txt";

        //Read input files --------------------------------------------------------------
        /*
        What we need to do:
        1. Read the configuration from the first line of the input file
        2. Read the meals from the second line of the input file
        3. Read the customers from the rest of the lines of the input file
        4. Create and initialize the buffers and arrays
        */
        try {
            FileReader fr = new FileReader(inputFile1);
            BufferedReader br = new BufferedReader(fr);

            readConfig(br);

            orderBuffer = new CircularBuffer<>(numTables);
            tableBuffer = new CircularBuffer<>(numTables);
            chefs = new Chef[numChefs];

            readCustomers(br);

        } catch (Exception e) {
            System.err.println("Exception in main method (Reading input files): " + e.getMessage());
        }

        //Start simulation --------------------------------------------------------------
        /*
        What we need to do:
        1. Calculate the delay time for each customer
        2. Start the customer threads
        3. Start the chef threads
        */
        try{
            arrivalTimeToDelay();

            for (Customer customer : customerArrivalQueue) {
                Thread thread = new Thread(customer);
                customerThreads.add(thread);
                thread.start();

            }

            for (int i = 0; i < numChefs; i++) {
                chefs[i] = new Chef(i, orderBuffer, tableBuffer);
                chefThreads.add(chefs[i]);
                chefs[i].start();
            }
        }catch (Exception e){
            System.err.println("Exception in main method (Simulation): " + e.getMessage());
        }

        //Wait for threads --------------------------------------------------------------
        /*
        What we need to do:
        1. Wait for all customer threads to finish
        2. End the shifts for all chefs
        3. Wait for all chef threads to finish
        */
        try {
            for (int i = 0; i < customerThreads.size(); i++) {
                Thread thread = customerThreads.get(i);
                thread.join(10000); // Add timeout of 10 seconds for each thread
                if (thread.isAlive()) {
                    System.out.println("WARNING: Customer thread " + (i + 1) + " did not finish within timeout, Thread state: " + thread.getState());
                }
            }

            if (chefs == null) {
                System.out.println("Error: Chefs array is null.");
            } else {
                // End shifts for all chefs
                for (Chef chef : chefs) {
                    chef.endShift();
                }
                // Wait for chef threads with timeout
                for (int i = 0; i < chefThreads.size(); i++) {
                    Thread thread = chefThreads.get(i);
                    thread.join(10000); // Add timeout of 10 seconds for each thread
                    if (thread.isAlive()) {
                        System.out.println("WARNING: Chef thread " + (i + 1) + " did not finish within timeout, Thread state: " + thread.getState());
                    } 
                }
            }
        } catch (Exception e) {
            System.err.println("Exception in main method (Waiting for threads): " + e.getMessage());
        }

    }

    //Helper methods --------------------------------------------------------------

    //Calculate the delay time for each customer
    /*
    Based on the arrival time of the first customer, calculate the delay time for each customer,
    by subtracting the arrival time of the first customer from the arrival time of each customer.
    This will be used to simulate the arrival time of each customer by setting a delay time at the beginning for each customer.
    */
    public static void arrivalTimeToDelay() {
        LocalTime starTime = customerArrivalQueue.peek().getArrivalTime();
        for (Customer customer : customerArrivalQueue) {
            LocalTime arrivalTime = customer.getArrivalTime();
            int delay = (int) (arrivalTime.getMinute() - starTime.getMinute());
            customer.setDelay(delay);
        }
    }

    //Read the configuration and meals from the input file
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

    //Read the customers from the input file
    public static void readCustomers(BufferedReader br) throws Exception {
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
                Customer customer = new Customer(customerId, arrivalTime, order, orderBuffer, tableBuffer, meals);
                customerArrivalQueue.add(customer);
            }
        } catch (Exception e) {
            throw e;
        }
    }

}
