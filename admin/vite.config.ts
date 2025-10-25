import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      // Proxy API requests to the backend to avoid CORS during development
      '/api': {
        target: 'http://localhost:8082',
        changeOrigin: true,
        secure: false,
      },
      // Proxy websocket endpoints if used
      '/ws': {
        target: 'ws://localhost:8082',
        ws: true,
        changeOrigin: true,
        secure: false,
      }
    }
  }
});
