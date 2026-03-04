# Windows 下 Maven 安装与配置

本机当前未检测到 `mvn` 命令，请按下面**手动安装**方式配置。

> **说明**：winget 目前没有官方 Maven 包，执行 `winget install Apache.Maven` 会提示 **"No package found matching input criteria"**，需改用手动安装。

---

## 手动安装（推荐）

### 1. 安装 JDK（若未安装）

- Maven 需要 **JDK 8+**，本项目使用 **JDK 17**。
- 检查：`java -version`
- 下载： [Adoptium Temurin 17](https://adoptium.net/) 或 Oracle JDK，安装后设置 `JAVA_HOME` 指向 JDK 根目录。

### 2. 下载 Maven

- 打开 [Apache Maven 下载页](https://maven.apache.org/download.cgi)。
- 在 **Files** 区域下载 **Binary zip**（例如 `apache-maven-3.9.12-bin.zip`），不要下载 **Source** 版。
- 直接下载链接（3.9.x）：<https://dlcdn.apache.org/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip>

### 3. 解压到固定目录

- 解压到不含中文和空格的路径，例如：
  - `C:\Program Files\Apache\maven`
  - 或 `D:\tools\apache-maven-3.9.12`
- 解压后目录内应包含 `bin`、`lib`、`conf` 等文件夹。

### 4. 配置环境变量

1. **MAVEN_HOME**
   - 右键「此电脑」→ 属性 → 高级系统设置 → 环境变量。
   - 在「系统变量」中新建：
     - 变量名：`MAVEN_HOME`
     - 变量值：Maven 解压路径，如 `C:\Program Files\Apache\maven` 或 `D:\tools\apache-maven-3.9.12`

2. **Path**
   - 在「系统变量」中找到 `Path`，编辑 → 新建，添加：
     - `%MAVEN_HOME%\bin`
   - 确定保存。

3. **（可选）本地仓库与镜像**
   - 若希望依赖下载到指定目录或使用国内镜像，可配置用户级 `settings.xml`，见下文。

### 5. 验证

- **新开一个** PowerShell 或 CMD 窗口，执行：
  ```powershell
  mvn -v
  ```
- 应看到 Maven 版本与 Java 版本信息。

---

## 可选：配置国内镜像（加速依赖下载）

1. 找到 Maven 的 `conf/settings.xml`（或在用户目录 `.m2` 下复制一份）。
2. 在 `<mirrors>` 内添加阿里云镜像，例如：
   ```xml
   <mirror>
     <id>aliyun</id>
     <mirrorOf>central</mirrorOf>
     <name>Aliyun Maven</name>
     <url>https://maven.aliyun.com/repository/public</url>
   </mirror>
   ```
3. 若需自定义本地仓库路径，在 `settings.xml` 中设置：
   ```xml
   <localRepository>D:/maven-repo</localRepository>
   ```
   （路径可自定，避免放在 C 盘可节省空间。）

---

## 在本项目中使用

- 在项目根目录 `D:\outdoor-gear-recommendation` 下执行：
  ```powershell
  mvn -v
  mvn clean compile
  ```
- 无数据库启动（阶段一验证）：
  ```powershell
  mvn spring-boot:run -Dspring-boot.run.profiles=no-db
  ```

配置完成后，新开终端再试上述命令即可。
