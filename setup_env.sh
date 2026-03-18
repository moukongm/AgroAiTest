#!/bin/bash

# AgroAi 项目环境自动配置脚本 (Linux/macOS)
# 功能：
# 1. 检查 JDK 版本是否符合要求 (JDK 17)
# 2. 检查 Android SDK 环境变量 (ANDROID_HOME)
# 3. 检查 Git Hooks 配置
# 4. 检查 Gradle 配置文件

# 检测操作系统
OS="$(uname)"
case $OS in
  'Linux')
    OS='Linux'
    ;;
  'Darwin') 
    OS='Mac'
    ;;
  *)
    echo "不支持的操作系统: $OS。如果是 Windows，请运行 setup_env.bat"
    exit 1
    ;;
esac

echo "============================================="
echo "   AgroAi 开发环境检查与配置脚本 ($OS)"
echo "============================================="

# ------------------------------------------------------------------
# 1. 检查 Java 版本 (JDK 17)
# ------------------------------------------------------------------
echo -e "\n[1/4] 检查 Java 环境..."

# 函数: 安装 SDKMAN
install_sdkman() {
    echo "正在安装 SDKMAN..."
    curl -s "https://get.sdkman.io" | bash
    source "$HOME/.sdkman/bin/sdkman-init.sh"
}

# 检查是否安装了 SDKMAN
if [[ ! -s "$HOME/.sdkman/bin/sdkman-init.sh" ]]; then
    echo -e "\033[33m[提示] 未检测到 SDKMAN，正在为您自动安装...\033[0m"
    install_sdkman
else
    source "$HOME/.sdkman/bin/sdkman-init.sh"
fi

# 检查 Java 版本
check_java_version() {
    if type -p java > /dev/null; then
        _java=java
    elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]]; then
        _java="$JAVA_HOME/bin/java"
    else
        return 1
    fi

    version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo "当前 Java 版本: $version"
    
    if [[ "$version" == "17"* ]]; then
        return 0
    else
        return 1
    fi
}

if check_java_version; then
    echo -e "\033[32m[成功] Java 版本符合要求 (JDK 17)。\033[0m"
else
    echo -e "\033[33m[警告] Java 版本不符合要求或未安装，正在尝试自动安装 JDK 17...\033[0m"
    # 使用 SDKMAN 安装 JDK 17 (这里使用 Liberica JDK，也可以换成其他发行版)
    sdk install java 17.0.10-librc
    
    # 再次检查
    if check_java_version; then
        echo -e "\033[32m[成功] JDK 17 已安装并配置成功。\033[0m"
    else
        echo -e "\033[31m[失败] JDK 17 安装失败，请手动检查 SDKMAN 配置。\033[0m"
        exit 1
    fi
fi

# ------------------------------------------------------------------
# 2. 检查 Android SDK 环境
# ------------------------------------------------------------------
echo -e "\n[2/4] 检查 Android SDK 环境..."

# 尝试自动定位 Android SDK (针对 macOS)
if [[ -z "$ANDROID_HOME" && "$OS" == "Mac" ]]; then
    possible_path="$HOME/Library/Android/sdk"
    if [ -d "$possible_path" ]; then
        echo -e "\033[33m[提示] 发现 Android SDK 路径: $possible_path，正在自动配置...\033[0m"
        export ANDROID_HOME="$possible_path"
        # 尝试写入 Shell 配置文件
        if [[ "$SHELL" == *"zsh"* ]]; then
            echo "export ANDROID_HOME=$possible_path" >> ~/.zshrc
        elif [[ "$SHELL" == *"bash"* ]]; then
            echo "export ANDROID_HOME=$possible_path" >> ~/.bash_profile
        fi
    fi
fi

if [[ -z "$ANDROID_HOME" ]]; then
    echo -e "\033[31m[错误] 未找到 Android SDK。请先安装 Android Studio 并下载 SDK。\033[0m"
    echo "下载地址: https://developer.android.com/studio"
    # 注意：Android SDK 比较大且涉及 GUI 安装，通常不建议脚本静默自动下载，引导用户去官网安装更稳妥
else
    echo "ANDROID_HOME: $ANDROID_HOME"
    
    # 检查 cmdline-tools
    cmdline_tools="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
    if [ ! -f "$cmdline_tools" ]; then
        # 尝试寻找旧版路径
        cmdline_tools="$ANDROID_HOME/tools/bin/sdkmanager"
    fi

    if [ -f "$cmdline_tools" ]; then
        echo -e "\033[32m[成功] Android SDK 及命令行工具已就绪。\033[0m"
        # 这里可以添加自动接受 License 的逻辑
        # yes | "$cmdline_tools" --licenses > /dev/null 2>&1
    else
        echo -e "\033[33m[警告] 未找到 sdkmanager，可能无法自动更新 SDK 组件。\033[0m"
        echo "建议在 Android Studio 中安装 'Android SDK Command-line Tools'。"
        if [[ "$OS" == "Linux" ]]; then
             echo "或者手动安装: sudo apt install android-sdk"
        fi
    fi
fi

# ------------------------------------------------------------------
# 3. 检查并配置 Git Hooks
# ------------------------------------------------------------------
echo -e "\n[3/4] 检查 Git Hooks 配置..."
current_hooks_path=$(git config core.hooksPath)
expected_hooks_path=".githooks"

if [[ "$current_hooks_path" == "$expected_hooks_path" ]]; then
    echo -e "\033[32m[成功] Git Hooks 已正确配置 ($current_hooks_path)。\033[0m"
else
    echo "当前 Git Hooks 路径: ${current_hooks_path:-默认}"
    echo "正在自动配置 Git Hooks..."
    git config core.hooksPath .githooks
    chmod +x .githooks/pre-push
    
    # 二次检查
    if [[ "$(git config core.hooksPath)" == "$expected_hooks_path" ]]; then
        echo -e "\033[32m[成功] Git Hooks 已自动配置完成。\033[0m"
    else
        echo -e "\033[31m[错误] Git Hooks 配置失败，请手动执行: git config core.hooksPath .githooks\033[0m"
    fi
fi

# ------------------------------------------------------------------
# 4. 检查 local.properties
# ------------------------------------------------------------------
echo -e "\n[4/4] 检查 local.properties..."
if [ ! -f "local.properties" ]; then
    echo -e "\033[33m[提示] local.properties 不存在，正在尝试自动生成...\033[0m"
    if [[ -n "$ANDROID_HOME" ]]; then
        echo "sdk.dir=$ANDROID_HOME" > local.properties
        echo -e "\033[32m[成功] 已根据 ANDROID_HOME 生成 local.properties。\033[0m"
    else
        echo -e "\033[31m[失败] 无法生成 local.properties，因为未设置 ANDROID_HOME。\033[0m"
        echo "请使用 Android Studio 打开项目以自动生成该文件。"
    fi
else
    echo -e "\033[32m[成功] local.properties 已存在。\033[0m"
fi

echo -e "\n============================================="
echo -e "   环境检查完成！"
echo -e "============================================="