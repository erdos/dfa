package dev.erdos.automata

import spock.lang.Specification

import static dev.erdos.automata.LevenshteinDFA.uniqueLetters

class LevenshteinDFASpec extends Specification {

    def 'test unique letter checking'() {
        expect:
            uniqueLetters('')
            uniqueLetters('a')
            uniqueLetters('abcdefghijklm')
            !uniqueLetters('abcdaefgh')
            !uniqueLetters('abcdabcd')
            !uniqueLetters('aa')
    }

    def 'cached isomorphic dfa has same complexity'() {
        expect:
        def cache = new LevenshteinDFA(2)
        cache.cached('janos').test(word) == success
        where:
        word    | success
        'janos' | true
        'jaxos' | true
        'jaxys' | true
        'jaxyz' | false
    }

    def 'see runtime of cached levenshtein builder'() {
        expect:
        def cache = new LevenshteinDFA(2)
        4.times {
            System.gc()
            def word = 'abcdefghijklmnopqrstuv' + 'ABCDEFGHIJKLM'.substring(0, it)
            assert uniqueLetters(word)

            long before = System.nanoTime() / 1000
            cache.cached(word)
            long mid = System.nanoTime() / 1000
            cache.cached(word)
            long after = System.nanoTime() / 1000
            System.out.println("Elapsed: ${mid - before}us vs ${after - mid}us")
        }
    }
}
