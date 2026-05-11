package com.example.smartfeather

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

object SupabaseProvider {
    val client = createSupabaseClient(
        supabaseUrl = "https://fgtqbfmnehnzwanzqzyb.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImZndHFiZm1uZWhuendhbnpxenliIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzM3Nzg5NjksImV4cCI6MjA4OTM1NDk2OX0.5LEaKlYT_X2tdl6w-wTYMhcanzLH6TpYyjID1gx9AK0"
    ) {
        install(Storage)
    }
}
