import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { transfer } from '@/api/wallet'
import { walletKeys } from '@/hooks/useWallet'
import { getErrorMessage } from '@/lib/errors'
import { formatCurrency } from '@/lib/format'
import type { TransferResponse, Wallet } from '@/types/api'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Alert } from '@/components/ui/Alert'

const schema = z.object({
  recipientEmail: z.string().email('Enter a valid email'),
  amount: z
    .string()
    .min(1, 'Amount is required')
    .refine((v) => !Number.isNaN(Number(v)) && Number(v) >= 0.01, 'Minimum ₹0.01')
    .refine((v) => Number(v) <= 1_000_000, 'Maximum ₹10,00,000'),
})

type FormData = z.infer<typeof schema>

export function TransferForm({
  onSuccess,
  defaultRecipient,
}: {
  onSuccess?: (result: TransferResponse) => void
  defaultRecipient?: string
}) {
  const queryClient = useQueryClient()
  const {
    register,
    handleSubmit,
    formState: { errors },
    reset,
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { recipientEmail: defaultRecipient ?? '', amount: '' },
  })

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      transfer(
        { recipientEmail: data.recipientEmail, amount: Number(data.amount) },
        crypto.randomUUID(),
      ),
    onSuccess: (result) => {
      // Optimistically reflect the new balance immediately, then revalidate.
      queryClient.setQueryData<Wallet>(walletKeys.wallet, (prev) =>
        prev ? { ...prev, balance: result.newSenderBalance } : prev,
      )
      queryClient.invalidateQueries({ queryKey: walletKeys.wallet })
      queryClient.invalidateQueries({ queryKey: ['transactions'] })
      toast.success(`Sent ${formatCurrency(result.amount, result.currency)}`, {
        description: `To ${result.recipientEmail}`,
      })
      reset({ recipientEmail: '', amount: '' })
      onSuccess?.(result)
    },
    onError: (error) => {
      toast.error('Transfer failed', { description: getErrorMessage(error) })
    },
  })

  return (
    <form
      onSubmit={handleSubmit((data) => {
        mutation.reset()
        mutation.mutate(data)
      })}
      className="space-y-4"
    >
      {mutation.isSuccess && mutation.data && (
        <Alert variant="success" title="Transfer completed">
          Sent {formatCurrency(mutation.data.amount)} to {mutation.data.recipientEmail}. New balance:{' '}
          {formatCurrency(mutation.data.newSenderBalance)}.
        </Alert>
      )}
      {mutation.isError && <Alert variant="error">{getErrorMessage(mutation.error)}</Alert>}

      <Input
        label="Recipient email"
        type="email"
        placeholder="friend@example.com"
        autoComplete="email"
        error={errors.recipientEmail?.message}
        {...register('recipientEmail')}
      />

      <Input
        label="Amount (INR)"
        type="number"
        step="0.01"
        min="0.01"
        placeholder="100.00"
        error={errors.amount?.message}
        {...register('amount')}
      />

      <p className="text-xs text-slate-500 dark:text-slate-400">
        Each transfer uses a unique idempotency key — safe to retry if the network fails.
      </p>

      <Button type="submit" className="w-full" size="lg" loading={mutation.isPending}>
        Send money
      </Button>
    </form>
  )
}
