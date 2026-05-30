import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { ChevronLeft, ChevronRight, Search, SearchX } from 'lucide-react'
import { useTransactions } from '@/hooks/useWallet'
import { useDebounce } from '@/hooks/useDebounce'
import type { TransactionItem, TransactionType } from '@/types/api'
import { TransactionList } from '@/components/wallet/TransactionList'
import { TransactionDetailModal } from '@/components/wallet/TransactionDetailModal'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Alert } from '@/components/ui/Alert'
import { EmptyState } from '@/components/ui/EmptyState'
import { cn } from '@/lib/utils'

// Backend has no server-side search/filter, so we pull a generous window and
// filter on the client. Plenty for a demo wallet; swap for query params later.
const FETCH_SIZE = 100
const PAGE_SIZE = 10

type Filter = 'ALL' | TransactionType
type Sort = 'recent' | 'amount-desc' | 'amount-asc'

const filters: { value: Filter; label: string }[] = [
  { value: 'ALL', label: 'All' },
  { value: 'RECEIVED', label: 'Received' },
  { value: 'SENT', label: 'Sent' },
]

export function HistoryPage() {
  const navigate = useNavigate()
  const [search, setSearch] = useState('')
  const [filter, setFilter] = useState<Filter>('ALL')
  const [sort, setSort] = useState<Sort>('recent')
  const [page, setPage] = useState(0)
  const [selected, setSelected] = useState<TransactionItem | null>(null)

  const debouncedSearch = useDebounce(search.trim().toLowerCase(), 250)
  const query = useTransactions(0, FETCH_SIZE)
  const all = useMemo(() => query.data?.content ?? [], [query.data])

  const filtered = useMemo(() => {
    let list = all
    if (filter !== 'ALL') list = list.filter((t) => t.type === filter)
    if (debouncedSearch) {
      list = list.filter(
        (t) =>
          t.counterpartyName.toLowerCase().includes(debouncedSearch) ||
          t.counterpartyEmail.toLowerCase().includes(debouncedSearch),
      )
    }
    const sorted = [...list]
    if (sort === 'amount-desc') sorted.sort((a, b) => b.amount - a.amount)
    else if (sort === 'amount-asc') sorted.sort((a, b) => a.amount - b.amount)
    else sorted.sort((a, b) => b.transactionId - a.transactionId)
    return sorted
  }, [all, filter, debouncedSearch, sort])

  // Reset to first page whenever the result set changes shape.
  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE))
  const safePage = Math.min(page, totalPages - 1)
  const pageItems = filtered.slice(safePage * PAGE_SIZE, safePage * PAGE_SIZE + PAGE_SIZE)

  const resetPage = () => setPage(0)

  return (
    <div className="mx-auto max-w-3xl space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 sm:text-3xl dark:text-white">
          Transaction history
        </h1>
        <p className="mt-1 text-slate-500">
          {all.length > 0
            ? `${filtered.length} of ${all.length} transaction${all.length === 1 ? '' : 's'}`
            : 'All sent and received transfers'}
        </p>
      </div>

      {query.isError && (
        <Alert variant="error">Could not load transactions. Please try again.</Alert>
      )}

      {/* Filter bar */}
      <Card padding="md" className="space-y-4">
        <div className="relative">
          <Search className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
          <Input
            placeholder="Search by name or email"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value)
              resetPage()
            }}
            className="pl-10"
            aria-label="Search transactions"
          />
        </div>

        <div className="flex flex-wrap items-center justify-between gap-3">
          <div className="inline-flex rounded-xl bg-slate-100 p-1 dark:bg-slate-800">
            {filters.map((f) => (
              <button
                key={f.value}
                type="button"
                onClick={() => {
                  setFilter(f.value)
                  resetPage()
                }}
                className={cn(
                  'rounded-lg px-3 py-1.5 text-sm font-medium transition-colors',
                  filter === f.value
                    ? 'bg-white text-slate-900 shadow-sm dark:bg-slate-700 dark:text-white'
                    : 'text-slate-500 hover:text-slate-900 dark:hover:text-white',
                )}
              >
                {f.label}
              </button>
            ))}
          </div>

          <select
            value={sort}
            onChange={(e) => {
              setSort(e.target.value as Sort)
              resetPage()
            }}
            aria-label="Sort transactions"
            className="rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm text-slate-700 focus:border-brand-500 focus:outline-none focus:ring-2 focus:ring-brand-500/20 dark:border-slate-700 dark:bg-slate-900 dark:text-slate-200"
          >
            <option value="recent">Most recent</option>
            <option value="amount-desc">Amount: high to low</option>
            <option value="amount-asc">Amount: low to high</option>
          </select>
        </div>
      </Card>

      <Card padding="md">
        {!query.isLoading && all.length > 0 && filtered.length === 0 ? (
          <EmptyState
            icon={SearchX}
            title="No matches"
            description="Try a different name, email, or filter."
          />
        ) : (
          <TransactionList items={pageItems} loading={query.isLoading} onSelect={setSelected} />
        )}

        {totalPages > 1 && (
          <div className="mt-6 flex items-center justify-between border-t border-slate-100 pt-4 dark:border-slate-800">
            <Button
              variant="secondary"
              size="sm"
              disabled={safePage === 0}
              onClick={() => setPage((p) => Math.max(0, p - 1))}
            >
              <ChevronLeft className="h-4 w-4" />
              Previous
            </Button>
            <span className="text-sm text-slate-500">
              Page {safePage + 1} of {totalPages}
            </span>
            <Button
              variant="secondary"
              size="sm"
              disabled={safePage >= totalPages - 1}
              onClick={() => setPage((p) => p + 1)}
            >
              Next
              <ChevronRight className="h-4 w-4" />
            </Button>
          </div>
        )}
      </Card>

      <TransactionDetailModal
        txn={selected}
        onClose={() => setSelected(null)}
        onSendAgain={(email) => navigate('/transfer', { state: { recipient: email } })}
      />
    </div>
  )
}
