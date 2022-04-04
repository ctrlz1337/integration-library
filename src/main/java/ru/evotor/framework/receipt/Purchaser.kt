package ru.evotor.framework.receipt

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import ru.evotor.IBundlable
import ru.evotor.framework.core.IntegrationLibraryParsingException

/**
 * Реквизиты покупателя, которые могут быть записаны в [печатную группу чека][ru.evotor.framework.receipt.PrintGroup].
 *
 * Реквизиты покупателя необходимо указывать при расчёте между организациями и (или) индивидуальными предпринимателями, наличными или банковской картой.
 *
 * @property name Наименование покупателя, например, название организации. Данные сохраняются в теге 1227 фискального документа.
 * @property innNumber Номер ИНН покупателя. Данные сохраняются в теге 1228 фискального документа.
 * @property birthDate Дата рождения покупателя. Данные сохраняются в теге 1243 фискального документа.
 * @property documentTypeCode Код вида документа, удостоверяющего личность. Данные сохраняются в теге 1245 фискального документа.
 * @property documentNumber Данные документа, удостоверяющего личность. Данные сохраняются в теге 1246 фискального документа.
 * @property type Тип покупателя, например, физическое лицо. Не сохраняется в фискальном документе.
 */
data class Purchaser(
        val name: String,
        val innNumber: String?,
        val birthDate: String?,
        val documentTypeCode: Int?,
        val documentNumber: String?,
        val type: PurchaserType?
) : Parcelable, IBundlable {

    override fun toBundle(): Bundle {
        return Bundle().apply {
            putString(KEY_NAME, name)
            putString(KEY_INN_NUMBER_V2, innNumber)
            putString(KEY_BIRTH_DATE_V2, birthDate)
            putInt(KEY_DOCUMENT_TYPE_CODE_V2, documentTypeCode ?: -1)
            putString(KEY_DOCUMENT_NUMBER, documentNumber ?: innNumber)
            putString(KEY_DOCUMENT_NUMBER_V2, documentNumber)
            putInt(KEY_TYPE, type?.ordinal ?: -1)
            putInt(KEY_BUNDLE_VERSION, BUNDLE_VERSION)
        }
    }

    private constructor(parcel: Parcel) : this(
            parcel.readString()
                ?: throw IntegrationLibraryParsingException(Purchaser::class.java),
            parcel.readString()
                ?: throw IntegrationLibraryParsingException(Purchaser::class.java),
            parcel.readString()
                ?: throw IntegrationLibraryParsingException(Purchaser::class.java),
            parcel.readInt(),
            parcel.readString()
                ?: throw IntegrationLibraryParsingException(Purchaser::class.java),
            if (parcel.readInt() == 0) null else PurchaserType.values()[parcel.readInt() % PurchaserType.values().size]
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(innNumber)
        parcel.writeString(birthDate)
        parcel.writeInt(documentTypeCode ?: -1)
        parcel.writeString(documentNumber)
        parcel.writeInt(if (type == null) 0 else 1)
        type?.let { parcel.writeInt(it.ordinal) }
    }

    override fun describeContents(): Int = 0

    companion object {

        @JvmField
        val CREATOR = object : Parcelable.Creator<Purchaser> {
            override fun createFromParcel(parcel: Parcel) = Purchaser(parcel)
            override fun newArray(size: Int) = arrayOfNulls<Purchaser>(size)
        }

        private const val KEY_NAME = "KEY_NAME"
        private const val KEY_INN_NUMBER_V2 = "KEY_INN_NUMBER_V2"
        private const val KEY_BIRTH_DATE_V2 = "KEY_BIRTH_DATE_V2"
        private const val KEY_DOCUMENT_TYPE_CODE_V2 = "KEY_DOCUMENT_TYPE_CODE_V2"
        private const val KEY_DOCUMENT_NUMBER = "KEY_DOCUMENT_NUMBER"
        private const val KEY_DOCUMENT_NUMBER_V2 = "KEY_DOCUMENT_NUMBER_V2"
        private const val KEY_TYPE = "KEY_TYPE"
        private const val KEY_BUNDLE_VERSION = "KEY_BUNDLE_VERSION"
        private const val BUNDLE_VERSION = 2

        fun fromBundle(bundle: Bundle?): Purchaser? {
            return bundle?.let {
                val bundleVersion = it.getInt(KEY_BUNDLE_VERSION, 1)
                val name = it.getString(KEY_NAME) ?: return null
                val innNumber = it.getString(KEY_NAME) ?: return null
                val birthDate = it.getString(KEY_NAME) ?: return null
                val documentTypeCode = it.getInt(KEY_NAME)
                val documentNumber =
                    if(bundleVersion == BUNDLE_VERSION) it.getString(KEY_DOCUMENT_NUMBER_V2) ?: return null
                    else it.getString(KEY_DOCUMENT_NUMBER) ?: return null
                Purchaser(name, innNumber, birthDate, documentTypeCode, documentNumber,
                    it.getInt(KEY_TYPE).let {
                        if (it == -1) {
                            null
                        } else {
                            PurchaserType.values()[it % PurchaserType.values().size]
                        }
                    }
                )
            }
        }
    }
}

/**
 * Тип покупателя. Не сохраняется в фискальном документе.
 */
enum class PurchaserType {

    /**
     * Физическое лицо.
     */
    NATURAL_PERSON,

    /**
     * Индивидуальный предприниматель.
     */
    ENTREPRENEUR,

    /**
     * Юридическое лицо.
     */
    LEGAL_ENTITY
}

/**
 * Значения реквизита "код вида документа, удостоверяющего личность". Данные сохраняются в теге 1245 фискального документа.
 */
enum class DocumentType(documentCode: Int) {

    /**
     * Паспорт гражданина РФ
     */
    PASSPORT_RF(21),

    /**
     * Паспорт гражданина РФ, дипломатический паспорт, служебный паспорт,
     * удостоверяющие личность гражданина Российской Федерации за пределами РФ.
     */
    PASSPORT_DIPLOMATIC(22),

    /**
     * Временное удостоверение личности гражданина Российской Федерации, выдаваемое на период оформления паспорта гражданина РФ.
     */
    TEMP_IDENTIFICATION(26),

    /**
     * Свидетельство о рождении гражданина РФ (для граждан Российской Федерации в возрасте до 14 лет)
     */
    BIRTH_CERTIFICATE(27),

    /**
     * Иные документы, признаваемые документами, удостоверяющими личность гражданина РФ в соответствии с законодательством РФ
     */
    OTHER_IDENTITY_DOC_RF(28),

    /**
     * Паспорт иностранного гражданина
     */
    PASSPORT_FOREIGN_CITIZEN(31),

    /**
     * Иные документы, признаваемые документами,
     * удостоверяющими личность иностранного гражданина в соответствии с законодательством РФ и международным договором РФ
     */
    OTHER_DOC_FOREIGN_CITIZEN_RECOGNIZED_RF(32),

    /**
     * Документ, выданный иностранным государством и признаваемый в соответствии с международным договором РФ в качестве документа,
     * удостоверяющего личность лица без гражданства.
     */
    DOC_BY_FOREIGN_STATE_TO_STATELESS_PERSON(33),

    /**
     * Вид на жительство (для лиц без гражданства)
     */
    RESIDENT_CARD(34),

    /**
     * Разрешение на временное проживание (для лиц без гражданства)
     */
    TEMP_RESIDENCE_PERMIT(35),

    /**
     * Свидетельство о рассмотрении ходатайства о признании лица без гражданства беженцем на территории РФ по существу
     */
    APP_REVIEW_CERTIFICATE_RECOGNITION_REFUGEE_PERSON_RF(36),

    /**
     * Удостоверение беженца
     */
    REFUGEE_IDENTIFICATION(37),

    /**
     * Иные документы, признаваемые документами,
     * удостоверяющими личность лиц без гражданства в соответствии с законодательством РФ и международным договором РФ
     */
    OTHER_IDENTITY_DOC_STATELESS_PERSONS(38),

    /**
     * Документ, удостоверяющий личность лица, не имеющего действительного документа,
     * удостоверяющего личность, на период рассмотрения заявления о признании гражданином РФ или о приеме в гражданство РФ
     */
    DOC_FOR_PERIOD_OF_CONSIDERATION_CITIZENSHIP_RF(40),
}
