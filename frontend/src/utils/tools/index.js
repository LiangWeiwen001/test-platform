/**
 * 工具菜单注册表（REQ-004，可插拔）
 *
 * 新增工具 = 在 views/tool/<key>/index.vue 新建页面 + 在下方数组注册一项，
 * 无需修改路由表 / 菜单组件（菜单与 /tool/:key 路由均由本注册表驱动）。
 *
 * 字段说明：
 * - key         路由参数（/tool/:key），须与 views/tool/<key>/ 目录名一致
 * - name        菜单与页面标题
 * - icon        Element Plus 图标名（MainLayout 子菜单展示）
 * - path        菜单跳转路径
 * - component   页面组件（defineAsyncComponent 懒加载）
 * - description 页面副标题说明
 */
import { defineAsyncComponent } from 'vue'

export const tools = [
  {
    key: 'json',
    name: 'JSON 工具',
    icon: 'Document',
    path: '/tool/json',
    component: defineAsyncComponent(() => import('@/views/tool/json/index.vue')),
    description: '格式化 / 压缩 / 校验 / 排序键',
  },
  {
    key: 'crypto',
    name: '加解密',
    icon: 'Lock',
    path: '/tool/crypto',
    component: defineAsyncComponent(() => import('@/views/tool/crypto/index.vue')),
    description: 'MD5 / SHA1 / SHA256 / Base64 编解码',
  },
  {
    key: 'timestamp',
    name: '时间戳',
    icon: 'Clock',
    path: '/tool/timestamp',
    component: defineAsyncComponent(() => import('@/views/tool/timestamp/index.vue')),
    description: '时间戳 ↔ 日期互转（秒 / 毫秒）',
  },
  {
    key: 'regex',
    name: '正则测试',
    icon: 'Search',
    path: '/tool/regex',
    component: defineAsyncComponent(() => import('@/views/tool/regex/index.vue')),
    description: '正则匹配高亮 / 匹配数量 / 分组',
  },
  {
    key: 'url',
    name: 'URL 工具',
    icon: 'Link',
    path: '/tool/url',
    component: defineAsyncComponent(() => import('@/views/tool/url/index.vue')),
    description: 'URL 编码 / 解码 / 参数解析',
  },
  {
    key: 'random',
    name: '随机数据',
    icon: 'MagicStick',
    path: '/tool/random',
    component: defineAsyncComponent(() => import('@/views/tool/random/index.vue')),
    description: '随机数 / 字符串 / UUID / 手机号 / 邮箱',
  },
  {
    key: 'qrcode',
    name: '二维码',
    icon: 'FullScreen',
    path: '/tool/qrcode',
    component: defineAsyncComponent(() => import('@/views/tool/qrcode/index.vue')),
    description: '文本 / URL 转二维码',
  },
  {
    key: 'text',
    name: '文本工具',
    icon: 'EditPen',
    path: '/tool/text',
    component: defineAsyncComponent(() => import('@/views/tool/text/index.vue')),
    description: '文本对比 / 大小写转换 / 去重排序 / 统计',
  },
]

/** 按 key 查找工具条目 */
export function findTool(key) {
  return tools.find((t) => t.key === key)
}