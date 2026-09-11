import axios from "axios";
import router from "@/router";
import { API_URL } from "@/config";

const api = axios.create({
  baseURL: API_URL,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem("jwt_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && router.currentRoute.value.path !== "/login") {
      localStorage.clear();
      router.push("/login");
    }
    return Promise.reject(error);
  },
);

export default api;