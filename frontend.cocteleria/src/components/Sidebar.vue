<template>
  <div class="sidebar">
    <h2>Admin</h2>

    <ul>
      <li @click="go('/dashboard')">Dashboard</li>
      <li @click="go('/ventas')">Ventas</li>
      <li @click="go('/inventario')">Inventario</li>
    </ul>
  </div>
</template>

<script setup>
import { useRouter } from "vue-router";

const router = useRouter();

const go = (ruta) => {
  router.push(ruta);
};
</script>

<style>
.sidebar {
  width: 220px;
  height: 100vh;
  background: var(--card);
  padding: 20px;
  position: fixed;
}

.sidebar ul {
  list-style: none;
  padding: 0;
}

.sidebar li {
  padding: 10px;
  cursor: pointer;
}
</style>