@file:Suppress("PackageDirectoryMismatch")

package day16

import org.junit.Test
import readPuzzleInputLines
import kotlin.test.assertEquals

class BitStream(var stream: List<Char>) {
    fun pop(bits: Int) = stream.take(bits).toCharArray().concatToString().toInt(2).also {
        stream = stream.drop(bits)
    }

    fun subStream(bits: Int): BitStream {
        return BitStream(stream.take(bits)).also {
            stream = stream.drop(bits)
        }
    }

    fun isNotEmpty() = stream.isNotEmpty()

    companion object {
        fun fromHex(hex: String): BitStream =
            BitStream(hex.flatMap { it.toString().toInt(16).toString(2).padStart(4, '0').toList() })
    }
}

fun parsePacket(bitStream: BitStream): Packet {
    val version = bitStream.pop(3)
    return when (val typeId = bitStream.pop(3)) {
        4 -> LiteralValue.finishParse(version, typeId, bitStream)
        else -> Operator.finishParse(version, typeId, bitStream)
    }
}

sealed class Packet {
    abstract fun versionSum(): Int
}

data class LiteralValue(val version: Int, val typeId: Int, val number: Long) : Packet() {
    companion object {
        fun finishParse(version: Int, typeId: Int, bitStream: BitStream): LiteralValue {
            val number = buildList {
                do {
                    val hasNext = bitStream.pop(1)
                    add(bitStream.pop(4).toString(16))
                } while (hasNext > 0)
            }.joinToString("").toLong(16)
            return LiteralValue(version, typeId, number)
        }
    }

    override fun versionSum() = version
}

data class Operator(val version: Int, val typeId: Int, val subPackets: List<Packet>) : Packet() {
    companion object {
        fun finishParse(version: Int, typeId: Int, bitStream: BitStream): Operator {
            val subPackets = buildList {
                if (bitStream.pop(1) > 0) {
                    // number of sub-packets immediately contained
                    repeat(bitStream.pop(11)) {
                        add(parsePacket(bitStream))
                    }
                } else {
                    val subStream = bitStream.subStream(bitStream.pop(15))
                    while (subStream.isNotEmpty()) {
                        add(parsePacket(subStream))
                    }
                }
            }
            return Operator(version, typeId, subPackets)
        }
    }

    override fun versionSum() = version + subPackets.sumOf { it.versionSum() }
}

class Day16 {


    @Test
    fun main() {
        val parsed = readPuzzleInputLines("Day16")
        val part1 = parsePacket(BitStream.fromHex(parsed.first())).versionSum()
        println("Day 16, Part 1: $part1")
        // assertEquals(, part1)
        val part2 = 2
        println("Day 16, Part 2: $part2")
        //assertEquals(, part2)
    }

    @Test
    fun `test bitstreamFromHex`() {
        assertEquals("110100101111111000101000".toList(), BitStream.fromHex("D2FE28").stream)
        assertEquals(
            "00111000000000000110111101000101001010010001001000000000".toList(),
            BitStream.fromHex("38006F45291200").stream
        )
        assertEquals(
            "11101110000000001101010000001100100000100011000001100000".toList(),
            BitStream.fromHex("EE00D40C823060").stream
        )
    }

    @Test
    fun `test LiteralValue`() {
        assertEquals(2021, (parsePacket(BitStream.fromHex("D2FE28")) as LiteralValue).number)
    }

    @Test
    fun `test Operator`() {
        assertEquals(
            Operator(
                1, 6, listOf(
                    LiteralValue(6, 4, 10),
                    LiteralValue(2, 4, 20),
                )
            ), parsePacket(BitStream.fromHex("38006F45291200"))
        )
        assertEquals(
            Operator(
                7, 3, listOf(
                    LiteralValue(2, 4, 1),
                    LiteralValue(4, 4, 2),
                    LiteralValue(1, 4, 3),
                )
            ), parsePacket(BitStream.fromHex("EE00D40C823060"))
        )
        assertEquals(
            Operator(
                4, 2, listOf(
                    Operator(
                        1, 2, listOf(
                            Operator(
                                5, 2, listOf(
                                    LiteralValue(6, 4, 15),
                                )
                            ),
                        )
                    ),
                )
            ), parsePacket(BitStream.fromHex("8A004A801A8002F478"))
        )
        assertEquals(16, parsePacket(BitStream.fromHex("8A004A801A8002F478")).versionSum())
        assertEquals(
            Operator(
                3, 0, listOf(
                    Operator(
                        0, 0, listOf(
                            LiteralValue(0, 4, 10),
                            LiteralValue(5, 4, 11),
                        )
                    ),
                    Operator(
                        1, 0, listOf(
                            LiteralValue(0, 4, 12),
                            LiteralValue(3, 4, 13),
                        )
                    ),
                )
            ), parsePacket(BitStream.fromHex("620080001611562C8802118E34"))
        )
        assertEquals(12, parsePacket(BitStream.fromHex("620080001611562C8802118E34")).versionSum())
        assertEquals(
            Operator(
                6, 0, listOf(
                    Operator(
                        0, 0, listOf(
                            LiteralValue(0, 4, 10),
                            LiteralValue(6, 4, 11),
                        )
                    ),
                    Operator(
                        4, 0, listOf(
                            LiteralValue(7, 4, 12),
                            LiteralValue(0, 4, 13),
                        )
                    ),
                )
            ), parsePacket(BitStream.fromHex("C0015000016115A2E0802F182340"))
        )
        assertEquals(23, parsePacket(BitStream.fromHex("C0015000016115A2E0802F182340")).versionSum())
        assertEquals(
            Operator(
                5, 0, listOf(
                    Operator(
                        1, 0, listOf(
                            Operator(
                                3, 0, listOf(
                                    LiteralValue(7, 4, 6),
                                    LiteralValue(6, 4, 6),
                                    LiteralValue(5, 4, 12),
                                    LiteralValue(2, 4, 15),
                                    LiteralValue(2, 4, 15),
                                )
                            ),
                        )
                    ),
                )
            ), parsePacket(BitStream.fromHex("A0016C880162017C3686B18A3D4780"))
        )
        assertEquals(31, parsePacket(BitStream.fromHex("A0016C880162017C3686B18A3D4780")).versionSum())
    }

    @Test
    fun `test solution`() {
    }
}
