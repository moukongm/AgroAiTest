chcp 65001 >nul
@echo off
setlocal enabledelayedexpansion

echo =============================================
echo    AgroAi Windows 开发环境检查与配置脚本
echo =============================================

:: ------------------------------------------------------------------
:: 1. 检查 Java 版本 (JDK 17)
:: ------------------------------------------------------------------
echo.
echo [1/4] 检查 Java 环境...

java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [警告] 未找到 Java。正在尝试自动寻找常见安装路径...
    
    :: 尝试自动寻找 JDK 17
    if exist "C:\Program Files\Java\jdk-17*" (
        for /d %%d in ("C:\Program Files\Java\jdk-17*") do (
            echo [提示] 发现 JDK 路径: %%d
            echo 正在设置 JAVA_HOME...
            setx JAVA_HOME "%%d"
            set "JAVA_HOME=%%d"
            set "PATH=%%d\bin;%PATH%"
        )
    )
)

:: 二次检查
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [错误] 仍未找到 Java。正在尝试自动下载 JDK 17 (Microsoft OpenJDK)...
    
    :: 使用 PowerShell 下载并解压 JDK 17
    powershell -Command "Invoke-WebRequest -Uri 'https://aka.ms/download-jdk/microsoft-jdk-17-windows-x64.zip' -OutFile 'jdk17.zip'"
    if exist jdk17.zip (
        echo 下载完成，正在解压...
        powershell -Command "Expand-Archive -Path 'jdk17.zip' -DestinationPath 'C:\Java'"
        del jdk17.zip
        
        :: 找到解压后的目录并设置环境变量
        for /d %%d in ("C:\Java\jdk-17*") do (
            echo [成功] JDK 已安装至 %%d
            setx JAVA_HOME "%%d"
            set "JAVA_HOME=%%d"
            set "PATH=%%d\bin;%PATH%"
        )
    ) else (
        echo [失败] 自动下载失败。请手动安装 JDK 17。
        pause
        exit /b 1
    )
)

for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set "JAVA_VER=%%g"
)
set JAVA_VER=!JAVA_VER:"=!
echo 当前 Java 版本: !JAVA_VER!

echo !JAVA_VER! | findstr "17." >nul
if %errorlevel% equ 0 (
    echo [成功] Java 版本符合要求 (JDK 17)。
) else (
    echo [警告] 当前 Java 版本可能不兼容。推荐使用 JDK 17。
)

:: ------------------------------------------------------------------
:: 2. 检查 Android SDK 环境
:: ------------------------------------------------------------------
echo.
echo [2/4] 检查 Android SDK 环境...

:: 尝试自动寻找 Android SDK
if "%ANDROID_HOME%"=="" (
    set "POSSIBLE_SDK=%LOCALAPPDATA%\Android\Sdk"
    if exist "!POSSIBLE_SDK!" (
        echo [提示] 发现 Android SDK 路径: !POSSIBLE_SDK!
        echo 正在设置 ANDROID_HOME...
        setx ANDROID_HOME "!POSSIBLE_SDK!"
        set "ANDROID_HOME=!POSSIBLE_SDK!"
    )
)

if "%ANDROID_HOME%"=="" (
    echo [警告] 未设置 ANDROID_HOME 环境变量。
    echo 请在系统环境变量中配置 ANDROID_HOME，通常路径为:
    echo C:\Users\%USERNAME%\AppData\Local\Android\Sdk
) else (
    echo ANDROID_HOME: %ANDROID_HOME%
    if exist "%ANDROID_HOME%" (
        echo [成功] Android SDK 路径有效。
    ) else (
        echo [错误] Android SDK 路径不存在，请检查配置。
    )
)

:: ------------------------------------------------------------------
:: 3. 检查并配置 Git Hooks
:: ------------------------------------------------------------------
echo.
echo [3/4] 检查 Git Hooks 配置...

for /f "delims=" %%i in ('git config core.hooksPath') do set CURRENT_HOOKS=%%i
set EXPECTED_HOOKS=.githooks

if "%CURRENT_HOOKS%"=="%EXPECTED_HOOKS%" (
    echo [成功] Git Hooks 已正确配置 (%CURRENT_HOOKS%)。
) else (
    echo 当前 Git Hooks 路径: %CURRENT_HOOKS%
    echo 正在自动配置 Git Hooks...
    git config core.hooksPath .githooks
    
    :: Windows 不需要 chmod，但需要确保 shell 脚本格式正确
    echo [提示] 请确保您的 git bash 环境能正确运行 .githooks/pre-push
    
    for /f "delims=" %%i in ('git config core.hooksPath') do set NEW_HOOKS=%%i
    if "!NEW_HOOKS!"=="%EXPECTED_HOOKS%" (
        echo [成功] Git Hooks 已自动配置完成。
    ) else (
        echo [错误] Git Hooks 配置失败，请手动执行: git config core.hooksPath .githooks
    )
)

:: ------------------------------------------------------------------
:: 4. 检查 local.properties
:: ------------------------------------------------------------------
echo.
echo [4/4] 检查 local.properties...

if exist local.properties (
    echo [成功] local.properties 已存在。
) else (
    echo [提示] local.properties 不存在，正在尝试自动生成...
    if not "%ANDROID_HOME%"=="" (
        :: 注意 Windows 路径转义，这里直接写入原始路径，Gradle 通常能识别
        echo sdk.dir=%ANDROID_HOME:\=\\%> local.properties
        echo [成功] 已根据 ANDROID_HOME 生成 local.properties。
    ) else (
        echo [失败] 无法生成 local.properties，因为未设置 ANDROID_HOME。
        echo 请使用 Android Studio 打开项目以自动生成该文件。
    )
)

echo.
echo =============================================
echo    环境检查完成！按任意键退出...
echo =============================================
pause