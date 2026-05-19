-- Fix: make guest_id nullable in bills table for walk-in dine-in customers
ALTER TABLE IF EXISTS bills ALTER COLUMN guest_id DROP NOT NULL;

-- Room Types
INSERT INTO room_types (type_name, base_price, max_occupancy) VALUES
('Standard', 2000.00, 2),
('Deluxe', 3500.00, 2),
('Suite', 6500.00, 4)
ON CONFLICT (type_name) DO NOTHING;

-- Sample Rooms
INSERT INTO rooms (room_number, room_type_id, floor, status) VALUES
('101', 1, 1, 'AVAILABLE'),
('102', 1, 1, 'AVAILABLE'),
('201', 2, 2, 'AVAILABLE'),
('202', 2, 2, 'AVAILABLE'),
('301', 3, 3, 'AVAILABLE')
ON CONFLICT (room_number) DO NOTHING;

-- Menu Categories
-- (Skipping separate menu_categories table as MenuItem entity uses String categoryName)

-- Sample Menu Items
INSERT INTO menu_items (category_name, item_name, price, is_veg, is_available, prep_time_mins) VALUES
('Breakfast', 'Masala Omelette', 120.00, false, true, 10),
('Breakfast', 'Poha', 80.00, true, true, 8),
('Mains', 'Butter Chicken', 280.00, false, true, 20),
('Mains', 'Paneer Tikka Masala', 240.00, true, true, 18),
('Mains', 'Dal Makhani', 200.00, true, true, 15),
('Beverages', 'Mango Lassi', 120.00, true, true, 5),
('Beverages', 'Fresh Lime Soda', 80.00, true, true, 3),
('Desserts', 'Gulab Jamun', 100.00, true, true, 5)
ON CONFLICT DO NOTHING;

-- Restaurant Tables
INSERT INTO restaurant_tables (table_number, section, capacity, status) VALUES
('T1', 'Indoor', 4, 'AVAILABLE'),
('T2', 'Indoor', 2, 'AVAILABLE'),
('T3', 'Outdoor', 4, 'AVAILABLE'),
('T4', 'Outdoor', 6, 'AVAILABLE'),
('T5', 'Private', 8, 'AVAILABLE')
ON CONFLICT (table_number) DO NOTHING;

-- Admin Staff (password: admin123 → BCrypt hash)
INSERT INTO staff (full_name, email, password_hash, role, is_active, created_at) VALUES
('Admin User', 'admin@lrms.com', '$2a$10$ehzXmmiCKCeB/AzEOpXrI.AImDHr7JuES4QCoDZ5eGxQl71PIgVZy', 'ADMIN', true, NOW())
ON CONFLICT (email) DO UPDATE SET password_hash = EXCLUDED.password_hash;


