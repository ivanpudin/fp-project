import * as React from 'react'
import { Link } from 'react-router-dom'
import { 
  AppBar, 
  Box, 
  Toolbar, 
  Typography, 
  Menu, 
  Container, 
  Button, 
  MenuItem, 
  IconButton 
} from '@mui/material'
import { useTheme } from '@mui/material/styles'
import Brightness4Icon from '@mui/icons-material/Brightness4'
import Brightness7Icon from '@mui/icons-material/Brightness7'
import MenuIcon from '@mui/icons-material/Menu'
import { useColorMode } from '../context/ThemeContext'

const PAGES = [
  { label: 'Dashboard', path: '/' }
]

function Header() {
  const theme = useTheme()
  const colorMode = useColorMode()

  const [anchorElNav, setAnchorElNav] = React.useState<null | HTMLElement>(null)


  const handleOpenNavMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorElNav(event.currentTarget)
  }

  const handleCloseNavMenu = () => {
    setAnchorElNav(null)
  }

  return (
    <AppBar position="static">
      <Container maxWidth="xl">
        <Toolbar disableGutters>
          {/* DESKTOP LOGO */}
          <Typography
            variant="h6"
            noWrap
            component={Link}
            to="/"
            sx={{
              mr: 2,
              display: { xs: 'none', sm: 'flex' },
              fontFamily: 'monospace',
              fontWeight: 700,
              letterSpacing: '.08rem',
              color: 'inherit',
              textDecoration: 'none'
            }}
          >
            REPS Management System
          </Typography>

          {/* MOBILE NAV MENU */}
          <Box sx={{ flexGrow: 1, display: { xs: 'flex', sm: 'none' } }}>
            <IconButton
              size="large"
              aria-label="open navigation menu"
              aria-controls="menu-appbar"
              aria-haspopup="true"
              onClick={handleOpenNavMenu}
              color="inherit"
            >
              <MenuIcon />
            </IconButton>
            <Menu
              id="menu-appbar"
              anchorEl={anchorElNav}
              anchorOrigin={{
                vertical: 'bottom',
                horizontal: 'left'
              }}
              keepMounted
              transformOrigin={{
                vertical: 'top',
                horizontal: 'left'
              }}
              open={Boolean(anchorElNav)}
              onClose={handleCloseNavMenu}
              sx={{
                display: { xs: 'block', sm: 'none' }
              }}
            >
              {PAGES.map((page) => (
                <MenuItem 
                  key={page.label} 
                  component={Link} 
                  to={page.path} 
                  onClick={handleCloseNavMenu}
                >
                  <Typography textAlign="center">{page.label}</Typography>
                </MenuItem>
              ))}
            </Menu>
          </Box>

          {/* MOBILE LOGO */}
          <Typography
            variant="h6"
            noWrap
            component={Link}
            to="/"
            sx={{
              mr: 2,
              display: { xs: 'flex', sm: 'none' },
              flexGrow: 1,
              fontFamily: 'monospace',
              fontWeight: 700,
              letterSpacing: '.08rem',
              color: 'inherit',
              textDecoration: 'none'
            }}
          >
            REPS Management System
          </Typography>

          {/* DESKTOP NAV LINKS */}
          <Box sx={{ flexGrow: 1, display: { xs: 'none', sm: 'flex' }, ml: 2 }}>
            {PAGES.map((page) => (
              <Button
                key={page.label}
                component={Link}
                to={page.path}
                onClick={handleCloseNavMenu}
                sx={{ my: 2, color: 'white', display: 'block', textTransform: 'none' }}
              >
                {page.label}
              </Button>
            ))}
          </Box>

          {/* CONTROLS */}
          <Box>
            <IconButton onClick={colorMode.toggleColorMode} color="inherit">
              {theme.palette.mode === 'dark' ? <Brightness7Icon /> : <Brightness4Icon />}
            </IconButton>
          </Box>
        </Toolbar>
      </Container>
    </AppBar>
  )
}

export default Header
