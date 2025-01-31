package ru.evotor.framework.core.action.event.receipt.payment.combined.event

import android.os.Bundle
import ru.evotor.IBundlable

/**
 * Событие, которое возникает при передаче оплаты чека продажи, возврата БЕЗ чека, покупки, возврата покупки БЕЗ чека и чеков коррекции другому приложению (например, при выборе приложения "Комбооплата").
 *
 * Константа <code>NAME_ACTION</code> указывает, что чек будет оплачен в другом приложении.
 *
 * Чтобы приложение получало событие, значение константы <code>NAME_ACTION</code> необходимо указать в элементе <code><action></code> intent-фильтра соответствующей службы.
 */
class PaymentDelegatorEvent(val receiptUuid: String) : IBundlable {
    override fun toBundle(): Bundle =
        Bundle().apply { putString(KEY_RECEIPT_UUID, receiptUuid) }

    companion object {
        /**
         * Оплата чека будет передана другому приложению.
         *
         * Значение константы: <code>evo.v2.receipt.sell.payment.COMBINED</code>.
         */
        const val NAME_ACTION = "evo.v2.receipt.sell.payment.COMBINED"
        /**
         * Разрешение, которое необходимо указать в манифесте приложения.
         */
        const val NAME_PERMISSION = "ru.evotor.permission.COMBINED"

        private const val KEY_RECEIPT_UUID = "receiptUuid"
        private const val KEY_AVAILABLE_PAYBACK_SUM = "availablePaybackSum"

        fun create(bundle: Bundle?): PaymentDelegatorEvent? {
            if (bundle == null) {
                return null
            }
            val receiptUuid = PaymentDelegatorEvent.getReceiptUuid(bundle) ?: return null
            return PaymentDelegatorEvent(receiptUuid)
        }

        fun getReceiptUuid(bundle: Bundle?): String? =
                bundle?.getString(KEY_RECEIPT_UUID)
    }
}
