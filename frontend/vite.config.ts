import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  build: {
    outDir: "../target/classes/static",
    emptyOutDir: true,
  },
  server: {
    proxy: {
      "/auth": process.env.GROCERY_API_TARGET ?? "http://localhost:8080",
      "/items": process.env.GROCERY_API_TARGET ?? "http://localhost:8080",
      "/stores": process.env.GROCERY_API_TARGET ?? "http://localhost:8080",
      "/prices": process.env.GROCERY_API_TARGET ?? "http://localhost:8080",
      "/dashboard": process.env.GROCERY_API_TARGET ?? "http://localhost:8080",
    },
  },
});
