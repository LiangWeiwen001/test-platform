<script setup>
// 主布局占位：侧边菜单 + 顶栏 + 内容区
// 完整菜单（按权限过滤）、环境切换器、用户下拉由 TASK-004+ 填充
</script>

<template>
  <el-container class="main-layout">
    <el-aside width="200px" class="layout-aside">
      <div class="logo">测试平台</div>
      <el-menu :default-active="$route.path" router>
        <el-menu-item index="/login">登录页</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="layout-header">测试平台</el-header>
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
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
}
</style>
