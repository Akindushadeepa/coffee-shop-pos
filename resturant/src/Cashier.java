//package resturant;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class Cashier extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtSearch;
	private JTable table;
	private JTable table_1;
	private JTextField txtProduct;
	private JTextField txtQty;
	private int tmpId;
	private int tmpPrice;
	private JLabel lbltotal;
	private DefaultTableModel allFoodData;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Cashier frame = new Cashier();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public Cashier() {
		setResizable(false);
		setTitle("Cashier");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 825, 547);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Resturant");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 50));
		lblNewLabel.setBounds(88, 29, 392, 75);
		contentPane.add(lblNewLabel);
		
		txtSearch = new JTextField();
		txtSearch.setBounds(88, 131, 150, 20);
		contentPane.add(txtSearch);
		txtSearch.setColumns(10);
		
		JButton btnSearch = new JButton("Serach");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				searchFood();
			}
		});
		btnSearch.setBounds(248, 130, 89, 23);
		contentPane.add(btnSearch);
		
		table = new JTable();
		table.setDefaultEditor(Object.class, null); // Disable all editing
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBounds(88, 162, 411, 194);
		contentPane.add(scrollPane);
		
		// Add double-click listener to table
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					handleTableDoubleClick();
				}
			}
		});
		
		JButton btnCart = new JButton("Add to cart");
		btnCart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addToCart();
			}
		});
		btnCart.setBounds(88, 431, 130, 23);
		contentPane.add(btnCart);
		
		JPanel panel = new JPanel();
		panel.setBounds(526, 29, 259, 448);
		contentPane.add(panel);
		panel.setLayout(null);
		
		JLabel lblNewLabel_1 = new JLabel("Cart");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblNewLabel_1.setBounds(10, 63, 155, 30);
		panel.add(lblNewLabel_1);
		
		table_1 = new JTable();
		table_1.setDefaultEditor(Object.class, null); // Disable editing
		JScrollPane scrollPane_1 = new JScrollPane(table_1);
		scrollPane_1.setBounds(10, 104, 239, 238);
		panel.add(scrollPane_1);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeFromCart();
			}
		});
		btnRemove.setBounds(10, 414, 89, 23);
		panel.add(btnRemove);
		
		JButton btncheckout = new JButton("Checkout");
		btncheckout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btncheckout.setBounds(160, 414, 89, 23);
		panel.add(btncheckout);
		
		JLabel lblNewLabel_3 = new JLabel("Total :");
		lblNewLabel_3.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lblNewLabel_3.setBounds(10, 368, 78, 23);
		panel.add(lblNewLabel_3);
		
		lbltotal = new JLabel("0");
		lbltotal.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lbltotal.setBounds(141, 365, 108, 29);
		panel.add(lbltotal);
		
		txtProduct = new JTextField();
		txtProduct.setColumns(10);
		txtProduct.setBounds(154, 382, 150, 20);
		txtProduct.setEditable(false);
		contentPane.add(txtProduct);
		
		txtQty = new JTextField();
		txtQty.setColumns(10);
		txtQty.setBounds(337, 382, 49, 20);
		contentPane.add(txtQty);
		
		JLabel lblNewLabel_2 = new JLabel("Product");
		lblNewLabel_2.setBounds(88, 388, 46, 14);
		contentPane.add(lblNewLabel_2);
		
		JLabel lblNewLabel_2_1 = new JLabel("Qty");
		lblNewLabel_2_1.setBounds(313, 385, 28, 14);
		contentPane.add(lblNewLabel_2_1);
		dataload();
	}

	/**
	 * Load food data from the restaurant database and populate the table
	 */
	public void dataload() {
		try {
			// Load MySQL JDBC driver
			Class.forName("com.mysql.cj.jdbc.Driver");

			// Database connection details
			String url = "jdbc:mysql://localhost:3306/restaurant";
			String username = "root";
			String password = "";

			// Establish connection
			Connection conn = DriverManager.getConnection(url, username, password);

			// Create SQL query to fetch id, fName, fPrice from food table
			String sql = "SELECT id, fName, fPrice FROM food";
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);

			// Create table model with column names
			allFoodData = new DefaultTableModel();
			allFoodData.addColumn("ID");
			allFoodData.addColumn("Food Name");
			allFoodData.addColumn("price");

			// Populate table model with data from ResultSet
			while (rs.next()) {
				int id = rs.getInt("id");
				String fName = rs.getString("fName");
				int fPrice = rs.getInt("fPrice");

				// Add row to table model
				allFoodData.addRow(new Object[]{id, fName, fPrice});
			}

			// Set the table model to the JTable
			table.setModel(allFoodData);

			// Close connections
			rs.close();
			stmt.close();
			conn.close();

			System.out.println("Data loaded successfully!");

		} catch (Exception e) {
			System.out.println("Error loading data: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Handle double-click event on table row
	 */
	private void handleTableDoubleClick() {
		int selectedRow = table.getSelectedRow();
		
		if (selectedRow != -1) {
			// Get data from selected row
			tmpId = (int) table.getValueAt(selectedRow, 0);           // ID column
			String foodName = (String) table.getValueAt(selectedRow, 1); // Food Name column
			tmpPrice = (int) table.getValueAt(selectedRow, 2);        // Price column
			
			// Set food name to txtProduct
			txtProduct.setText(foodName);
			
			// Debug output
			System.out.println("Row selected - ID: " + tmpId + ", Name: " + foodName + ", Price: " + tmpPrice);
		}
	}

	/**
	 * Add item to cart table
	 */
	private void addToCart() {
		String productName = txtProduct.getText().trim();
		String qtyStr = txtQty.getText().trim();
		
		// Validate product selection
		if (productName.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please select item!", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		// Validate quantity
		if (qtyStr.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please enter qty!", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		try {
			int qty = Integer.parseInt(qtyStr);
			
			if (qty <= 0) {
				JOptionPane.showMessageDialog(this, "Quantity must be greater than 0!", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
			// Initialize cart table model if not already done
			if (table_1.getModel().getRowCount() == 0 && table_1.getColumnCount() == 0) {
				DefaultTableModel cartModel = new DefaultTableModel();
				cartModel.addColumn("Id");
				cartModel.addColumn("name");
				cartModel.addColumn("price");
				cartModel.addColumn("qty");
				cartModel.addColumn("total");
				table_1.setModel(cartModel);
			}
			
			DefaultTableModel cartModel = (DefaultTableModel) table_1.getModel();
			
			// Check if product already exists in cart
			int existingRowIndex = -1;
			for (int i = 0; i < cartModel.getRowCount(); i++) {
				String existingProductName = (String) cartModel.getValueAt(i, 1); // name column
				if (existingProductName.equals(productName)) {
					existingRowIndex = i;
					break;
				}
			}
			
			if (existingRowIndex != -1) {
				// Product already exists, update quantity and total
				int existingQty = (int) cartModel.getValueAt(existingRowIndex, 3); // qty column
				int newQty = existingQty + qty;
				int total = tmpPrice * newQty;
				
				cartModel.setValueAt(newQty, existingRowIndex, 3); // Update qty
				cartModel.setValueAt(total, existingRowIndex, 4);  // Update total
				
				System.out.println("Product updated - Name: " + productName + ", New Qty: " + newQty + ", New Total: " + total);
			} else {
				// Product is new, add as new row
				int total = tmpPrice * qty;
				cartModel.addRow(new Object[]{tmpId, productName, tmpPrice, qty, total});
				
				System.out.println("Item added to cart - ID: " + tmpId + ", Name: " + productName + ", Price: " + tmpPrice + ", Qty: " + qty + ", Total: " + total);
			}
			
			// Clear fields
			txtProduct.setText("");
			txtQty.setText("");
			
			// Update total
			updateTotal();
			
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(this, "Invalid quantity!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Update cart total
	 */
	private void updateTotal() {
		int grandTotal = 0;
		DefaultTableModel cartModel = (DefaultTableModel) table_1.getModel();
		
		// Sum all totals from the cart table
		for (int i = 0; i < cartModel.getRowCount(); i++) {
			int itemTotal = (int) cartModel.getValueAt(i, 4); // Total column
			grandTotal += itemTotal;
		}
		
		lbltotal.setText(String.valueOf(grandTotal));
		System.out.println("Cart total updated: " + grandTotal);
	}

	/**
	 * Remove selected item from cart and update total
	 */
	private void removeFromCart() {
		int selectedRow = table_1.getSelectedRow();
		
		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "Please select item to remove!", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		DefaultTableModel cartModel = (DefaultTableModel) table_1.getModel();
		String productName = (String) cartModel.getValueAt(selectedRow, 1); // name column
		
		// Remove the selected row
		cartModel.removeRow(selectedRow);
		
		System.out.println("Item removed from cart: " + productName);
		
		// Update total after removing item
		updateTotal();
		
		JOptionPane.showMessageDialog(this, "Item removed from cart!", "Success", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Search for food items by name
	 */
	private void searchFood() {
		String searchText = txtSearch.getText().trim().toLowerCase();
		
		// If search is empty, show all items
		if (searchText.isEmpty()) {
			table.setModel(allFoodData);
			System.out.println("Showing all items");
			return;
		}
		
		// Create a new model for filtered results
		DefaultTableModel filteredModel = new DefaultTableModel();
		filteredModel.addColumn("ID");
		filteredModel.addColumn("Food Name");
		filteredModel.addColumn("price");
		
		// Filter data based on search text
		for (int i = 0; i < allFoodData.getRowCount(); i++) {
			String foodName = (String) allFoodData.getValueAt(i, 1);
			if (foodName.toLowerCase().contains(searchText)) {
				int id = (int) allFoodData.getValueAt(i, 0);
				int price = (int) allFoodData.getValueAt(i, 2);
				filteredModel.addRow(new Object[]{id, foodName, price});
			}
		}
		
		// Set the filtered model to the table
		table.setModel(filteredModel);
		
		if (filteredModel.getRowCount() == 0) {
			System.out.println("No items found matching: " + searchText);
			JOptionPane.showMessageDialog(this, "No items found!", "Search Result", JOptionPane.INFORMATION_MESSAGE);
		} else {
			System.out.println("Found " + filteredModel.getRowCount() + " items");
		}
	}
}
