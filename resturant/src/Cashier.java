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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Cashier extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField txtSearch;
	private JTable table;
	private JTable table_1;
	private JTextField txtProduct;
	private JTextField txtQty;

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
			}
		});
		btnSearch.setBounds(248, 130, 89, 23);
		contentPane.add(btnSearch);
		
		table = new JTable();
		table.setBounds(88, 162, 411, 194);
		contentPane.add(table);
		
		JButton btnCart = new JButton("Add to cart");
		btnCart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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
		table_1.setBounds(10, 104, 239, 238);
		panel.add(table_1);
		
		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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
		
		JLabel lbltotal = new JLabel("0");
		lbltotal.setFont(new Font("Tahoma", Font.PLAIN, 20));
		lbltotal.setBounds(141, 365, 108, 29);
		panel.add(lbltotal);
		
		txtProduct = new JTextField();
		txtProduct.setColumns(10);
		txtProduct.setBounds(154, 382, 150, 20);
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

	}
}
