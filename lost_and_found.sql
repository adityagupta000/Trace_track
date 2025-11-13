-- Drop and create the database
DROP DATABASE IF EXISTS lost_and_founds;
CREATE DATABASE lost_and_founds;
USE lost_and_founds;

-- Users Table
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('user', 'admin') DEFAULT 'user'
);

-- Items Table
CREATE TABLE items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    location VARCHAR(255) NOT NULL,
    status ENUM('lost', 'found', 'claimed') NOT NULL DEFAULT 'lost',
    image VARCHAR(255) NOT NULL,
    created_by INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Claims Table
CREATE TABLE claims (
    id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    claimed_by INT NOT NULL,
    claimant_name VARCHAR(255) NOT NULL,
    claimant_email VARCHAR(255) NOT NULL,
    claimed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE,
    FOREIGN KEY (claimed_by) REFERENCES users(id) ON DELETE CASCADE
);

-- Messages Table
CREATE TABLE messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id INT NOT NULL,
    receiver_id INT NOT NULL,
    item_id INT NOT NULL,
    message TEXT NOT NULL,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(id) ON DELETE CASCADE
);

-- Feedback Table
CREATE TABLE feedback (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    feedback_text TEXT NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Insert an admin user
INSERT INTO users (name, email, password, role) VALUES 
('Admin', 'admin@example.com', '$2b$12$/mfZLFCm9krbLWeMYZfVD.neFNZ7y4VvK0ysOMPt1HT1Q0y5Li/HC', 'admin');

-- Trigger to update item status when claimed
DELIMITER //
CREATE TRIGGER update_item_status_on_claim
AFTER INSERT ON claims
FOR EACH ROW
BEGIN
    UPDATE items
    SET status = 'claimed'
    WHERE id = NEW.item_id;
END;
//
DELIMITER ;

-- Stored Procedure to search for items
DELIMITER //
CREATE PROCEDURE search_items(IN search_term VARCHAR(255))
BEGIN
    SELECT * 
    FROM items 
    WHERE name LIKE CONCAT('%', search_term, '%') 
       OR description LIKE CONCAT('%', search_term, '%');
END;
//
DELIMITER ;

-- Example Data (optional)
INSERT INTO users (name, email, password, role) VALUES 
('John Doe', 'john@example.com', 'password123', 'user'),
('Jane Smith', 'jane@example.com', 'password456', 'user');

INSERT INTO items (name, description, location, status, image, created_by) VALUES
('Lost Wallet', 'A brown leather wallet lost near the park.', 'City Park', 'lost', 'wallet.jpg', 1),
('Found Keys', 'A set of keys with a red keychain found in the library.', 'Library', 'found', 'keys.jpg', 2);

INSERT INTO claims (item_id, claimed_by, claimant_name, claimant_email) VALUES
(1, 2, 'Jane Smith', 'jane@example.com');

INSERT INTO messages (sender_id, receiver_id, item_id, message) VALUES
(1, 2, 1, 'Hi, I think this is my wallet. Can we meet to discuss?'),
(2, 1, 1, 'Sure, let’s meet at the library tomorrow.');

INSERT INTO feedback (user_id, feedback_text) VALUES
(1, 'Great platform, very helpful!'),
(2, 'It was easy to find the owner of the keys.');
