package com.example.smartfeather

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.decodeList
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import org.mindrot.jbcrypt.BCrypt

object SupabaseConfig {
    const val SUPABASE_URL = "https://fgtqbfmnehnzwanzqzyb.supabase.co"
    const val SUPABASE_PUBLISHABLE_KEY = "sb_publishable_hWs6UmOa8zQFICiImm4qvw_1HlUSJ1T"
}

@Serializable
data class UserLoginRow(
    @SerialName("EmployeeId")
    val employeeId: Int,
    @SerialName("Role")
    val role: String,
    @SerialName("Password")
    val passwordHash: String
)

class SupabaseAuthService(
    private val baseUrl: String = SupabaseConfig.SUPABASE_URL,
    private val publishableKey: String = SupabaseConfig.SUPABASE_PUBLISHABLE_KEY
) {
    private val supabase = createSupabaseClient(
        supabaseUrl = baseUrl,
        supabaseKey = publishableKey
    ) {
        install(Postgrest)
    }

    suspend fun signInFlockman(employeeId: String, password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val employeeIdValue = employeeId.toIntOrNull()
                    ?: error("Employee ID should contain numbers only.")

                val user = supabase
                    .from("user")
                    .select {
                        filter {
                            filter("EmployeeId", FilterOperator.EQ, employeeIdValue)
                        }
                        limit(1)
                    }
                    .decodeList<UserLoginRow>()
                    .firstOrNull()
                    ?: error("Employee ID not found.")

                if (!user.role.equals("Flockman", ignoreCase = true)) {
                    error("Only Flockman accounts can sign in on mobile.")
                }

                if (user.passwordHash.isBlank() || !BCrypt.checkpw(password, user.passwordHash)) {
                    error("Invalid employee ID or password.")
                }

                Unit
            }
        }
    }
}
