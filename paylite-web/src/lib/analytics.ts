import type { TransactionItem } from '@/types/api'

export interface FlowStats {
  totalIn: number
  totalOut: number
  net: number
  sentCount: number
  receivedCount: number
}

export function computeFlowStats(items: TransactionItem[]): FlowStats {
  return items.reduce<FlowStats>(
    (acc, t) => {
      if (t.type === 'RECEIVED') {
        acc.totalIn += t.amount
        acc.receivedCount += 1
      } else {
        acc.totalOut += t.amount
        acc.sentCount += 1
      }
      acc.net = acc.totalIn - acc.totalOut
      return acc
    },
    { totalIn: 0, totalOut: 0, net: 0, sentCount: 0, receivedCount: 0 },
  )
}

export interface DailyFlow {
  /** ISO date (yyyy-mm-dd) for the bucket. */
  date: string
  label: string
  in: number
  out: number
}

/**
 * Buckets transactions into the last `days` calendar days (oldest → newest),
 * summing inflow/outflow per day. Days with no activity are kept as zeroes so
 * the chart has an even axis.
 */
export function dailyFlow(items: TransactionItem[], days = 7): DailyFlow[] {
  const buckets = new Map<string, DailyFlow>()
  const today = new Date()
  today.setHours(0, 0, 0, 0)

  for (let i = days - 1; i >= 0; i--) {
    const d = new Date(today)
    d.setDate(today.getDate() - i)
    const key = d.toISOString().slice(0, 10)
    buckets.set(key, {
      date: key,
      label: d.toLocaleDateString('en-IN', { weekday: 'short' }),
      in: 0,
      out: 0,
    })
  }

  for (const t of items) {
    const key = new Date(t.createdAt).toISOString().slice(0, 10)
    const bucket = buckets.get(key)
    if (!bucket) continue
    if (t.type === 'RECEIVED') bucket.in += t.amount
    else bucket.out += t.amount
  }

  return [...buckets.values()]
}
