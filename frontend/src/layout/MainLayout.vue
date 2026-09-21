<script setup>
/**
 * 主布局：侧边动态菜单（按权限码过滤）+ 顶栏 + 内容区
 * 菜单权限码：dashboard:view / tool:view / env:env:list / case:case:list / system:menu
 * （系统管理子菜单：system:user:list / system:role:list / system:log:list）
 */
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { hasPerm } from '@/utils/permission'
import { tools } from '@/utils/tools'
import { search } from '@/api/search'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 全局搜索
const searchKw = ref('')
const searchLoading = ref(false)
const searchDialog = ref(false)
const searchResult = ref({ keyword: '', cases: [], envs: [], users: [] })

async function doSearch() {
  const kw = searchKw.value.trim()
  if (!kw) {
    ElMessage.warning('请输入搜索关键字')
    return
  }
  searchLoading.value = true
  try {
    const res = await search(kw)
    searchResult.value = res.data || { keyword: kw, cases: [], envs: [], users: [] }
    searchDialog.value = true
  } catch (e) {
    ElMessage.error('搜索失败')
  } finally {
    searchLoading.value = false
  }
}

function goTo(path) {
  searchDialog.value = false
  searchKw.value = ''
  router.push(path)
}

/** 菜单配置：path + 标题 + 所需权限码（工具菜单子项由注册表 tools 生成） */
const menuConfig = [
  { path: '/dashboard', title: '工作台', perm: 'dashboard:view' },
  {
    title: '工具菜单',
    perm: 'tool:view',
    children: tools.map((t) => ({ path: t.path, title: t.name, perm: 'tool:view' })),
  },
  { path: '/env', title: '环境管理', perm: 'env:env:list' },
  { path: '/case', title: '用例管理', perm: 'case:case:list' },
  {
    title: '系统管理',
    perm: 'system:menu',
    children: [
      { path: '/system/user', title: '用户管理', perm: 'system:user:list' },
      { path: '/system/role', title: '角色权限', perm: 'system:role:list' },
      { path: '/system/log', title: '操作日志', perm: 'system:log:list' },
    ],
  },
]

/** 按权限码过滤后的菜单 */
const visibleMenus = computed(() =>
  menuConfig
    .map((item) => {
      if (item.children) {
        const children = item.children.filter((child) => hasPerm(child.perm))
        if (children.length || hasPerm(item.perm)) {
          return { ...item, children }
        }
        return null
      }
      return hasPerm(item.perm) ? item : null
    })
    .filter(Boolean),
)
</script>

<template>
  <el-container class="main-layout">
    <el-aside width="200px" class="layout-aside">
      <div class="logo">测试平台</div>
      <el-menu :default-active="route.path" router background-color="#1f2d3d" text-color="#c0c4cc">
        <template v-for="item in visibleMenus" :key="item.path || item.title">
          <el-sub-menu v-if="item.children" :index="item.title">
            <template #title>{{ item.title }}</template>
            <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
              {{ child.title }}
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="item.path">{{ item.title }}</el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">
        <span class="app-name">测试平台</span>
        <span class="search-area">
          <el-input
            v-model="searchKw"
            placeholder="全局搜索：用例/环境/用户"
            clearable
            size="small"
            style="width: 260px"
            @keyup.enter="doSearch"
          >
            <template #append>
              <el-button :loading="searchLoading" @click="doSearch">搜索</el-button>
            </template>
          </el-input>
        </span>
        <span class="user-area">
          <span class="nickname">{{ userStore.nickname }}</span>
          <el-button link type="primary" @click="userStore.logout()">退出登录</el-button>
        </span>
      </el-header>

      <!-- 搜索结果弹窗 -->
      <el-dialog v-model="searchDialog" title="搜索结果" width="640px">
        <template v-if="searchResult.cases.length">
          <div class="result-group-title">用例（{{ searchResult.cases.length }}）</div>
          <el-table :data="searchResult.cases" size="small" @row-click="(r) => goTo('/case')">
            <el-table-column prop="name" label="用例名称" show-overflow-tooltip />
            <el-table-column prop="type" label="类型" width="80" align="center" />
            <el-table-column prop="level" label="级别" width="70" align="center" />
            <el-table-column prop="matchField" label="命中字段" width="100" align="center" />
          </el-table>
        </template>
        <template v-if="searchResult.envs.length">
          <div class="result-group-title">环境（{{ searchResult.envs.length }}）</div>
          <el-table :data="searchResult.envs" size="small" @row-click="(r) => goTo('/env')">
            <el-table-column prop="name" label="环境名称" show-overflow-tooltip />
            <el-table-column prop="code" label="编码" width="120" />
            <el-table-column prop="matchField" label="命中字段" width="100" align="center" />
          </el-table>
        </template>
        <template v-if="searchResult.users.length">
          <div class="result-group-title">用户（{{ searchResult.users.length }}）</div>
          <el-table :data="searchResult.users" size="small" @row-click="(r) => goTo('/system/user')">
            <el-table-column prop="username" label="用户名" width="140" />
            <el-table-column prop="nickname" label="昵称" show-overflow-tooltip />
            <el-table-column prop="matchField" label="命中字段" width="100" align="center" />
          </el-table>
        </template>
        <el-empty
          v-if="!searchResult.cases.length && !searchResult.envs.length && !searchResult.users.length"
          description="未找到匹配结果"
        />
      </el-dialog>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.main-layout {
  height: 100%;
}

.layout-aside {
  background-color: #1f2d3d;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  color: #fff;
  font-weight: 600;
}

.layout-header {
  display: flex;
  align-items: center;
  gap: 16px;
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
}

.app-name {
  font-weight: 600;
  flex-shrink: 0;
}

.search-area {
  flex: 1;
  display: flex;
  justify-content: center;
}

.user-area {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.result-group-title {
  font-weight: 600;
  margin: 8px 0 4px;
  color: #303133;
}

.nickname {
  color: #606266;
}
</style>
