import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    { path: '/login',  component: () => import('@/views/LoginView.vue') },
    {path: '/oauth2/callback',component: () => import('@/views/OAuth2Callback.vue')},
    { path: '/sede',   component: () => import('@/views/SedeSelector.vue') },

    {
      path: '/admin',
      component: () => import('@/layouts/AdminLayout.vue'),
      redirect: '/admin/estadisticas',
      children: [
        { path: 'estadisticas', component: () => import('@/views/admin/AdminEstadisticas.vue') },
        { path: 'categorias',   component: () => import('@/views/admin/AdminCategorias.vue') },
        { path: 'productos',    component: () => import('@/views/admin/AdminProductos.vue') },
        { path: 'tamanos',      component: () => import('@/views/admin/AdminTamanos.vue') },
        { path: 'precios',      component: () => import('@/views/admin/AdminPrecios.vue') },
        { path: 'inventario',   component: () => import('@/views/admin/AdminInventario.vue') },
        { path: 'ventas/nueva', component: () => import('@/views/admin/AdminNuevaVenta.vue') },
        { path: 'ventas/mis',   component: () => import('@/views/admin/AdminMisVentas.vue') },
        { path: 'ventas/todas', component: () => import('@/views/admin/AdminTodasVentas.vue') },
        { path: 'usuarios',     component: () => import('@/views/admin/AdminUsuarios.vue') },
        { path: 'sedes',        component: () => import('@/views/admin/AdminSedes.vue') },
        { path: 'promociones',  component: () => import('@/views/admin/AdminPromocion.vue') },
        { path: 'asistente', component: () => import('@/views/admin/AdminAsistente.vue') }
      ]
    },

    {
      path: '/empleado',
      component: () => import('@/layouts/AdminLayout.vue'),
      redirect: '/empleado/ventas',
      children: [
        { path: 'ventas',     component: () => import('@/views/admin/AdminNuevaVenta.vue') },
        { path: 'mis-ventas', component: () => import('@/views/admin/AdminMisVentas.vue') },
      ]
    },

    { path: '/:pathMatch(.*)*', redirect: '/login' }
  ]
})

router.beforeEach((to) => {
  const token  = localStorage.getItem('jwt_token')
  const rolRaw = localStorage.getItem('user_role') || ''
  const role   = rolRaw.replace('ROLE_', '')
  const sede   = localStorage.getItem('sede_id')

  const publicas = ['/login', '/oauth2/callback']
  if (publicas.includes(to.path)) {
    if (token && to.path === '/login') {
      if (!sede) return '/sede'
      return role === 'ADMIN' ? '/admin/estadisticas' : '/empleado/ventas'
    }
    return true
  }

  if (!token) return '/login'
  if (token && !sede && to.path !== '/sede') return '/sede'
  if (token && sede && to.path === '/sede') {
    return role === 'ADMIN' ? '/admin/estadisticas' : '/empleado/ventas'
  }

  return true
})

export default router