public class CoffeeItem {
    private String name;
    private String description;
    private double price;
    private int quantity;
    
    public CoffeeItem(String name, String description, double price) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = 1;
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public double getPrice() {
        return price;
    }
    
    public void setPrice(double price) {
        this.price = price;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    @Override
    public String toString() {
        return String.format("%s - $%.2f x%d", name, price, quantity);
    }
}