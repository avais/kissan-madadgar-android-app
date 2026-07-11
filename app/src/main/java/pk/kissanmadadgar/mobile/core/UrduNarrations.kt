package pk.kissanmadadgar.mobile.core

import android.content.Context
import pk.kissanmadadgar.mobile.R

object UrduNarrations {
    fun getMachineInfoNarration(
        context: Context,
        userName: String,
        machineName: String,
        ownerName: String,
        distance: String,
        projectName: String,
        subsidyInfo: String
    ): String {
        val cleanDistance = distance.replace("km", "کلومیٹر").replace("Kilometer", "کلومیٹر")
        val cleanSubsidy = subsidyInfo.replace("Rs.", "روپے").replace("Rs", "روپے").replace("per acre", "فی ایکڑ").replace("per", "فی").replace("acre", "ایکڑ")
        val cleanProject = projectName.replace("PCAP", "پی کیپ")
        
        return context.getString(
            R.string.booking_voice_machine_info_narration,
            userName,
            machineName,
            ownerName,
            cleanDistance,
            cleanProject,
            cleanSubsidy
        )
    }

    fun getBookingIntro(context: Context, userName: String, machineName: String, ownerName: String): String {
        return context.getString(R.string.booking_voice_intro, userName, machineName, ownerName)
    }

    fun getBookingDateConfirmation(context: Context, displayDateUr: String): String {
        return context.getString(R.string.booking_voice_date_confirmation, displayDateUr)
    }

    fun getBookingHoursConfirmation(context: Context, hours: Int): String {
        return context.getString(R.string.booking_voice_hours_confirmation, hours)
    }

    fun getBookingAcresConfirmation(context: Context, acres: String): String {
        return context.getString(R.string.booking_voice_acres_confirmation, acres)
    }

    fun getWelcomeNarration(context: Context): String {
        return context.getString(R.string.welcome_voice_narration)
    }

    fun getBookingSuccessNarration(acres: String, ownerName: String, date: String): String {
        val cleanAcres = acres.ifEmpty { "4" }
        return "آپ کی بکنگ $cleanAcres ایکڑ کے لیے، $date تاریخ کو کاشتکاری کا کام کرنے کے لیے کامیابی کے ساتھ درج ہو گئی ہے۔ سروس فراہم کنندہ $ownerName آپ سے جلد ہی رابطہ کریں گے۔"
    }
}
