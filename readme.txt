
API:
Swagger: http://localhost:8080/auth/swagger-ui/index.html

Post method  /auth/register  creates an inactive user
POST: /auth/register
Payload Body:
      {"firstname":"","lastname":"","email":"","password":"", "role":"USER"}
      
      Where role can be USER, MANAGER, ADMIN
      
Response are: 1) JWT token  2) Refresh token



POST  method /auth/authenticate
payload: {
    "email": "",
    "password":""
    }
 Response are: 1) JWT token  2) Refresh token   
    

POST  method /auth/authenticateWithToken
payload: {
    "email": ""
    }
put the JWT token with authorization key in the request header
Response are: 1) JWT token  2) Refresh token


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
 ---------------
 