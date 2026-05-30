import { useMemo } from 'react'
import type { DailyFlow } from '@/lib/analytics'
import { formatCurrency } from '@/lib/format'

/**
 * Dependency-free grouped bar chart of daily inflow vs outflow.
 * Pure SVG so it scales crisply and adds no bundle weight.
 */
export function FlowChart({ data }: { data: DailyFlow[] }) {
  const max = useMemo(
    () => Math.max(1, ...data.flatMap((d) => [d.in, d.out])),
    [data],
  )

  const hasActivity = data.some((d) => d.in > 0 || d.out > 0)

  return (
    <div>
      <div className="mb-3 flex items-center gap-4 text-xs text-slate-500">
        <span className="flex items-center gap-1.5">
          <span className="h-2.5 w-2.5 rounded-sm bg-emerald-500" /> In
        </span>
        <span className="flex items-center gap-1.5">
          <span className="h-2.5 w-2.5 rounded-sm bg-orange-400" /> Out
        </span>
      </div>

      <div className="flex h-40 items-end gap-2">
        {data.map((d) => {
          const inH = (d.in / max) * 100
          const outH = (d.out / max) * 100
          return (
            <div key={d.date} className="flex flex-1 flex-col items-center gap-1.5">
              <div className="flex h-full w-full items-end justify-center gap-1">
                <div
                  className="w-1/2 max-w-3 rounded-t bg-emerald-500 transition-all"
                  style={{ height: `${inH}%` }}
                  title={`In: ${formatCurrency(d.in)}`}
                />
                <div
                  className="w-1/2 max-w-3 rounded-t bg-orange-400 transition-all"
                  style={{ height: `${outH}%` }}
                  title={`Out: ${formatCurrency(d.out)}`}
                />
              </div>
              <span className="text-[11px] text-slate-400">{d.label}</span>
            </div>
          )
        })}
      </div>

      {!hasActivity && (
        <p className="mt-3 text-center text-xs text-slate-400">
          No activity in the last 7 days yet.
        </p>
      )}
    </div>
  )
}
