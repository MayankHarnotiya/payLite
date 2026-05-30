import { TrendingUp, Wallet } from 'lucide-react'
import { formatCurrency, formatRelative } from '@/lib/format'
import type { Wallet as WalletType } from '@/types/api'
import { Card } from '@/components/ui/Card'
import { Spinner } from '@/components/ui/Spinner'

export function BalanceCard({
  wallet,
  loading,
}: {
  wallet?: WalletType
  loading?: boolean
}) {
  return (
    <Card className="relative overflow-hidden border-0 bg-gradient-to-br from-brand-600 via-brand-600 to-brand-800 text-white shadow-xl shadow-brand-600/30">
      <div className="absolute -right-8 -top-8 h-40 w-40 rounded-full bg-white/10 blur-2xl" />
      <div className="absolute -bottom-12 -left-8 h-32 w-32 rounded-full bg-brand-400/30 blur-2xl" />

      <div className="relative">
        <div className="mb-6 flex items-center justify-between">
          <div className="flex items-center gap-2 text-brand-100">
            <Wallet className="h-5 w-5" />
            <span className="text-sm font-medium">Available balance</span>
          </div>
          <div className="flex items-center gap-1 rounded-full bg-white/15 px-2.5 py-1 text-xs font-medium">
            <TrendingUp className="h-3.5 w-3.5" />
            INR
          </div>
        </div>

        {loading ? (
          <div className="flex h-16 items-center">
            <Spinner className="border-white/30 border-t-white" />
          </div>
        ) : (
          <>
            <p className="font-mono text-4xl font-bold tracking-tight sm:text-5xl">
              {formatCurrency(wallet?.balance ?? 0, wallet?.currency ?? 'INR')}
            </p>
            {wallet?.lastUpdated && (
              <p className="mt-3 text-sm text-brand-100/90">
                Updated {formatRelative(wallet.lastUpdated)}
              </p>
            )}
          </>
        )}
      </div>
    </Card>
  )
}
