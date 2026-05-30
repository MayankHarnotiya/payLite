import { TrendingUp, Wallet } from 'lucide-react'
import { motion } from 'framer-motion'
import { formatCurrency, formatRelative } from '@/lib/format'
import type { Wallet as WalletType } from '@/types/api'
import { Spinner } from '@/components/ui/Spinner'

export function BalanceCard({
  wallet,
  loading,
}: {
  wallet?: WalletType
  loading?: boolean
}) {
  return (
    <div className="glow-border relative overflow-hidden rounded-3xl bg-gradient-to-br from-emerald-600 via-teal-700 to-slate-900 p-8 shadow-2xl shadow-emerald-900/40">
      <div className="pointer-events-none absolute -right-16 -top-16 h-56 w-56 rounded-full bg-emerald-400/20 blur-3xl" />
      <div className="pointer-events-none absolute -bottom-20 -left-10 h-48 w-48 rounded-full bg-cyan-400/15 blur-3xl" />
      <div className="pointer-events-none absolute inset-0 bg-[linear-gradient(135deg,rgba(255,255,255,0.12)_0%,transparent_50%)]" />

      <div className="relative">
        <div className="mb-8 flex items-center justify-between">
          <div className="flex items-center gap-2 text-emerald-100/90">
            <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-white/15 backdrop-blur">
              <Wallet className="h-4 w-4" />
            </div>
            <span className="text-sm font-medium tracking-wide uppercase">Available balance</span>
          </div>
          <div className="flex items-center gap-1.5 rounded-full border border-white/20 bg-white/10 px-3 py-1 text-xs font-semibold backdrop-blur">
            <TrendingUp className="h-3.5 w-3.5" />
            {wallet?.currency ?? 'INR'}
          </div>
        </div>

        {loading ? (
          <div className="flex h-20 items-center">
            <Spinner className="border-white/30 border-t-white" />
          </div>
        ) : (
          <motion.div
            key={wallet?.balance}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.4 }}
          >
            <p className="font-mono text-5xl font-bold tracking-tight text-white sm:text-6xl">
              {formatCurrency(wallet?.balance ?? 0, wallet?.currency ?? 'INR')}
            </p>
            {wallet?.lastUpdated && (
              <p className="mt-4 text-sm text-emerald-100/70">
                Updated {formatRelative(wallet.lastUpdated)}
              </p>
            )}
          </motion.div>
        )}

        <div className="mt-8 flex gap-3">
          <div className="h-8 w-12 rounded-md bg-gradient-to-br from-amber-300 to-amber-500 opacity-90" />
          <div className="flex flex-1 flex-col justify-center gap-1">
            <div className="h-1.5 w-full max-w-[120px] rounded-full bg-white/30" />
            <div className="h-1.5 w-16 rounded-full bg-white/20" />
          </div>
        </div>
      </div>
    </div>
  )
}
