import kotlinx.serialization.Serializable

@Serializable
data class Pokemon(
    val height: Int,
    val weight: Int,
    val types: List<TypeSlot>
)

@Serializable
data class TypeSlot(
    val type: Type
)

@Serializable
data class Type(
    val name: String
)

data class Average(val height: Double, val weight: Double)
