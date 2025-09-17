import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'
import path from 'path'

export default defineConfig({
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  plugins: [
    tailwindcss(),
    vue()
  ],
  server: {
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://192.168.178.3:8000',
        changeOrigin: true
      },
      '/maomaobot': {
        target: 'http://192.168.178.3:7000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/maomaobot/, '')
      }
    }
  }
})
