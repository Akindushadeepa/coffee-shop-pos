-- phpMyAdmin SQL Dump
-- version 4.7.9
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Nov 19, 2025 at 06:11 PM
-- Server version: 10.1.31-MariaDB
-- PHP Version: 7.2.3

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET AUTOCOMMIT = 0;
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `restaurant`
--

-- --------------------------------------------------------

--
-- Table structure for table `bills`
--

CREATE TABLE `bills` (
  `id` varchar(64) NOT NULL,
  `bill_date` datetime DEFAULT NULL,
  `total` double DEFAULT NULL,
  `cashier` varchar(100) DEFAULT NULL,
  `table_number` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `bills`
--

INSERT INTO `bills` (`id`, `bill_date`, `total`, `cashier`, `table_number`) VALUES
('251119_10', '2025-11-19 22:02:39', 150, 'cashier1', '1'),
('251119_11', '2025-11-19 22:03:52', 200, 'cashier1', '2'),
('251119_12', '2025-11-19 22:06:27', 100, 'cashier2', '3'),
('251119_13', '2025-11-19 22:08:08', 450, 'cashier2', '4'),
('251119_6', '2025-11-19 20:54:17', 100, 'cashier1', '5');

-- --------------------------------------------------------

--
-- Table structure for table `bill_items`
--

CREATE TABLE `bill_items` (
  `id` int(11) NOT NULL,
  `bill_id` varchar(64) NOT NULL,
  `food_id` int(11) NOT NULL,
  `qty` int(11) DEFAULT NULL,
  `unit_price` double DEFAULT NULL,
  `total` double DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `bill_items`
--

INSERT INTO `bill_items` (`id`, `bill_id`, `food_id`, `qty`, `unit_price`, `total`) VALUES
(1, '251119_10', 1, 1, 50, 50),
(2, '251119_10', 2, 1, 100, 100),
(3, '251119_11', 1, 4, 50, 200),
(4, '251119_12', 1, 2, 50, 100),
(5, '251119_13', 2, 4, 100, 400),
(6, '251119_13', 1, 1, 50, 50);

-- --------------------------------------------------------

--
-- Table structure for table `food`
--

CREATE TABLE `food` (
  `id` int(11) NOT NULL,
  `fName` varchar(20) NOT NULL,
  `fPrice` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `food`
--

INSERT INTO `food` (`id`, `fName`, `fPrice`) VALUES
(1, 'cacke', 50),
(2, 'rools', 100);

--
-- Indexes for dumped tables
--

--
-- Indexes for table `bills`
--
ALTER TABLE `bills`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `bill_items`
--
ALTER TABLE `bill_items`
  ADD PRIMARY KEY (`id`),
  ADD KEY `bill_id` (`bill_id`),
  ADD KEY `food_id` (`food_id`);

--
-- Indexes for table `food`
--
ALTER TABLE `food`
  ADD PRIMARY KEY (`id`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bill_items`
--
ALTER TABLE `bill_items`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=7;

--
-- AUTO_INCREMENT for table `food`
--
ALTER TABLE `food`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `bill_items`
--
ALTER TABLE `bill_items`
  ADD CONSTRAINT `bill_items_ibfk_1` FOREIGN KEY (`bill_id`) REFERENCES `bills` (`id`),
  ADD CONSTRAINT `bill_items_ibfk_2` FOREIGN KEY (`food_id`) REFERENCES `food` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

-- --------------------------------------------------------
--
-- Table structure for table `users`
--

CREATE TABLE IF NOT EXISTS `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` varchar(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- Sample users
INSERT INTO `users` (`username`, `password`, `role`) VALUES
('admin1', 'admin1', 'ADMIN'),
('admin2', 'admin2', 'ADMIN'),
('cashier1', 'cashier1', 'CASHIER'),
('cashier2', 'cashier2', 'CASHIER');

-- kitchen_orders table for kitchen dashboard
CREATE TABLE IF NOT EXISTS kitchen_orders (
  id INT AUTO_INCREMENT PRIMARY KEY,
  bill_id VARCHAR(64) NOT NULL,
  food_id INT NOT NULL,
  name VARCHAR(255) NOT NULL,
  qty INT NOT NULL,
  table_number VARCHAR(20) DEFAULT NULL,
  status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX (bill_id),
  INDEX (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- sample kitchen user
INSERT INTO users (username, password, role) VALUES ('kitchen1', 'kitchen1', 'KITCHEN');
