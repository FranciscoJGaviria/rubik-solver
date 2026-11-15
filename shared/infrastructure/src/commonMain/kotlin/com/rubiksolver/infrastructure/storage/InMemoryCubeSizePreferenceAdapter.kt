package com.rubiksolver.infrastructure.storage

import com.rubiksolver.domain.cube.CubeSize

class InMemoryCubeSizePreferenceAdapter {
    fun defaultSize(): CubeSize = CubeSize.THREE
}
