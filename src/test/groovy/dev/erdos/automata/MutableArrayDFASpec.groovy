package dev.erdos.automata

import dev.erdos.automata.MutableArrayDFA
import dev.erdos.automata.NDFA
import spock.lang.Specification

import static NDFA.levenshtein

class MutableArrayDFASpec extends Specification {

    def 'ctor'() {
        given:
            def parent = NDFA.levenshtein('janos', 1).dfa()
        when:
            def created = new MutableArrayDFA(parent)
        then:
            parent.size() == created.size()
            ['janos', 'jano', 'xanos', 'anos', 'jaos', 'jnos'].forEach { assert created.test(it) }
            !['jan', 'ano', 'xxx', 'xano', 'ja', '', 'janosxy'].forEach { assert ! created.test(it) }
    }
}
