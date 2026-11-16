package com.rubiksolver.domain.solving

import com.rubiksolver.domain.cube.CubeSize

class UnsupportedMethodException(
    val size: CubeSize,
    val method: Method,
) : IllegalArgumentException("Method $method is not supported for cube size ${size.dimension}x${size.dimension}")
