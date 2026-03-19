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
  - 来源：基于 `master` 分支拉取。
  - 归宿：开发完成后合并回 `master` 分支。
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

基于最新的 `master` 分支，创建并切换到你的功能分支：

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

然后去代码托管平台（如 GitLab / GitHub / Gitee）发起一个从 `feature/zhangsan-community` 合并到 `master` 的 Merge Request。
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

### 3.4 使用 Rebase 合并或修改多个 Commit

为了保持主分支历史记录的整洁，在提交 PR/MR 之前，如果你的分支包含大量琐碎的提交（例如 "fix typo", "update", "test"），建议将它们合并为一个完整的 commit。

**1. 启动交互式 rebase**
假设你要合并最近的 3 次提交：
```bash
git rebase -i HEAD~3
```

**2. 在编辑器中修改命令**
执行上述命令后，会打开一个文本编辑器，显示类似如下内容：
```text
pick 1a2b3c4 feat: 第一步完成
pick 5d6e7f8 fix: 修复了一个小bug
pick 9a0b1c2 chore: 更新了一些注释
```
将需要合并的提交前面的 `pick` 改为 `squash` (或简写为 `s`)：
```text
pick 1a2b3c4 feat: 完整的社区功能
squash 5d6e7f8 fix: 修复了一个小bug
squash 9a0b1c2 chore: 更新了一些注释
```
保存并退出编辑器。

**3. 编辑合并后的 Commit Message**
随后会弹出第二个编辑器让你编写合并后的最终提交信息。保留你需要的信息，删除多余的，然后保存退出。

**4. 强制推送到自己的分支**
由于你修改了历史记录，如果这些 commit 已经推送过，需要强制推送：
```bash
git push origin <你的分支名> -f
```
> **⚠️ 警告：** 只能在自己独立的开发分支上使用 `git push -f`，**绝对禁止**在 `master` 等公共分支上执行此操作！

***

## 4. 🚨 团队红线行为 (绝对禁止)

1. **禁止直接向 `master` 分支 push 代码。**
2. **禁止使用 `git push -f` (强制推送) 覆盖公共分支。** 如果由于操作失误覆盖了别人的代码，后果非常严重。
3. **禁止提交包含密钥、密码、真实 Token 的明文代码。**
4. **禁止提交 IDE 自动生成的缓存文件和日志文件。** （`.idea/`, `build/`, `*.iml`, `log/` 等必须被 `.gitignore` 过滤，如果在 PR 中看到了这些文件，请立刻剔除）。

---

## 5. 如何防止误 Push 到 master 分支？

为了从根本上防止开发人员将代码直接 push 到 `master` 分支，团队应该采取以下双重防护措施：

### 5.1 远端仓库保护 (最有效的方式)
这是最根本的防护方式，直接在代码托管平台（GitHub/GitLab/Gitee）上进行设置：
- **GitHub**: 进入仓库 `Settings` -> `Branches` -> `Branch protection rules`，添加 `master` 分支的保护规则。勾选 `Require a pull request before merging` 和 `Do not allow bypassing the above settings`。
- **GitLab**: 进入项目 `Settings` -> `Repository` -> `Protected branches`，将 `master` 分支设置为 `Protected`，并将 `Allowed to push` 设置为 `No one`，只允许通过 Merge Request 合并代码。

### 5.2 本地 Git Hook 拦截
可以通过在本地 Git 仓库中配置 `pre-push` 钩子，在代码推送到远程前进行拦截。

**注意：** 默认情况下，`.git/hooks/` 目录下的文件是**不会被 Git 追踪**的，它只存在于当前电脑的本地仓库中。为了让整个团队都能共享这个拦截脚本，我们需要做如下改造：

1. 在项目根目录下新建一个名为 `.githooks` 的文件夹（这个文件夹会被 Git 追踪）。
2. 在该文件夹下新建 `pre-push` 文件，写入以下代码：

```bash
#!/bin/bash

protected_branch='master'
current_branch=$(git symbolic-ref HEAD | sed -e 's,.*/\(.*\),\1,')

if [ $protected_branch = $current_branch ]
then
    echo "\033[31m[错误] 绝对禁止直接向 $protected_branch 分支 push 代码！\033[0m"
    echo "请创建 feature 或 bugfix 分支，提交 PR/MR 进行合并。"
    exit 1 # 退出码非0，阻止 push 动作
fi

exit 0
```
3. 给这个脚本加上执行权限并提交到远程仓库：
```bash
chmod +x .githooks/pre-push
git add .githooks/pre-push
git commit -m "chore: 添加 pre-push hook 防止误推 master"
```

4. **团队成员配置（只需执行一次）：**
当新成员克隆完代码后，只需要在项目根目录下执行以下命令，告诉 Git 把钩子目录指向我们刚才创建的 `.githooks` 文件夹即可：
```bash
git config core.hooksPath .githooks
```
这样，当任何人在本地尝试执行 `git push origin master` 时，终端会直接报错并终止推送操作。

### 5.3 如何移除或禁用 Hook？

如果需要临时禁用或彻底移除该拦截脚本，可以使用以下方法：

#### 1. 临时跳过检查 (Emergency)
如果你确认当前操作是安全的，并且急需推送（例如紧急修复），可以使用 `--no-verify` 参数跳过钩子检查：
```bash
git push origin master --no-verify
```

#### 2. 本地不再使用该脚本 (Disable Locally)
如果你想在本地取消这个钩子的绑定，恢复 Git 的默认行为：
```bash
git config --unset core.hooksPath
```


#### 3. 彻底从项目中移除 (Remove from Project)
如果团队决定不再使用该机制，请删除 `.githooks` 文件夹并提交代码：
```bash
git rm -r .githooks
git commit -m "chore: 移除 git hooks"
```

