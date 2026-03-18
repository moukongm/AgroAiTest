# Git 团队协作流程与规范

为了保证代码的稳定性和团队协作的效率，我们采用标准的分支管理策略。所有团队成员必须严格遵守以下规范。

## 1. 分支模型

项目主要包含以下两类分支：

### 1.1 长期分支 (Protected)

- **`master`** **(主分支)**
  - 作用：用于发布生产环境（线上）代码。
  - **红线行为**：**绝对禁止**任何开发人员直接在 `master` 分支上进行 `commit` 或 `push`！
  - 合并方式：只能通过 Merge Request (MR) / Pull Request (PR) 从开发分支合并。

### 1.2 短期分支

- **`feature/xxx`** **(功能分支)**
  - 作用：开发新需求。
  - 命名规范：`feature/你的名字-功能名`（如：`feature/zhangsan-login`）。
  - 来源：基于 `develop` 分支拉取。
  - 归宿：开发完成后合并回 `develop` 分支。
- **`bugfix/xxx`** **或** **`hotfix/xxx`** **(修复分支)**
  - 作用：修复测试环境或线上的 Bug。

***

## 2. 标准开发流程

### Step 1: 同步最新代码并创建分支

每天开始开发前，或者开发新功能前，先切到 **`master`** 分支并拉取最新代码：

```bash
git checkout master
git pull origin master
```

基于最新的 `develop` 分支，创建并切换到你的功能分支：

```bash
git checkout -b feature/zhangsan-community
```

### Step 2: 提交代码

在本地开发并测试通过后，将代码提交到本地仓库。

```bash
git add .
git commit -m "feat: 新增了社区列表展示功能"
```

**Commit Message 规范（非常重要）：**
每次提交必须带上明确的前缀，以便生成 ChangeLog 和回溯：

- `feat:`：新功能（feature）
- `fix:`：修补 bug
- `docs:`：文档修改
- `style:`：格式修改（不影响代码运行的变动，如空格、格式化）
- `refactor:`：重构（即不是新增功能，也不是修改bug的代码变动）
- `chore:`：构建过程或辅助工具的变动

### Step 3: 推送分支并提交合并请求 (PR/MR)

将你的本地分支推送到远程仓库：

```bash
git push origin feature/zhangsan-community
```

然后去代码托管平台（如 GitLab / GitHub / Gitee）发起一个从 `feature/zhangsan-community` 合并到 `develop` 的 Merge Request。
由 Code Reviewer（代码审查人）审核通过后，才能合并。

***

## 3. 常见问题处理与 Git 命令

### 3.1 撤销本地未提交的修改

如果代码写乱了，想放弃本地所有未 commit 的修改：

```bash
git checkout .     # 放弃工作区的修改
git clean -df      # 删除未被 git 追踪的新文件
```

### 3.2 撤销刚才的 Commit（但保留代码）

如果你刚 `commit` 完发现漏了文件或者写错了备注，但还没有 `push`：

```bash
git reset --soft HEAD^
```

### 3.3 暂存本地代码 (Stash)

当你在开发功能 A 时，突然被叫去修复一个紧急 Bug，但功能 A 的代码还没写完不能 commit：

```bash
git stash save "正在开发功能A"   # 暂存代码
git checkout bugfix/xxx         # 切走去修bug

# ... 修完 bug 切回来 ...
git checkout feature/A
git stash pop                   # 恢复刚才暂存的代码
```

***

## 4. 🚨 团队红线行为 (绝对禁止)

1. **禁止直接向** **`master`** **分支 push 代码。**
2. **禁止使用** **`git push -f`** **(强制推送) 覆盖公共分支。** 如果由于操作失误覆盖了别人的代码，后果非常严重。
3. **禁止提交包含密钥、密码、真实 Token 的明文代码。**
4. **禁止提交 IDE 自动生成的缓存文件。** （`.idea/`, `build/`, `*.iml` 等必须被 `.gitignore` 过滤，如果在 PR 中看到了这些文件，请立刻剔除）。

