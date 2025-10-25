-- Insert default admin user
-- Default password: Admin@123 (will be hashed by the application)
-- Note: In a production environment, this should be done through a secure setup process

-- Create a function to hash passwords (will be used by the application)
CREATE OR REPLACE FUNCTION hash_password()
RETURNS TRIGGER AS $$
BEGIN
    -- This is just a placeholder. In reality, password hashing should be done in the application
    -- using a proper password hashing algorithm like bcrypt
    IF NEW.password IS NOT NULL AND NEW.password NOT LIKE '$2a$%' THEN
        NEW.password = crypt(NEW.password, gen_salt('bf'));
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create a trigger to hash passwords before insert or update
CREATE TRIGGER hash_user_password
BEFORE INSERT OR UPDATE OF password ON users
FOR EACH ROW
EXECUTE FUNCTION hash_password();

-- Insert the default admin user
-- Note: The password will be hashed by the trigger
INSERT INTO users (
    first_name,
    last_name,
    email,
    password,
    role,
    is_email_verified,
    is_active,
    created_by
) VALUES (
    'Admin',
    'User',
    'admin@elevate.com',
    'Admin@123', -- This will be hashed by the trigger
    'ADMIN'::role_enum,
    true,
    true,
    'system'
) ON CONFLICT (email) DO NOTHING;

-- Create a function to generate a random password
CREATE OR REPLACE FUNCTION generate_random_password(length INTEGER)
RETURNS TEXT AS $$
DECLARE
    chars TEXT := 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-=[]{}|;:,.<>?';
    result TEXT := '';
    i INTEGER := 0;
BEGIN
    FOR i IN 1..length LOOP
        result := result || substr(chars, floor(random() * length(chars) + 1)::INTEGER, 1);
    END LOOP;
    RETURN result;
END;
$$ LANGUAGE plpgsql;
