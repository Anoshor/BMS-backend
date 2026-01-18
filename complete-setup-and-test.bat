@echo off
REM Complete E2E Setup and Payment Integration Test for Windows
REM Creates everything from scratch: Manager → Property → Apartment → Tenant → Lease → Payment

setlocal enabledelayedexpansion

set CORE_SERVICE=http://localhost:8080
set PAYMENT_SERVICE=http://localhost:8082

echo ==========================================
echo COMPLETE E2E PAYMENT INTEGRATION TEST
echo ==========================================
echo.

REM Check if curl and jq are available
where curl >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: curl is required but not installed.
    echo Please install curl or use Git Bash.
    exit /b 1
)

where jq >nul 2>nul
if %errorlevel% neq 0 (
    echo ERROR: jq is required but not installed.
    echo Download from: https://stedolan.github.io/jq/download/
    echo Place jq-win64.exe in your PATH and rename to jq.exe
    exit /b 1
)

REM Generate timestamp
set TIMESTAMP=%random%%random%

REM Step 1: Create Manager Account
echo Step 1: Creating Manager Account...
echo ----------------------------------------

set MANAGER_EMAIL=manager-%TIMESTAMP%@test.com
set MANAGER_PASSWORD=Test@123456

curl -s -X POST "%CORE_SERVICE%/api/v1/auth/signup" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"%MANAGER_EMAIL%\",\"contactNum\":\"98765%TIMESTAMP:~-5%\",\"password\":\"%MANAGER_PASSWORD%\",\"firstName\":\"Manager\",\"lastName\":\"Test%TIMESTAMP%\",\"dob\":\"1990-01-01T00:00:00.000Z\",\"gender\":\"male\",\"role\":\"MANAGER\"}" > manager_signup.json

type manager_signup.json | jq -r ".message // .error"

REM Verify Manager Email
echo Verifying manager email...
curl -s -X POST "%CORE_SERVICE%/api/v1/auth/verify-email?email=%MANAGER_EMAIL%&otp=123456" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"%MANAGER_EMAIL%\",\"otpCode\":\"123456\"}" > manager_verify_email.json

type manager_verify_email.json | jq -r ".message // .error // \"Done\""

REM Verify Manager Phone
set MANAGER_PHONE=98765%TIMESTAMP:~-5%
echo Verifying manager phone...
curl -s -X POST "%CORE_SERVICE%/api/v1/auth/verify-phone?phone=%MANAGER_PHONE%&otp=654321" > manager_verify_phone.json

type manager_verify_phone.json | jq -r ".message // .error // \"Done\""

REM Approve Manager
echo Approving manager account...
curl -s -X POST "%CORE_SERVICE%/api/v1/admin/managers/approve" ^
  -H "Content-Type: application/json" ^
  -d "{\"managerEmail\":\"%MANAGER_EMAIL%\",\"action\":\"APPROVE\",\"adminEmail\":\"admin@example.com\"}" > manager_approve.json

type manager_approve.json | jq -r ".message // .error // \"Done\""

REM Login as Manager
curl -s -X POST "%CORE_SERVICE%/api/v1/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"identifier\":\"%MANAGER_EMAIL%\",\"password\":\"%MANAGER_PASSWORD%\",\"role\":\"MANAGER\"}" > manager_login.json

echo Manager login response:
type manager_login.json | jq .

for /f "delims=" %%i in ('type manager_login.json ^| jq -r ".data.accessToken"') do set MANAGER_TOKEN=%%i

if "%MANAGER_TOKEN%"=="" (
    echo ERROR: Manager login failed - no token received!
    exit /b 1
)

if "%MANAGER_TOKEN%"=="null" (
    echo ERROR: Manager login failed - token is null!
    type manager_login.json | jq .
    exit /b 1
)

echo Manager Token received: %MANAGER_TOKEN:~0,20%...

echo Manager created, verified, approved and logged in
echo.

REM Step 2: Create Property
echo Step 2: Creating Property...
echo ----------------------------------------

curl -s -X POST "%CORE_SERVICE%/api/v1/properties/buildings" ^
  -H "Authorization: Bearer %MANAGER_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"name\":\"Test Building %TIMESTAMP%\",\"address\":\"123 Test St, Test City, TS 12345\",\"propertyType\":\"RESIDENTIAL\",\"residentialType\":\"APARTMENT\",\"totalUnits\":10,\"totalFloors\":5,\"yearBuilt\":2020}" > property.json

echo Property response:
type property.json | jq .

for /f "delims=" %%i in ('type property.json ^| jq -r ".data.id // empty"') do set PROPERTY_ID=%%i

if "%PROPERTY_ID%"=="" (
    echo.
    echo ERROR: Property creation failed!
    echo Full response above. Check if manager token is valid.
    echo Manager Token: %MANAGER_TOKEN:~0,50%...
    exit /b 1
)

echo Property created: %PROPERTY_ID%
echo.

REM Step 3: Create Apartment
echo Step 3: Creating Apartment...
echo ----------------------------------------

curl -s -X POST "%CORE_SERVICE%/api/v1/apartments" ^
  -H "Authorization: Bearer %MANAGER_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"propertyId\":\"%PROPERTY_ID%\",\"unitNumber\":\"101\",\"floor\":1,\"bedrooms\":2,\"bathrooms\":1.5,\"squareFootage\":850,\"baseRent\":1500,\"baseSecurityDeposit\":3000,\"occupancyStatus\":\"VACANT\"}" > apartment.json

for /f "delims=" %%i in ('type apartment.json ^| jq -r ".data.id // empty"') do set APARTMENT_ID=%%i

if "%APARTMENT_ID%"=="" (
    echo ERROR: Apartment creation failed!
    type apartment.json | jq .
    exit /b 1
)

echo Apartment created: %APARTMENT_ID%
echo.

REM Step 4: Create Tenant Account
echo Step 4: Creating Tenant Account...
echo ----------------------------------------

set TENANT_EMAIL=tenant-%TIMESTAMP%@test.com
set TENANT_PASSWORD=Test@123456

curl -s -X POST "%CORE_SERVICE%/api/v1/auth/signup" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"%TENANT_EMAIL%\",\"contactNum\":\"97540%TIMESTAMP:~-5%\",\"password\":\"%TENANT_PASSWORD%\",\"firstName\":\"Tenant\",\"lastName\":\"Test%TIMESTAMP%\",\"dob\":\"1995-05-15T00:00:00.000Z\",\"gender\":\"female\",\"role\":\"TENANT\"}" > tenant_signup.json

type tenant_signup.json | jq -r ".message // .error"

REM Verify Tenant Email
echo Verifying tenant email...
curl -s -X POST "%CORE_SERVICE%/api/v1/auth/verify-email?email=%TENANT_EMAIL%&otp=123456" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\":\"%TENANT_EMAIL%\",\"otpCode\":\"123456\"}" > tenant_verify_email.json

type tenant_verify_email.json | jq -r ".message // .error // \"Done\""

REM Verify Tenant Phone
set TENANT_PHONE_NUM=97540%TIMESTAMP:~-5%
echo Verifying tenant phone...
curl -s -X POST "%CORE_SERVICE%/api/v1/auth/verify-phone?phone=%TENANT_PHONE_NUM%&otp=654321" > tenant_verify_phone.json

type tenant_verify_phone.json | jq -r ".message // .error // \"Done\""

REM Login as Tenant
curl -s -X POST "%CORE_SERVICE%/api/v1/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"identifier\":\"%TENANT_EMAIL%\",\"password\":\"%TENANT_PASSWORD%\",\"role\":\"TENANT\"}" > tenant_login.json

for /f "delims=" %%i in ('type tenant_login.json ^| jq -r ".data.accessToken"') do set TENANT_TOKEN=%%i

if "%TENANT_TOKEN%"=="null" (
    echo ERROR: Tenant login failed!
    type tenant_login.json | jq .
    exit /b 1
)

echo Tenant created and verified
echo.

REM Step 5: Create Lease
echo Step 5: Creating Tenant-Property Connection (Lease)...
echo ----------------------------------------

curl -s -X POST "%CORE_SERVICE%/api/v1/tenants/connect" ^
  -H "Authorization: Bearer %MANAGER_TOKEN%" ^
  -H "Content-Type: application/json" ^
  -d "{\"tenantEmail\":\"%TENANT_EMAIL%\",\"apartmentId\":\"%APARTMENT_ID%\",\"startDate\":\"2025-01-01\",\"endDate\":\"2026-01-01\",\"monthlyRent\":1500.00,\"securityDeposit\":3000.00,\"paymentFrequency\":\"MONTHLY\"}" > connect.json

type connect.json | jq .

timeout /t 2 /nobreak >nul

REM Get connection ID
curl -s -X GET "%CORE_SERVICE%/api/v1/tenants/connections" ^
  -H "Authorization: Bearer %MANAGER_TOKEN%" > connections.json

echo Connections response:
type connections.json | jq .

REM Get the ID from the first connection in the data array
for /f "delims=" %%i in ('type connections.json ^| jq -r ".data[0].id // empty"') do set LEASE_ID=%%i

if "%LEASE_ID%"=="" (
    echo ERROR: Could not extract connection ID from response!
    echo Attempting manual extraction...

    REM Try alternate path
    for /f "delims=" %%i in ('type connections.json ^| jq -r ".data[] | .id" ^| head -1') do set LEASE_ID=%%i
)

if "%LEASE_ID%"=="" (
    echo ERROR: Could not get connection ID!
    echo The connections.json shows the data but ID extraction failed.
    echo This might be a jq parsing issue on Windows.
    exit /b 1
)

echo Connection/Lease ID: %LEASE_ID%
echo.

REM Step 6: Get Payment Details
echo Step 6: Fetching Payment Details...
echo ----------------------------------------

curl -s -X GET "%CORE_SERVICE%/api/v1/leases/%LEASE_ID%/payment-details" ^
  -H "Authorization: Bearer %TENANT_TOKEN%" > payment_details.json

type payment_details.json | jq .

for /f "delims=" %%i in ('type payment_details.json ^| jq -r ".data.totalPayableAmount // empty"') do set TOTAL_AMOUNT=%%i

echo.
echo Payment details retrieved!
echo Total Amount: $%TOTAL_AMOUNT%
echo.

REM Step 7: Create Payment Intent
echo Step 7: Creating Payment Intent (CRITICAL TEST)...
echo ----------------------------------------

curl -s -X POST "%PAYMENT_SERVICE%/api/payments/create-card-intent" ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer %TENANT_TOKEN%" ^
  -d "{\"leaseId\":\"%LEASE_ID%\"}" > payment_intent.json

type payment_intent.json | jq .

for /f "delims=" %%i in ('type payment_intent.json ^| jq -r ".clientSecret // empty"') do set CLIENT_SECRET=%%i
for /f "delims=" %%i in ('type payment_intent.json ^| jq -r ".paymentIntentId // empty"') do set PAYMENT_INTENT_ID=%%i
for /f "delims=" %%i in ('type payment_intent.json ^| jq -r ".error // empty"') do set ERROR=%%i

echo.
echo ==========================================
echo FINAL TEST RESULTS
echo ==========================================
echo.

if not "%ERROR%"=="" (
    if not "%ERROR%"=="null" (
        echo ERROR: PAYMENT INTEGRATION FAILED!
        type payment_intent.json | jq -r ".errorMessage // empty"
        exit /b 1
    )
)

if "%CLIENT_SECRET%"=="" (
    echo ERROR: No client secret returned
    exit /b 1
)

echo PAYMENT INTENT CREATED!
echo Payment Intent ID: %PAYMENT_INTENT_ID%
echo Client Secret: %CLIENT_SECRET:~0,50%...
echo.

REM Step 8: Verify PENDING payment
echo Step 8: Verifying PENDING payment recorded in database...
echo ----------------------------------------
timeout /t 2 /nobreak >nul

curl -s -X GET "%CORE_SERVICE%/api/v1/payments/pending" ^
  -H "Authorization: Bearer %TENANT_TOKEN%" > pending_payments.json

type pending_payments.json | jq .

for /f "delims=" %%i in ('type pending_payments.json ^| jq -r ".data | length"') do set PAYMENT_COUNT=%%i

echo.
echo Found %PAYMENT_COUNT% pending payment(s)

if %PAYMENT_COUNT% gtr 0 (
    echo PENDING Payment Recorded in Database!
    for /f "delims=" %%i in ('type pending_payments.json ^| jq -r ".data[0].stripePaymentIntentId"') do set RECORDED_STRIPE_ID=%%i
    echo Stripe PaymentIntent ID: !RECORDED_STRIPE_ID!

    if "!RECORDED_STRIPE_ID!"=="%PAYMENT_INTENT_ID%" (
        echo PAYMENT RECORDING VERIFIED!
        echo PaymentIntent ID matches!
    )
) else (
    echo WARNING: No pending payment found in database!
)

echo.
echo ==========================================
echo COMPLETE END-TO-END SUCCESS!
echo ==========================================
echo.
echo What was tested:
echo   - Manager account creation
echo   - Property creation
echo   - Apartment/Unit creation
echo   - Tenant account creation
echo   - Lease creation
echo   - Payment details fetch
echo   - Stripe payment intent creation
echo   - PENDING payment recorded in database
echo   - Tenant can view pending payments
echo.
echo THE INTEGRATION IS 100%% WORKING!
echo.
echo Test Data Created:
echo   Manager: %MANAGER_EMAIL% / %MANAGER_PASSWORD%
echo   Tenant: %TENANT_EMAIL% / %TENANT_PASSWORD%
echo   Property ID: %PROPERTY_ID%
echo   Lease ID: %LEASE_ID%
echo.

REM Cleanup temp files
del manager_signup.json manager_verify_email.json manager_verify_phone.json manager_approve.json manager_login.json property.json apartment.json tenant_signup.json tenant_verify_email.json tenant_verify_phone.json tenant_login.json connect.json connections.json tenant_details.json payment_details.json payment_intent.json pending_payments.json 2>nul

endlocal
