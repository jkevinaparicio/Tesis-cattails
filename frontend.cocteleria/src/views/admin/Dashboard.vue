<template>
  <div class="layout">
    <Sidebar />

    <div class="content">
      <h1>Panel Admin</h1>
      <p>Sede: {{ auth.sede?.nombre }}</p>
    </div>
  </div>
</template>

<script setup>
import Sidebar from "../../components/Sidebar.vue";
import { useAuthStore } from "../../store/auth";

const auth = useAuthStore();
</script>

<style>
.layout {
  display: flex;
}

.content {
  margin-left: 220px;
  padding: 20px;
  width: 100%;
}
</style>