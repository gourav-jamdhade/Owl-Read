package com.example.owlread.model

import android.util.Log
import org.simpleframework.xml.Attribute
import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Namespace
import org.simpleframework.xml.Root


@Root(name = "rss", strict = false)
data class RssFeed(
    @field:Element(name = "channel")
    var channel: Channel? = null
)

@Root(name = "channel", strict = false)
data class Channel(
    @field:ElementList(name = "item", inline = true)
    var items: List<Chapter>? = null,

    @field:Element(name = "image", required = false)
    @field:Namespace(prefix = "itunes")
    var itunesImage: ItunesImage? = null // Corrected to ItunesImage
)

@Root(name = "image", strict = false)
@Namespace(prefix = "itunes")
data class ItunesImage(
    @field:Attribute(name = "href")
    var href: String? = null
)


@Root(name = "item", strict = false)
data class Chapter(
    @field:Element(name = "title")
    var title: String = "",


    @Namespace(reference = "http://www.itunes.com/dtds/podcast-1.0.dtd")
    @field:Element(name = "duration", required = false)
    var duration: String? = null,


    @field:Element(name = "enclosure", required = false)
    var enclosure: Enclosure? = null

)


@Root(name = "enclosure", strict = false)
data class Enclosure(
    @field:Attribute(name = "url", required = false)
    var url: String = "",
)