-- Complete SQL file for Restaurant Reservation System
-- Includes: database creation, table structure definition, test data
-- All statuses use integers, no enum types, no physical foreign key constraints

-- Create database
CREATE DATABASE restaurant_reservation_sys
    WITH 
    ENCODING = 'UTF8'
    LC_COLLATE = 'zh_CN.UTF-8'
    LC_CTYPE = 'zh_CN.UTF-8'
    TEMPLATE = template0;

-- Connect to the newly created database
\c restaurant_reservation_sys;

-- drop database restaurant_reservation_system;

-- Table structure definition begins
-- Create extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create update timestamp trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 1. Users table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role INTEGER NOT NULL, -- 0: Administrator, 1: Restaurant Manager, 2: User, 3: Waiter
    avatar TEXT, -- User avatar URL
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status INTEGER NOT NULL DEFAULT 1 -- 0: Banned, 1: Enabled
);

-- Users table trigger
DROP TRIGGER IF EXISTS update_users_updated_at ON users;
CREATE TRIGGER update_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Users table indexes
DROP INDEX IF EXISTS idx_users_email;
CREATE INDEX idx_users_email ON users(email);
DROP INDEX IF EXISTS idx_users_role;
CREATE INDEX idx_users_role ON users(role);
DROP INDEX IF EXISTS idx_users_status;
CREATE INDEX idx_users_status ON users(status);

-- 2. Restaurants table
CREATE TABLE IF NOT EXISTS restaurants (
    id SERIAL PRIMARY KEY,
    owner_id INTEGER NOT NULL, -- Logical foreign key, references users.id
    name VARCHAR(100) NOT NULL,
    description TEXT, -- Restaurant description
    address TEXT NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL, -- Latitude
    longitude DECIMAL(11, 8) NOT NULL, -- Longitude
    phone VARCHAR(20) NOT NULL,
    business_license VARCHAR(100),
    photos TEXT, -- Image URLs separated by |
    status INTEGER NOT NULL DEFAULT 0, -- 0: Pending Review, 1: Approved, 2: Closed, 3: Operating
    business_hours TEXT, -- Business hours, format: "Monday-Friday 9:00-22:00|Saturday-Sunday 10:00-23:00"
    cuisine VARCHAR(50), -- Cuisine type, e.g., Chinese, Western, Japanese, Korean
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Restaurants table trigger
DROP TRIGGER IF EXISTS update_restaurants_updated_at ON restaurants;
CREATE TRIGGER update_restaurants_updated_at
BEFORE UPDATE ON restaurants
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Restaurants table indexes
DROP INDEX IF EXISTS idx_restaurants_owner;
CREATE INDEX idx_restaurants_owner ON restaurants(owner_id);
DROP INDEX IF EXISTS idx_restaurants_status;
CREATE INDEX idx_restaurants_status ON restaurants(status);

-- 3. Reservations table
CREATE TABLE IF NOT EXISTS reservations (
    id SERIAL PRIMARY KEY,
    restaurant_id INTEGER NOT NULL, -- Logical foreign key, references restaurants.id
    table_id INTEGER NOT NULL, -- Logical foreign key, references tables.id
    user_id INTEGER NOT NULL, -- Logical foreign key, references users.id
    review_id VARCHAR(64), -- Logical foreign key, references reviews.id
    reservation_time TIMESTAMP WITH TIME ZONE NOT NULL,
    reservation_date INTEGER NOT NULL, -- Reservation duration
    person_count INTEGER NOT NULL CHECK (person_count > 0), -- Number of people
    remarks TEXT, -- Reservation remarks
    status INTEGER NOT NULL DEFAULT 0, -- 0: Pending Confirmation, 1: Confirmed, 2: Cancelled, 3: Completed, 4: No-show
    cancel_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Reservations table trigger
DROP TRIGGER IF EXISTS update_reservations_updated_at ON reservations;
CREATE TRIGGER update_reservations_updated_at
BEFORE UPDATE ON reservations
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Reservations table indexes
DROP INDEX IF EXISTS idx_reservations_restaurant;
CREATE INDEX idx_reservations_restaurant ON reservations(restaurant_id);
DROP INDEX IF EXISTS idx_reservations_user;
CREATE INDEX idx_reservations_user ON reservations(user_id);
DROP INDEX IF EXISTS idx_reservations_time;
CREATE INDEX idx_reservations_time ON reservations(reservation_time);
DROP INDEX IF EXISTS idx_reservations_status;
CREATE INDEX idx_reservations_status ON reservations(status);

-- 4. Reviews table
CREATE TABLE IF NOT EXISTS reviews (
    id VARCHAR(64) PRIMARY KEY,
    restaurant_id INTEGER NOT NULL, -- Logical foreign key, references restaurants.id
    user_id INTEGER NOT NULL, -- Logical foreign key, references users.id
    content TEXT,
    photos TEXT, -- Image URLs separated by |
    videos TEXT, -- Video URLs separated by |
    rating INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Reviews table trigger
DROP TRIGGER IF EXISTS update_reviews_updated_at ON reviews;
CREATE TRIGGER update_reviews_updated_at
BEFORE UPDATE ON reviews
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Reviews table indexes
DROP INDEX IF EXISTS idx_reviews_restaurant;
CREATE INDEX idx_reviews_restaurant ON reviews(restaurant_id);
DROP INDEX IF EXISTS idx_reviews_user;
CREATE INDEX idx_reviews_user ON reviews(user_id);
DROP INDEX IF EXISTS idx_reviews_rating;
CREATE INDEX idx_reviews_rating ON reviews(rating);

-- 5. Favorites table
CREATE TABLE IF NOT EXISTS favorites (
    id SERIAL PRIMARY KEY,
    restaurant_id INTEGER NOT NULL, -- Logical foreign key, references restaurants.id
    user_id INTEGER NOT NULL, -- Logical foreign key, references users.id
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Favorites table indexes
DROP INDEX IF EXISTS idx_favorites_user;
CREATE INDEX idx_favorites_user ON favorites(user_id);
DROP INDEX IF EXISTS idx_favorites_restaurant;
CREATE INDEX idx_favorites_restaurant ON favorites(restaurant_id);
DROP INDEX IF EXISTS idx_favorites_restaurant_user;
CREATE UNIQUE INDEX idx_favorites_restaurant_user ON favorites(restaurant_id, user_id);

-- 6. Tables table
CREATE TABLE IF NOT EXISTS tables (
    id SERIAL PRIMARY KEY,
    restaurant_id INTEGER NOT NULL, -- Logical foreign key, references restaurants.id
    type VARCHAR(50) NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tables table trigger
DROP TRIGGER IF EXISTS update_tables_updated_at ON tables;
CREATE TRIGGER update_tables_updated_at
BEFORE UPDATE ON tables
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- Tables table indexes
DROP INDEX IF EXISTS idx_tables_restaurant;
CREATE INDEX idx_tables_restaurant ON tables(restaurant_id);
DROP INDEX IF EXISTS idx_tables_capacity;
CREATE INDEX idx_tables_capacity ON tables(capacity);

-- Add table comments
COMMENT ON TABLE users IS 'Users table';
COMMENT ON TABLE restaurants IS 'Restaurants table';
COMMENT ON TABLE reservations IS 'Reservations table';
COMMENT ON TABLE reviews IS 'Reviews table';
COMMENT ON TABLE favorites IS 'Favorites table';
COMMENT ON TABLE tables IS 'Tables table';

-- Users table column comments
COMMENT ON COLUMN users.id IS 'User ID, primary key';
COMMENT ON COLUMN users.name IS 'User name';
COMMENT ON COLUMN users.email IS 'User email, unique';
COMMENT ON COLUMN users.password IS 'User password hash';
COMMENT ON COLUMN users.role IS 'User role: 0: Administrator, 1: Restaurant Manager, 2: User, 3: Waiter';
COMMENT ON COLUMN users.avatar IS 'User avatar URL';
COMMENT ON COLUMN users.created_at IS 'Creation time';
COMMENT ON COLUMN users.updated_at IS 'Update time';
COMMENT ON COLUMN users.status IS 'User status: 0: Banned, 1: Enabled';

-- Restaurants table column comments
COMMENT ON COLUMN restaurants.id IS 'Restaurant ID, primary key';
COMMENT ON COLUMN restaurants.owner_id IS 'Owner ID, logical foreign key references users table';
COMMENT ON COLUMN restaurants.name IS 'Restaurant name';
COMMENT ON COLUMN restaurants.description IS 'Restaurant description';
COMMENT ON COLUMN restaurants.address IS 'Restaurant address';
COMMENT ON COLUMN restaurants.latitude IS 'Restaurant latitude';
COMMENT ON COLUMN restaurants.longitude IS 'Restaurant longitude';
COMMENT ON COLUMN restaurants.phone IS 'Restaurant phone';
COMMENT ON COLUMN restaurants.business_license IS 'Business license';
COMMENT ON COLUMN restaurants.photos IS 'Restaurant photos, URLs separated by |';
COMMENT ON COLUMN restaurants.status IS 'Restaurant status: 0: Pending Review, 1: Approved, 2: Closed, 3: Operating';
COMMENT ON COLUMN restaurants.business_hours IS 'Business hours';
COMMENT ON COLUMN restaurants.cuisine IS 'Cuisine type, e.g., Chinese, Western, Japanese, Korean';
COMMENT ON COLUMN restaurants.created_at IS 'Creation time';
COMMENT ON COLUMN restaurants.updated_at IS 'Update time';

-- Reservations table column comments
COMMENT ON COLUMN reservations.id IS 'Reservation ID, primary key';
COMMENT ON COLUMN reservations.restaurant_id IS 'Restaurant ID, logical foreign key references restaurants table';
COMMENT ON COLUMN reservations.user_id IS 'User ID, logical foreign key references users table';
COMMENT ON COLUMN reservations.reservation_time IS 'Reservation time';
COMMENT ON COLUMN reservations.reservation_date IS 'Reservation duration';
COMMENT ON COLUMN reservations.table_id IS 'Table ID, logical foreign key references tables table';
COMMENT ON COLUMN reservations.person_count IS 'Number of people';
COMMENT ON COLUMN reservations.remarks IS 'Reservation remarks';
COMMENT ON COLUMN reservations.status IS 'Reservation status: 0: Pending Confirmation, 1: Confirmed, 2: Cancelled, 3: Completed, 4: No-show';
COMMENT ON COLUMN reservations.cancel_reason IS 'Cancellation reason';
COMMENT ON COLUMN reservations.created_at IS 'Creation time';
COMMENT ON COLUMN reservations.updated_at IS 'Update time';

-- Reviews table column comments
COMMENT ON COLUMN reviews.id IS 'Review ID, primary key';
COMMENT ON COLUMN reviews.restaurant_id IS 'Restaurant ID, logical foreign key references restaurants table';
COMMENT ON COLUMN reviews.user_id IS 'User ID, logical foreign key references users table';
COMMENT ON COLUMN reviews.content IS 'Review content';
COMMENT ON COLUMN reviews.photos IS 'Review photos, URLs separated by |';
COMMENT ON COLUMN reviews.videos IS 'Review videos, URLs separated by |';
COMMENT ON COLUMN reviews.rating IS 'Rating, 1-5 points';
COMMENT ON COLUMN reviews.created_at IS 'Creation time';
COMMENT ON COLUMN reviews.updated_at IS 'Update time';

-- Favorites table column comments
COMMENT ON COLUMN favorites.id IS 'Favorite ID, primary key';
COMMENT ON COLUMN favorites.restaurant_id IS 'Restaurant ID, logical foreign key references restaurants table';
COMMENT ON COLUMN favorites.user_id IS 'User ID, logical foreign key references users table';
COMMENT ON COLUMN favorites.created_at IS 'Creation time';

-- Tables table column comments
COMMENT ON COLUMN tables.id IS 'Table ID, primary key';
COMMENT ON COLUMN tables.restaurant_id IS 'Restaurant ID, logical foreign key references restaurants table';
COMMENT ON COLUMN tables.type IS 'Table type';
COMMENT ON COLUMN tables.capacity IS 'Table capacity';
COMMENT ON COLUMN tables.created_at IS 'Creation time';
COMMENT ON COLUMN tables.updated_at IS 'Update time';

-- Insert test data
-- 1. Insert user data
INSERT INTO users (name, email, password, role, avatar, status) VALUES
('Admin', 'admin@example.com', '$2a$10$b2KG1Ug8MhMfC1OUeItwSuWsICsS.SlqcUfCN/txe/L1ZquExf6xm', 0, 'https://example.com/avatars/admin.jpg', 1),
('Manager Zhang', 'manager@example.com', '$2a$10$b2KG1Ug8MhMfC1OUeItwSuWsICsS.SlqcUfCN/txe/L1ZquExf6xm', 1, 'https://example.com/avatars/manager.jpg', 1),
('Lee', 'waiter@example.com', '$2a$10$b2KG1Ug8MhMfC1OUeItwSuWsICsS.SlqcUfCN/txe/L1ZquExf6xm', 3, 'https://example.com/avatars/waiter.jpg', 1),
('Chris Tao', 'user1@example.com', '$2a$10$b2KG1Ug8MhMfC1OUeItwSuWsICsS.SlqcUfCN/txe/L1ZquExf6xm', 2, 'https://example.com/avatars/user1.jpg', 1),
('Zoe', 'user3@example.com', '$2a$10$b2KG1Ug8MhMfC1OUeItwSuWsICsS.SlqcUfCN/txe/L1ZquExf6xm', 2, 'https://example.com/avatars/user3.jpg', 0);

-- 2. Insert restaurant data
INSERT INTO restaurants (owner_id, name, description, address, latitude, longitude, phone, business_license, photos, status, business_hours, cuisine) VALUES
(2, 'Seafood house', 'We provide a variety of fresh seafood ingredients, mainly Cantonese cuisine. We insist on purchasing fresh seafood every day to ensure that customers can enjoy the freshest seafood.', '1704 Washington St, Boston, MA 02118', 42.343308, -71.078953, '8561231234', 'BL123456789', 'https://example.com/restaurants/seafood1.jpg|https://example.com/restaurants/seafood2.jpg', 3, 'Monday to Friday 11:00-22:00| Saturday 10:00-23:00', 'Seafood'),
(2, 'Try Italian', 'Authentic Italian cuisine, imported ingredients. The chef studied in Milan, Italy for many years and brought back authentic Italian flavors. Pizza and pasta are made with imported ingredients.', '1700 Washington St, Boston, MA 02118', 42.3348, -71.0764, '8561111111', 'BL555666777', 'https://example.com/restaurants/italian1.jpg|https://example.com/restaurants/italian2.jpg', 0, 'Monday to Sunday 11:30-22:30', 'Italian');

-- 3. Insert table data
INSERT INTO tables (restaurant_id, type, capacity) VALUES
(1, 'Normal table', 4),
(1, 'Big table', 8),
(1, 'VIP rooms', 12),
(2, 'Normal table', 4),
(2, 'Normal table', 4),
(2, 'Big table', 10);


-- 5. Insert review data
INSERT INTO reviews (id, restaurant_id, user_id, content, photos, videos, rating) VALUES
(1,1, 4, 'The seafood is very fresh and the service is also very good! I especially recommend their steamed grouper and salt and pepper mantis shrimp. I will definitely come again next time.', 'https://example.com/reviews/seafood_review1.jpg|https://example.com/reviews/seafood_review2.jpg', 'https://example.com/reviews/seafood_video.mp4', 5),
(2,2, 4, 'The dishes are very authentic, but the price is a bit expensive. The roast duck is crispy and delicious, and the noodles with soybean paste are also authentic, but the per capita consumption is a bit high.', 'https://example.com/reviews/beijing_review.jpg', NULL, 4),
(3,2, 5, 'The environment is good and the service is average. The restaurant is decorated in the old Beijing style, but the waiters are indifferent and can be improved.', NULL, NULL, 3),
(4,1, 5, 'High cost performance, I will come again next time! I ordered three seafood dishes, all of which were very fresh and reasonably priced. I will bring my family to try it again on the weekend.', 'https://example.com/reviews/seafood_value.jpg', NULL, 5);


-- 6. Insert favorite data
INSERT INTO favorites (id, restaurant_id, user_id) VALUES
(1, 1, 4),
(2, 1, 5),
(3, 2, 4);

-- Query to verify test data
SELECT 'Users count:' as info, COUNT(*) FROM users;
SELECT 'Restaurants count:' as info, COUNT(*) FROM restaurants;
SELECT 'Tables count:' as info, COUNT(*) FROM tables;
SELECT 'Reservations count:' as info, COUNT(*) FROM reservations;
SELECT 'Reviews count:' as info, COUNT(*) FROM reviews;
SELECT 'Favorites count:' as info, COUNT(*) FROM favorites;

-- Create staff table
CREATE TABLE staff (
    id SERIAL PRIMARY KEY,
    manager_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    status INTEGER NOT NULL DEFAULT 0, -- 0: Pending Review, 1: Approved
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (manager_id) REFERENCES users(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create update timestamp trigger
CREATE OR REPLACE FUNCTION update_staff_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_staff_updated_at
    BEFORE UPDATE ON staff
    FOR EACH ROW
    EXECUTE FUNCTION update_staff_updated_at();

-- Add indexes
CREATE INDEX idx_staff_manager_id ON staff(manager_id);
CREATE INDEX idx_staff_user_id ON staff(user_id);
CREATE INDEX idx_staff_status ON staff(status);