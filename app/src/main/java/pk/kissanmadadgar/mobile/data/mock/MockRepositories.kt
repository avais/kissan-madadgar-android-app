package pk.kissanmadadgar.mobile.data.mock

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import pk.kissanmadadgar.mobile.domain.model.*
import pk.kissanmadadgar.mobile.domain.repository.*
import java.util.UUID

class MockAuthRepository : AuthRepository {
    private val currentUserState = MutableStateFlow<User?>(null)
    
    // Initial users database
    private val mockUsers = mutableListOf(
        User("usr_farmer", "03001234567", "چوہدری اصغر علی", UserRole.FARMER, null, true),
        User("usr_provider", "03111234567", "میاں اختر رضوان", UserRole.PROVIDER, null, true),
        User("usr_admin", "admin@kissan.pk", "سید زین العابدین", UserRole.ADMIN, null, true)
    )

    override suspend fun login(phoneNumber: String): Result<Boolean> {
        // Any mobile number is allowed. If existing, returns true.
        return Result.success(true)
    }

    override suspend fun verifyOtp(phoneNumber: String, otp: String, role: UserRole): Result<User> {
        if (otp == "0000" || otp.length == 4) {
            val existing = mockUsers.find { it.phoneNumber == phoneNumber && it.role == role }
            val user = if (existing != null) {
                existing
            } else {
                val newUser = User(
                    id = "usr_" + UUID.randomUUID().toString().take(6),
                    phoneNumber = phoneNumber,
                    fullName = if (role == UserRole.FARMER) "کسان دوست" else "سروس پرووائیڈر",
                    role = role,
                    profileImageUrl = null,
                    isActive = true
                )
                mockUsers.add(newUser)
                newUser
            }
            currentUserState.value = user
            return Result.success(user)
        }
        return Result.failure(Exception("غلط او ٹی پی کوڈ"))
    }

    override suspend fun registerFarmer(phoneNumber: String, fullName: String, address: String): Result<User> {
        val newUser = User(
            id = "usr_" + UUID.randomUUID().toString().take(6),
            phoneNumber = phoneNumber,
            fullName = fullName,
            role = UserRole.FARMER,
            profileImageUrl = null,
            isActive = true
        )
        mockUsers.add(newUser)
        currentUserState.value = newUser
        return Result.success(newUser)
    }

    override suspend fun verifySupplierCnic(cnic: String): Result<User> {
        kotlinx.coroutines.delay(1000)
        
        // Extract raw digits
        val digits = cnic.filter { it.isDigit() }
        if (digits.length != 13) {
            return Result.failure(Exception("شناختی کارڈ کا نمبر درست نہیں ہے۔"))
        }
        
        // Standardize as XXXXX-XXXXXXX-X format
        val formattedCnic = "${digits.substring(0, 5)}-${digits.substring(5, 12)}-${digits.substring(12)}"
        
        // Default correct CNIC (00000-0000000-0)
        if (formattedCnic == "00000-0000000-0") {
            val existing = mockUsers.find { it.role == UserRole.PROVIDER }
            val newUser = if (existing != null) {
                val updated = existing.copy(
                    fullName = "چوہدری طارق محمود",
                    phoneNumber = "03007654321"
                )
                mockUsers[mockUsers.indexOf(existing)] = updated
                updated
            } else {
                val created = User(
                    id = "usr_provider",
                    phoneNumber = "03007654321",
                    fullName = "چوہدری طارق محمود",
                    role = UserRole.PROVIDER,
                    profileImageUrl = null,
                    isActive = true
                )
                mockUsers.add(created)
                created
            }
            return Result.success(newUser)
        }
        
        // If it matches 12345-1234567-1, return the mock provider
        if (formattedCnic == "12345-1234567-1") {
            return Result.success(mockUsers.find { it.role == UserRole.PROVIDER }!!)
        }

        // Otherwise generate a new mock provider for demonstration
        val randomPhone = "03${(10..99).random()}${(1000000..9999999).random().toString().take(7)}"
        val newUser = User(
            id = "usr_" + UUID.randomUUID().toString().take(6),
            phoneNumber = randomPhone,
            fullName = "سپلائر",
            role = UserRole.PROVIDER,
            profileImageUrl = null,
            isActive = true
        )
        mockUsers.add(newUser)
        return Result.success(newUser)
    }

    override suspend fun adminLogin(email: String, pass: String): Result<User> {
        if (email.contains("admin") && pass == "admin123") {
            val adminUser = mockUsers.find { it.role == UserRole.ADMIN }!!
            currentUserState.value = adminUser
            return Result.success(adminUser)
        }
        return Result.failure(Exception("غلط ای میل یا پاس ورڈ"))
    }

    override suspend fun getCurrentUser(): User? {
        return currentUserState.value
    }

    override suspend fun logout(): Result<Unit> {
        currentUserState.value = null
        return Result.success(Unit)
    }
    
    fun setCurrentUser(user: User?) {
        currentUserState.value = user
    }
}

class MockMachineryRepository : MachineryRepository {
    private val categories = listOf(
        Category("cat_1", "Tractor", "ٹریکٹر", "tractor"),
        Category("cat_2", "Harvester", "ہارویسٹر", "harvester"),
        Category("cat_3", "Rotavator", "روٹا ویٹر", "rotavator"),
        Category("cat_4", "Seed Drill", "بیج بونے والی مشین", "seed_drill"),
        Category("cat_5", "Cultivator", "ہل", "cultivator"),
        Category("cat_6", "Sprayer", "اسپریئر", "sprayer"),
        Category("cat_7", "Thresher", "تھریشر", "thresher"),
        Category("cat_8", "Laser Leveler", "لیزر لیولر", "laser_leveler"),
        Category("cat_9", "Water Tanker", "پانی کا ٹینکر", "water_tanker")
    )

    private val machineryListState = MutableStateFlow<List<Machinery>>(
        listOf(
            Machinery(
                id = "mach_1",
                providerId = "usr_provider",
                providerName = "میاں اختر رضوان",
                providerPhone = "03111234567",
                categoryId = "cat_4",
                nameUr = "سپر سیڈر",
                descriptionUr = "گندم کی براہ راست بجائی کے لیے سپر سیڈر۔ وقت اور کھاد کی بچت کے لیے بہترین حل۔",
                modelYear = 2025,
                hourlyRate = 2200.0,
                latitude = 32.074,
                longitude = 72.686,
                isAvailable = true,
                status = MachineryStatus.APPROVED,
                imageUrls = listOf("machinery_1", "machinery_2", "machinery_3"),
                rating = 4.9,
                acresDone = 186.0,
                distanceCoveredKm = 94.5
            ),
            Machinery(
                id = "mach_2",
                providerId = "usr_provider_2",
                providerName = "رانا شہباز",
                providerPhone = "03009876543",
                categoryId = "cat_4",
                nameUr = "پاک سپر سیڈر پرو",
                descriptionUr = "اعلیٰ معیار کا سپر سیڈر، بڑے رقبے کے لیے موزوں۔",
                modelYear = 2024,
                hourlyRate = 2000.0,
                latitude = 32.080,
                longitude = 72.690,
                isAvailable = true,
                status = MachineryStatus.APPROVED,
                imageUrls = listOf("machinery_4", "machinery_5", "machinery_6"),
                rating = 4.7,
                acresDone = 142.0,
                distanceCoveredKm = 78.2
            ),
            Machinery(
                id = "mach_3",
                providerId = "usr_provider_3",
                providerName = "حاجی نواز",
                providerPhone = "03334567890",
                categoryId = "cat_4",
                nameUr = "ملت سپر سیڈر ایکسٹرا",
                descriptionUr = "کم فیول میں بہترین کارکردگی۔ تجربہ کار ڈرائیور کے ساتھ۔",
                modelYear = 2026,
                hourlyRate = 2500.0,
                latitude = 32.065,
                longitude = 72.670,
                isAvailable = true,
                status = MachineryStatus.APPROVED,
                imageUrls = listOf("machinery_7", "machinery_8", "machinery_9"),
                rating = 5.0,
                acresDone = 219.0,
                distanceCoveredKm = 121.8
            )
        )
    )

    override fun getCategories(): Flow<List<Category>> = MutableStateFlow(categories)

    override fun getAvailableMachinery(): Flow<List<Machinery>> {
        return machineryListState.map { list ->
            list.filter { it.isAvailable && it.status == MachineryStatus.APPROVED }
        }
    }

    override fun getMachineryByProvider(providerId: String): Flow<List<Machinery>> {
        return machineryListState.map { list ->
            list.filter { it.providerId == providerId }
        }
    }

    override suspend fun getMachineryById(id: String): Machinery? {
        return machineryListState.value.find { it.id == id }
    }

    override suspend fun addMachinery(machinery: Machinery): Result<Unit> {
        val current = machineryListState.value.toMutableList()
        current.add(machinery)
        machineryListState.value = current
        return Result.success(Unit)
    }

    override suspend fun updateMachineryStatus(id: String, status: MachineryStatus): Result<Unit> {
        val current = machineryListState.value.toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index != -1) {
            val item = current[index]
            current[index] = item.copy(status = status)
            machineryListState.value = current
            return Result.success(Unit)
        }
        return Result.failure(Exception("مشینری نہیں ملی"))
    }

    override fun getAllMachineryAdmin(): Flow<List<Machinery>> {
        return machineryListState
    }
}

class MockBookingRepository : BookingRepository {
    private val demoNow = System.currentTimeMillis()

    private val bookingsState = MutableStateFlow<List<Booking>>(
        listOf(
            Booking(
                id = "book_1",
                farmerId = "usr_farmer",
                farmerName = "چوہدری اصغر علی",
                farmerPhone = "03001234567",
                machineryId = "mach_1",
                machineryName = "سپر سیڈر - سرگودھا",
                bookingDate = demoNow + 86400000,
                durationHours = 5,
                totalPrice = 11000.0,
                status = BookingStatus.PENDING,
                createdAt = demoNow,
                locationUr = "چک 47 شمالی، سرگودھا",
                acres = 6.5
            ),
            Booking(
                id = "book_2",
                farmerId = "usr_farmer",
                farmerName = "چوہدری اصغر علی",
                farmerPhone = "03001234567",
                machineryId = "mach_1",
                machineryName = "سپر سیڈر - منظور شدہ",
                bookingDate = demoNow + 172800000,
                durationHours = 4,
                totalPrice = 8800.0,
                status = BookingStatus.ACCEPTED,
                createdAt = demoNow - 3600000,
                locationUr = "بھلوال روڈ، سرگودھا",
                acres = 4.0
            ),
            Booking(
                id = "book_3",
                farmerId = "usr_farmer",
                farmerName = "چوہدری اصغر علی",
                farmerPhone = "03001234567",
                machineryId = "mach_1",
                machineryName = "سپر سیڈر - کام شروع",
                bookingDate = demoNow - 7200000,
                durationHours = 6,
                totalPrice = 13200.0,
                status = BookingStatus.ACTIVE,
                createdAt = demoNow - 7200000,
                locationUr = "کوٹ مومن، سرگودھا",
                acres = 7.5,
                lifecyclePhotos = listOf(
                    BookingLifecyclePhoto(
                        step = BookingPhotoStep.SERVICE_ACQUIRED,
                        imageLabelUr = "سروس حاصل کرنے کی تصویر",
                        uploadedAt = demoNow - 6500000
                    ),
                    BookingLifecyclePhoto(
                        step = BookingPhotoStep.SUBSIDY_STARTED,
                        imageLabelUr = "سبسڈی شروع ہونے کی تصویر",
                        uploadedAt = demoNow - 5400000
                    )
                )
            ),
            Booking(
                id = "book_4",
                farmerId = "usr_farmer",
                farmerName = "چوہدری اصغر علی",
                farmerPhone = "03001234567",
                machineryId = "mach_1",
                machineryName = "سپر سیڈر - کسان تصدیق باقی",
                bookingDate = demoNow - 14400000,
                durationHours = 5,
                totalPrice = 11000.0,
                status = BookingStatus.ACTIVE,
                createdAt = demoNow - 14400000,
                locationUr = "ساہیوال روڈ، سرگودھا",
                acres = 5.0,
                lifecyclePhotos = listOf(
                    BookingLifecyclePhoto(
                        step = BookingPhotoStep.SERVICE_ACQUIRED,
                        imageLabelUr = "سروس حاصل کرنے کی تصویر",
                        uploadedAt = demoNow - 13200000
                    ),
                    BookingLifecyclePhoto(
                        step = BookingPhotoStep.SUBSIDY_STARTED,
                        imageLabelUr = "سبسڈی شروع ہونے کی تصویر",
                        uploadedAt = demoNow - 12100000
                    ),
                    BookingLifecyclePhoto(
                        step = BookingPhotoStep.WORK_COMPLETED,
                        imageLabelUr = "کام مکمل ہونے کی تصویر",
                        uploadedAt = demoNow - 9800000
                    )
                )
            ),
            Booking(
                id = "book_5",
                farmerId = "usr_farmer",
                farmerName = "چوہدری اصغر علی",
                farmerPhone = "03001234567",
                machineryId = "mach_1",
                machineryName = "سپر سیڈر - مسترد شدہ",
                bookingDate = demoNow + 259200000,
                durationHours = 3,
                totalPrice = 6600.0,
                status = BookingStatus.REJECTED,
                createdAt = demoNow - 18000000,
                locationUr = "جھال چکیاں، سرگودھا",
                acres = 3.0,
                rejectionReason = "اس وقت مشین دستیاب نہیں۔"
            ),
            Booking(
                id = "book_6",
                farmerId = "usr_farmer",
                farmerName = "چوہدری اصغر علی",
                farmerPhone = "03001234567",
                machineryId = "mach_1",
                machineryName = "سپر سیڈر - مکمل",
                bookingDate = demoNow - 86400000,
                durationHours = 8,
                totalPrice = 17600.0,
                status = BookingStatus.COMPLETED,
                createdAt = demoNow - 90000000,
                locationUr = "سلانوالی، سرگودھا",
                acres = 9.0,
                lifecyclePhotos = listOf(
                    BookingLifecyclePhoto(
                        step = BookingPhotoStep.SERVICE_ACQUIRED,
                        imageLabelUr = "سروس حاصل کرنے کی تصویر",
                        uploadedAt = demoNow - 85000000
                    ),
                    BookingLifecyclePhoto(
                        step = BookingPhotoStep.SUBSIDY_STARTED,
                        imageLabelUr = "سبسڈی شروع ہونے کی تصویر",
                        uploadedAt = demoNow - 83000000
                    ),
                    BookingLifecyclePhoto(
                        step = BookingPhotoStep.WORK_COMPLETED,
                        imageLabelUr = "کام مکمل ہونے کی تصویر",
                        uploadedAt = demoNow - 80000000
                    ),
                    BookingLifecyclePhoto(
                        step = BookingPhotoStep.FARMER_CONFIRMATION,
                        imageLabelUr = "کسان کی تصدیقی تصویر",
                        uploadedAt = demoNow - 79000000
                    )
                )
            ),
            Booking(
                id = "book_7",
                farmerId = "usr_farmer",
                farmerName = "چوہدری اصغر علی",
                farmerPhone = "03001234567",
                machineryId = "mach_1",
                machineryName = "سپر سیڈر - منسوخ",
                bookingDate = demoNow + 345600000,
                durationHours = 2,
                totalPrice = 4400.0,
                status = BookingStatus.CANCELLED,
                createdAt = demoNow - 22000000,
                locationUr = "لکسیاں، سرگودھا",
                acres = 2.0
            )
        )
    )

    override fun getBookingsForFarmer(farmerId: String): Flow<List<Booking>> {
        return bookingsState.map { list ->
            list.filter { it.farmerId == farmerId || it.farmerId == "usr_farmer" }
                .distinctBy { it.id }
        }
    }

    override fun getBookingsForProvider(providerId: String): Flow<List<Booking>> {
        // Any mock booking maps to our mock provider for demonstration
        return bookingsState
    }

    override suspend fun createBooking(booking: Booking): Result<Unit> {
        val current = bookingsState.value.toMutableList()
        current.add(booking)
        bookingsState.value = current
        return Result.success(Unit)
    }

    override suspend fun updateBooking(booking: Booking): Result<Unit> {
        val current = bookingsState.value.toMutableList()
        val index = current.indexOfFirst { it.id == booking.id }
        if (index != -1) {
            current[index] = booking
            bookingsState.value = current
            return Result.success(Unit)
        }
        return Result.failure(Exception("بکنگ نہیں ملی"))
    }

    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): Result<Unit> {
        val current = bookingsState.value.toMutableList()
        val index = current.indexOfFirst { it.id == bookingId }
        if (index != -1) {
            val item = current[index]
            current[index] = item.copy(status = status)
            bookingsState.value = current
            return Result.success(Unit)
        }
        return Result.failure(Exception("بکنگ نہیں ملی"))
    }

    override fun getAllBookingsAdmin(): Flow<List<Booking>> {
        return bookingsState
    }
}
