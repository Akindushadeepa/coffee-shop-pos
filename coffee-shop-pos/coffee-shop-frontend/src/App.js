import React, { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';

const API_BASE_URL = 'http://localhost:8080/api';

function App() {
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState([]);
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [orderSuccess, setOrderSuccess] = useState(false);

  const categories = [
    { id: 'ALL', name: 'All Items' },
    { id: 'HOT_DRINKS', name: 'Hot Drinks' },
    { id: 'COLD_DRINKS', name: 'Cold Drinks' },
    { id: 'FOOD', name: 'Food' }
  ];

  // Load products
  useEffect(() => {
    fetchProducts();
  }, [selectedCategory]);

  const fetchProducts = async () => {
    try {
      setLoading(true);
      let url = `${API_BASE_URL}/products`;
      
      if (selectedCategory !== 'ALL') {
        url = `${API_BASE_URL}/products/category/${selectedCategory}`;
      }
      
      const response = await axios.get(url);
      setProducts(response.data);
      setLoading(false);
    } catch (error) {
      console.error('Error fetching products:', error);
      setLoading(false);
      alert('Backend server එක run වෙනවද බලන්න! (http://localhost:8080)');
    }
  };

  const addToCart = (product) => {
    const existingItem = cart.find(item => item.productId === product.id);
    
    if (existingItem) {
      setCart(cart.map(item =>
        item.productId === product.id
          ? { ...item, quantity: item.quantity + 1, subtotal: (item.quantity + 1) * item.price }
          : item
      ));
    } else {
      setCart([...cart, {
        productId: product.id,
        productName: product.name,
        price: product.price,
        quantity: 1,
        subtotal: product.price
      }]);
    }
  };

  const removeFromCart = (productId) => {
    setCart(cart.filter(item => item.productId !== productId));
  };

  const updateQuantity = (productId, change) => {
    setCart(cart.map(item => {
      if (item.productId === productId) {
        const newQuantity = Math.max(0, item.quantity + change);
        return {
          ...item,
          quantity: newQuantity,
          subtotal: newQuantity * item.price
        };
      }
      return item;
    }).filter(item => item.quantity > 0));
  };

  const calculateTotal = () => {
    return cart.reduce((sum, item) => sum + item.subtotal, 0);
  };

  const clearCart = () => {
    setCart([]);
  };

  const completeOrder = async () => {
    if (cart.length === 0) {
      alert('Cart එක empty! කරුණාකර items add කරන්න.');
      return;
    }

    try {
      const response = await axios.post(`${API_BASE_URL}/orders`, cart);
      console.log('Order created:', response.data);
      
      setOrderSuccess(true);
      setCart([]);
      
      setTimeout(() => {
        setOrderSuccess(false);
      }, 3000);
      
      alert(`Order එක successfully complete වුණා! Order ID: ${response.data.id}\nTotal: Rs. ${response.data.totalAmount}`);
    } catch (error) {
      console.error('Error creating order:', error);
      alert('Order එක create කරන්න බැහැ. Backend එක check කරන්න.');
    }
  };

  return (
    <div className="app">
      <header className="header">
        <div className="header-content">
          <div className="header-left">
            <h1>☕ Coffee Shop POS</h1>
            <p>Point of Sale System</p>
          </div>
          <div className="header-right">
            <div className="cart-badge">
              🛒 {cart.reduce((sum, item) => sum + item.quantity, 0)} Items
            </div>
          </div>
        </div>
      </header>

      {orderSuccess && (
        <div className="success-banner">
          ✅ Order එක successfully complete වුණා!
        </div>
      )}

      <div className="main-container">
        <div className="products-section">
          <div className="category-filters">
            {categories.map(cat => (
              <button
                key={cat.id}
                className={`category-btn ${selectedCategory === cat.id ? 'active' : ''}`}
                onClick={() => setSelectedCategory(cat.id)}
              >
                {cat.name}
              </button>
            ))}
          </div>

          {loading ? (
            <div className="loading">Loading products...</div>
          ) : (
            <div className="products-grid">
              {products.map(product => (
                <div
                  key={product.id}
                  className="product-card"
                  onClick={() => addToCart(product)}
                >
                  <div className="product-icon">☕</div>
                  <h3>{product.name}</h3>
                  <p className="product-price">Rs. {product.price}</p>
                  <button className="add-btn">Add to Cart</button>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="cart-section">
          <div className="cart-container">
            <div className="cart-header">
              <h2>🛒 Cart</h2>
              {cart.length > 0 && (
                <button className="clear-btn" onClick={clearCart}>
                  Clear All
                </button>
              )}
            </div>

            <div className="cart-items">
              {cart.length === 0 ? (
                <div className="empty-cart">
                  <div className="empty-icon">🛒</div>
                  <p>Cart is empty</p>
                </div>
              ) : (
                cart.map(item => (
                  <div key={item.productId} className="cart-item">
                    <div className="item-header">
                      <h3>{item.productName}</h3>
                      <button
                        className="remove-btn"
                        onClick={() => removeFromCart(item.productId)}
                      >
                        🗑️
                      </button>
                    </div>
                    <div className="item-footer">
                      <div className="quantity-controls">
                        <button
                          className="qty-btn"
                          onClick={() => updateQuantity(item.productId, -1)}
                        >
                          -
                        </button>
                        <span className="quantity">{item.quantity}</span>
                        <button
                          className="qty-btn plus"
                          onClick={() => updateQuantity(item.productId, 1)}
                        >
                          +
                        </button>
                      </div>
                      <p className="item-price">Rs. {item.subtotal}</p>
                    </div>
                  </div>
                ))
              )}
            </div>

            {cart.length > 0 && (
              <div className="cart-footer">
                <div className="total-section">
                  <span>Total:</span>
                  <span className="total-amount">Rs. {calculateTotal()}</span>
                </div>
                <button className="checkout-btn" onClick={completeOrder}>
                  💰 Complete Payment
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default App;
