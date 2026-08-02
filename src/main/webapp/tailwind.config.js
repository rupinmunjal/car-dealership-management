/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ['./src/**/*.{html,ts}'],
  theme: {
    extend: {
      colors: {
        brand: {
          50:  '#eef3ff',
          100: '#dde6ff',
          200: '#b3caff',
          300: '#80a8ff',
          400: '#4d7cff',
          500: '#2660ff',
          600: '#0052ff',
          700: '#003fcc',
          800: '#002fa8',
          900: '#002285',
          950: '#001155',
        },
        background: '#FAFAFA',
        surface: '#FFFFFF',
        foreground: '#0F172A',
      },
      fontFamily: {
        sans:    ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
        display: ['Inter', 'system-ui', '-apple-system', 'sans-serif'],
        mono:    ['JetBrains Mono', 'monospace'],
      },
      boxShadow: {
        'sm':      '0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04)',
        'md':      '0 4px 6px -1px rgba(0,0,0,0.07), 0 2px 4px -2px rgba(0,0,0,0.05)',
        'lg':      '0 10px 15px -3px rgba(0,0,0,0.08), 0 4px 6px -4px rgba(0,0,0,0.04)',
        'xl':      '0 20px 25px -5px rgba(0,0,0,0.1), 0 8px 10px -6px rgba(0,0,0,0.04)',
        'accent':  '0 4px 14px rgba(0,82,255,0.25)',
        'accent-lg':'0 8px 24px rgba(0,82,255,0.35)',
      },
      backgroundImage: {
        'gradient-brand': 'linear-gradient(135deg, #0052FF 0%, #4D7CFF 100%)',
      },
    },
  },
  plugins: [],
};
