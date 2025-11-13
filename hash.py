import bcrypt

# Normal password
password = input("Admin@123").encode('utf-8')

# Generate a salt
salt = bcrypt.gensalt()

# Hash the password
hashed_password = bcrypt.hashpw(password, salt)

print("Bcrypt Hash:", hashed_password.decode('utf-8'))
