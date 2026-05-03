export interface DataRow {
  energyType: string;
  startDate: string;
  endDate: string;
  energyProduction: number;
}

export interface DataStatus {
  severity: string;
  message: string;
}

export interface AnalyticsStats {
  mean: string;
  median: string;
  mode: string;
  range: string;
  midrange: string;
  sum: string;
}

export interface AnalyticsResponse {
  [energyType: string]: AnalyticsStats;
}
