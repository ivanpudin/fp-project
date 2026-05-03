import { createTheme, Theme } from '@mui/material/styles'
import tokens from './material-theme.json'


// Extract themes from json file


const { light, dark } = tokens.schemes

const commonConfig = {
  cssVariables: true,
  shape: { borderRadius: 8 },
  typography: {
    fontFamily: 'Roboto, sans-serif',
  },
}

export const lightTheme: Theme = createTheme({
  ...commonConfig,
  palette: {
    mode: 'light',
    primary: {
      main: light.primary,
      onPrimary: light.onPrimary,
      container: light.primaryContainer,
      onContainer: light.onPrimaryContainer,
    },
    secondary: {
      main: light.secondary,
      container: light.secondaryContainer,
      onContainer: light.onSecondaryContainer,
    },
    error: {
      main: light.error,
    },
    background: {
      default: light.background,
      paper: light.surface,
    },
    text: {
      primary: light.onSurface,
      secondary: light.onSurfaceVariant,
    },
    divider: light.outlineVariant,
  },
})

export const darkTheme: Theme = createTheme({
  ...commonConfig,
  palette: {
    mode: 'dark',
    primary: {
      main: dark.primary,
      onPrimary: dark.onPrimary,
      container: dark.primaryContainer,
      onContainer: dark.onPrimaryContainer,
    },
    secondary: {
      main: dark.secondary,
      container: dark.secondaryContainer,
      onContainer: dark.onSecondaryContainer,
    },
    error: {
      main: dark.error,
    },
    background: {
      default: dark.background,
      paper: dark.surface,
    },
    text: {
      primary: dark.onSurface,
      secondary: dark.onSurfaceVariant,
    },
    divider: dark.outlineVariant,
  },
})
