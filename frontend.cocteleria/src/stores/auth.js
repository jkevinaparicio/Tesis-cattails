import { defineStore } from "pinia";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    token: localStorage.getItem("jwt_token") || null,
    rol: localStorage.getItem("user_role") || null,
    sede: localStorage.getItem("sede_id") || null,
  }),

  actions: {
    setAuth(data) {
      this.token = data.token;
      this.rol = data.rol;
      localStorage.setItem("jwt_token", data.token);
      localStorage.setItem("user_role", data.rol);
    },

    setSede(sede) {
      this.sede = sede;
      localStorage.setItem("sede_id", sede);
    },

    logout() {
      this.token = null;
      this.rol = null;
      this.sede = null;
      localStorage.removeItem("jwt_token");
      localStorage.removeItem("user_role");
      localStorage.removeItem("sede_id");
      localStorage.removeItem("user_correo");
      localStorage.removeItem("sede_nombre");
    }
  }
});