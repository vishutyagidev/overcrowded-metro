package metro.overcrowded.fourtheye.s3

import android.content.Context
import org.json.JSONObject
import java.nio.charset.Charset

data class AwsCredentials(
    val accessKeyId: String,
    val secretAccessKey: String,
    val region: String,
    val bucketName: String
)

object AwsCredentialsProvider {
    fun loadCredentials(context: Context): AwsCredentials {
        val inputStream = context.assets.open("secrets/aws_credentials.json")
        val jsonString = inputStream.readBytes().toString(Charset.defaultCharset())
        val jsonObject = JSONObject(jsonString)

        return AwsCredentials(
            accessKeyId = jsonObject.getString("AWS_ACCESS_KEY_ID"),
            secretAccessKey = jsonObject.getString("AWS_SECRET_ACCESS_KEY"),
            region = jsonObject.getString("AWS_REGION"),
            bucketName = jsonObject.getString("BUCKET_NAME")
        )
    }
}
