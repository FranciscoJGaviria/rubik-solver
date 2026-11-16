package com.rubiksolver.presentation.cube3d

import com.rubiksolver.domain.cube.CubeSize
import com.rubiksolver.domain.cube.CubeState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.enum
import io.kotest.property.checkAll

class QuaternionAndProjectionPropertySpec :
    FunSpec({
        val vecArb =
            Arb.bind(
                Arb.double(-10.0, 10.0),
                Arb.double(-10.0, 10.0),
                Arb.double(-10.0, 10.0),
            ) { x, y, z -> Vec3(x, y, z) }

        val axisAngleQuatArb =
            Arb.bind(
                Arb.double(-1.0, 1.0),
                Arb.double(-1.0, 1.0),
                Arb.double(-1.0, 1.0),
                Arb.double(-3.14159, 3.14159),
            ) { ax, ay, az, angle ->
                val raw = Vec3(ax, ay, az)
                val axis = if (raw.length() < 1e-4) Vec3(0.0, 1.0, 0.0) else raw.normalized()
                Quaternion.fromAxisAngle(axis, angle)
            }

        test("unit quaternion rotation preserves Euclidean vector length and round-trips via conjugate") {
            checkAll(vecArb, axisAngleQuatArb) { v, q ->
                val rotated = q.rotate(v)
                rotated.length() shouldBe (v.length() plusOrMinus 1e-6)

                val roundTrip = q.conjugate().rotate(rotated)
                roundTrip.x shouldBe (v.x plusOrMinus 1e-6)
                roundTrip.y shouldBe (v.y plusOrMinus 1e-6)
                roundTrip.z shouldBe (v.z plusOrMinus 1e-6)
            }
        }

        test("painter's algorithm sorts visible quads from farthest to nearest for any camera orbit") {
            val orbitArb =
                Arb.bind(
                    Arb.double(-180.0, 180.0),
                    Arb.double(-75.0, 75.0),
                ) { yaw, pitch -> CameraOrbit(yawDegrees = yaw, pitchDegrees = pitch) }

            checkAll(Arb.enum<CubeSize>(), orbitArb) { size, orbit ->
                val scene =
                    Cube3DProjectionEngine.project(
                        state = CubeState.solved(size),
                        orbit = orbit,
                        animation = null,
                        colorBlindSymbols = true,
                        viewportWidth = 600.0,
                        viewportHeight = 600.0,
                    )
                for (i in 0 until scene.visibleQuads.size - 1) {
                    (scene.visibleQuads[i].depthZ >= scene.visibleQuads[i + 1].depthZ) shouldBe true
                }
            }
        }
    })
