<template>
  <div>
    <div class="ph">
      <h2 class="sec-t">Promociones</h2>
    </div>

    <!-- DÍAS DE PROMOCIÓN -->
    <div class="fbox">
      <label style="font-size:0.78rem; color:var(--text3); font-weight:600;
        text-transform:uppercase; letter-spacing:0.08em; display:block; margin-bottom:8px;">
        Escoge días de promoción
      </label>

      <p style="font-size:0.85rem; color:var(--text2); margin-bottom:1.25rem;">
        Al comprar cantidad par (2, 4, 6...) de una variante se usa el precio promo;
        la unidad impar queda al precio normal.
      </p>

      <div style="display:flex; gap:8px; flex-wrap:wrap; margin-bottom:1.25rem;">
        <button
          v-for="d in dias"
          :key="d.key"
          :class="['bsm', config[d.key] ? 'be2' : 'bd']"
          style="min-width:100px; font-weight:600;"
          @click="config[d.key] = !config[d.key]"
        >
          {{ config[d.key] ? '✓ ' : '' }}{{ d.label }}
        </button>
      </div>

      <div class="fact">
        <button class="bpri" @click="guardarConfig">Guardar días</button>
      </div>
      <p v-if="msgConfig" class="fmsg" :class="msgConfigOk ? 'ok' : 'err'">{{ msgConfig }}</p>
    </div>

    <!-- INDICADOR DÍA ACTUAL -->
    <div :style="{
      margin: '0 0 1.25rem',
      padding: '12px 16px',
      borderRadius: '10px',
      fontSize: '0.85rem',
      background: esHoyPromo
        ? 'color-mix(in srgb, var(--ok) 10%, transparent)'
        : 'color-mix(in srgb, var(--err) 10%, transparent)',
      border: esHoyPromo
        ? '1px solid color-mix(in srgb, var(--ok) 30%, transparent)'
        : '1px solid color-mix(in srgb, var(--err) 30%, transparent)',
      color: 'var(--text2)'
    }">
      Hoy es <strong style="color:var(--text);">{{ hoyLabel }}</strong> —
      <span :style="{ color: esHoyPromo ? 'var(--ok)' : 'var(--err)', fontWeight: 600 }">
        {{ esHoyPromo ? '✓ Promoción activa hoy' : '✗ Promoción no aplica hoy' }}
      </span>
    </div>

    <!-- SELECTOR DE PRODUCTO PARA EDITAR EN BLOQUE -->
    <div class="fbox">
      <p style="font-weight:700; font-size:0.95rem; color:var(--text); margin-bottom:0.5rem;">
        Precios promocionales por variante
      </p>
      <p style="font-size:0.85rem; color:var(--text2); margin-bottom:1.25rem;">
        Selecciona un producto para editar todos sus tamaños a la vez, o filtra la lista completa.
      </p>

      <!-- Edición en bloque por producto -->
      <div style="margin-bottom:1.5rem;">
        <div class="ff" style="max-width:280px; margin-bottom:1rem;">
          <label>Editar precios promo de un producto</label>
          <select v-model="prodEdicion" @change="cargarEdicion">
            <option value="">Seleccionar producto...</option>
            <option v-for="p in productos" :key="p.id" :value="p.id">{{ p.nombre }}</option>
          </select>
        </div>

        <div v-if="variantesEdicion.length" style="
          background: var(--bg3);
          border: 1px solid var(--border);
          border-radius: 10px;
          overflow: hidden;
          margin-bottom: 1rem;
        ">
          <div style="
            padding: 10px 16px;
            border-bottom: 1px solid var(--border);
            font-size: 0.82rem;
            font-weight: 700;
            color: var(--text2);
            text-transform: uppercase;
            letter-spacing: 0.06em;
            display: grid;
            grid-template-columns: 1fr 140px 120px;
            gap: 12px;
          ">
            <span>Tamaño</span>
            <span>Precio normal</span>
            <span>Precio promo</span>
          </div>

          <div
            v-for="v in variantesEdicion"
            :key="v.id"
            style="
              padding: 10px 16px;
              border-bottom: 1px solid var(--border2);
              display: grid;
              grid-template-columns: 1fr 140px 120px;
              gap: 12px;
              align-items: center;
            "
          >
            <span style="font-weight:600; color:var(--purple);">
              {{ v.tamaño?.nombre ?? v.tamano?.nombre }}
            </span>
            <span style="color:var(--cyan); font-weight:600;">
              ${{ Number(v.precio).toLocaleString() }}
            </span>
            <div style="position:relative;">
              <span style="
                position:absolute; left:10px; top:50%; transform:translateY(-50%);
                color:var(--text3); font-size:0.85rem; pointer-events:none;
              ">$</span>
              <input
                v-model="preciosPromo[v.id]"
                type="number"
                placeholder="—"
                min="0"
                style="
                  width:100%; padding:6px 8px 6px 22px;
                  border-radius:6px;
                  border:1px solid var(--border);
                  background:var(--bg2);
                  color:var(--ok);
                  font-weight:600;
                  font-size:0.9rem;
                "
              />
            </div>
          </div>
        </div>

        <div v-if="variantesEdicion.length" class="fact">
          <button class="bpri" @click="guardarBloque">
            💾 Guardar todos los precios promo
          </button>
          <button class="bsm bd" @click="quitarBloque">
            Quitar promo a todos
          </button>
        </div>
        <p v-if="msgBloque" class="fmsg" :class="msgBloqueOk ? 'ok' : 'err'">{{ msgBloque }}</p>
      </div>

      <!-- Lista completa con filtros -->
      <div style="border-top:1px solid var(--border2); padding-top:1.25rem;">
        <p style="font-size:0.82rem; font-weight:700; color:var(--text2);
          text-transform:uppercase; letter-spacing:0.06em; margin-bottom:1rem;">
          Lista completa
        </p>

        <div style="display:flex; gap:10px; flex-wrap:wrap; margin-bottom:1.25rem; align-items:flex-end;">
          <div class="ff" style="margin:0; min-width:220px;">
            <label>Filtrar por producto</label>
            <select v-model="prodFiltro" @change="cargarVariantes">
              <option value="">Todos los productos</option>
              <option v-for="p in productos" :key="p.id" :value="p.id">{{ p.nombre }}</option>
            </select>
          </div>
          <div style="display:flex; gap:8px;">
            <button :class="['bsm', vistaFiltro === 'todas'    ? 'be2' : 'bd']" @click="vistaFiltro = 'todas'">Todas</button>
            <button :class="['bsm', vistaFiltro === 'conPromo' ? 'be2' : 'bd']" @click="vistaFiltro = 'conPromo'">🎉 Con promo</button>
            <button :class="['bsm', vistaFiltro === 'sinPromo' ? 'be2' : 'bd']" @click="vistaFiltro = 'sinPromo'">Sin promo</button>
          </div>
        </div>

        <div class="tbl-w">
          <table>
            <thead>
              <tr>
                <th>Producto</th>
                <th>Tamaño</th>
                <th>Precio normal</th>
                <th>Precio promo (c/u)</th>
                <th>Ahorro x2</th>
                <th>Acción</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="cargando">
                <td colspan="6" class="empty">Cargando...</td>
              </tr>
              <tr v-else-if="!variantesFiltradas.length">
                <td colspan="6" class="empty">Sin variantes.</td>
              </tr>
              <tr v-for="v in variantesFiltradas" :key="v.id">
                <td style="font-weight:600; color:var(--text);">{{ v.producto?.nombre }}</td>
                <td style="color:var(--purple); font-weight:500;">
                  {{ v.tamaño?.nombre ?? v.tamano?.nombre }}
                </td>
                <td class="c-cyan">${{ Number(v.precio).toLocaleString() }}</td>
                <td>
                  <div style="position:relative; max-width:140px;">
                    <span style="
                      position:absolute; left:10px; top:50%; transform:translateY(-50%);
                      color:var(--text3); font-size:0.85rem; pointer-events:none;
                    ">$</span>
                    <input
                      v-model="preciosPromo[v.id]"
                      type="number"
                      placeholder="Sin promo"
                      min="0"
                      style="
                        width:100%; padding:6px 8px 6px 22px;
                        border-radius:6px;
                        border:1px solid var(--border);
                        background:var(--bg2);
                        color:var(--ok);
                        font-weight:600;
                        font-size:0.9rem;
                      "
                    />
                  </div>
                </td>
                <td>
                  <span v-if="calcularAhorro(v)"
                    style="color:var(--ok); font-size:0.82rem; font-weight:600;">
                    -${{ calcularAhorro(v) }}
                  </span>
                  <span v-else style="color:var(--text3); font-size:0.82rem;">—</span>
                </td>
                <td>
                  <div class="btn-r">
                    <button class="bsm be2" @click="guardarPromo(v)">Guardar</button>
                    <button
                      v-if="preciosPromo[v.id] !== '' && preciosPromo[v.id] != null"
                      class="bsm bd"
                      @click="quitarPromo(v)"
                    >Quitar</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <p v-if="msgPromo" class="fmsg" :class="msgPromoOk ? 'ok' : 'err'">{{ msgPromo }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAdmin } from '@/composables/useAdmin'

const { api } = useAdmin()

const dias = [
  { key: 'lunes',     label: 'Lunes'     },
  { key: 'martes',    label: 'Martes'    },
  { key: 'miercoles', label: 'Miércoles' },
  { key: 'jueves',    label: 'Jueves'    },
  { key: 'viernes',   label: 'Viernes'   },
  { key: 'sabado',    label: 'Sábado'    },
  { key: 'domingo',   label: 'Domingo'   },
]

const config          = ref({ lunes:false, martes:false, miercoles:false, jueves:false, viernes:false, sabado:false, domingo:false })
const productos       = ref([])
const variantes       = ref([])
const prodFiltro      = ref('')
const prodEdicion     = ref('')
const variantesEdicion = ref([])
const vistaFiltro     = ref('todas')
const preciosPromo    = ref({})
const cargando        = ref(false)

const msgConfig  = ref(''); const msgConfigOk  = ref(true)
const msgPromo   = ref(''); const msgPromoOk   = ref(true)
const msgBloque  = ref(''); const msgBloqueOk  = ref(true)

function setMsgConfig(t, ok=true) { msgConfig.value=t; msgConfigOk.value=ok; setTimeout(()=>msgConfig.value='',3000) }
function setMsgPromo(t,  ok=true) { msgPromo.value=t;  msgPromoOk.value=ok;  setTimeout(()=>msgPromo.value='',3000)  }
function setMsgBloque(t, ok=true) { msgBloque.value=t; msgBloqueOk.value=ok; setTimeout(()=>msgBloque.value='',3000) }

const diasKeys   = ['domingo','lunes','martes','miercoles','jueves','viernes','sabado']
const hoyIdx     = new Date().getDay()
const hoyKey     = diasKeys[hoyIdx]
const hoyLabel   = dias.find(d => d.key === hoyKey)?.label || ''
const esHoyPromo = computed(() => !!config.value[hoyKey])

const variantesFiltradas = computed(() => {
  let lista = variantes.value

  if (prodFiltro.value)
    lista = lista.filter(v => v.producto?.id === parseInt(prodFiltro.value))

  if (vistaFiltro.value === 'conPromo')
    lista = lista.filter(v => preciosPromo.value[v.id] !== '' && preciosPromo.value[v.id] != null)

  if (vistaFiltro.value === 'sinPromo')
    lista = lista.filter(v => preciosPromo.value[v.id] === '' || preciosPromo.value[v.id] == null)

  return lista
})

function calcularAhorro(v) {
  const promo  = Number(preciosPromo.value[v.id])
  const normal = Number(v.precio)
  if (!promo || promo >= normal) return null
  return ((normal - promo) * 2).toLocaleString()
}

onMounted(async () => {
  cargando.value = true
  try {
    const [cfg, prods, vars] = await Promise.all([
      api('GET', '/promociones/config').catch(() => null),
      api('GET', '/productos/todos').catch(() => []),
      api('GET', '/variantes').catch(() => []),
    ])
    if (cfg) config.value = cfg
    productos.value = prods
    variantes.value = vars
    vars.forEach(v => { preciosPromo.value[v.id] = v.precioPromo ?? '' })
  } finally {
    cargando.value = false
  }
})

async function cargarVariantes() {
  cargando.value = true
  try {
    const url = prodFiltro.value ? `/variantes/producto/${prodFiltro.value}` : '/variantes'
    variantes.value = await api('GET', url).catch(() => [])
    variantes.value.forEach(v => { preciosPromo.value[v.id] = v.precioPromo ?? '' })
  } finally {
    cargando.value = false
  }
}

async function cargarEdicion() {
  variantesEdicion.value = []
  if (!prodEdicion.value) return
  const vars = await api('GET', `/variantes/producto/${prodEdicion.value}`).catch(() => [])
  variantesEdicion.value = vars
  vars.forEach(v => { preciosPromo.value[v.id] = v.precioPromo ?? '' })
}

async function guardarConfig() {
  try {
    const res = await api('PUT', '/promociones/config', config.value)
    if (res) config.value = res
    setMsgConfig('Días de promoción actualizados')
  } catch (e) {
    setMsgConfig(e.message, false)
  }
}

async function guardarPromo(v) {
  try {
    const val = preciosPromo.value[v.id]
    await api('PUT', `/variantes/${v.id}/precio-promo`, {
      precioPromo: val === '' ? null : Number(val)
    })
    v.precioPromo = val === '' ? null : Number(val)
    setMsgPromo(`${v.producto?.nombre} ${v.tamaño?.nombre ?? v.tamano?.nombre} actualizado`)
  } catch (e) {
    setMsgPromo(e.message, false)
  }
}

async function quitarPromo(v) {
  if (!confirm(`¿Quitar precio promo de ${v.producto?.nombre} ${v.tamaño?.nombre ?? v.tamano?.nombre}?`)) return
  try {
    await api('PUT', `/variantes/${v.id}/precio-promo`, { precioPromo: null })
    v.precioPromo = null
    preciosPromo.value[v.id] = ''
    setMsgPromo('Precio promo eliminado')
  } catch (e) {
    setMsgPromo(e.message, false)
  }
}

async function guardarBloque() {
  try {
    await Promise.all(
      variantesEdicion.value.map(v => {
        const val = preciosPromo.value[v.id]
        return api('PUT', `/variantes/${v.id}/precio-promo`, {
          precioPromo: val === '' ? null : Number(val)
        })
      })
    )
    setMsgBloque('Precios promo guardados correctamente')
  } catch (e) {
    setMsgBloque(e.message, false)
  }
}

async function quitarBloque() {
  const prod = productos.value.find(p => p.id === parseInt(prodEdicion.value))
  if (!confirm(`¿Quitar precio promo a todos los tamaños de ${prod?.nombre}?`)) return
  try {
    await Promise.all(
      variantesEdicion.value.map(v => {
        preciosPromo.value[v.id] = ''
        return api('PUT', `/variantes/${v.id}/precio-promo`, { precioPromo: null })
      })
    )
    setMsgBloque('Precio promo eliminado de todos los tamaños')
  } catch (e) {
    setMsgBloque(e.message, false)
  }
}
</script>

<style scoped src="@/assets/admin.css" />