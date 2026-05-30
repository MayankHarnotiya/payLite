import { useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { ArrowDownLeft, ArrowLeftRight, ArrowUpRight, Plus, Receipt } from 'lucide-react'
import { useAuth } from '@/context/AuthContext'
import { useTransactions, useWallet } from '@/hooks/useWallet'
import { computeFlowStats } from '@/lib/analytics'
import { formatCurrency } from '@/lib/format'
import type { TransactionItem } from '@/types/api'
import { BalanceCard } from '@/components/wallet/BalanceCard'
import { TransactionList } from '@/components/wallet/TransactionList'
import { AddMoneyForm } from '@/components/wallet/AddMoneyForm'
import { TransactionDetailModal } from '@/components/wallet/TransactionDetailModal'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Modal } from '@/components/ui/Modal'
import { Alert } from '@/components/ui/Alert'
import { StatCard } from '@/components/ui/StatCard'
import { AnimatedPage } from '@/components/motion/AnimatedPage'
import { PageHeader } from '@/components/ui/PageHeader'

export function DashboardPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [addMoneyOpen, setAddMoneyOpen] = useState(false)
  const [selected, setSelected] = useState<TransactionItem | null>(null)

  const walletQuery = useWallet()
  const txQuery = useTransactions(0, 50)

  const items = useMemo(() => txQuery.data?.content ?? [], [txQuery.data])
  const stats = useMemo(() => computeFlowStats(items), [items])
  const recent = items.slice(0, 5)

  return (
    <AnimatedPage className="mx-auto max-w-5xl space-y-8">
      <PageHeader
        title={`Hello, ${user?.fullName?.split(' ')[0] ?? 'there'}`}
        subtitle="Here's your wallet overview"
      />

      {walletQuery.isError && (
        <Alert variant="error">Could not load wallet. Please refresh the page.</Alert>
      )}

      <BalanceCard wallet={walletQuery.data} loading={walletQuery.isLoading} />

      <div className="grid gap-4 sm:grid-cols-3">
        <Button className="h-auto flex-col gap-2 py-5" onClick={() => setAddMoneyOpen(true)}>
          <Plus className="h-6 w-6" />
          Add money
        </Button>
        <Link to="/transfer" className="contents">
          <Button variant="secondary" className="h-auto w-full flex-col gap-2 py-5">
            <ArrowLeftRight className="h-6 w-6" />
            Send money
          </Button>
        </Link>
        <Link to="/history" className="contents">
          <Button variant="secondary" className="h-auto w-full flex-col gap-2 py-5">
            <Receipt className="h-6 w-6" />
            Full history
          </Button>
        </Link>
      </div>

      <div className="grid gap-4 sm:grid-cols-2">
        <StatCard
          icon={ArrowDownLeft}
          tone="emerald"
          label="Money in"
          value={formatCurrency(stats.totalIn)}
          hint={`${stats.receivedCount} received`}
          loading={txQuery.isLoading}
        />
        <StatCard
          icon={ArrowUpRight}
          tone="orange"
          label="Money out"
          value={formatCurrency(stats.totalOut)}
          hint={`${stats.sentCount} sent`}
          loading={txQuery.isLoading}
        />
      </div>

      <Card>
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-lg font-semibold text-slate-900 dark:text-white">Recent activity</h2>
          <Link to="/history" className="text-sm font-medium text-brand-600 hover:text-brand-500">
            View all
          </Link>
        </div>
        <TransactionList
          items={recent}
          loading={txQuery.isLoading}
          compact
          onSelect={setSelected}
        />
      </Card>

      <Modal open={addMoneyOpen} onClose={() => setAddMoneyOpen(false)} title="Add money">
        <AddMoneyForm onSuccess={() => setAddMoneyOpen(false)} />
      </Modal>

      <TransactionDetailModal
        txn={selected}
        onClose={() => setSelected(null)}
        onSendAgain={(email) => navigate('/transfer', { state: { recipient: email } })}
      />
    </AnimatedPage>
  )
}
