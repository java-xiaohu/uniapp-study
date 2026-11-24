# vue-vant

uni-app（H5）+ Vue 3 + TypeScript + Wot Design Uni 的移动端项目，接口联调依赖后端 `AI-llm`（默认 `http://localhost:8080`）。

## 环境要求

| 依赖 | 要求 |
| --- | --- |
| Node.js | 20.x（见 `.nvmrc`） |
| 包管理器 | npm（`package-lock.json`） |
| 后端 | `AI-llm` 已启动，监听 `http://localhost:8080` |
| 浏览器 | 建议 Chrome / Edge 最新版，访问 `http://localhost:5173` |

```bash
# 使用 nvm 时切换到项目指定版本
nvm use 20
```

## 安装依赖

```bash
npm install
```

## 启动项目

先确认后端 `http://localhost:8080` 已启动，再执行：

```bash
npm run dev
```

启动后：

- 平台为 H5，地址固定为 `http://localhost:5173`（`strictPort` 已开启，端口被占用会直接报错，不会自动换端口）
- 启动成功后会自动打开浏览器
- 前端 `/api` 请求由 Vite 代理转发到 `http://localhost:8080`，无需额外配置跨域

## 关闭项目

在运行 `npm run dev` 的终端里按 **Ctrl+C**，看到进程退出即已关闭。

> Git Bash / PowerShell / VS Code 终端不会出现“终止批处理操作吗(Y/N)”提示，直接退出属于正常现象。

确认端口已释放（无输出即已关闭）：

```bash
netstat -ano | grep :5173
```

**不要直接关闭终端窗口**，Windows 上可能出现子进程残留、端口未释放。

### 清理残留进程（端口被占用时）

```bash
# Git Bash
netstat -ano | grep :5173
taskkill //F //T //PID <上面查到的PID>
```

```powershell
# PowerShell 一行清理
Get-NetTCPConnection -LocalPort 5173 -State Listen | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
```

```bat
:: cmd
netstat -ano | findstr :5173
taskkill /F /T /PID <PID>
```

`taskkill` 必须带 `/T`，否则只杀父进程，底下的 `uni/vite` 子进程仍会占用端口。

## 构建与类型检查

```bash
npm run build       # 构建 H5，产物在 dist/build/h5
npm run type-check  # vue-tsc 类型检查
npm run about       # 查看 unh / 环境信息
```

构建产物部署时，需要把 `/api` 反向代理到后端服务；也可以设置环境变量 `VITE_API_BASE_URL` 指定接口前缀（默认空字符串，即同源 `/api`）。

## 后端启动（联调必需）

后端项目位于同级目录 `../AI-llm`，需要 JDK 21、MySQL（库名 `ai`）、Redis（连接信息见后端 `application.yaml`）。

```bash
cd ../AI-llm
JAVA_HOME="<JDK 21 安装路径>" ./mvnw spring-boot:run
```

> 修改后端代码后必须重启后端进程才会生效。

## 目录结构

```
src/
  api/          # 请求封装与接口定义（request.ts / auth.ts）
  composables/  # 组合式函数（useAuth.ts）
  pages/        # 页面（index / login / register / users）
  utils/        # 工具（auth 存储、路由守卫）
  styles/       # 公共样式
vite.config.ts  # 端口 5173、strictPort、/api 代理
```

## 常见问题

1. **启动报 `Port 5173 is already in use`**
   上次的 dev server 未完全退出，按上面“清理残留进程”的命令处理后再启动。

2. **登录接口返回 401「未认证或令牌已失效」**
   浏览器会发送多个 `Accept-Language`，旧版后端会因 Tomcat 11 的 locale 上限（默认 10）报错并被包装成 401。请确认后端为最新代码并已重启（相关修复在 `AI-llm` 的 `TomcatConfig` / `SecurityConfig`）。

3. **Apifox 正常但浏览器请求失败**
   先确认后端已启动、页面访问的是 `http://localhost:5173`（而不是直接打开 dist 里的静态文件），再检查终端里 Vite 是否有代理报错。
