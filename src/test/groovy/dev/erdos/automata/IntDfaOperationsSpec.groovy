package dev.erdos.automata

import spock.lang.Specification

class IntDfaOperationsSpec extends Specification {

    def 'simple union'() {
        given:
            def word = NDFA.levenshtein('janos', 1).dfa()
            def word2 = NDFA.levenshtein('erdos', 1).dfa()
        when:
            new IntDfaOperations().union(word, word2)
        then:
            word.test('janos')
            word.test('erdos')
            word.test('anos')
            ! word.test('nos')
            ! word.test('erd')
    }

    def 'union where right side has parallel edges'() {
        given:
            def word = NDFA.levenshtein('test', 0).dfa()
            def word2 = new MutableArrayDFA().with {
                accept(newState())
                connect(0, 't' as char, 1)
                connect(0, 'r' as char, 1)
            }
        when:
            new IntDfaOperations().union(word, word2)
        then:
            word.size() == 6
            word.test('test')
            ! word.test('rest')
            word.test('t')
            word.test('r')
    }

    def 'union: right side is larger'() {
        given:
            def word = NDFA.levenshtein('jan', 0).dfa()
            def word2 = NDFA.levenshtein('janos', 1).dfa()
        when:
            new IntDfaOperations().union(word, word2)
        then:
            word2.debug()
            word.debug() // sajnos nagyobb az uniozas utan :'-(
            word.size() == word2.size()
            word.test('janos')
            word.test('xanos')
            word.test('jan')
            word.test('jano')
            ! word.test('janoxx')
    }

    def 'union: minimal example'() {
        given:
        def word = NDFA.levenshtein('j', 0).dfa()
        def word2 = NDFA.levenshtein('j', 1).dfa()
        when:
        new IntDfaOperations().union(word, word2)
        then:
        word2.debug()
        word.debug() // sajnos nagyobb az uniozas utan :'-(
        word.size() == word2.size()
    }

    def 'union: left side is larger'() {
        given:
            def word = NDFA.levenshtein('janos', 1).dfa()
            def word2 = NDFA.levenshtein('jan', 0).dfa()
            int size1 = word.size()
        when:
            new IntDfaOperations().union(word, word2)
        then:
            word2.debug() // sajnos nagyobb
            word.debug()
            word.size() == size1 // size does not change because right-side is subset of left-side.
            word.test('janos')
            word.test('xanos')
            word.test('jan')
            word.test('jano')
            ! word.test('janoxx')
    }

    def 'simple of exact '() {
        given:
            def word = NDFA.levenshtein('janos', 0).dfa()
        when:
            new IntDfaOperations().union(word, new MutableArrayDFA())
        then:
            word.size() == 6
        when:
            def word2 = NDFA.levenshtein('erdos', 0).dfa()
            new IntDfaOperations().union(word, word2)
        then:
            word.size() == 11
            word.test('janos')
            word.test('erdos')
            !word.test('anos')
            !word.test('nos')
            !word.test('erd')
        when:
            def word3 = NDFA.levenshtein('erdei', 0).dfa()
            new IntDfaOperations().union(word, word3)
        then:
            word.size() == 13
    }

    def 'union 1'() {
        setup:
            def aaa = new MutableArrayDFA()
            aaa.connect(0, 'a' as char, aaa.newState())
                    .connect(0, 'b' as char, aaa.newState())
        and:
            def bbb = new MutableArrayDFA()
            bbb.connect(0, 'a' as char, bbb.newState())
                    .connect(0, 'b' as char, bbb.newState())
        when:
            def ccc = new IntDfaOperations().union(aaa, bbb)
        then:
            ccc.size() == 3
    }

    def 'union 2'() {
        setup:
            def aaa = new MutableArrayDFA()
            aaa.connect(0, 'a' as char, aaa.newState())
                    .connect(0, '*' as char, aaa.newState())
        and:
            def bbb = new MutableArrayDFA()
            bbb.connect(0, 'a' as char, bbb.newState())
                    .connect(0, 'b' as char, bbb.newState())
        when:
            def ccc = new IntDfaOperations().union(aaa, bbb)
        then:
            ccc.size() == 4
    }

    def 'union 2 reversed'() {
        setup:
            def aaa = new MutableArrayDFA()
            aaa.connect(0, 'a' as char, aaa.newState())
                    .connect(0, 'b' as char, aaa.newState())
        and:
            def bbb = new MutableArrayDFA()
            bbb.connect(0, 'a' as char, bbb.newState())
                    .connect(0, '*' as char, bbb.newState())
        when:
            def ccc = new IntDfaOperations().union(aaa, bbb)
        then:
            ccc.size() == 4
    }

    def 'union 3 - both has wildcard'() {
        setup:
            def aaa = new MutableArrayDFA()
            aaa.connect(0, 'a' as char, aaa.newState())
                    .connect(0, '*' as char, aaa.newState())
        and:
            def bbb = new MutableArrayDFA()
            bbb.connect(0, 'b' as char, bbb.newState())
                    .connect(0, '*' as char, bbb.newState())
        expect:
            new IntDfaOperations().union(aaa, bbb).size() == 4
    }

    def 'complex union'() {
        setup:
            def aaa = new MutableArrayDFA()
            6.times {aaa.newState()}

            aaa.connect(0, 'a' as char, 1)
            aaa.connect(0, '*' as char, 2)
            aaa.connect(0, 'b' as char, 3)

            aaa.connect(1, '1' as char, 4)
            aaa.connect(2, '2' as char, 5)
            aaa.connect(3, '3' as char, 6)
        and:
            def bbb = new MutableArrayDFA()
            6.times {bbb.newState()}

            bbb.connect(0, 'a' as char, 1)
            bbb.connect(0, '*' as char, 2)
            bbb.connect(0, 'c' as char, 3)

            bbb.connect(1, '4' as char, 4)
            bbb.connect(2, '5' as char, 5)
            bbb.connect(3, '6' as char, 6)
        expect:
            new IntDfaOperations().union(aaa, bbb).size() ==11
    }

    def 'concatenation 1'() {
        given:
            def word1 = NDFA.levenshtein('hello', 1).dfa()
            def word2 = NDFA.levenshtein('world', 1).dfa()
        when:
            def word = new IntDfaOperations().concat(word1, word2)
        then:
            word.size() == 60 // not sure
            word.test('helloworld')  // d = 0 + 0
            word.test('halloworld')  // d = 1 + 0
            word.test('halloworld!') // d = 0 + 1
            ! word.test('hallloworld') // d = 3 + 0 breaks
        and: // the original words are not matched any more
            ! word.test('hello')
            ! word.test('hallo')
            ! word.test('world')
    }
}
