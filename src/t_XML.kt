package jm.test.Serialize

import jm.lib.Holder
import jm.lib.console.progress
import jm.lib.console.warn
import jm.lib.iterator
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

// -------------------------------------------------------------------------
class TestXML : RandomSerialData() {
  override fun Description() = "Manual Java (org.w3c.dom) XML serialization"
  override fun CloneData(ar : List<MethodItem>) = ar

  // -------------------------------------------------------------------------
  // SAVE
  // -------------------------------------------------------------------------
  fun newElement(par : Element, nm : String) : Element {
    val e = dom.h.createElement(nm) ?: throw RuntimeException("!el[$nm]")
    par.appendChild(e)
    return e
  }

  fun addAttr(par : Element, name : String, v : String) = par.setAttribute(name, v)

  fun addName(par : Element, child : String, p : Named) {
    if (p.isEmpty()) return
    addName(newElement(par, child), p)
  }

  fun addName(e : Element, cl : Named) {
    if (cl.isNotEmpty()) addAttr(e, "name", cl.name)
    if (cl.pkg.isNotEmpty()) addAttr(e, "pkg", cl.pkg)
  }

  fun addTyped(par : Element, p : TypedItem) {
    if (p.isEmpty()) return
    addName(par, p)
    addName(par, "type", p.type)
  }

  fun OutMethod(e : Element, p : MethodItem) {
    addTyped(e, p)
    addName(newElement(e, "src"), p.src)

    if (p.params.isEmpty()) return
    p.params.forEach {
      addTyped(newElement(e, "param"), it)
    }
  }

  private var dom = Holder<Document>()

  override fun StoreArray(ar : List<BaseData.Named>, s : OutputStream) {
    dom = Holder(DocumentBuilderFactory
                   .newInstance()
                   .newDocumentBuilder()
                   .newDocument())
    if (dom.isNull()) throw RuntimeException("!dom")

    // create the root element
    val root = Holder(dom.h.appendChild(dom.h.createElement("ALL")) as Element)
    if (root.isNull()) throw RuntimeException("!root")
    root.h.setAttribute("count", ar.size.toString())

    val cn = ar.size
    ar.forEachIndexed { n, it ->
      progress("Storing %3d%% %d/%d...", n * 100 / cn, n, cn)
      OutMethod(newElement(root.h, "method"), (it as MethodItem))
    }

    val tr = TransformerFactory.newInstance().newTransformer()
    tr.setOutputProperty(OutputKeys.INDENT, "yes")
    tr.setOutputProperty(OutputKeys.METHOD, "xml")
    tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8")
    tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")

    // send DOM to file
    tr.transform(DOMSource(dom.h), StreamResult(s))

    root.close()
    dom.close()
  }

  // -------------------------------------------------------------------------
  // LOAD
  // -------------------------------------------------------------------------
  fun enumAttributes(e : Element, p : Named) {
    for (it in e.attributes)
      when (it.nodeName) {
        "name" -> p.name = it.nodeValue
        "pkg"  -> p.pkg = it.nodeValue
        else   -> warn("Unrecognized attribute ${it.nodeName}")
      }
  }

  fun enumNodes(e : Element, p : Named?) {
    for (it in e.childNodes)
      if (it is Element)
        when (it.nodeName) {
          "type"  -> {
            if (p !is TypedItem) error("Expected typed")
            val v = TypedItem()
            enumAttributes(it, v)
            p.type = if (v.isEmpty()) VOID else v
          }
          "src"   -> {
            if (p !is MethodItem) error("Expected method")
            val v = Named()
            enumAttributes(it, v)
            p.src = if (v.isEmpty()) NONAME else v
          }
          "param" -> {
            if (p !is MethodItem) error("Expected method")
            val v = TypedItem()
            enumAttributes(it, v)
            enumNodes(it, v)
            p.params.add(if (v.isEmpty()) VOID else v)
          }
          else    -> warn("Unrecognized node ${it.nodeName}")
        }
  }

  override fun LoadArray(s : InputStream) : List<BaseData.Named> {
    val ar = ArrayList<BaseData.Named>()
    val doc = Holder(DocumentBuilderFactory
                       .newInstance()
                       .newDocumentBuilder()
                       .parse(s))
    val root = Holder(doc.handle?.documentElement)
    if (root.isNull() || root.h.nodeName != "ALL") error("Invalid classes XML format!")
    try {
      for (it in root.h.childNodes) {
        if (it.nodeName != "method" || it !is Element) continue

        val cl = MethodItem()
        enumAttributes(it, cl)
        enumNodes(it, cl)
        ar.add(cl)

        progress("Loaded %d classes", ar.size)
      }
    } finally {
      root.close()
      doc.close()
    }
    return ar
  }
}

