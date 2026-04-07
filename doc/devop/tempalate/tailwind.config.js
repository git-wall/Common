/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        dark: {
          primary: '#1e3a8a', // Deep blue for primary elements
          secondary: '#0f172a', // Dark blue-gray for secondary elements
          accent: '#4f46e5', // Indigo for accent elements
          background: '#0f172a', // Dark blue-gray for main background
          surface: '#1e293b', // Slightly lighter blue-gray for cards/surfaces
          text: '#e2e8f0', // Light gray for text
          muted: '#94a3b8', // Muted text color
          border: '#334155', // Border color
        },
      },
    },
  },
  plugins: [],
};