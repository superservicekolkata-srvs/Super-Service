package com.example.localization

enum class AppLanguage(val code: String, val displayName: String, val localName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिंदी"),
    BENGALI("bn", "Bengali", "বাংলা")
}

object AppStrings {
    fun get(key: String, lang: AppLanguage): String {
        return translations[key]?.get(lang) ?: translations[key]?.get(AppLanguage.ENGLISH) ?: key
    }

    private val translations = mapOf(
        "app_title" to mapOf(
            AppLanguage.ENGLISH to "Super Service Kolkata",
            AppLanguage.HINDI to "सुपर सर्विस कोलकाता",
            AppLanguage.BENGALI to "সুপার সার্ভিস কলকাতা"
        ),
        "admin_portal" to mapOf(
            AppLanguage.ENGLISH to "Admin Portal",
            AppLanguage.HINDI to "एडमिन पोर्टल",
            AppLanguage.BENGALI to "অ্যাডমিন পোর্টাল"
        ),
        "staff_portal" to mapOf(
            AppLanguage.ENGLISH to "Staff Portal",
            AppLanguage.HINDI to "स्टाफ पोर्टल",
            AppLanguage.BENGALI to "স্টাফ পোর্টাল"
        ),
        "guest_portal" to mapOf(
            AppLanguage.ENGLISH to "Guest & Client Access",
            AppLanguage.HINDI to "अतिथि एवं ग्राहक प्रवेश",
            AppLanguage.BENGALI to "অতিথি ও গ্রাহক প্রবেশ"
        ),
        "admin_login_sub" to mapOf(
            AppLanguage.ENGLISH to "Owner & Administrator Access (superservice.kolkata@gmail.com)",
            AppLanguage.HINDI to "मालिक और प्रशासक प्रवेश (superservice.kolkata@gmail.com)",
            AppLanguage.BENGALI to "মালিক ও প্রশাসক অ্যাক্সেস (superservice.kolkata@gmail.com)"
        ),
        "staff_login_sub" to mapOf(
            AppLanguage.ENGLISH to "Sign in with Staff ID, Registered Phone, or Email",
            AppLanguage.HINDI to "स्टाफ आईडी, पंजीकृत फोन, या ईमेल से साइन इन करें",
            AppLanguage.BENGALI to "স্টাফ আইডি, নিবন্ধিত ফোন বা ইমেল দিয়ে সাইন ইন করুন"
        ),
        "staff_signup_sub" to mapOf(
            AppLanguage.ENGLISH to "Register new facility technician for Admin approval",
            AppLanguage.HINDI to "एडमिन अनुमोदन के लिए नए तकनीशियन का पंजीकरण करें",
            AppLanguage.BENGALI to "অ্যাডমিনের অনুমোদনের জন্য নতুন টেকনিশিয়ান নিবন্ধন করুন"
        ),
        "login_button" to mapOf(
            AppLanguage.ENGLISH to "Sign In",
            AppLanguage.HINDI to "साइन इन करें",
            AppLanguage.BENGALI to "সাইন ইন করুন"
        ),
        "pending_approval_notice" to mapOf(
            AppLanguage.ENGLISH to "Staff login rejected: Your account is pending approval by the Admin. Please contact Admin.",
            AppLanguage.HINDI to "स्टाफ लॉगिन अस्वीकृत: आपका खाता एडमिन द्वारा अनुमोदन के लिए लंबित है। कृपया एडमिन से संपर्क करें।",
            AppLanguage.BENGALI to "স্টাফ লগইন প্রত্যাখ্যাত: আপনার অ্যাকাউন্টটি অ্যাডমিনের অনুমোদনের জন্য মুলতুবি রয়েছে। অনুগ্রহ করে অ্যাডমিনের সাথে যোগাযোগ করুন।"
        ),
        "rejected_notice" to mapOf(
            AppLanguage.ENGLISH to "Staff login rejected: Your account application was declined by the Admin. Please contact Owner.",
            AppLanguage.HINDI to "स्टाफ लॉगिन अस्वीकृत: आपका आवेदन एडमिन द्वारा अस्वीकार कर दिया गया है। कृपया मालिक से संपर्क करें।",
            AppLanguage.BENGALI to "স্টাফ লগইন প্রত্যাখ্যাত: অ্যাডমিন দ্বারা আপনার আবেদন প্রত্যাখ্যাত হয়েছে। অনুগ্রহ করে মালিকের সাথে যোগাযোগ করুন।"
        ),
        "approval_restricted_badge" to mapOf(
            AppLanguage.ENGLISH to "Approval Restricted: Pending Owner",
            AppLanguage.HINDI to "अनुमोदन प्रतिबंधित: स्वामी का अनुमोदन लंबित",
            AppLanguage.BENGALI to "অনুমোদন সীমাবদ্ধ: মালিকের অনুমোদন মুলতুবি"
        ),
        "owner_approved_badge" to mapOf(
            AppLanguage.ENGLISH to "Owner Approved",
            AppLanguage.HINDI to "स्वामी द्वारा स्वीकृत",
            AppLanguage.BENGALI to "মালিক দ্বারা অনুমোদিত"
        ),
        "approve_btn" to mapOf(
            AppLanguage.ENGLISH to "Approve",
            AppLanguage.HINDI to "स्वीकृत करें",
            AppLanguage.BENGALI to "অনুমোদন করুন"
        ),
        "reject_btn" to mapOf(
            AppLanguage.ENGLISH to "Reject",
            AppLanguage.HINDI to "अस्वीकार करें",
            AppLanguage.BENGALI to "প্রত্যাখ্যান করুন"
        ),
        "switch_account" to mapOf(
            AppLanguage.ENGLISH to "Switch Account / Logout",
            AppLanguage.HINDI to "खाता बदलें / लॉगआउट",
            AppLanguage.BENGALI to "অ্যাকাউন্ট পরিবর্তন / লগআউট"
        ),
        "logout_confirm_title" to mapOf(
            AppLanguage.ENGLISH to "Confirm Sign Out?",
            AppLanguage.HINDI to "क्या आप लॉगआउट करना चाहते हैं?",
            AppLanguage.BENGALI to "লগআউট নিশ্চিত করবেন?"
        ),
        "logout_confirm_desc" to mapOf(
            AppLanguage.ENGLISH to "You will be returned to the authentication portal. Any offline changes are safely cached.",
            AppLanguage.HINDI to "आप प्रमाणीकरण पोर्टल पर वापस आ जाएंगे। ऑफ़लाइन परिवर्तन सुरक्षित हैं।",
            AppLanguage.BENGALI to "আপনি প্রমাণীকরণ পোর্টালে ফিরে যাবেন। অফলাইন পরিবর্তনগুলি নিরাপদে ক্যাশ করা আছে।"
        ),
        "tab_dashboard" to mapOf(
            AppLanguage.ENGLISH to "Admin Ops",
            AppLanguage.HINDI to "एडमिन नियंत्रण",
            AppLanguage.BENGALI to "অ্যাডমিন অপ্স"
        ),
        "tab_tasks" to mapOf(
            AppLanguage.ENGLISH to "Tasks & Duty",
            AppLanguage.HINDI to "कार्य एवं ड्यूटी",
            AppLanguage.BENGALI to "কাজ ও দায়িত্ব"
        ),
        "tab_shifts" to mapOf(
            AppLanguage.ENGLISH to "Shifts Calendar",
            AppLanguage.HINDI to "शिफ्ट कैलेंडर",
            AppLanguage.BENGALI to "শিফট ক্যালেন্ডার"
        ),
        "tab_client" to mapOf(
            AppLanguage.ENGLISH to "Client Portal",
            AppLanguage.HINDI to "ग्राहक पोर्टल",
            AppLanguage.BENGALI to "ক্লায়েন্ট পোর্টাল"
        ),
        "tab_reports" to mapOf(
            AppLanguage.ENGLISH to "Payroll & Billing",
            AppLanguage.HINDI to "वेतन एवं बिलिंग",
            AppLanguage.BENGALI to "বেতন ও বিলিং"
        ),
        "clock_in" to mapOf(
            AppLanguage.ENGLISH to "Clock In (GPS Tagged)",
            AppLanguage.HINDI to "क्लॉक इन (जीपीएस प्रमाणित)",
            AppLanguage.BENGALI to "ক্লক ইন (জিপিএস ট্যাগড)"
        ),
        "clock_out" to mapOf(
            AppLanguage.ENGLISH to "Clock Out & Save Shift",
            AppLanguage.HINDI to "क्लॉक आउट एवं शिफ्ट दर्ज करें",
            AppLanguage.BENGALI to "ক্লক আউট ও শিফট রেকর্ড করুন"
        ),
        "duty_active" to mapOf(
            AppLanguage.ENGLISH to "On Active Duty",
            AppLanguage.HINDI to "सक्रिय ड्यूटी पर",
            AppLanguage.BENGALI to "সক্রিয় ডিউটিতে রয়েছেন"
        ),
        "offline_synced" to mapOf(
            AppLanguage.ENGLISH to "Online & Synced",
            AppLanguage.HINDI to "ऑनलाइन एवं सिंक",
            AppLanguage.BENGALI to "অনলাইন ও সিঙ্কড"
        ),
        "offline_cached" to mapOf(
            AppLanguage.ENGLISH to "Offline Cached Mode",
            AppLanguage.HINDI to "ऑफ़लाइन कैश मोड",
            AppLanguage.BENGALI to "অফলাইন ক্যাশ মোড"
        ),
        "language_select" to mapOf(
            AppLanguage.ENGLISH to "Select Language",
            AppLanguage.HINDI to "भाषा चुनें",
            AppLanguage.BENGALI to "ভাষা নির্বাচন করুন"
        ),
        "kolkata_facilities" to mapOf(
            AppLanguage.ENGLISH to "Kolkata Facilities",
            AppLanguage.HINDI to "कोलकाता सुविधाएं",
            AppLanguage.BENGALI to "কলকাতা সুবিধাসমূহ"
        ),
        "debt_management" to mapOf(
            AppLanguage.ENGLISH to "Debt Portfolio & Recovery",
            AppLanguage.HINDI to "ऋण पोर्टफोलियो और वसूली",
            AppLanguage.BENGALI to "বকেয়া পোর্টফোলিও ও আদায়"
        ),
        "performance_metrics" to mapOf(
            AppLanguage.ENGLISH to "Employee Performance Metrics",
            AppLanguage.HINDI to "कर्मचारी प्रदर्शन मेट्रिक्स",
            AppLanguage.BENGALI to "কর্মচারী কর্মক্ষমতা মেট্রিক্স"
        ),
        "payslip_generator" to mapOf(
            AppLanguage.ENGLISH to "Automated Payslip & Payroll",
            AppLanguage.HINDI to "स्वचालित वेतन पर्ची एवं पेरोल",
            AppLanguage.BENGALI to "স্বয়ংক্রিয় পে-স্লিপ ও বেতন ব্যবস্থা"
        ),
        "export_payroll" to mapOf(
            AppLanguage.ENGLISH to "Export Payroll Data",
            AppLanguage.HINDI to "पेरोल डेटा निर्यात करें",
            AppLanguage.BENGALI to "বেতন ডেটা রপ্তানি করুন"
        )
    )
}
