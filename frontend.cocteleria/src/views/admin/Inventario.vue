<template>
  <div class="layout">
    <Sidebar />

    <div class="content">
      <h2>Inventario - {{ auth.sede?.nombre }}</h2>

      <table>
        <thead>
          <tr>
            <th>Tamaño</th>
            <th>Stock</th>
          </tr>
        </thead>

        <tbody>
          <tr v-for="i in inventario" :key="i.id">
            <td>{{ i.nombreTamano }}</td>
            <td>{{ i.stock }}</td>
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

const inventario = ref([]);
const auth = useAuthStore();

const cargarInventario = async () => {
  const res = await api.get(`/inventario/sede/${auth.sede.id}`);
  inventario.value = res.data;
};

onMounted(() => {
  cargarInventario();
});
</script>