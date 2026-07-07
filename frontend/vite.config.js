import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import { resolve } from 'path'

export default defineConfig({
  plugins: [react()],
  root: '.',
  build: {
    outDir: '../src/main/resources/static',
    emptyOutDir: false,
    rollupOptions: {
      input: {
        'graph-editor': resolve(__dirname, 'graph-editor.html'),
      },
      output: {
        entryFileNames: 'assets/[name]-[hash].js',
        chunkFileNames: 'assets/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash].[ext]'
      }
    }
  },
  server: {
    proxy: {
      '/api': 'http://localhost:8080',
      '/graphs': 'http://localhost:8080',
      '/logout': 'http://localhost:8080',
      '/login': 'http://localhost:8080'
    }
  }
})
