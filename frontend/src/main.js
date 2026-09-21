import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'

import App from './App.vue'
import router from './router'
import { setupRouterGuard } from './router/guard'
import { setupPermissionDirective } from './utils/permission'
import './style.css'

const app = createApp(App)

// 状态管理
app.use(createPinia())
// 路由
setupRouterGuard(router)
app.use(router)
// 按钮级权限指令 v-perm
setupPermissionDirective(app)
// UI 组件库（完整引入，M1 不搞按需）
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
