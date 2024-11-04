class Order{
    //Constructor --------------------------------------------------------------------
    private String mealName;
    private int mealTime;
    private int customerId;

    public Order(String mealName, int mealTime, int customerId){
        this.mealName = mealName;
        this.mealTime = mealTime;
        this.customerId = customerId;
    }

    //getters ------------------------------------------------------------------------
    public String getMealName(){
        return mealName;
    }

    public int getMealTime(){
        return mealTime;
    }

    public int getCustomerId(){
        return customerId;
    }

    @Override
    public String toString(){
        return "Meal Name: " + mealName + " Meal Time: " + mealTime + " Customer ID: " + customerId;
    }
}