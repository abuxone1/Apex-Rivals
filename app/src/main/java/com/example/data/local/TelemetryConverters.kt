package com.example.data.local

import androidx.room.TypeConverter
import com.example.model.PlatformType
import com.example.model.TelemetrySnapshot
import com.example.model.VehicleType
import org.json.JSONArray
import org.json.JSONObject

class TelemetryConverters {

  @TypeConverter
  fun fromVehicleType(type: VehicleType): String = type.name

  @TypeConverter
  fun toVehicleType(value: String): VehicleType = try {
    VehicleType.valueOf(value)
  } catch (e: Exception) {
    VehicleType.CAR
  }

  @TypeConverter
  fun fromPlatformType(platform: PlatformType): String = platform.name

  @TypeConverter
  fun toPlatformType(value: String): PlatformType = try {
    PlatformType.valueOf(value)
  } catch (e: Exception) {
    PlatformType.ANDROID
  }

  @TypeConverter
  fun fromStringList(list: List<String>?): String {
    return list?.joinToString("|||") ?: ""
  }

  @TypeConverter
  fun toStringList(data: String?): List<String> {
    if (data.isNullOrBlank()) return emptyList()
    return data.split("|||").map { it.trim() }.filter { it.isNotEmpty() }
  }

  companion object {
    fun serializeTelemetryList(snapshots: List<TelemetrySnapshot>): String {
      val array = JSONArray()
      // Downsample if list is very large to keep storage compact & fast
      val step = if (snapshots.size > 500) (snapshots.size / 500).coerceAtLeast(1) else 1
      for (i in snapshots.indices step step) {
        val s = snapshots[i]
        val obj = JSONObject().apply {
          put("t", s.timestampMs)
          put("spd", s.speedKmh.toDouble())
          put("rpm", s.rpm)
          put("g", s.gear)
          put("thr", s.throttle.toDouble())
          put("brk", s.brake.toDouble())
          put("str", s.steerAngleDeg.toDouble())
          put("lean", s.leanAngleDeg.toDouble())
          put("latg", s.lateralG.toDouble())
          put("longg", s.longitudinalG.toDouble())
          put("prg", s.trackProgress.toDouble())
          put("lap", s.lapTimeMs)
          put("sec", s.sector)
        }
        array.put(obj)
      }
      return array.toString()
    }

    fun deserializeTelemetryList(json: String): List<TelemetrySnapshot> {
      if (json.isBlank()) return emptyList()
      val result = mutableListOf<TelemetrySnapshot>()
      try {
        val array = JSONArray(json)
        for (i in 0 until array.length()) {
          val obj = array.getJSONObject(i)
          result.add(
            TelemetrySnapshot(
              timestampMs = obj.optLong("t", 0L),
              speedKmh = obj.optDouble("spd", 0.0).toFloat(),
              rpm = obj.optInt("rpm", 1000),
              gear = obj.optInt("g", 1),
              throttle = obj.optDouble("thr", 0.0).toFloat(),
              brake = obj.optDouble("brk", 0.0).toFloat(),
              steerAngleDeg = obj.optDouble("str", 0.0).toFloat(),
              leanAngleDeg = obj.optDouble("lean", 0.0).toFloat(),
              lateralG = obj.optDouble("latg", 0.0).toFloat(),
              longitudinalG = obj.optDouble("longg", 0.0).toFloat(),
              trackProgress = obj.optDouble("prg", 0.0).toFloat(),
              lapTimeMs = obj.optLong("lap", 0L),
              sector = obj.optInt("sec", 1)
            )
          )
        }
      } catch (e: Exception) {
        // Fallback gracefully
      }
      return result
    }
  }
}
