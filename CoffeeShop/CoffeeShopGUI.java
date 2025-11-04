import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class CoffeeShopGUI {
    private JFrame frame;
    private JPanel mainPanel, menuPanel, cartPanel;
    private JLabel totalLabel;
    private java.util.List<CartItem> cartItems;
    private double totalAmount = 0.0;

    // Coffee menu items
    private final String[] coffeeNames = {"Latte", "Cappuccino", "Espresso", "Americano"};
    private final String[] descriptions = {
        "Smooth milk coffee", 
        "Strong and creamy", 
        "Pure coffee essence", 
        "Simple and strong"
    };
    private final double[] prices = {8.99, 8.99, 3.99, 3.99};

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CoffeeShopGUI().createAndShowGUI();
        });
    }

    public void createAndShowGUI() {
        cartItems = new ArrayList<>();
        
        // Create main frame
        frame = new JFrame("Coffee Shop");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 700);
        frame.setLocationRelativeTo(null);
        
        // Create main panel with border layout
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 245, 245));
        
        createHeader();
        createHeroSection();
        createMenuSection();
        createCartSection();
        createFooter();
        
        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(139, 69, 19)); // Brown color
        headerPanel.setPreferredSize(new Dimension(900, 60));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        // Navigation labels
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navPanel.setBackground(new Color(139, 69, 19));
        
        String[] navItems = {"COFFEE", "Menu", "Offers", "Cart", "Profile"};
        for (String item : navItems) {
            JLabel navLabel = new JLabel(item);
            navLabel.setForeground(Color.WHITE);
            navLabel.setFont(new Font("Arial", Font.BOLD, 14));
            navLabel.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
            navPanel.add(navLabel);
        }
        
        headerPanel.add(navPanel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
    }

    private void createHeroSection() {
        JPanel heroPanel = new JPanel();
        heroPanel.setLayout(new BoxLayout(heroPanel, BoxLayout.Y_AXIS));
        heroPanel.setBackground(new Color(245, 222, 179)); // Light wheat color
        heroPanel.setBorder(BorderFactory.createEmptyBorder(40, 0, 40, 0));
        
        JLabel title1 = new JLabel("Freshly Brewed.");
        title1.setFont(new Font("Arial", Font.BOLD, 32));
        title1.setForeground(new Color(101, 67, 33));
        title1.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel title2 = new JLabel("Just for You.");
        title2.setFont(new Font("Arial", Font.BOLD, 32));
        title2.setForeground(new Color(101, 67, 33));
        title2.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton orderButton = new JButton("ORDER NOW");
        orderButton.setFont(new Font("Arial", Font.BOLD, 18));
        orderButton.setBackground(new Color(139, 69, 19));
        orderButton.setForeground(Color.WHITE);
        orderButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        orderButton.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        orderButton.addActionListener(e -> showMenu());
        
        heroPanel.add(title1);
        heroPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        heroPanel.add(title2);
        heroPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        heroPanel.add(orderButton);
        
        mainPanel.add(heroPanel, BorderLayout.CENTER);
    }

    private void createMenuSection() {
        // Category buttons
        JPanel categoryPanel = new JPanel();
        categoryPanel.setBackground(Color.WHITE);
        categoryPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        String[] categories = {"Hot Coffee", "Cold Coffee", "Snacks", "Specials"};
        for (String category : categories) {
            JButton catButton = new JButton(category);
            catButton.setFont(new Font("Arial", Font.PLAIN, 14));
            catButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            categoryPanel.add(catButton);
        }
        
        // Coffee items grid
        JPanel coffeeGridPanel = new JPanel(new GridLayout(2, 4, 20, 20));
        coffeeGridPanel.setBackground(Color.WHITE);
        coffeeGridPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        for (int i = 0; i < coffeeNames.length; i++) {
            JPanel coffeeCard = createCoffeeCard(coffeeNames[i], descriptions[i], prices[i], i);
            coffeeGridPanel.add(coffeeCard);
        }
        
        JPanel menuContainer = new JPanel(new BorderLayout());
        menuContainer.setBackground(Color.WHITE);
        menuContainer.add(categoryPanel, BorderLayout.NORTH);
        menuContainer.add(coffeeGridPanel, BorderLayout.CENTER);
        
        mainPanel.add(menuContainer, BorderLayout.CENTER);
    }

    private JPanel createCoffeeCard(String name, String description, double price, int index) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setPreferredSize(new Dimension(180, 200));
        
        // Coffee name
        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Description
        JLabel descLabel = new JLabel("<html><center>" + description + "</center></html>");
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Price
        JLabel priceLabel = new JLabel(String.format("$%.2f", price));
        priceLabel.setFont(new Font("Arial", Font.BOLD, 16));
        priceLabel.setForeground(new Color(139, 69, 19));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Customize button
        JButton customButton = new JButton("CUSTOMIZE");
        customButton.setFont(new Font("Arial", Font.PLAIN, 12));
        customButton.setBackground(new Color(139, 69, 19));
        customButton.setForeground(Color.WHITE);
        customButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        customButton.addActionListener(e -> addToCart(index));
        
        card.add(nameLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(descLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(priceLabel);
        card.add(Box.createRigidArea(new Dimension(0, 15)));
        card.add(customButton);
        
        return card;
    }

    private void createCartSection() {
        cartPanel = new JPanel();
        cartPanel.setLayout(new BoxLayout(cartPanel, BoxLayout.Y_AXIS));
        cartPanel.setBackground(Color.WHITE);
        cartPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        cartPanel.setVisible(false);
        
        JLabel cartTitle = new JLabel("Your Cart");
        cartTitle.setFont(new Font("Arial", Font.BOLD, 24));
        cartTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));
        totalLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JButton checkoutButton = new JButton("CHECKOUT");
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 16));
        checkoutButton.setBackground(new Color(139, 69, 19));
        checkoutButton.setForeground(Color.WHITE);
        checkoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        checkoutButton.addActionListener(e -> checkout());
        
        cartPanel.add(cartTitle);
        cartPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        cartPanel.add(totalLabel);
        cartPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        cartPanel.add(checkoutButton);
    }

    private void createFooter() {
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(240, 240, 240));
        footerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        
        String[] footerItems = {"About", "Contact", "Terms"};
        for (String item : footerItems) {
            JLabel footerLabel = new JLabel(item);
            footerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            footerLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
            footerPanel.add(footerLabel);
        }
        
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
    }

    private void addToCart(int itemIndex) {
        // Show quantity dialog
        String quantityStr = JOptionPane.showInputDialog(frame, 
            "Enter quantity for " + coffeeNames[itemIndex] + ":", "1");
        
        if (quantityStr != null && !quantityStr.trim().isEmpty()) {
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity > 0) {
                    double itemPrice = prices[itemIndex];
                    double totalPrice = itemPrice * quantity;
                    
                    cartItems.add(new CartItem(coffeeNames[itemIndex], quantity, itemPrice, totalPrice));
                    totalAmount += totalPrice;
                    
                    updateCartDisplay();
                    showCart();
                    
                    JOptionPane.showMessageDialog(frame, 
                        "Added " + quantity + " x " + coffeeNames[itemIndex] + " to cart!");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Please enter a valid number!");
            }
        }
    }

    private void updateCartDisplay() {
        totalLabel.setText(String.format("Total: $%.2f", totalAmount));
    }

    private void showMenu() {
        // Remove cart panel if it exists
        mainPanel.remove(cartPanel);
        
        // Recreate menu section
        createMenuSection();
        
        frame.revalidate();
        frame.repaint();
    }

    private void showCart() {
        // Remove current center content
        mainPanel.remove(1); // Remove the center component (menu)
        
        // Add cart panel
        mainPanel.add(cartPanel, BorderLayout.CENTER);
        
        frame.revalidate();
        frame.repaint();
    }

    private void checkout() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Your cart is empty!");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(frame, 
            "Proceed with checkout? Total: $" + String.format("%.2f", totalAmount),
            "Checkout", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            JOptionPane.showMessageDialog(frame, 
                "Order confirmed! Thank you for your purchase!\nTotal: $" + String.format("%.2f", totalAmount));
            
            // Clear cart
            cartItems.clear();
            totalAmount = 0.0;
            updateCartDisplay();
            showMenu();
        }
    }

    // Inner class for cart items
    private class CartItem {
        String name;
        int quantity;
        double unitPrice;
        double totalPrice;
        
        CartItem(String name, int quantity, double unitPrice, double totalPrice) {
            this.name = name;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.totalPrice = totalPrice;
        }
    }
}