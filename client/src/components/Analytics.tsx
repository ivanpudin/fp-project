import { useState, useEffect } from 'react'
import {
  Box,
  Card,
  CardContent,
  Typography,
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  Grid,
  SelectChangeEvent
} from '@mui/material'
import { AnalyticsResponse } from '../types/schema'


// regex to extract the first mode value from the list
// since the list comes as a string, we are pretty much unable to do it the other way
const formatStatValue = (val: string): string => {
  if (!val) return '0'

  const match = val.match(/-?\d+(\.\d+)?/)

  if (match) {
    return Math.round(parseFloat(match[0])).toLocaleString()
  }
  
  return val
}

interface AnalyticsProps {
  analyticsData: AnalyticsResponse | null
}

export default function Analytics({ analyticsData }: AnalyticsProps) {
  const [selectedType, setSelectedType] = useState<string>('')

  useEffect(() => {
    if (analyticsData) {
      const availableTypes = Object.keys(analyticsData)
      if (availableTypes.length > 0) {
        if (!availableTypes.includes(selectedType)) {
          setSelectedType(availableTypes[0])
        }
      } else {
        setSelectedType('')
      }
    }
  }, [analyticsData, selectedType])

  const handleTypeChange = (event: SelectChangeEvent) => {
    setSelectedType(event.target.value)
  }

  if (!analyticsData || Object.keys(analyticsData).length === 0 || !selectedType) {
    return (
      <Typography variant="body1" color="text.secondary" sx={{ mt: 2 }}>
        No analytics data available for the current selection.
      </Typography>
    )
  }

  const safeType = analyticsData[selectedType] ? selectedType : Object.keys(analyticsData)[0]
  const currentStats = analyticsData[safeType]

  const statCards = [
    { label: 'Mean', value: formatStatValue(currentStats.mean) },
    { label: 'Median', value: formatStatValue(currentStats.median) },
    { label: 'Mode', value: formatStatValue(currentStats.mode) }, 
    { label: 'Range', value: formatStatValue(currentStats.range) },
    { label: 'Midrange', value: formatStatValue(currentStats.midrange) },
    { label: 'Total Sum', value: formatStatValue(currentStats.sum) }
  ]

  return (
    <Box sx={{ mb: 4, mt: 4 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
        <Typography variant="h5" sx={{ fontWeight: 'bold' }}>
          Statistical Analysis
        </Typography>

        <FormControl sx={{ minWidth: 200 }} size="small">
          <InputLabel id="energy-type-select-label">Energy Type</InputLabel>
          <Select
            labelId="energy-type-select-label"
            id="energy-type-select"
            value={selectedType}
            label="Energy Type"
            onChange={handleTypeChange}
          >
            {Object.keys(analyticsData).map((type) => (
              <MenuItem key={type} value={type}>
                {type}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </Box>

      <Grid container spacing={2} justifyContent="center">
        {statCards.map((stat, index) => (
          <Grid item xs={12} sm={6} md={4} lg={2} key={index}>
            <Card 
              elevation={2} 
              sx={{ 
                height: '100%',
                minHeight: '130px',
                minWidth: '140px',
                display: 'flex', 
                flexDirection: 'column', 
                justifyContent: 'center',
                textAlign: 'center',
                bgcolor: 'background.paper',
                transition: 'transform 0.2s',
                '&:hover': { transform: 'translateY(-4px)' },
                borderRadius: 3
              }}
            >
              <CardContent sx={{ py: 3 }}>
                <Typography variant="subtitle1" color="text.secondary" gutterBottom>
                  {stat.label}
                </Typography>
                <Typography variant="h4" sx={{ fontWeight: 'medium' }}>
                  {stat.value}
                </Typography>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>
    </Box>
  )
}
