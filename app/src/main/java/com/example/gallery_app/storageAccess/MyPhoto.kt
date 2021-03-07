package com.example.gallery_app.storageAccess

import android.net.Uri

class MyPhoto(
    var uri: Uri,
    var DATA: String,
    var RELATIVE_PATH: String?,
    var DATE_MODIFIED: String?,
    SIZE: String?,
    var WIDTH: String?,
    var HEIGHT: String?
) {
    var SIZE: String? = "%.2f".format(SIZE?.toDouble()?.div(1024))
    var selected: Boolean = false
    var name: String
    var albumFullPath: String

    init {
        var splitPath = this.DATA.split('/')
        this.name=splitPath.last()
        splitPath=splitPath.dropLast(1)
        this.albumFullPath= splitPath.joinToString(separator = "/")
    }

    override fun toString(): String {
        return "uri:$uri |  name:$name | path(data):${this.DATA} | realtive path:$RELATIVE_PATH | date modified:$DATE_MODIFIED | size:$SIZE | width:$WIDTH | height:$HEIGHT"
    }

//    companion object {
//        private const val serialVersionUID = 20180617104400L
//    }
}



