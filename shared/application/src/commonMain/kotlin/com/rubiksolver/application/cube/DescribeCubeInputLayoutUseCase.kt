package com.rubiksolver.application.cube

import com.rubiksolver.domain.cube.CubeSize

class DescribeCubeInputLayoutUseCase {
    operator fun invoke(size: CubeSize): Int = size.stickersPerFace
}
