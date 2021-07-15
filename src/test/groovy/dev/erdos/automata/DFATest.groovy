package dev.erdos.automata

import spock.lang.Specification
import spock.lang.Unroll

class DFATest extends Specification {

	def 'test dfa for trivial automata'() {
		expect:
			def automata = NDFA.levenshtein('abc', 0).dfa()
			automata.size == 4
	}

	@Unroll
	def 'number of states for #n/#d'() {
		expect:
			def word = 'abcdefghijklmnopqrstuvwxyz'.substring(0, n)
			def automata = NDFA.levenshtein(word, d).dfa()
			automata.size == size
		where:
			n  | d | size
			1  | 0 | 2
			2  | 0 | 3
			3  | 1 | 12
			4  | 1 | 16
			15 | 2 | 218
			20 | 2 | 298
	}

	// https://julesjacobs.com/2015/06/17/disqus-levenshtein-simple-and-fast.html
	def 'simple levenshtein test'() {
		expect:
			def automata = NDFA.levenshtein('woof', 1).dfa()
			automata.size == 15 // why not 16?
	}

	@Unroll
	def 'test #word with d=1'() {
		expect:
			def automata = NDFA.levenshtein('janos', 1).dfa()
			automata.test(word) == expected
		where:
			expected | word
			true     | 'anos'
			true     | 'jjanos'
			true     | 'aanos'
			true     | 'anos'
			true     | 'xanos'
			false    | 'jjjanos'
			false    | 'xadfg'
			false    | 'jan'
	}

	def 'test levenshtein'() {
		setup:
			def word = NDFA.levenshtein('janos', 1).dfa()
		    word.debug()
		expect:
			['janos', 'xanos', 'anos', 'aanos', 'jjanos'].forEach { assert word.test(it) }
			['jjjanos'].forEach { assert ! word.test(it) }
	}
}
