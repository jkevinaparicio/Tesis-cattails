<template>
  <div class="app-wrap" :class="theme">

    <div
      class="sidebar-overlay"
      :class="{ open: menuOpen }"
      @click="menuOpen = false"
    />

    <aside class="sidebar" :class="{ open: menuOpen }">

      <div class="sb-brand">
        <p class="sb-title">Cattails</p>
        <p class="sb-sub">Granizados</p>
      </div>

      <div class="sb-meta">
        <div class="sb-role">
          <div class="sb-dot"></div>
          <span class="sb-role-txt">{{ rol }}</span>
        </div>
        <span class="sb-sede">{{ sedeNombre }}</span>
      </div>

      <nav class="sb-nav" @click="menuOpen = false">
        <template v-if="rol === 'ADMIN'">

          <p class="sb-sec">Principal</p>
          <RouterLink class="sb-item" to="/admin/estadisticas">
            <BarChart2 class="sbi" :size="17" /><span class="sbl">Estadísticas</span>
          </RouterLink>

          <p class="sb-sec">Gestión</p>
          <RouterLink class="sb-item" to="/admin/usuarios">
            <Users class="sbi" :size="17" /><span class="sbl">Usuarios</span>
          </RouterLink>
          <RouterLink class="sb-item" to="/admin/sedes">
            <Building2 class="sbi" :size="17" /><span class="sbl">Sedes</span>
          </RouterLink>

          <!-- ✅ AGREGAR ESTO -->
          <p class="sb-sec">Inteligencia</p>
          <RouterLink class="sb-item" to="/admin/asistente">
            <Sparkles class="sbi" :size="17" /><span class="sbl">Asistente IA</span>
          </RouterLink>

          <p class="sb-sec">Catálogo</p>
          <RouterLink class="sb-item" to="/admin/categorias">
            <Tag class="sbi" :size="17" /><span class="sbl">Categorías</span>
          </RouterLink>
          <RouterLink class="sb-item" to="/admin/productos">
            <CupSoda class="sbi" :size="17" /><span class="sbl">Productos</span>
          </RouterLink>
          <RouterLink class="sb-item" to="/admin/tamanos">
            <Ruler class="sbi" :size="17" /><span class="sbl">Tamaños</span>
          </RouterLink>
          <RouterLink class="sb-item" to="/admin/precios">
            <DollarSign class="sbi" :size="17" /><span class="sbl">Variantes y Precios</span>
          </RouterLink>
          <RouterLink class="sb-item" to="/admin/promociones">
            <Sparkles class="sbi" :size="17" /><span class="sbl">Promociones</span>
          </RouterLink>

          <p class="sb-sec">Operaciones</p>
          <RouterLink class="sb-item" to="/admin/inventario">
            <Package class="sbi" :size="17" /><span class="sbl">Inventario</span>
          </RouterLink>
          <RouterLink class="sb-item" to="/admin/ventas/nueva">
            <ShoppingCart class="sbi" :size="17" /><span class="sbl">Nueva venta</span>
          </RouterLink>
          <RouterLink class="sb-item" to="/admin/ventas/mis">
            <ClipboardList class="sbi" :size="17" /><span class="sbl">Mis ventas</span>
          </RouterLink>
          <RouterLink class="sb-item" to="/admin/ventas/todas">
            <LayoutList class="sbi" :size="17" /><span class="sbl">Todas las ventas</span>
          </RouterLink>

      

        </template>

        <template v-else>
          <p class="sb-sec">Operaciones</p>
          <RouterLink class="sb-item" to="/empleado/ventas">
            <ShoppingCart class="sbi" :size="17" /><span class="sbl">Nueva venta</span>
          </RouterLink>
          <RouterLink class="sb-item" to="/empleado/mis-ventas">
            <ClipboardList class="sbi" :size="17" /><span class="sbl">Mis ventas</span>
          </RouterLink>
        </template>
      </nav>

      <div class="sb-bottom">
        <button class="sb-logout" @click="logout">
          <LogOut class="sbi" :size="15" /> Salir
        </button>
        <button class="theme-toggle" @click="themeStore.toggle()">
          <Sun v-if="theme === 'dark'" class="sbi" :size="15" /> 
          <Moon v-else class="sbi" :size="15" />
          {{ theme === 'dark' ? 'Claro' : 'Oscuro' }}
        </button>
      </div>
    </aside>

    <div class="main">
      <div class="topbar">
        <div class="topbar-left">
          <button class="hamburger" @click="menuOpen = !menuOpen">
            <Menu v-if="!menuOpen" :size="20" />
            <X v-else :size="20" />
          </button>
          <span class="topbar-title">{{ pageTitle }}</span>
        </div>
        <span class="topbar-user">{{ correo }}</span>
      </div>

      <div class="content">
        <RouterView />
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useThemeStore } from '@/stores/theme'
import { storeToRefs } from 'pinia'
import {
  BarChart2, Tag, CupSoda, Ruler, DollarSign, Sparkles,
  Package, ShoppingCart, ClipboardList, LayoutList,
  Users, Building2, LogOut, Sun, Moon, Menu, X
} from 'lucide-vue-next'

const router     = useRouter()
const route      = useRoute()
const themeStore = useThemeStore()
const { theme }  = storeToRefs(themeStore)
const menuOpen   = ref(false)

const correo     = localStorage.getItem('user_correo') || ''
const sedeNombre = localStorage.getItem('sede_nombre') || '—'
const rolRaw     = localStorage.getItem('user_role')   || ''
const rol        = rolRaw.replace('ROLE_', '')

const TITLES = {
  '/admin/estadisticas': 'Estadísticas',
  '/admin/categorias':   'Categorías',
  '/admin/productos':    'Productos',
  '/admin/tamanos':      'Tamaños',
  '/admin/precios':      'Variantes y Precios',
  '/admin/promociones':  'Promociones',
  '/admin/inventario':   'Inventario',
  '/admin/ventas/nueva': 'Nueva venta',
  '/admin/ventas/mis':   'Mis ventas',
  '/admin/ventas/todas': 'Todas las ventas',
  '/admin/asistente':    'Asistente IA',
  '/admin/usuarios':     'Usuarios',
  '/admin/sedes':        'Sedes',
  '/empleado/ventas':    'Nueva venta',
  '/empleado/mis-ventas':'Mis ventas',
}
const pageTitle = computed(() => TITLES[route.path] || 'Cattails Granizados')

function logout() {
  localStorage.clear()
  router.push('/login')
}
</script>

<style scoped>
.sb-brand {
  text-align: center;
  padding: 24px 16px 16px;
}

.sb-title {
  font-size: 1.5rem;
  font-weight: 800;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  margin: 0;
  line-height: 1.1;
}

.sb-sub {
  font-size: 0.75rem;
  font-weight: 400;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  opacity: 0.5;
  margin: 4px 0 0;
}



</style>