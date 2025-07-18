package metro.overcrowded.fourtheye.s3

import android.content.Context
import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3Client
import java.io.File

object S3Uploader {

    fun uploadFile(context: Context, file: File, objectKey: String) {
        val creds = AwsCredentialsProvider.loadCredentials(context)

        val credentials = BasicAWSCredentials(
            creds.accessKeyId,
            creds.secretAccessKey
        )

        val s3Client = AmazonS3Client(
            credentials,
            Region.getRegion(Regions.fromName(creds.region))
        )

        val transferUtility = TransferUtility.builder()
            .context(context.applicationContext)
            .s3Client(s3Client)
            .build()

        val uploadObserver = transferUtility.upload(
            creds.bucketName,
            objectKey,
            file
        )

        uploadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState?) {
                Log.d("S3Uploader", "State: $state")
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {
                val progress = (bytesCurrent.toFloat() / bytesTotal) * 100
                Log.d("S3Uploader", "Progress: $progress%")
            }

            override fun onError(id: Int, ex: Exception?) {
                Log.e("S3Uploader", "Upload error: ${ex?.localizedMessage}")
            }
        })
    }
}
