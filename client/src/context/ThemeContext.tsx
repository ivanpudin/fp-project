import React, { createContext, useContext, useMemo, useState } from 'react'
import { ThemeProvider } from '@mui/material/styles'
import { lightTheme, darkTheme } from './theme'


/**
 * Manages global light/dark mode state and saves user preference
 * Wraps the application with Material-UI's ThemeProvider and a custom Provider
 * It initializes the theme based on localStorage to remember the user's choice across sessions
 */


const ColorModeContext = createContext({ toggleColorMode: () => {} })
export const useColorMode = () => useContext(ColorModeContext)

export const MyThemeProvider = ({ children }: { children: React.ReactNode }) => {
  const [mode, setMode] = useState<'light' | 'dark'>(() => {
    const savedMode = localStorage.getItem('themeMode')
    return (savedMode as 'light' | 'dark') || 'light'
  })

  // Memorize theme
  const colorMode = useMemo(() => ({
    toggleColorMode: () => {
      setMode((prev) => {
          const newMode = prev === 'light' ? 'dark' : 'light'
          localStorage.setItem('themeMode', newMode)
          return newMode
        })
    },
  }), [])

  const theme = useMemo(() => (mode === 'light' ? lightTheme : darkTheme), [mode])

  return (
    <ColorModeContext.Provider value={colorMode}>
      <ThemeProvider theme={theme}>
        {children}
      </ThemeProvider>
    </ColorModeContext.Provider>
  )
}
