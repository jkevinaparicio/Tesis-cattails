<template>
  <div>
    <p class="sec-t">
      Nueva venta — Sede: <span style="color:var(--cyan);">{{ sedeNombre }}</span>
    </p>

    <!-- BANNER PROMO AUTOMÁTICA -->
    <div v-if="esHoyPromo" style="
      margin-bottom:1.25rem;
      padding:10px 16px;
      borderRadius:10px;
      background: color-mix(in srgb, var(--ok) 12%, transparent);
      border: 1px solid color-mix(in srgb, var(--ok) 30%, transparent);
      font-size:0.85rem; color:var(--ok); font-weight:600;
      display:flex; align-items:center; gap:8px;
    ">
      🎉 Hoy aplica precio promocional — las cantidades pares van al precio promo automáticamente
    </div>

    <div class="fbox">
      <p class="fep">Agregar productos al pedido</p>
      <div class="frow">
        <div class="ff">
          <label>Producto</label>
          <select v-model="sel.prodId" @change="onProdChange">
            <option value="">
              {{ cargandoInv ? 'Cargando...' : 'Seleccionar...' }}
            </option>
            <option v-for="p in productosConStock" :key="p.id" :value="p.id">
              {{ p.nombre }}
            </option>
          </select>
        </div>
        <div class="ff">
          <label>Tamaño</label>
          <select v-model="sel.varId" :disabled="!variantesDisponibles.length">
            <option value="">
              {{ !sel.prodId ? 'Primero elige producto'
                : cargandoVar ? 'Cargando...'
                : variantesDisponibles.length ? 'Seleccionar...'
                : 'Sin stock' }}
            </option>
            <option v-for="v in variantesDisponibles" :key="v.varianteId" :value="v.varianteId">
              {{ v.tamNombre }} — ${{ Number(v.precio).toLocaleString() }}
              {{ esHoyPromo && v.precioPromo ? `(promo: $${Number(v.precioPromo).toLocaleString()})` : '' }}
              (stock: {{ v.stock }})
            </option>
          </select>
        </div>
      </div>

      <div class="frow s1" style="max-width:200px;">
        <div class="ff">
          <label>Cantidad</label>
          <input v-model="sel.cantidad" type="number" min="1" placeholder="1"/>
        </div>
      </div>

      <p v-if="msgSel" style="font-size:0.82rem; color:var(--err); margin-bottom:0.85rem;">
        {{ msgSel }}
      </p>

      <button class="bpri" style="margin-bottom:1rem;" @click="agregar">
        + Agregar al pedido
      </button>

      <div v-if="carrito.length">
        <p class="fep" style="margin-top:0.5rem;">Pedido actual</p>
        <div class="tbl-w" style="margin:0;">
          <table>
            <thead>
              <tr>
                <th>Producto</th>
                <th>Tamaño</th>
                <th>Cant.</th>
                <th>Precio unit.</th>
                <th>Subtotal</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, i) in carrito" :key="i">
                <td style="font-weight:500; color:var(--text);">{{ item.nombreProducto }}</td>
                <td style="color:var(--purple);">{{ item.nombreTamano }}</td>
                <td style="color:var(--text2);">{{ item.cantidad }}</td>
                <td style="color:var(--text2);">
                  ${{ Number(item.precio).toLocaleString() }}
                  <span v-if="esHoyPromo && item.precioPromo && item.cantidad >= 2"
                    style="font-size:0.72rem; color:var(--ok); margin-left:4px;">
                    🎉
                  </span>
                </td>
                <td class="c-cyan">${{ calcularSubtotal(item) }}</td>
                <td>
                  <button class="bsm bd" style="padding:3px 8px;" @click="quitar(i)">✕</button>
                </td>
              </tr>
            </tbody>
          </table>
          <div class="cart-total">
            <span class="ct-lbl">Total estimado</span>
            <span class="ct-val">${{ totalEstimado }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- OPCIONES DE VENTA -->
    <div class="fbox" style="margin-top:1rem;">
      <p class="fep">Opciones de venta</p>
      <div class="frow">
        <!-- Método de pago -->
        <div class="ff">
          <label>Método de pago</label>
          <div style="display:flex; gap:8px; flex-wrap:wrap;">
            <button
              v-for="m in metodos"
              :key="m.value"
              :class="['bsm', metodoPago === m.value ? 'be2' : 'bd']"
              @click="metodoPago = m.value"
            >
              {{ m.icon }} {{ m.label }}
            </button>
          </div>
        </div>

        <!-- Tipo pedido -->
        <div class="ff">
          <label>Tipo de pedido</label>
          <div style="display:flex; gap:8px;">
            <button
              :class="['bsm', tipoPedido === 'LOCAL' ? 'be2' : 'bd']"
              @click="tipoPedido = 'LOCAL'"
            >
              🏠 Local
            </button>
            <button
              :class="['bsm', tipoPedido === 'DOMICILIO' ? 'be2' : 'bd']"
              @click="tipoPedido = 'DOMICILIO'"
            >
              🛵 Domicilio
            </button>
          </div>
        </div>
      </div>
    </div>

    <div style="display:flex; gap:10px; align-items:center; flex-wrap:wrap; margin-top:1rem;">
      <button class="bpri" :disabled="!carrito.length || loading" @click="finalizar">
        {{ loading ? 'Procesando...' : '✓ Finalizar venta' }}
      </button>
      <button class="bsm bd" style="padding:0.5rem 1rem;" @click="carrito = []">
        Limpiar pedido
      </button>
    </div>

    <p v-if="msg" class="fmsg" style="margin-top:0.75rem;" :class="msgOk ? 'ok' : 'err'">
      {{ msg }}
    </p>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAdmin } from '@/composables/useAdmin'

const { api, loading } = useAdmin()

const productosConStock    = ref([])
const variantesDisponibles = ref([])
const inventarioSede       = ref([])
const carrito              = ref([])
const cargandoInv          = ref(false)
const cargandoVar          = ref(false)
const esHoyPromo           = ref(false)

const sedeNombre = localStorage.getItem('sede_nombre') || 'Sin sede'
const sedeId     = parseInt(localStorage.getItem('sede_id'))

const sel        = ref({ prodId: '', varId: '', cantidad: 1 })
const msgSel     = ref('')
const msg        = ref('')
const msgOk      = ref(true)
const metodoPago = ref('EFECTIVO')
const tipoPedido = ref('LOCAL')

const metodos = [
  { value: 'EFECTIVO',    label: 'Efectivo',    icon: '💵' },
  { value: 'NEQUI',       label: 'Nequi',       icon: '📱' },
  { value: 'BANCOLOMBIA', label: 'Bancolombia', icon: '🏦' },
]

function setMsg(t, ok = true) {
  msg.value = t; msgOk.value = ok
  setTimeout(() => { msg.value = '' }, 4000)
}

function calcularSubtotal(item) {
  if (esHoyPromo.value && item.precioPromo && item.cantidad >= 2) {
    const pares = Math.floor(item.cantidad / 2) * 2
    const resto = item.cantidad % 2
    return Number((item.precioPromo * pares) + (item.precio * resto)).toLocaleString()
  }
  return Number(item.precio * item.cantidad).toLocaleString()
}

const totalEstimado = computed(() => {
  const total = carrito.value.reduce((acc, item) => {
    if (esHoyPromo.value && item.precioPromo && item.cantidad >= 2) {
      const pares = Math.floor(item.cantidad / 2) * 2
      const resto = item.cantidad % 2
      return acc + (item.precioPromo * pares) + (item.precio * resto)
    }
    return acc + item.precio * item.cantidad
  }, 0)
  return Number(total).toLocaleString()
})

onMounted(async () => {
  cargandoInv.value = true
  try {
    const [inv, config] = await Promise.all([
      api('GET', `/inventario/sede/${sedeId}`).catch(() => []),
      api('GET', '/promociones/config').catch(() => null)
    ])
    inventarioSede.value = inv

    // Verificar si hoy es día de promo
    if (config) {
      const diasKeys = ['domingo','lunes','martes','miercoles','jueves','viernes','sabado']
      const hoyKey   = diasKeys[new Date().getDay()]
      esHoyPromo.value = !!config[hoyKey]
    }

    const productosMap = new Map()
    for (const item of inv) {
      if (item.stock > 0 && item.variante?.producto) {
        const prod = item.variante.producto
        if (!productosMap.has(prod.id)) productosMap.set(prod.id, prod)
      }
    }
    productosConStock.value = Array.from(productosMap.values())
  } finally {
    cargandoInv.value = false
  }
})

async function onProdChange() {
  sel.value.varId = ''
  variantesDisponibles.value = []
  msgSel.value = ''
  if (!sel.value.prodId) return

  cargandoVar.value = true
  try {
    const resultado = inventarioSede.value
      .filter(i =>
        i.variante?.producto?.id === parseInt(sel.value.prodId) && i.stock > 0
      )
      .map(i => ({
        varianteId:  i.variante.id,
        tamNombre:   i.variante.tamaño?.nombre ?? i.variante.tamano?.nombre ?? '—',
        precio:      parseFloat(i.variante.precio),
        precioPromo: i.variante.precioPromo ? parseFloat(i.variante.precioPromo) : null,
        stock:       i.stock
      }))

    if (!resultado.length) {
      msgSel.value = 'Sin stock disponible para este producto en esta sede.'
      return
    }
    variantesDisponibles.value = resultado
  } finally {
    cargandoVar.value = false
  }
}

function agregar() {
  if (!sel.value.prodId) { setMsg('Selecciona un producto', false); return }
  if (!sel.value.varId)  { setMsg('Selecciona un tamaño', false); return }

  const prod = productosConStock.value.find(p => p.id === parseInt(sel.value.prodId))
  const vari = variantesDisponibles.value.find(v => v.varianteId === parseInt(sel.value.varId))
  const cant = parseInt(sel.value.cantidad) || 1

  const yaEnCarrito = carrito.value
    .filter(x => x.varianteId === vari.varianteId)
    .reduce((acc, x) => acc + x.cantidad, 0)

  if (yaEnCarrito + cant > vari.stock) {
    setMsg(`Stock insuficiente. Disponible: ${vari.stock - yaEnCarrito}`, false)
    return
  }

  const idx = carrito.value.findIndex(x => x.varianteId === vari.varianteId)
  if (idx >= 0) {
    carrito.value[idx].cantidad += cant
  } else {
    carrito.value.push({
      varianteId:     vari.varianteId,
      nombreProducto: prod.nombre,
      nombreTamano:   vari.tamNombre,
      precio:         vari.precio,
      precioPromo:    vari.precioPromo,
      cantidad:       cant
    })
  }

  sel.value.prodId   = ''
  sel.value.varId    = ''
  sel.value.cantidad = 1
  variantesDisponibles.value = []
}

function quitar(i) { carrito.value.splice(i, 1) }

async function finalizar() {
  if (!sedeId)               { setMsg('Sin sede asignada', false); return }
  if (!carrito.value.length) { setMsg('Agrega al menos un producto', false); return }

  loading.value = true
  try {
    const res = await api('POST', '/ventas/crear', {
      idSede:      sedeId,
      metodoPago:  metodoPago.value,
      tipoPedido:  tipoPedido.value,
      esPromocion: esHoyPromo.value,
      detalles:    carrito.value.map(it => ({
        idVariante: it.varianteId,
        cantidad:   it.cantidad
      }))
    })
    setMsg(`✓ Venta #${res.id} — Total: $${Number(res.total).toLocaleString()}`)
    carrito.value = []

    const inv = await api('GET', `/inventario/sede/${sedeId}`).catch(() => [])
    inventarioSede.value = inv
    const productosMap = new Map()
    for (const item of inv) {
      if (item.stock > 0 && item.variante?.producto) {
        const prod = item.variante.producto
        if (!productosMap.has(prod.id)) productosMap.set(prod.id, prod)
      }
    }
    productosConStock.value = Array.from(productosMap.values())
  } catch (e) {
    setMsg(e.response?.data || e.message, false)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped></style>