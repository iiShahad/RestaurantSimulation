//First: read from input files

import java.io.BufferedReader;
import java.io.FileReader;

class RestaurantSimulation {

    //Global variables
    static int numChefs;
    static int numWaiters;
    static int numTables;

    public static void main(String[] args) {
        //input files
        String inputFile1 = "restaurant_simulation_input1.txt";
        String inputFile2 = "restaurant_simulation_input2.txt";
        String inputFile3 = "restaurant_simulation_input3.txt";

        readInputFile(inputFile1);

        System.out.println("Number of Chefs: " + numChefs);
        System.out.println("Number of Waiters: " + numWaiters);
        System.out.println("Number of Tables: " + numTables);

    }

    public static void readInputFile(String inputFile) {
        try {
            FileReader fr = new FileReader(inputFile);
            try (BufferedReader br = new BufferedReader(fr)) {
                String line;
                int currentLine = 1;
                while ((line = br.readLine()) != null) {
                    //Reading the first line: NC NW NT
                    if (currentLine == 1) {
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
                    }else if(currentLine == 2){
              
                    }
                    currentLine++;
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e);
        }
    }
}
