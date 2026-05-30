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
