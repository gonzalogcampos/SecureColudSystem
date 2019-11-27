# SCS - Secure Cloud Server
Compresion y seguridad 18/19

## Resume
  This is a simple server-client program example for security in java.
  It has a SQL database, and it can Signup users, modify it, and delete it. And the users can uploadfiles, witch will be crypted on client. The files can be download, delete, and share with other users.
  
## Security tools & protocols
  It uses AES, RSA and SHA for all the security.
  First conection creates a safe comunication between server and client.
  Paswords are hashed, salted an saved on the database. It also creates a pair of keys, and the private key is crypted with the password.
  Files are crypted with a random key, and it is saved on the database crypred with the public key for each user.
  
## Test
  This program has been tested with xammp in localhost.
  
 (C) Gonzalo G. Campos, 2019
