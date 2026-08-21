<template>
  <div class="layout">
    <Sidebar />

    <div class="content">
      <h2>Ventas</h2>

      <!-- FILTROS -->
      <input v-model="usuario" placeholder="Buscar por usuario" />
      <button @click="buscarPorUsuario">Buscar</button>

      <button @click="cargarTodas">Todas</button>
      <button @click="misVentas">Mis ventas</button>
      <button @click="porSede">Por sede</button>

      <!-- TABLA -->
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>Usuario</th>
            <th>Sede</th>
            <th>Total</th>
            <th>Fecha</th>
            <th>Acciones</th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="v in ventas" :key="v.id">
            <td>{{ v.id }}</td>
            <td>{{ v.usuario }}</td>
            <td>{{ v.sede }}</td>
            <td>{{ v.total }}</td>
            <td>{{ v.fecha }}</td>

            <td>
              <button @click="eliminar(v.id)">Eliminar</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import Sidebar from "../../components/Sidebar.vue";
import { api } from "../../services/api";
import { useAuthStore } from "../../store/auth";

const ventas = ref([]);
const usuario = ref("");
const auth = useAuthStore();

// 🔥 ADMIN
const cargarTodas = async () => {
  const res = await api.get("/ventas/todas");
  ventas.value = res.data;
};

// 🔥 EMPLEADO
const misVentas = async () => {
  const res = await api.get("/ventas/mis-ventas");
  ventas.value = res.data;
};

// 🔥 FILTRO USUARIO
const buscarPorUsuario = async () => {
  const res = await api.get(`/ventas/usuario/${usuario.value}`);
  ventas.value = res.data;
};

// 🔥 POR SEDE
const porSede = async () => {
  const res = await api.get(`/ventas/sede/${auth.sede.id}`);
  ventas.value = res.data;
};

// 🔥 ELIMINAR
const eliminar = async (id) => {
  await api.delete(`/ventas/eliminar/${id}`);
  porSede();
};

onMounted(() => {
  porSede();
});
</script>