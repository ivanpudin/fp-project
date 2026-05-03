import { createContext, useState, useContext, useCallback, type ReactNode } from "react"
import { Snackbar, Alert, type AlertColor } from "@mui/material"

/**
 * Used to wrap the application to display any backend or database error message
 * Supposed to work anywhere in the app.
 */

interface Status {
  message: string
  severity: AlertColor
}

interface NotificationContextType {
  showNotification: (message: string, severity?: AlertColor) => void
}

const NotificationContext = createContext<NotificationContextType | undefined>(undefined)

export const NotificationProvider = ({ children }: { children: ReactNode }) => {
  const [status, setStatus] = useState<Status | null>(null)

  const showNotification = useCallback((message: string, severity: AlertColor = "error") => {
      setStatus({ message, severity })
    }, [])

  return (
    <NotificationContext.Provider value={{ showNotification }}>
      {children}
      
      <Snackbar 
        open={!!status} 
        autoHideDuration={6000}
        onClose={() => setStatus(null)}
        anchorOrigin={{ vertical: 'top', horizontal: 'center' }}
      >
        <Alert 
          severity={status?.severity} 
          onClose={() => setStatus(null)} 
          variant="filled"
          sx={{ width: '100%', boxShadow: 3 }}
        >
          {status?.message || ''}
        </Alert>
      </Snackbar>
    </NotificationContext.Provider>
  )
}

export const useNotification = () => {
  const context = useContext(NotificationContext)
  if (!context) {
    throw new Error("useNotification must be used within a NotificationProvider")
  }
  return context
}
