package com.example.gallery_app.storageAccess

enum class SortBy {
    NAME,DATE_MODIFIED
}

enum class SortOrder {
    ASC, DESC
}

enum class GridSize{
    S1,S2,S3,S4
}

fun getPortraitGridColumns(gridSize: GridSize): Int{
    return when (gridSize) {
        GridSize.S1 -> 3
        GridSize.S2 -> 4
        GridSize.S3 -> 6
        GridSize.S4 -> 8
    }
}

fun getLandscapeGridColumns(gridSize: GridSize): Int{
    return when (gridSize) {
        GridSize.S1 -> 5
        GridSize.S2 -> 7
        GridSize.S3 -> 11
        GridSize.S4 -> 14
    }
}

fun shouldShowFullscreenIcon(gridSize: GridSize): Boolean{
    return when (gridSize) {
        GridSize.S1 -> true
        GridSize.S2 -> true
        GridSize.S3 -> false
        GridSize.S4 -> false
    }
}