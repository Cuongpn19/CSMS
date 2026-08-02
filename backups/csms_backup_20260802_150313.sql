-- MariaDB dump 10.19  Distrib 10.4.32-MariaDB, for Win64 (AMD64)
--
-- Host: 127.0.0.1    Database: csms
-- ------------------------------------------------------
-- Server version	10.4.32-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `branches`
--

DROP TABLE IF EXISTS `branches`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `branches` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL,
  `address` varchar(255) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `opening_time` time DEFAULT NULL,
  `closing_time` time DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `branches`
--

LOCK TABLES `branches` WRITE;
/*!40000 ALTER TABLE `branches` DISABLE KEYS */;
INSERT INTO `branches` VALUES (1,'Chi nhánh Bình Thạnh','123 Nguyễn Xí, Bình Thạnh, TP.HCM','02812345678','07:00:00','22:00:00','ACTIVE','2026-07-31 15:54:40','2026-08-02 05:04:41'),(2,'Chi nhánh Quận 1','45 Nguyễn Huệ, Quận 1, TP.HCM','02823456789','07:00:00','23:00:00','ACTIVE','2026-07-31 15:54:40','2026-07-31 15:54:40'),(3,'Chi nhánh Thủ Đức','88 Võ Văn Ngân, TP. Thủ Đức, TP.HCM','02834567890','06:30:00','22:30:00','ACTIVE','2026-07-31 15:54:40','2026-08-02 05:04:34'),(4,'Chi nhánh Quận 3','332 Điện Biên Phủ, Quận 3','0987654321','08:00:00','22:00:00','ACTIVE','2026-08-02 05:05:35','2026-08-02 05:14:57');
/*!40000 ALTER TABLE `branches` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `categories` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'Cà phê','Các loại cà phê','ACTIVE','2026-07-28 17:16:47'),(2,'Trà','Các loại trà','ACTIVE','2026-07-28 17:16:47'),(3,'Đá xay','Các loại thức uống đá xay','ACTIVE','2026-07-28 17:16:47'),(4,'Bánh ngọt','Các loại bánh ăn kèm','ACTIVE','2026-07-28 17:16:47');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coffee_tables`
--

DROP TABLE IF EXISTS `coffee_tables`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `coffee_tables` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `capacity` int(11) NOT NULL DEFAULT 2,
  `status` enum('AVAILABLE','OCCUPIED','RESERVED','INACTIVE') NOT NULL DEFAULT 'AVAILABLE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coffee_tables`
--

LOCK TABLES `coffee_tables` WRITE;
/*!40000 ALTER TABLE `coffee_tables` DISABLE KEYS */;
INSERT INTO `coffee_tables` VALUES (1,'Bàn 01',2,'OCCUPIED','2026-07-28 17:34:48'),(2,'Bàn 02',2,'AVAILABLE','2026-07-28 17:34:48'),(3,'Bàn 03',4,'OCCUPIED','2026-07-28 17:34:48'),(4,'Bàn 04',4,'AVAILABLE','2026-07-28 17:34:48'),(5,'Bàn 05',6,'AVAILABLE','2026-07-28 17:34:48'),(6,'Bàn 06',6,'AVAILABLE','2026-07-28 17:34:48');
/*!40000 ALTER TABLE `coffee_tables` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ingredients`
--

DROP TABLE IF EXISTS `ingredients`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ingredients` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL,
  `unit` varchar(30) NOT NULL,
  `quantity` decimal(12,2) NOT NULL DEFAULT 0.00,
  `minimum_stock` decimal(12,2) NOT NULL DEFAULT 0.00,
  `import_price` decimal(12,2) NOT NULL DEFAULT 0.00,
  `status` enum('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ingredients`
--

LOCK TABLES `ingredients` WRITE;
/*!40000 ALTER TABLE `ingredients` DISABLE KEYS */;
INSERT INTO `ingredients` VALUES (1,'Cà phê rang xay','GRAM',5000.00,500.00,3500000.00,'ACTIVE','2026-08-01 07:26:23','2026-08-02 04:25:26'),(2,'Sữa đặc','ML',10000.00,1000.00,500000.00,'ACTIVE','2026-08-01 07:26:23','2026-08-02 04:25:10'),(3,'Đường','GRAM',8000.00,800.00,300000.00,'ACTIVE','2026-08-01 07:26:23','2026-08-02 04:25:00'),(4,'Đá viên','GRAM',30000.00,3000.00,50000.00,'ACTIVE','2026-08-01 07:26:23','2026-08-02 04:24:52'),(5,'Trà đào','GRAM',4000.00,400.00,180000.00,'ACTIVE','2026-08-01 07:26:23','2026-08-02 04:24:46'),(6,'Syrup đào','ML',5000.00,500.00,120000.00,'ACTIVE','2026-08-01 07:26:23','2026-08-02 04:24:41'),(7,'Bột matcha','GRAM',2000.00,200.00,450000.00,'ACTIVE','2026-08-01 07:26:23','2026-08-02 04:24:35'),(8,'Sữa tươi','ML',10000.00,1000.00,70000.00,'ACTIVE','2026-08-01 07:26:23','2026-08-02 04:35:47'),(9,'Phô mai Mascarpone','GRAM',500000.00,200000.00,2000000.00,'ACTIVE','2026-08-02 04:27:46','2026-08-02 04:27:46'),(10,'Kem tươi','ML',10000.00,5000.00,1100000.00,'ACTIVE','2026-08-02 04:28:50','2026-08-02 04:28:50'),(11,'Bột cacao','GRAM',1000.00,500.00,300000.00,'ACTIVE','2026-08-02 04:29:38','2026-08-02 04:29:38'),(12,'Trứng gà','PIECE',200.00,100.00,440000.00,'ACTIVE','2026-08-02 04:30:56','2026-08-02 04:30:56');
/*!40000 ALTER TABLE `ingredients` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_details`
--

DROP TABLE IF EXISTS `order_details`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `order_details` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_id` int(11) NOT NULL,
  `product_id` int(11) NOT NULL,
  `product_name` varchar(150) NOT NULL,
  `unit_price` decimal(12,2) NOT NULL,
  `quantity` int(11) NOT NULL,
  `subtotal` decimal(12,2) NOT NULL,
  `vat_rate` decimal(5,2) NOT NULL DEFAULT 0.00,
  `vat_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `note` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_order_details_order` (`order_id`),
  KEY `fk_order_details_product` (`product_id`),
  CONSTRAINT `fk_order_details_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_order_details_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_details`
--

LOCK TABLES `order_details` WRITE;
/*!40000 ALTER TABLE `order_details` DISABLE KEYS */;
INSERT INTO `order_details` VALUES (1,1,4,'Matcha đá xay',45000.00,1,45000.00,0.00,0.00,NULL),(2,1,1,'Cà phê đen',25000.00,3,75000.00,0.00,0.00,NULL),(3,1,2,'Cà phê sữa',30000.00,2,60000.00,0.00,0.00,NULL),(4,2,2,'Cà phê sữa',30000.00,1,30000.00,0.00,0.00,NULL),(5,2,1,'Cà phê đen',25000.00,1,25000.00,0.00,0.00,NULL),(6,2,3,'Trà đào',35000.00,1,35000.00,0.00,0.00,NULL),(7,2,4,'Matcha đá xay',45000.00,1,45000.00,0.00,0.00,NULL),(8,3,4,'Matcha đá xay',45000.00,2,90000.00,0.00,0.00,NULL),(9,4,1,'Cà phê đen',25000.00,1,25000.00,0.00,0.00,NULL),(10,4,2,'Cà phê sữa',30000.00,1,30000.00,0.00,0.00,NULL),(11,5,3,'Trà đào',35000.00,4,140000.00,0.00,0.00,NULL),(12,5,4,'Matcha đá xay',45000.00,1,45000.00,0.00,0.00,NULL),(13,6,3,'Trà đào',35000.00,3,105000.00,0.00,0.00,NULL),(14,7,1,'Cà phê đen',25000.00,1,25000.00,0.00,0.00,NULL),(15,8,1,'Cà phê đen',25000.00,1,25000.00,0.00,0.00,NULL),(16,8,3,'Trà đào',35000.00,1,35000.00,0.00,0.00,NULL),(17,8,2,'Cà phê sữa',30000.00,1,30000.00,0.00,0.00,NULL),(18,8,4,'Matcha đá xay',45000.00,1,45000.00,0.00,0.00,NULL),(19,9,2,'Cà phê sữa',30000.00,1,30000.00,0.00,0.00,NULL),(20,9,1,'Cà phê đen',25000.00,1,25000.00,0.00,0.00,NULL),(21,10,3,'Trà đào',35000.00,2,70000.00,0.00,0.00,NULL),(22,10,4,'Matcha đá xay',45000.00,2,90000.00,0.00,0.00,NULL),(23,11,1,'Cà phê đen',25000.00,3,75000.00,0.00,0.00,NULL),(24,11,3,'Trà đào',35000.00,1,35000.00,0.00,0.00,NULL),(25,11,4,'Matcha đá xay',45000.00,1,45000.00,0.00,0.00,NULL);
/*!40000 ALTER TABLE `order_details` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `orders` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `table_id` int(11) DEFAULT NULL,
  `cashier_id` int(11) NOT NULL,
  `order_code` varchar(30) NOT NULL,
  `order_type` enum('DINE_IN','TAKE_AWAY') NOT NULL,
  `status` enum('PENDING','PREPARED','SERVED','PENDING_PAYMENT','PAID','CANCELLED') NOT NULL DEFAULT 'PENDING',
  `subtotal` decimal(12,2) NOT NULL DEFAULT 0.00,
  `discount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `vat_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `total_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `note` varchar(500) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_code` (`order_code`),
  KEY `fk_orders_table` (`table_id`),
  KEY `fk_orders_cashier` (`cashier_id`),
  CONSTRAINT `fk_orders_cashier` FOREIGN KEY (`cashier_id`) REFERENCES `users` (`id`),
  CONSTRAINT `fk_orders_table` FOREIGN KEY (`table_id`) REFERENCES `coffee_tables` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,1,1,'ORD-20260729004824-001','DINE_IN','CANCELLED',180000.00,0.00,0.00,180000.00,NULL,'2026-07-28 17:48:26','2026-07-28 18:39:47'),(2,6,1,'ORD-20260729004912-002','DINE_IN','CANCELLED',135000.00,0.00,0.00,135000.00,NULL,'2026-07-28 17:49:13','2026-07-28 18:08:56'),(3,4,1,'ORD-20260729004922-003','DINE_IN','CANCELLED',90000.00,0.00,0.00,90000.00,NULL,'2026-07-28 17:49:23','2026-07-28 18:39:50'),(4,3,1,'ORD-20260729004930-004','DINE_IN','CANCELLED',55000.00,0.00,0.00,55000.00,NULL,'2026-07-28 17:49:31','2026-07-28 18:08:53'),(5,5,1,'ORD-20260729004943-005','DINE_IN','CANCELLED',185000.00,0.00,0.00,185000.00,NULL,'2026-07-28 17:49:44','2026-07-28 18:08:50'),(6,2,1,'ORD-20260729005059-006','DINE_IN','CANCELLED',105000.00,5000.00,0.00,100000.00,NULL,'2026-07-28 17:51:00','2026-07-28 18:39:53'),(7,NULL,1,'ORD-20260729010950-001','TAKE_AWAY','CANCELLED',25000.00,0.00,0.00,25000.00,NULL,'2026-07-28 18:09:51','2026-07-28 18:39:58'),(8,1,1,'ORD-20260729204850-001','DINE_IN','PAID',135000.00,5000.00,0.00,130000.00,NULL,'2026-07-29 13:48:51','2026-07-29 13:50:38'),(9,1,1,'ORD-20260729212313-001','DINE_IN','PAID',55000.00,5000.00,0.00,50000.00,NULL,'2026-07-29 14:23:14','2026-07-29 14:24:56'),(10,1,1,'ORD-20260801200526-001','DINE_IN','PENDING',160000.00,0.00,0.00,160000.00,NULL,'2026-08-01 13:05:28','2026-08-01 13:05:28'),(11,3,4,'ORD-20260801202713-003','DINE_IN','PENDING',155000.00,0.00,0.00,155000.00,NULL,'2026-08-01 13:27:13','2026-08-01 13:27:13');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payments`
--

DROP TABLE IF EXISTS `payments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `payments` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `order_id` int(11) NOT NULL,
  `payment_method` enum('CASH','BANK_TRANSFER','CARD') NOT NULL,
  `amount_received` decimal(12,2) NOT NULL,
  `change_amount` decimal(12,2) NOT NULL DEFAULT 0.00,
  `paid_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_id` (`order_id`),
  CONSTRAINT `fk_payments_order` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payments`
--

LOCK TABLES `payments` WRITE;
/*!40000 ALTER TABLE `payments` DISABLE KEYS */;
INSERT INTO `payments` VALUES (1,8,'CASH',13000000.00,12870000.00,'2026-07-29 13:50:38'),(2,9,'CASH',55000.00,5000.00,'2026-07-29 14:24:56');
/*!40000 ALTER TABLE `payments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_recipes`
--

DROP TABLE IF EXISTS `product_recipes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `product_recipes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `product_id` int(11) NOT NULL,
  `ingredient_id` int(11) NOT NULL,
  `quantity_required` decimal(12,3) NOT NULL,
  `unit` varchar(30) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_ingredient` (`product_id`,`ingredient_id`),
  UNIQUE KEY `uk_product_recipe` (`product_id`,`ingredient_id`),
  KEY `fk_recipe_ingredient` (`ingredient_id`),
  CONSTRAINT `fk_recipe_ingredient` FOREIGN KEY (`ingredient_id`) REFERENCES `ingredients` (`id`),
  CONSTRAINT `fk_recipe_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_recipes`
--

LOCK TABLES `product_recipes` WRITE;
/*!40000 ALTER TABLE `product_recipes` DISABLE KEYS */;
INSERT INTO `product_recipes` VALUES (6,4,7,4.000,'GRAM'),(7,4,8,200.000,'ML'),(8,4,2,25.000,'ML'),(9,3,5,5.000,'GRAM'),(10,3,6,20.000,'ML'),(11,3,3,20.000,'GRAM'),(12,2,1,25.000,'GRAM'),(13,2,2,25.000,'ML'),(14,1,1,25.000,'GRAM'),(15,1,3,10.000,'GRAM'),(16,1,4,150.000,'GRAM'),(17,2,4,150.000,'GRAM'),(18,3,4,150.000,'GRAM'),(19,4,4,150.000,'GRAM'),(20,6,6,100.000,'ML'),(21,6,4,150.000,'GRAM'),(22,6,8,200.000,'ML'),(23,7,9,300.000,'Gram'),(24,7,10,250.000,'Mililit'),(25,7,12,3.000,'Cái'),(26,7,3,100.000,'Gram'),(27,7,1,100.000,'Gram'),(28,7,11,20.000,'Gram');
/*!40000 ALTER TABLE `product_recipes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `products` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `category_id` int(11) NOT NULL,
  `name` varchar(150) NOT NULL,
  `price` decimal(12,2) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT 0,
  `image` varchar(255) DEFAULT NULL,
  `description` varchar(500) DEFAULT NULL,
  `status` enum('AVAILABLE','UNAVAILABLE') NOT NULL DEFAULT 'AVAILABLE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `fk_products_category` (`category_id`),
  CONSTRAINT `fk_products_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,1,'Cà phê đen',25000.00,100,NULL,'Cà phê đen truyền thống','AVAILABLE','2026-07-28 17:16:47','2026-08-01 13:27:13'),(2,1,'Cà phê sữa',30000.00,102,NULL,'Cà phê sữa đá','AVAILABLE','2026-07-28 17:16:47','2026-07-29 14:23:14'),(3,2,'Trà đào',35000.00,84,NULL,'Trà đào cam sả','AVAILABLE','2026-07-28 17:16:47','2026-08-02 03:40:10'),(4,3,'Matcha đá xay',45000.00,51,NULL,'Matcha kết hợp sữa','AVAILABLE','2026-07-28 17:16:47','2026-08-01 13:27:13'),(6,3,'Đào sữa đá xay',45000.00,50,NULL,'Đào sữa đá xay','AVAILABLE','2026-08-02 03:38:22','2026-08-02 03:38:22'),(7,4,'Bánh tiramisu',55000.00,20,NULL,'Bánh tiramisu vị cacao','AVAILABLE','2026-08-02 03:50:04','2026-08-02 04:33:14');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `roles`
--

DROP TABLE IF EXISTS `roles`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `roles` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `roles`
--

LOCK TABLES `roles` WRITE;
/*!40000 ALTER TABLE `roles` DISABLE KEYS */;
INSERT INTO `roles` VALUES (1,'ADMIN'),(5,'BARISTA'),(2,'CASHIER'),(3,'MANAGER'),(4,'WAITER');
/*!40000 ALTER TABLE `roles` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `email` varchar(150) DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL,
  `role_id` int(11) NOT NULL,
  `status` enum('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `branch_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  KEY `fk_users_role` (`role_id`),
  KEY `fk_users_branch` (`branch_id`),
  CONSTRAINT `fk_users_branch` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`),
  CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin','$2a$12$r1ku2jp5y9tpwOyKukCBDeBTVu/dhFXrg8Dor6fQwj83o.YVQGOvG','Quản trị viên','admin@gmail.com','0858233174',1,'ACTIVE','2026-07-28 17:05:01','2026-07-31 14:55:13',NULL),(2,'nguyenpmb','$2a$12$vrPJ3PaPWaF2I/dYU0iQFOEnhGS7QVSOU2ads9iQ4UEaEgqf5pTVC','Phan Minh Bao Nguyen','nguyenpmb@gmail.com','0987654321',3,'ACTIVE','2026-07-31 16:12:14','2026-08-01 14:24:25',2),(3,'tinnh','$2a$12$sNwbLhjBL5wsnNLoX8UvUu4WUmGeFrbROTMLOPi6Cd8yIw4VfTvc2','Nguyen Hung Tin','nguyenhungtin2003@gmail.com','0987654321',2,'ACTIVE','2026-07-31 16:14:34','2026-08-01 13:03:53',2),(4,'datnt','$2a$12$6Vwaghr9XceTlSnw9EcbPe71A4d7Jz2I5Q2wW5Umy3Vko9Om35fzm','Nguyen Tuan Dat','datnt@gmail.com','0987654321',4,'ACTIVE','2026-07-31 16:15:39','2026-07-31 16:15:39',2),(5,'thangpnp','$2a$12$NaO9lQFYcWTko48JrB9uGuOOWGsHyiMgUlYlylcYXHKef/YdqS7xm','Pham Nguyen Phi Thang','thangpnp@gmail.com','0987654321',5,'ACTIVE','2026-07-31 16:16:18','2026-07-31 16:16:18',2),(6,'cuongpn','$2a$12$cqlcOg1NVWruRcY3MhNrC.OtH2qzmn3KmukaOSWRQJp7GNqggt1RG','Pham Ngoc Cuong','cuongpn1908@gmail.com','0987654321',3,'ACTIVE','2026-07-31 16:16:39','2026-07-31 16:17:06',1),(7,'nva1','$2a$12$Sc6Jj1uALnEMJTnmglkyjesWvKFccawe3n.NHiYZ3VglHHuqfqYl2','Nguyen Van A','nva@gmail.com','0987654321',3,'ACTIVE','2026-08-02 05:07:59','2026-08-02 05:10:41',3),(8,'nvb1','$2a$12$0HmcSZpDyMAucfBhIfTCauacvZcLf42JiiS98FMXejRBq7Koz3RSW','Nguyen Van B','nvb@gmail.com','0987654321',5,'ACTIVE','2026-08-02 05:09:36','2026-08-02 05:09:36',3),(9,'nvc1','$2a$12$Et5EMTmFmVrPOADjjw9nLO7xHEo6EtSd7nDHPr.P3MEdu0cjZ/iHa','Nguyen Van C','nvc@gmail.com','0987654321',2,'ACTIVE','2026-08-02 05:10:29','2026-08-02 05:10:29',3),(10,'nvd1','$2a$12$i7QLChTMURN2ArBT1LJoKerJ..EDw2gUZwRJ5JgF.eTWZ.SPRfReS','Nguyen Van D','nvd@gmail.com','0987654321',4,'ACTIVE','2026-08-02 05:11:22','2026-08-02 05:11:22',3),(11,'nve1','$2a$12$HVOqpeG7.rm9KQi9I/EvAes/.9Q3QPGZgmw36mazYZvmFXjho7wRi','Nguyen Van E','nve@gmail.com','0987654321',2,'ACTIVE','2026-08-02 05:12:01','2026-08-02 05:12:27',1),(12,'nvg1','$2a$12$jF9/QnWrVwvPKq.Z1AINSeeDehouWVKky.lw.kapjsvfs1aXTvqVy','Nguyen Van G','nvg@gmail.com','0987654321',5,'ACTIVE','2026-08-02 05:13:05','2026-08-02 05:13:05',1),(13,'nvh1','$2a$12$XR9aSYtth0o3P1GxLA7.NuQLiNS9A7bcILePvKI/xf7nHQYRssDTe','Nguyen Van H','nvh@gmail.com','0987654321',4,'ACTIVE','2026-08-02 05:13:44','2026-08-02 05:13:44',1),(14,'nvk1','$2a$12$2omXkKl8XfSKW8/W8vW97eohRHxbetBWX9.ZpPK4UCvgYOEzWy2ou','Nguyen Van K','nvk@gmail.com','0987654321',3,'ACTIVE','2026-08-02 05:14:15','2026-08-02 05:14:15',4);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vat_settings`
--

DROP TABLE IF EXISTS `vat_settings`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `vat_settings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `scope_type` enum('GLOBAL','CATEGORY','PRODUCT') NOT NULL,
  `category_id` int(11) DEFAULT NULL,
  `product_id` int(11) DEFAULT NULL,
  `vat_rate` decimal(5,2) NOT NULL DEFAULT 0.00,
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  `effective_from` datetime NOT NULL DEFAULT current_timestamp(),
  `effective_to` datetime DEFAULT NULL,
  `created_by` int(11) DEFAULT NULL,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`id`),
  KEY `fk_vat_category` (`category_id`),
  KEY `fk_vat_product` (`product_id`),
  KEY `fk_vat_created_by` (`created_by`),
  CONSTRAINT `fk_vat_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_vat_created_by` FOREIGN KEY (`created_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_vat_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vat_settings`
--

LOCK TABLES `vat_settings` WRITE;
/*!40000 ALTER TABLE `vat_settings` DISABLE KEYS */;
INSERT INTO `vat_settings` VALUES (2,'CATEGORY',4,NULL,2.00,0,'2026-08-02 13:42:00',NULL,1,'2026-08-02 06:42:17','2026-08-02 07:37:48'),(3,'CATEGORY',1,NULL,10.00,1,'2026-08-02 13:42:00',NULL,1,'2026-08-02 06:42:24','2026-08-02 06:43:58'),(4,'CATEGORY',3,NULL,5.00,0,'2026-08-02 13:42:00',NULL,1,'2026-08-02 06:42:36','2026-08-02 07:38:22'),(5,'CATEGORY',2,NULL,3.00,1,'2026-08-02 13:42:00',NULL,1,'2026-08-02 06:42:42','2026-08-02 06:42:42'),(6,'PRODUCT',NULL,1,7.00,1,'2026-08-02 13:44:00',NULL,1,'2026-08-02 06:44:15','2026-08-02 07:37:36'),(7,'GLOBAL',NULL,NULL,8.00,1,'2026-08-02 13:44:00',NULL,1,'2026-08-02 06:44:57','2026-08-02 06:44:57');
/*!40000 ALTER TABLE `vat_settings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'csms'
--

--
-- Dumping routines for database 'csms'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-02 15:03:13
