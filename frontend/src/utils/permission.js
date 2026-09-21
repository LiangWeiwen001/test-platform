/**
 * 按钮级权限：v-perm 指令 + hasPerm 判断
 * 用法：<el-button v-perm="'system:user:add'">新增</el-button>
 * 无对应权限码时移除元素 DOM。
 */
import { useUserStore } from '@/store/user'

/**
 * 判断当前用户是否拥有指定权限码
 * @param {string} perm 权限码，如 'system:user:add'
 * @returns {boolean}
 */
export function hasPerm(perm) {
  if (!perm) return true
  const userStore = useUserStore()
  return userStore.perms.includes(perm)
}

/**
 * 注册 v-perm 指令（无权限时移除 DOM）
 * @param {import('vue').App} app
 */
export function setupPermissionDirective(app) {
  app.directive('perm', {
    mounted(el, binding) {
      if (!hasPerm(binding.value)) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    },
  })
}
