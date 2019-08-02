package com.lmichael.dfa

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
*/
class UnitTest1 {
    var dfa = DFA()
    @Before
    fun create() {
        val nfa = NFA()
        nfa.regexToNFA("1(00)*")
        dfa = nfa.Determine() as DFA
        dfa = dfa?.minimize() as DFA
    }
    @Test
    @Throws(Exception::class)
    fun string1_isCorrect() {
        assertEquals(true, dfa?.processString("1"))
    }
    @Test
    @Throws(Exception::class)
    fun string2_isCorrect() {
        assertEquals(true, dfa?.processString("100"))
    }
    @Test
    @Throws(Exception::class)
    fun string3_isCorrect() {
        assertEquals(true, dfa?.processString("10000"))
    }
    @Test
    @Throws(Exception::class)
    fun string4_isCorrect() {
        assertEquals(false, dfa?.processString("10"))
    }
}

class UnitTest2 {
    var dfa = DFA()
    @Before
    fun create() {
        val nfa = NFA()
        nfa.regexToNFA("(a+b)*abb")
        dfa = nfa.Determine() as DFA
        dfa = dfa?.minimize() as DFA
    }
    @Test
    @Throws(Exception::class)
    fun string1_isCorrect() {
        assertEquals(true, dfa?.processString("abb"))
    }
    @Test
    @Throws(Exception::class)
    fun string2_isCorrect() {
        assertEquals(true, dfa?.processString("aabb"))
    }
    @Test
    @Throws(Exception::class)
    fun string3_isCorrect() {
        assertEquals(false, dfa?.processString("abaa"))
    }
    @Test
    @Throws(Exception::class)
    fun string4_isCorrect() {
        assertEquals(false, dfa?.processString(""))
    }
    @Test
    @Throws(Exception::class)
    fun string5_isCorrect() {
        assertEquals(true, dfa?.processString("abbabbabb"))
    }
}

class UnitTest3 {
    var dfa = DFA()
    @Before
    fun create() {
        val nfa = NFA()
        nfa.regexToNFA("(1+01)*0*")
        dfa = nfa.Determine() as DFA
        dfa = dfa?.minimize() as DFA
    }
    @Test
    @Throws(Exception::class)
    fun string1_isCorrect() {
        assertEquals(true, dfa?.processString("11"))
    }
    @Test
    @Throws(Exception::class)
    fun string2_isCorrect() {
        assertEquals(true, dfa?.processString(""))
    }
    @Test
    @Throws(Exception::class)
    fun string3_isCorrect() {
        assertEquals(false, dfa?.processString("0011"))
    }
    @Test
    @Throws(Exception::class)
    fun string4_isCorrect() {
        assertEquals(false, dfa?.processString("1001"))
    }
    @Test
    @Throws(Exception::class)
    fun string5_isCorrect() {
        assertEquals(true, dfa?.processString("11111100"))
    }
}