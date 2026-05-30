import { useLocation } from 'react-router-dom'
import { Card } from '@/components/ui/Card'
import { TransferForm } from '@/components/wallet/TransferForm'

export function TransferPage() {
  const location = useLocation()
  const recipient = (location.state as { recipient?: string } | null)?.recipient

  return (
    <div className="mx-auto max-w-lg space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900 sm:text-3xl dark:text-white">Send money</h1>
        <p className="mt-1 text-slate-500">Transfer to any PayLite user by email</p>
      </div>

      <Card>
        <TransferForm defaultRecipient={recipient} />
      </Card>
    </div>
  )
}
