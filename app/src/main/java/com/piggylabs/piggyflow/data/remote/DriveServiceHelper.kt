package com.piggylabs.piggyflow.data.remote

import android.content.Context
import android.util.Log
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.FileContent
import com.google.api.client.http.GenericUrl
import com.google.api.client.http.HttpContent
import com.google.api.client.http.HttpRequestFactory
import com.google.api.client.http.HttpResponse
import com.google.api.client.http.HttpResponseException
import com.google.api.client.http.MultipartContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlin.collections.get

class DriveServiceHelper(context: Context, accountEmail: String) {

    private val tag = "DriveServiceHelper"
    private val backupFileName = "piggyflow_backup.db"

    private val credential = GoogleAccountCredential.usingOAuth2(
        context,
        listOf("https://www.googleapis.com/auth/drive.appdata")
    ).apply {
        selectedAccountName = accountEmail
    }

    private val httpTransport = NetHttpTransport()
    private val jsonFactory = GsonFactory.getDefaultInstance()
    private val requestFactory: HttpRequestFactory =
        httpTransport.createRequestFactory { request ->
            credential.initialize(request)
            request.parser = jsonFactory.createJsonObjectParser()
        }

    // -------------------------
    // Upload or Update Database
    // -------------------------
    suspend fun uploadDatabase(context: Context): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val dbFile = context.getDatabasePath("app_database")
                if (!dbFile.exists()) {
                    return@withContext Result.failure(Exception("Database file not found"))
                }

                val existingFileId = findBackupFile()

                // Metadata JSON
                val metadata = mapOf(
                    "name" to backupFileName,
                    "parents" to listOf("appDataFolder")
                )

                val metadataContent = ByteArrayContent(
                    "application/json",
                    GsonFactory.getDefaultInstance()
                        .toByteArray(metadata)
                )

                val fileContent = FileContent("application/octet-stream", dbFile)

                // Google multipart
                val multipart = MultipartContent().apply {
                    boundary = UUID.randomUUID().toString()

                    addPart(
                        MultipartContent.Part()
                            .setContent(metadataContent)
                    )

                    addPart(
                        MultipartContent.Part()
                            .setContent(fileContent)
                    )
                }

                val url: GenericUrl =
                    if (existingFileId.isNullOrEmpty()) {
                        GenericUrl("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart")
                    } else {
                        GenericUrl("https://www.googleapis.com/upload/drive/v3/files/$existingFileId?uploadType=multipart")
                    }

                val request = requestFactory.buildPostRequest(url, multipart)

                try {
                    val response = request.execute()
                    val responseData = response.parseAs(HashMap::class.java)
                    return@withContext Result.success(responseData["id"].toString())
                } catch (e: Exception) {
                    // If file does not exist → create a new one
                    if (e is HttpResponseException && e.statusCode == 404) {
                        Log.w(tag, "Backup file not found, recreating...")

                        val createUrl = GenericUrl(
                            "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
                        )

                        val createRequest = requestFactory.buildPostRequest(createUrl, multipart)
                        val createResponse = createRequest.execute()
                        val newData = createResponse.parseAs(HashMap::class.java)

                        return@withContext Result.success(newData["id"].toString())
                    }

                    throw e
                }
            } catch (e: Exception) {
                Log.e("DriveServiceHelper", "Upload failed", e)
                Result.failure(e)
            }
        }

    // -------------------------
    // Download Database File
    // -------------------------
    suspend fun downloadDatabase(context: Context): Result<File> = withContext(Dispatchers.IO) {
        try {
            val fileId = findBackupFile()
                ?: return@withContext Result.failure(Exception("Backup not found"))

            val tempFile = File(context.cacheDir, "temp_backup.db")
            val dbFile = context.getDatabasePath("app_database")

            val walFile = File(dbFile.absolutePath + "-wal")
            val shmFile = File(dbFile.absolutePath + "-shm")

            val downloadUrl = GenericUrl("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
            val request = requestFactory.buildGetRequest(downloadUrl)

            val response: HttpResponse = request.execute()
            FileOutputStream(tempFile).use { output ->
                response.download(output)
            }

            if (tempFile.length() == 0L) {
                return@withContext Result.failure(Exception("Downloaded file is empty"))
            }

            // Delete WAL/SHM before copying
            if (walFile.exists()) walFile.delete()
            if (shmFile.exists()) shmFile.delete()

            tempFile.copyTo(dbFile, overwrite = true)
            tempFile.delete()

            Result.success(dbFile)
        } catch (e: Exception) {
            Log.e(tag, "Download failed", e)
            Result.failure(e)
        }
    }

    // -------------------------
    // Find backup file ID
    // -------------------------
    private suspend fun findBackupFile(): String? = withContext(Dispatchers.IO) {
        try {
            val url = GenericUrl(
                "https://www.googleapis.com/drive/v3/files" +
                        "?spaces=appDataFolder&q=name='$backupFileName' and 'appDataFolder' in parents and trashed=false&fields=files(id)"
            )

            val request = requestFactory.buildGetRequest(url)
            val response = request.execute().parseAs(HashMap::class.java)
            val files = response["files"] as? List<*> ?: return@withContext null
            val file = files.firstOrNull() as? Map<*, *>
            file?.get("id")?.toString()
        } catch (e: Exception) {
            Log.e(tag, "Find failed", e)
            null
        }
    }

    // -------------------------
    // Get Backup Info
    // -------------------------
    suspend fun getBackupInfo(): Result<BackupInfo?> = withContext(Dispatchers.IO) {
        try {
            val url = GenericUrl(
                "https://www.googleapis.com/drive/v3/files" +
                        "?spaces=appDataFolder&q=name='$backupFileName' and trashed=false" +
                        "&fields=files(id,name,size,modifiedTime)"
            )

            val request = requestFactory.buildGetRequest(url)
            val response = request.execute().parseAs(HashMap::class.java)
            val files = response["files"] as? List<*> ?: return@withContext Result.success(null)
            val file = files.firstOrNull() as? Map<*, *> ?: return@withContext Result.success(null)

            val info = BackupInfo(
                fileId = file["id"].toString(),
                fileName = file["name"].toString(),
                size = (file["size"] as? String)?.toLong() ?: 0,
                modifiedTime = System.currentTimeMillis()
            )

            Result.success(info)
        } catch (e: Exception) {
            Log.e(tag, "Info fetch failed", e)
            Result.failure(e)
        }
    }

    // -------------------------
    // Delete Backup File
    // -------------------------
    suspend fun deleteBackup(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val fileId = findBackupFile()
                ?: return@withContext Result.failure(Exception("No backup found"))

            val url = GenericUrl("https://www.googleapis.com/drive/v3/files/$fileId")
            val request = requestFactory.buildDeleteRequest(url)
            request.execute()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Delete failed", e)
            Result.failure(e)
        }
    }
}

class MultipartBuilder {

    private val boundary = UUID.randomUUID().toString()
    private val parts = mutableListOf<ByteArray>()

    fun addJsonPart(jsonString: String): MultipartBuilder {
        val header = """
            --$boundary
            Content-Type: application/json; charset=UTF-8

        """.trimIndent()

        parts.add(header.toByteArray(StandardCharsets.UTF_8))
        parts.add(jsonString.toByteArray(StandardCharsets.UTF_8))
        parts.add("\r\n".toByteArray())
        return this
    }

    fun addFilePart(contentType: String, file: File): MultipartBuilder {
        val header = """
            --$boundary
            Content-Type: $contentType

        """.trimIndent()

        parts.add(header.toByteArray(StandardCharsets.UTF_8))
        parts.add(file.readBytes())
        parts.add("\r\n".toByteArray())
        return this
    }

    fun build(): HttpContent {
        val output = ByteArrayOutputStream()
        parts.forEach { output.write(it) }
        output.write("--$boundary--".toByteArray())

        return ByteArrayContent("multipart/related; boundary=$boundary", output.toByteArray())
    }
}

data class BackupInfo(
    val fileId: String,
    val fileName: String,
    val size: Long,
    val modifiedTime: Long
)