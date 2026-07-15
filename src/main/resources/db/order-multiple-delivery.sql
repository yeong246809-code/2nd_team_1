SET @schema_name = DATABASE();

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'order' AND COLUMN_NAME = 'ordererName') = 0,
    'ALTER TABLE `order` ADD COLUMN ordererName VARCHAR(50) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'order' AND COLUMN_NAME = 'ordererPhone') = 0,
    'ALTER TABLE `order` ADD COLUMN ordererPhone VARCHAR(30) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'order' AND COLUMN_NAME = 'ordererZipCode') = 0,
    'ALTER TABLE `order` ADD COLUMN ordererZipCode VARCHAR(20) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'order' AND COLUMN_NAME = 'ordererBaseAddress') = 0,
    'ALTER TABLE `order` ADD COLUMN ordererBaseAddress VARCHAR(255) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @sql = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @schema_name AND TABLE_NAME = 'order' AND COLUMN_NAME = 'ordererDetailAddress') = 0,
    'ALTER TABLE `order` ADD COLUMN ordererDetailAddress VARCHAR(255) NULL', 'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE order_delivery (
    orderDeliveryNo BIGINT NOT NULL AUTO_INCREMENT,
    orderNo BIGINT NOT NULL,
    orderDetailNo BIGINT NOT NULL,
    shipmentIndex INT NOT NULL,
    quantity INT NOT NULL,
    recipientName VARCHAR(50) NOT NULL,
    recipientPhone VARCHAR(30) NOT NULL,
    zipCode VARCHAR(20) NULL,
    baseAddress VARCHAR(255) NOT NULL,
    detailAddress VARCHAR(255) NULL,
    memo VARCHAR(255) NULL,
    PRIMARY KEY (orderDeliveryNo),
    INDEX idx_order_delivery_order (orderNo),
    INDEX idx_order_delivery_detail (orderDetailNo)
);
