import { StrictMode, Suspense } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.tsx'
import CssBaseline from '@mui/material/CssBaseline'
import { MyThemeProvider } from './context/ThemeContext'
import { NotificationProvider } from './context/NotificationContext'
import CircularProgress from '@mui/material/CircularProgress'
import Box from '@mui/material/Box'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <MyThemeProvider>
      <CssBaseline />
      <NotificationProvider>
        <Suspense 
          fallback={
            <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100vh', bgcolor: 'background.default' }}>
              <CircularProgress />
            </Box>
          }
        >
          <App />
        </Suspense>
      </NotificationProvider>
    </MyThemeProvider>
  </StrictMode>,
)
