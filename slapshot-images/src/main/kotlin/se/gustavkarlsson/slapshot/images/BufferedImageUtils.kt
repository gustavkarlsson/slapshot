package se.gustavkarlsson.slapshot.images

import java.awt.image.BufferedImage
import java.awt.image.WritableRaster
import java.util.Hashtable
import kotlin.collections.set

internal fun BufferedImage.copy(): BufferedImage {
    return BufferedImage(colorModel, copyRaster(), isAlphaPremultiplied, properties)
}

private fun BufferedImage.copyRaster(): WritableRaster {
    return raster.createCompatibleWritableRaster().also(::copyData)
}

private val BufferedImage.properties: Hashtable<String, Any?>?
    get() {
        if (propertyNames == null) {
            return null
        }
        val newProperties = Hashtable<String, Any?>()
        for (propName in propertyNames) {
            newProperties[propName] = getProperty(propName)
        }
        return newProperties
    }
