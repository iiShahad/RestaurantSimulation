
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

class RestaurantSimulation {

    static int numChefs;
    static int numWaiters;
    static int numTables;

    static PriorityQueue<Customer> customerArrivalQueue = new PriorityQueue<>();
    static Map<Integer, CustomerData> customerServingData = new HashMap<>();
    static HashMap<String, Integer> meals = new HashMap<>();

    static Chef[] chefs;
    static Waiter[] waiters;

    static CircularBuffer<Order> orderBuffer;
    static CircularBuffer<Order> readyOrders;
    static BoundedQueue<Customer> tableQueue;

    static List<Thread> customerThreads = new ArrayList<>();
    static List<Thread> chefThreads = new ArrayList<>();
    static List<Thread> waiterThreads = new ArrayList<>();

    public static void main(String[] args) throws FileNotFoundException {
        String[] inputFiles = {"restaurant_simulation_input1.txt", "restaurant_simulation_input2.txt", "restaurant_simulation_input3.txt"};

        for (int i = 0; i < inputFiles.length; i++) {
            System.out.println("\n[Simulation " + (i + 1) + "]\n");
            //Read input files 
            readAndInitializeData(inputFiles[i]);

            //Start simulation
            startThreads();

            //Wait for threads
            waitForThreads();

            //Write the customer serving data to the output file
            writeToOutputFile("restaurant_simulation_output%d.txt".formatted(i + 1));

            //Reset everything
            reset();
        }

    }

    public static void readAndInitializeData(String inputFile) {
        try {
            FileReader fr = new FileReader(inputFile);
            BufferedReader br = new BufferedReader(fr);

            readConfig(br);

            orderBuffer = new CircularBuffer<>(numTables);
            readyOrders = new CircularBuffer<>(numTables);
            tableQueue = new BoundedQueue<>(numTables);
            chefs = new Chef[numChefs];
            waiters = new Waiter[numWaiters];

            readCustomers(br);

        } catch (Exception e) {
            System.err.println("Exception in main method (Reading input files): " + e.getMessage());
        }
    }

    public static void startThreads() {
        try {
            arrivalTimeToDelay();

            for (Customer customer : customerArrivalQueue) {
                Thread thread = new Thread(customer);
                customerThreads.add(thread);
                thread.start();

            }

            for (int i = 0; i < numChefs; i++) {
                chefs[i] = new Chef(i + 1, orderBuffer, readyOrders);
                chefThreads.add(chefs[i]);
                chefs[i].start();
            }

            for (int i = 0; i < numWaiters; i++) {
                waiters[i] = new Waiter(i + 1, readyOrders);
                waiterThreads.add(waiters[i]);
                waiters[i].start();
            }
        } catch (Exception e) {
            System.err.println("Exception in main method (Simulation): " + e.getMessage());
        }
    }

    public static void waitForThreads() {
        try {
            for (int i = 0; i < customerThreads.size(); i++) {
                Thread thread = customerThreads.get(i);
                thread.join(TimeUnit.MINUTES.toMillis(5)); // Add timeout of 5 minutes for each thread
                if (thread.isAlive()) {
                    System.out.println("WARNING: Customer thread " + (i + 1) + " did not finish within timeout, Thread state: " + thread.getState());
                    printThreadInfo();
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
                    thread.join(TimeUnit.MINUTES.toMillis(5)); // Add timeout of 5 minutes for each thread
                    if (thread.isAlive()) {
                        System.out.println("WARNING: Chef thread " + (i + 1) + " did not finish within timeout, Thread state: " + thread.getState());
                        printThreadInfo();
                    }
                }
            }

            if (waiters == null) {
                System.out.println("Error: Waiters array is null.");
            } else {
                // End shifts for all waiters
                for (Waiter waiter : waiters) {
                    waiter.endShift();
                }
                // Wait for waiters threads with timeout
                for (int i = 0; i < waiterThreads.size(); i++) {
                    Thread thread = waiterThreads.get(i);
                    thread.join(TimeUnit.MINUTES.toMillis(5)); // Add timeout of 10 minutes for each thread
                    if (thread.isAlive()) {
                        System.out.println("WARNING: Waiter thread " + (i + 1) + " did not finish within timeout, Thread state: " + thread.getState());
                        printThreadInfo();
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Exception in main method (Waiting for threads): " + e.getMessage());
        }
    }

    public static void reset() {
        // Clear all thread lists
        customerThreads.clear();
        chefThreads.clear();
        waiterThreads.clear();

        // Clear customer arrival queue
        customerArrivalQueue.clear();

        // Reinitialize buffers and arrays
        orderBuffer = new CircularBuffer<>(numTables);
        readyOrders = new CircularBuffer<>(numTables);
        tableQueue = new BoundedQueue<>(numTables);
        chefs = new Chef[numChefs];
        waiters = new Waiter[numWaiters];

        //reset semaphores for chefs and waiters
        Waiter.resetSemaphore();
        Chef.resetSemaphore();
    }

    public static void writeToOutputFile(String outputFile) {
        try {
            FileWriter writer = new FileWriter(outputFile);
            for (CustomerData data : customerServingData.values()) {
                writer.write(data.toString());
            }

            //Write the summary of the simulation
            writer.write(summaryOfSimulation());
            System.out.println(summaryOfSimulation());

            writer.close();
        } catch (IOException e) {
            System.err.println("Error writing to output file: " + e.getMessage());
        }
    }

    public static String summaryOfSimulation() {
        double totalWaitTime = 0;
        double totalOrderPreparationTime = 0;

        for (CustomerData data : customerServingData.values()) {
            totalWaitTime += Duration.between(data.getEvents().get("Arrival"), data.getEvents().get("Seated")).toMinutes();
            totalOrderPreparationTime += Duration.between(data.getEvents().get("Order"), data.getEvents().get("ChefFinish")).toMinutes();
        }

        int totalCustomersServed = customerServingData.size();
        int averageWaitTime = (int) (totalWaitTime / totalCustomersServed);
        int averageOrderPreparationTime = (int) (totalOrderPreparationTime / totalCustomersServed);
        LocalTime firstArrival = customerServingData.get(0).getEvents().get("Arrival");
        LocalTime lastLeave = customerServingData.get(customerServingData.size() - 1).getEvents().get("Leave");
        long totalSimulationTime = Duration.between(firstArrival, lastLeave).toMinutes();

        StringBuilder summary = new StringBuilder();
        summary.append("\n[End of Simulation]\n")
                .append("\nSummary:\n")
                .append("- Total Customers Served: ").append(totalCustomersServed).append("\n")
                .append("- Average Wait Time for Table: ").append(averageWaitTime).append(" Minutes\n")
                .append("- Average Order Preparation Time: ").append(averageOrderPreparationTime).append(" Minutes\n")
                .append("- Total Simulation Time: ").append(totalSimulationTime).append(" Minutes\n");

        return summary.toString();
    }

    //Helper methods --------------------------------------------------------------
    //This method is used to get the delay of each customer based on the arrival time of the first customer
    public static void arrivalTimeToDelay() {
        LocalTime starTime = customerArrivalQueue.peek().getArrivalTime();
        for (Customer customer : customerArrivalQueue) {
            LocalTime arrivalTime = customer.getArrivalTime();
            int delay = (int) (arrivalTime.getMinute() - starTime.getMinute());
            customer.setDelay(delay);
        }
    }

    //Print the stack trace of all threads to figure out the state of each thread in case of a deadlock or starvation
    public static void printThreadInfo() {
        Thread.getAllStackTraces().keySet().forEach(thread -> {
            System.out.println("\nThread: " + thread.getName());
            System.out.println("State: " + thread.getState());
            for (StackTraceElement stackTrace : thread.getStackTrace()) {
                System.out.println("    at " + stackTrace);
            }
        });
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
                int preparingTime = meals.get(order);
                Order newOrder = new Order(order, preparingTime, customerId);
                Customer customer = new Customer(customerId, arrivalTime, newOrder, orderBuffer, tableQueue, customerServingData);
                customerArrivalQueue.add(customer);
            }
        } catch (Exception e) {
            throw e;
        }
    }

}
