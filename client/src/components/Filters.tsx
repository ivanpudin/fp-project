import { useState } from 'react'
import {
  Box,
  Button,
  Typography,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  TextField,
  SelectChangeEvent
} from '@mui/material'
import FilterAltOffIcon from '@mui/icons-material/FilterAltOff'
import FilterAltIcon from '@mui/icons-material/FilterAlt'
import CalendarMonthIcon from '@mui/icons-material/CalendarMonth'
import { useNotification } from '../context/NotificationContext'
import { DataRow, DataStatus, AnalyticsResponse } from '../types/schema'

interface FiltersProps {
  onFilterSuccess: (data: DataRow[], status: DataStatus, analytics: AnalyticsResponse) => void
  setLoading: (loading: boolean) => void
}

export default function Filters({ onFilterSuccess, setLoading }: FiltersProps) {
  const { showNotification } = useNotification()

  const [typeModalOpen, setTypeModalOpen] = useState(false)
  const [dateModalOpen, setDateModalOpen] = useState(false)

  const [isTypeFiltered, setIsTypeFiltered] = useState(false)
  const [isDateFiltered, setIsDateFiltered] = useState(false)

  const [energyType, setEnergyType] = useState('Wind')
  const [selectedDate, setSelectedDate] = useState('')
  const [timeBasis, setTimeBasis] = useState('Daily')

  const ENERGY_TYPES = ['Wind', 'Solar', 'Hydro', 'Nuclear', 'Consumption']
  const TIME_BASES = ['Hourly', 'Daily', 'Weekly', 'Monthly']

  const handleClear = async () => {
    setLoading(true)
    try {
      const response = await fetch('/api/update-data')
      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)
      
      const result = await response.json()
      onFilterSuccess(result.data, result.status, result.analytics)
      setIsTypeFiltered(false)
      setIsDateFiltered(false)
      showNotification('Filters cleared and data updated.', 'success')
    } catch (error: any) {
      showNotification(`Failed to clear filters: ${error.message}`, 'error')
    } finally {
      setLoading(false)
    }
  }

  const handleApplyTypeFilter = async () => {
    setTypeModalOpen(false)
    setLoading(true)
    try {
      const response = await fetch(`/api/filtering/energy-type?energyType=${energyType}`)
      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)
      
      const result = await response.json()
      onFilterSuccess(result.data, result.status, result.analytics)
      setIsTypeFiltered(true)
    } catch (error: any) {
      showNotification(`Failed to filter by type: ${error.message}`, 'error')
    } finally {
      setLoading(false)
    }
  }

  const handleApplyDateFilter = async () => {
    if (!selectedDate) {
      showNotification('Please select a date', 'warning')
      return
    }

    setDateModalOpen(false)
    setLoading(true)
    try {
      const [year, month, day] = selectedDate.split('-')
      const formattedDate = `${day}/${month}/${year}`

      const response = await fetch(`/api/filtering/date?date=${formattedDate}&basis=${timeBasis}`)
      if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`)
      
      const result = await response.json()
      onFilterSuccess(result.data, result.status, result.analytics)
      setIsDateFiltered(true)
    } catch (error: any) {
      showNotification(`Failed to filter by date: ${error.message}`, 'error')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Box sx={{ mb: 3 }}>
      <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, flexWrap: 'wrap' }}>
        <Typography variant="h6" sx={{ fontWeight: 'bold', mr: 1 }}>
          Filters:
        </Typography>
        
        <Button 
          variant="outlined" 
          color="inherit" 
          startIcon={<FilterAltOffIcon />}
          onClick={handleClear}
        >
          Clear
        </Button>
        
        <Button 
          variant="contained" 
          startIcon={<FilterAltIcon />}
          onClick={() => setTypeModalOpen(true)}
          disabled={isTypeFiltered}
        >
          By Type
        </Button>
        
        <Button 
          variant="contained" 
          startIcon={<CalendarMonthIcon />}
          onClick={() => setDateModalOpen(true)}
          disabled={isDateFiltered}
        >
          By Date
        </Button>
      </Box>

      <Dialog open={typeModalOpen} onClose={() => setTypeModalOpen(false)} fullWidth maxWidth="xs">
        <DialogTitle>Filter by Energy Type</DialogTitle>
        <DialogContent sx={{ pt: '20px !important' }}>
          <FormControl fullWidth>
            <InputLabel id="type-select-label">Energy Type</InputLabel>
            <Select
              labelId="type-select-label"
              value={energyType}
              label="Energy Type"
              onChange={(e: SelectChangeEvent) => setEnergyType(e.target.value)}
            >
              {ENERGY_TYPES.map(type => (
                <MenuItem key={type} value={type}>{type}</MenuItem>
              ))}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setTypeModalOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleApplyTypeFilter}>Apply</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={dateModalOpen} onClose={() => setDateModalOpen(false)} fullWidth maxWidth="xs">
        <DialogTitle>Filter by Date</DialogTitle>
        <DialogContent sx={{ pt: '20px !important', display: 'flex', flexDirection: 'column', gap: 3 }}>
          <TextField
            label="Reference Date"
            type="date"
            fullWidth
            InputLabelProps={{ shrink: true }}
            value={selectedDate}
            onChange={(e) => setSelectedDate(e.target.value)}
          />
          
          <FormControl fullWidth>
            <InputLabel id="basis-select-label">Time Basis</InputLabel>
            <Select
              labelId="basis-select-label"
              value={timeBasis}
              label="Time Basis"
              onChange={(e: SelectChangeEvent) => setTimeBasis(e.target.value)}
            >
              {TIME_BASES.map(basis => (
                <MenuItem key={basis} value={basis}>{basis}</MenuItem>
              ))}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions sx={{ p: 2 }}>
          <Button onClick={() => setDateModalOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleApplyDateFilter}>Apply</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
