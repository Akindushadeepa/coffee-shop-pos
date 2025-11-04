import java.util.*;

public class CoffeeShop {
    private static Scanner scanner = new Scanner(System.in);
    private static List<CoffeeItem> cart = new ArrayList<>();
    
    public static void main(String[] args) {
        displayMainMenu();
    }
    
    public static void displayMainMenu() {
        while (true) {
            System.out.println("=".repeat(50));
            System.out.println("COFFEE    Menu    Offers    Cart    Profile");
            System.out.println("=".repeat(50));
            System.out.println();
            System.out.println("Freshly Brewed.");
            System.out.println("Just for You.");
            System.out.println();
            System.out.println("ORDER NOW");
            System.out.println();
            
            System.out.println("Hot Coffee    Cold Coffee    Snacks    Specials");
            System.out.println();
            
            // Display coffee items
            displayCoffeeItems();
            
            System.out.println();
            System.out.println("About    Contact    Terms");
            System.out.println();
            
            System.out.println("1. View Categories");
            System.out.println("2. Add Item to Cart");
            System.out.println("3. View Cart");
            System.out.println("4. Checkout");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            
            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline
            
            switch (choice) {
                case 1 -> viewCategories();
                case 2 -> addItemToCart();
                case 3 -> viewCart();
                case 4 -> checkout();
                case 5 -> {
                    System.out.println("Thank you for visiting!");
                    return;
                }
                default -> System.out.println("Invalid option!");
            }
        }
    }
    
    public static void displayCoffeeItems() {
        CoffeeItem[] items = {
            new CoffeeItem("Latte", "Smooth milk coffee", 8.99),
            new CoffeeItem("Cappuccino", "Strong and creamy", 8.99),
            new CoffeeItem("Espresso", "Pure coffee essence", 3.99),
            new CoffeeItem("Americano", "Simple and strong", 3.99)
        };
        
        for (CoffeeItem item : items) {
            System.out.printf("%-12s", item.getName());
        }
        System.out.println();
        
        for (CoffeeItem item : items) {
            System.out.printf("%-12s", item.getDescription().substring(0, Math.min(11, item.getDescription().length())));
        }
        System.out.println();
        
        for (CoffeeItem item : items) {
            System.out.printf("$%-11.2f", item.getPrice());
        }
        System.out.println();
        
        for (int i = 0; i < items.length; i++) {
            System.out.printf("%-12s", "CUSTOMIZE");
        }
        System.out.println();
    }
    
    public static void viewCategories() {
        System.out.println("\n=== CATEGORIES ===");
        System.out.println("1. Hot Coffee");
        System.out.println("2. Cold Coffee");
        System.out.println("3. Snacks");
        System.out.println("4. Specials");
        System.out.print("Choose category: ");
        
        int category = scanner.nextInt();
        String[] categories = {"Hot Coffee", "Cold Coffee", "Snacks", "Specials"};
        
        if (category >= 1 && category <= 4) {
            System.out.println("\nYou selected: " + categories[category - 1]);
        } else {
            System.out.println("Invalid category!");
        }
    }
    
    public static void addItemToCart() {
        System.out.println("\n=== ADD ITEM TO CART ===");
        System.out.println("1. Latte - $8.99");
        System.out.println("2. Cappuccino - $8.99");
        System.out.println("3. Espresso - $3.99");
        System.out.println("4. Americano - $3.99");
        System.out.print("Choose item: ");
        
        int itemChoice = scanner.nextInt();
        scanner.nextLine(); // consume newline
        
        CoffeeItem selectedItem = null;
        
        switch (itemChoice) {
            case 1 -> selectedItem = new CoffeeItem("Latte", "Smooth milk coffee", 8.99);
            case 2 -> selectedItem = new CoffeeItem("Cappuccino", "Strong and creamy", 8.99);
            case 3 -> selectedItem = new CoffeeItem("Espresso", "Pure coffee essence", 3.99);
            case 4 -> selectedItem = new CoffeeItem("Americano", "Simple and strong", 3.99);
            default -> {
                System.out.println("Invalid item selection!");
                return;
            }
        }
        
        System.out.print("Enter quantity: ");
        int quantity = scanner.nextInt();
        scanner.nextLine();
        
        selectedItem.setQuantity(quantity);
        cart.add(selectedItem);
        
        System.out.println("Added " + quantity + " x " + selectedItem.getName() + " to cart!");
    }
    
    public static void viewCart() {
        if (cart.isEmpty()) {
            System.out.println("\nYour cart is empty!");
            return;
        }
        
        System.out.println("\n=== YOUR CART ===");
        double total = 0;
        
        for (int i = 0; i < cart.size(); i++) {
            CoffeeItem item = cart.get(i);
            double itemTotal = item.getPrice() * item.getQuantity();
            System.out.printf("%d. %s x%d - $%.2f each = $%.2f%n", 
                i + 1, item.getName(), item.getQuantity(), item.getPrice(), itemTotal);
            total += itemTotal;
        }
        
        System.out.printf("Total: $%.2f%n", total);
    }
    
    public static void checkout() {
        if (cart.isEmpty()) {
            System.out.println("\nYour cart is empty! Add items before checkout.");
            return;
        }
        
        viewCart();
        System.out.print("\nProceed with checkout? (yes/no): ");
        String confirm = scanner.nextLine();
        
        if (confirm.equalsIgnoreCase("yes")) {
            double total = cart.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
            
            System.out.printf("Order confirmed! Total: $%.2f%n", total);
            System.out.println("Thank you for your order!");
            cart.clear();
        } else {
            System.out.println("Checkout cancelled.");
        }
    }
}