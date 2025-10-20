
Post method  /auth/register  creates an inactive user
POST: /auth/register
Payload Body:
      {"firstname":"","lastname":"","email":"","password":"", "role":"USER"}
      
      Where role can be USER, MANAGER, ADMIN
      
Response are: 1) JWT token  2) Refresh token

----------------------

POST  method /auth/authenticate
payload: {
    "email": "",
    "password":""
    }
 Response are: 1) JWT token  2) Refresh token   
    
-----------------
POST  method /auth/authenticateWithToken
payload: {
    "email": ""
    }
put the JWT token with authorization key in the request header
Response are: 1) JWT token  2) Refresh token

-------------------
/auth/changePasswordByUser
PayloadBody:
{
    "userEmail": "",
    "currentPassword": "",
    "newPassword": "",
    "confirmationPassword": ""
}

No Authorization Token

----------------
/auth/changePasswordByAdmin
PayloadBody:
{
    "userEmail": "",
    "newPassword": "",
    "confirmationPassword": ""
}

No Authorization Token
---------------------------
/auth/deleteByUser
{
    "userEmail": "",
    "password": ""
}

-------------
/auth/deleteByAdmin
{
    "userEmail": ""
}
 Admin Authorization Token    

 
 -----  For RSA Keys (MAC) -------
 mkdir -p ~/.inrik/keys && cd ~/.inrik/keys

# 1) Raw PKCS#1 private key
openssl genrsa -out jwt.key 4096

# 2) Convert to PKCS#8 (Java-friendly)
openssl pkcs8 -topk8 -inform PEM -in jwt.key -out jwt-private.pem -nocrypt

# 3) Export public key
openssl rsa -in jwt.key -pubout -out jwt-public.pem

# 4) Lock down permissions
chmod 600 jwt.key jwt-private.pem jwt-public.pem

# (Optional) Remove the original PKCS#1 key to reduce clutter
rm jwt.key
 
 --- Kill Java processes
 sudo lsof -iTCP:8081 -sTCP:LISTEN -n -P
 pgrep -lf java 
 jps -lv 
kill <PID>
kill -9 <PID> 
pkill -f 'java.*YourMainClass'
# or
killall java
lsof -tiTCP:8081 -sTCP:LISTEN

kill $(lsof -tiTCP:8081 -sTCP:LISTEN)


kill -9 $(lsof -tiTCP:8081 -sTCP:LISTEN)

 
 -----------