import { Wallet } from 'lucide-react'
import { cn } from '@/lib/utils'

export function HeroSceneFallback({ className }: { className?: string }) {
  return (
    <div
      className={cn(
        'relative flex items-center justify-center overflow-hidden rounded-3xl bg-gradient-to-br from-emerald-950 via-slate-900 to-cyan-950',
        className,
      )}
    >
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_50%_40%,rgba(16,185,129,0.25),transparent_55%)]" />
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_80%_70%,rgba(34,211,238,0.15),transparent_40%)]" />

      <div className="relative animate-float">
        <div className="glow-border w-64 rotate-[-6deg] rounded-2xl bg-gradient-to-br from-emerald-600 to-teal-800 p-6 shadow-2xl shadow-emerald-900/50 sm:w-72">
          <div className="mb-6 flex items-center justify-between">
            <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-white/20">
              <Wallet className="h-4 w-4 text-white" />
            </div>
            <span className="rounded-full bg-white/15 px-2 py-0.5 text-xs font-semibold text-emerald-100">
              INR
            </span>
          </div>
          <p className="font-mono text-3xl font-bold text-white">₹12,450</p>
          <div className="mt-6 h-8 w-12 rounded bg-gradient-to-br from-amber-300 to-amber-500" />
        </div>

        <div className="absolute -right-6 -top-4 h-10 w-10 animate-pulse-slow rounded-full bg-gradient-to-br from-amber-300 to-amber-600 shadow-lg shadow-amber-500/40" />
        <div className="absolute -bottom-3 -left-8 h-8 w-8 animate-pulse-slow rounded-full bg-gradient-to-br from-amber-300 to-amber-600 shadow-lg shadow-amber-500/40 [animation-delay:1s]" />
      </div>
    </div>
  )
}
