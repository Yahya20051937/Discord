-- Create databases if they don't already exist
CREATE DATABASE IF NOT EXISTS discord_role_db;
CREATE DATABASE IF NOT EXISTS discord_server_db;
CREATE DATABASE IF NOT EXISTS discord_room_db;
CREATE DATABASE IF NOT EXISTS discord_text_chatting_db;

-- Grant all privileges to 'yahya' user on each database
GRANT ALL PRIVILEGES ON discord_role_db.* TO 'yahya'@'%' IDENTIFIED BY 'Wydad3719' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON discord_room_db.* TO 'yahya'@'%' IDENTIFIED BY 'Wydad3719' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON discord_server_db.* TO 'yahya'@'%' IDENTIFIED BY 'Wydad3719' WITH GRANT OPTION;
GRANT ALL PRIVILEGES ON discord_text_chatting_db.* TO 'yahya'@'%' IDENTIFIED BY 'Wydad3719' WITH GRANT OPTION;

-- Apply the changes
FLUSH PRIVILEGES;

