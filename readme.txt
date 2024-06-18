
API:

Post method  /auth/register  creates an inactive user
POST: /auth/register
Payload Body:
      {"firstname":"","lastname":"","email":"","password":"", "role":"USER"}
      
      Where role can be USER, MANAGER, ADMIN
      
Response are: 1) JWT token  2) Refresh token



POST  method /auth/authenticateWithCredentials
payload: {
    "email": "",
    "password":""
    }
 Response are: 1) JWT token  2) Refresh token   
    

POST  method /auth/authenticateWithToken
payload: {
    "email": "",
    "password":""
    }
Response are: 1) JWT token  2) Refresh token


     