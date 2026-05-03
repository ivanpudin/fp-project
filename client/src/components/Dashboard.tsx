import { useState, useEffect } from 'react'
import {
  Box,
  Container,
  Typography,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  CircularProgress,
  Alert,
  Chip,
  TablePagination
} from '@mui/material'
import { useNotification } from '../context/NotificationContext'
import { DataRow, DataStatus, AnalyticsResponse } from '../types/schema'
import Analytics from '../components/Analytics'
import Filters from '../components/Filters'

const mapSeverity = (color: string): 'success' | 'warning' | 'error' | 'info' => {
  switch (color.toLowerCase()) {
    case 'green': return 'success'
    case 'yellow': return 'warning'
    case 'red': return 'error'
    default: return 'info'
  }
}

const formatDate = (dateString: string) => {
  try {
    const date = new Date(dateString)
    return new Intl.DateTimeFormat('en-GB', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(date)
  } catch (error) {
    return dateString
  }
}

export default function Dashboard() {
  const [data, setData] = useState<DataRow[]>([])
  const [systemStatus, setSystemStatus] = useState<DataStatus | null>(null)
  const [analytics, setAnalytics] = useState<AnalyticsResponse | null>(null)
  const [loading, setLoading] = useState(true)
  
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(10)

  const { showNotification } = useNotification()

  useEffect(() => {
    const fetchData = async () => {
      try {
        const response = await fetch('/api/get-data')
        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }
        
        const result = await response.json()
        setSystemStatus(result.status)
        setData(result.data)
        setAnalytics(result.analytics)
      } catch (error: any) {
        showNotification(`Failed to load data: ${error.message}`, 'error')
      } finally {
        setLoading(false)
      }
    }

    fetchData()
  }, [showNotification])

  const handleFilterSuccess = (newData: DataRow[], newStatus: DataStatus, newAnalytics: AnalyticsResponse) => {
    setData(newData)
    setSystemStatus(newStatus)
    setAnalytics(newAnalytics)
    setPage(0)
  }

  const handleChangePage = (event: unknown, newPage: number) => {
    setPage(newPage)
  }

  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10))
    setPage(0) 
  }

  const displayedData = data.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
        Energy Production Overview
      </Typography>

      {systemStatus && (
        <Alert 
          severity={mapSeverity(systemStatus.severity)} 
          sx={{ mb: 3, boxShadow: 1 }}
        >
          {systemStatus.message}
        </Alert>
      )}

      <Analytics analyticsData={analytics} />

      <Filters 
        onFilterSuccess={handleFilterSuccess} 
        setLoading={setLoading} 
      />

      {/* Conditionally render the table OR the loading circle here */}
      {loading ? (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}>
          <CircularProgress />
        </Box>
      ) : (
        <Paper elevation={2} sx={{ width: '100%', overflow: 'hidden' }}>
          <TableContainer>
            <Table stickyHeader sx={{ minWidth: 650 }} aria-label="energy data table">
              <TableHead>
                <TableRow>
                  <TableCell sx={{ bgcolor: 'background.default' }}><strong>Energy Type</strong></TableCell>
                  <TableCell sx={{ bgcolor: 'background.default' }}><strong>Start Date</strong></TableCell>
                  <TableCell sx={{ bgcolor: 'background.default' }}><strong>End Date</strong></TableCell>
                  <TableCell sx={{ bgcolor: 'background.default' }} align="right"><strong>Production (MWh)</strong></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {data.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={4} align="center" sx={{ py: 3 }}>
                      <Typography variant="body1" color="text.secondary">
                        No data available.
                      </Typography>
                    </TableCell>
                  </TableRow>
                ) : (
                  displayedData.map((row, index) => (
                    <TableRow key={index} hover>
                      <TableCell component="th" scope="row">
                        <Chip 
                          label={row.energyType} 
                          color="primary" 
                          variant="outlined" 
                          size="small" 
                        />
                      </TableCell>
                      <TableCell>{formatDate(row.startDate)}</TableCell>
                      <TableCell>{formatDate(row.endDate)}</TableCell>
                      <TableCell align="right">{row.energyProduction.toFixed(2)}</TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </TableContainer>
          
          <TablePagination
            rowsPerPageOptions={[10, 25, 50, 100]}
            component="div"
            count={data.length}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={handleChangePage}
            onRowsPerPageChange={handleChangeRowsPerPage}
          />
        </Paper>
      )}
    </Container>
  )
}
