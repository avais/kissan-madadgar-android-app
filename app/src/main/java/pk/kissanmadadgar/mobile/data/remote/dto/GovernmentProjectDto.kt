package pk.kissanmadadgar.mobile.data.remote.dto

data class GovernmentProjectDto(
    val name: String,
    val implementNameUrdu: String? = null,
    val shortName: String? = null,
    val logo: String? = null,
    val audioNarration: String? = null,
    val implementPictures: List<String>? = null
)
