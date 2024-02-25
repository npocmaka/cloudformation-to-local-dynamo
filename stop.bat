@echo off

for /f "tokens=2 delims=:, " %%a in ('type dynamo.env ^| find """PORT"""') do (
	set "PORT=%%~a"
)

for /f "tokens=2 delims=:, " %%a in ('type dynamo.env ^| find """PID"""') do (
	set "PID=%%~a"
)

for /f "tokens=5 delims= " %%a in ('netstat /o /a ^| find "%PORT%"') do (
	set "FPID=%%~a"
)

if "%PID%" EQU "%FPID%" (
	TSKILL %PID%
)