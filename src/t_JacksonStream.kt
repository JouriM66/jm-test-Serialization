package jm.test.Serialize

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import jm.lib.PLog
import java.io.InputStream
import java.io.OutputStream

/* Auto serialization by JSON
   Lib: fasterXML-jackson
   Link: https://github.com/FasterXML/jackson
   Lib: jackson-annotations-2.0.4.jar, jackson-core-2.0.4.jar, jackson-databind-2.0.4.jar
 */

// -------------------------------------------------------------------------
class TestJacksonStream : RandomSerialData() {
  override fun Description() = "Manual (stream) serialization to JSON by fasterXML-jackson"
  override fun CloneData(ar : List<MethodItem>) = ar

  // -------------------------------------------------------------------------
  // SAVE
  // -------------------------------------------------------------------------
  fun OutNamed(s : JsonGenerator, p : Named) {
    s.writeFieldName("name")
    s.writeString(p.name)

    if (p.pkg.isNotEmpty()) {
      s.writeFieldName("pkg")
      s.writeString(p.pkg)
    }
  }

  fun SubNamed(s : JsonGenerator, nm : String, p : Named) {
    s.writeFieldName(nm)
    if (p.isNotEmpty()) {
      s.writeStartObject()
      OutNamed(s, p)
      s.writeEndObject()
    }
    else s.writeNull()
  }

  fun OutTyped(s : JsonGenerator, p : TypedItem) {
    OutNamed(s, p)
    if (p.type.isNotEmpty()) SubNamed(s, "type", p.type)
  }

  fun SubTyped(s : JsonGenerator, p : TypedItem) {
    if (p.isNotEmpty()) {
      s.writeStartObject()
      OutTyped(s, p)
      s.writeEndObject()
    } else {
      //s.writeNull()
      s.writeStartObject()
      s.writeEndObject()
    }
  }

  fun OutMethod(s : JsonGenerator, p : MethodItem) {
    s.writeStartObject()
    OutTyped(s, p)
    if (p.src.isNotEmpty()) SubNamed(s, "src", p.src)
    s.writeArrayFieldStart("params")
    for (it in p.params)
      SubTyped(s, it)
    s.writeEndArray()
    s.writeEndObject()
  }

  override fun StoreArray(ar : List<BaseData.Named>, s : OutputStream) {
    JsonFactory().createJsonGenerator(s).use {
      it.writeStartArray()
      for (p in ar)
        OutMethod(it, p as MethodItem)
      it.writeEndArray()
    }
  }

  // -------------------------------------------------------------------------
  // LOAD
  // -------------------------------------------------------------------------
  val log = PLog(false)

  fun loadString(s : JsonParser) : String {
    check(s.currentToken == JsonToken.VALUE_STRING) { "Must be string for ${s.currentName}" }
    log.log{"String: [${s.currentName}] = [${s.text}]"}
    return s.text
  }

  fun <T> loadObject(s : JsonParser,
                     def : T, cb : (s : JsonParser) -> T) : T = log.PROC("OBJECT") {
    val tp = s.currentToken
    if (tp == JsonToken.VALUE_NULL) {
      log{ "Def obj: [$def]" }
      return@PROC def
    }
    if (tp == JsonToken.START_OBJECT) {
      val obj = cb(s)
      log{ "New obj: [${s.currentName}] = [$def" }
      return@PROC obj
    }
    error("Invalid object start type \"%s$tp\" for \"${s.currentName}\"")
  }

  fun <T> loadArray(s : JsonParser,
                    ar : MutableCollection<T>,
                    cb : (s : JsonParser) -> T) = log.PROC("ARRAY") {
    val tp = s.currentToken
    if (tp == JsonToken.VALUE_NULL) {
      log("Array: empty")
      return@PROC
    }
    check(tp == JsonToken.START_ARRAY) { "Expected array start for \"${s.currentName}\" but \"$tp\" found" }
    log{ "Array: start [${s.currentName}]" }
    while (s.nextToken() != JsonToken.END_ARRAY) {
      log{ "Array: get item ${ar.size}" }
      val obj = cb(s)
      log{ "Array: item[${ar.size}] = [$obj]" }
      ar.add(obj)
    }
  }

  fun LoadNamed(s : JsonParser, p : Named, other : (s : JsonParser) -> Unit) : Named {
    while (s.nextToken() != JsonToken.END_OBJECT) {
      val nm = s.currentName  //field name
      s.nextToken() //move to field value
      when (nm) {
        "name" -> p.name = loadString(s)
        "pkg"  -> p.pkg = loadString(s)
        else   -> other(s)
      }
    }
    return p
  }

  fun CreateNamed(s : JsonParser) : Named =
    loadObject(s, NONAME) {
      val p = LoadNamed(s, Named()) {
        error("Unexpected field \"${s.currentName}\" in named")
      }
      if (p.isEmpty()) NONAME else p
    }

  fun LoadTyped(s : JsonParser, p : TypedItem, other : (s : JsonParser) -> Unit) : TypedItem {
    LoadNamed(s, p) {
      when (s.currentName) {
        "type" -> p.type = loadObject(s, VOID) { CreateNamed(s) }
        else   -> other(s)
      }
    }
    return p
  }

  fun CreateTyped(s : JsonParser) : TypedItem =
    loadObject(s, VOID) {
      val p = LoadTyped(s, TypedItem()) {
        error("Unexpected field \"${s.currentName}\" in typed")
      }
      if ( p.isEmpty() ) VOID else p
    }

  fun LoadMethod(s : JsonParser, p : MethodItem) : MethodItem {
    LoadTyped(s, p) {
      when (s.currentName) {
        "src"    -> p.src = loadObject(s, NONAME) { CreateNamed(s) }
        "params" -> loadArray(s, p.params) { CreateTyped(it) }
      }
    }
    return p
  }

  override fun LoadArray(s : InputStream) : List<BaseData.Named> {
    val ar = mutableListOf<MethodItem>()
    JsonFactory().createJsonParser(s).use {
      try {
        it.nextToken()
        loadArray(it, ar) {
          LoadMethod(it, MethodItem())
        }
      } catch(e : Throwable) {
        val loc = it.currentLocation
        val msg = "Parser error (at off: ${loc.charOffset}, l: ${loc.lineNr}, c: ${loc.columnNr}): ${e.message}"
        throw RuntimeException(msg, e)
      }
    }
    return ar
  }
}

