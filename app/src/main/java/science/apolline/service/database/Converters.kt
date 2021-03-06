package science.apolline.service.database

import android.arch.persistence.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.util.*

/**
 * Created by sparow on 10/31/17.
 */
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = if (value == null) null else Date(value)

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time

    @TypeConverter
    fun fromData(data: String?): JsonObject? {
        if( !data.equals("null") && data!=null){
            val gson = Gson()
            return  gson.fromJson(data, JsonObject::class.java)
        }
        return null
    }

    @TypeConverter
    fun dataToString(data: JsonObject?): String? {
        val gson = Gson()
        return gson.toJson(data)
    }
}